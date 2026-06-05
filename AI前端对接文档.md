# PathFinder AI 模块前端对接文档

> 面向前端开发人员，详细说明如何对接后端 SSE 流式 AI 对话接口，实现 PC 端 Vue 悬浮 AI 助手。

---

## 一、接口说明

### 1.1 接口基本信息

| 项目 | 值 |
|------|-----|
| 接口地址 | `POST http://localhost:8080/api/agent/chat/sse` |
| Content-Type | `application/json` |
| Accept | `text/event-stream` |
| 响应格式 | SSE（Server-Sent Events）流式 |

### 1.2 请求体

```json
{
  "message": "广东物理类位次5000能报哪些学校？",
  "chatId": "550e8400-e29b-41d4-a716-446655440000"   // 首次可不传，后端自动生成并返回
}
```

- `message`（必填）：用户输入的消息
- `chatId`（可选）：会话 ID，**首次不传**，后端通过 SSE `chatId` 事件返回；后续请求必须带上，以保持多轮对话上下文

### 1.3 SSE 事件格式（后端实际输出）

后端使用 Spring `SseEmitter`，每个事件格式如下：

```
event: chatId
data: 550e8400-e29b-41d4-a716-446655440000

event: message
data: 根据

event: message
data: 广东省

...

event: done
data: [DONE]
```

| event 名称 | 触发时机 | data 内容 |
|-----------|---------|-----------|
| `chatId` | 流式开始时，仅一次 | 会话 ID（字符串） |
| `message` | 流式进行中，多次 | 文本片段（一个 token / 几个字） |
| `done` | 流式结束时，仅一次 | `[DONE]` |

---

## 二、前端 SSE 对接核心代码

### 2.1 SSE 流解析工具 `src/utils/sse.js`

```js
/**
 * 对接后端 /api/agent/chat/sse SSE 接口
 *
 * 后端 SseEmitter 输出格式：
 *   event: chatId\n data: xxx\n\n
 *   event: message\n data: xxx\n\n
 *   event: done\n data: [DONE]\n\n
 *
 * @param {string} url  - SSE 接口地址
 * @param {object} body - { message, chatId }
 * @param {object} handlers
 *   - onChatId(chatId)
 *   - onMessage(chunk)  // 每个文本片段
 *   - onDone()
 *   - onError(err)
 * @returns {AbortController} 可用于中断流
 */
export function createSSEConnection(url, body, handlers) {
  const controller = new AbortController()

  fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
    signal: controller.signal
  })
    .then(async (res) => {
      if (!res.ok) {
        throw new Error(`HTTP ${res.status}`)
      }

      const reader = res.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''       // 未处理完的 SSE 文本
      let currentEvent = '' // 当前事件名称
      let currentData = ''  // 当前 data 内容

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })

        // 按行解析
        const lines = buffer.split('\n')
        // 最后一行可能不完整，保留到下次
        buffer = lines.pop()

        for (const rawLine of lines) {
          const line = rawLine.trim()

          if (line.startsWith('event:')) {
            currentEvent = line.slice(6).trim()
          } else if (line.startsWith('data:')) {
            currentData = line.slice(5).trim()
            // 触发回调
            dispatchEvent(currentEvent, currentData, handlers)
            currentEvent = ''
            currentData = ''
          } else if (line === '') {
            // 空行 = 事件结束，重置
            currentEvent = ''
            currentData = ''
          }
        }
      }

      handlers.onDone && handlers.onDone()
    })
    .catch((err) => {
      if (err.name !== 'AbortError') {
        handlers.onError && handlers.onError(err)
      }
    })

  return controller
}

function dispatchEvent(event, data, handlers) {
  if (event === 'chatId') {
    handlers.onChatId && handlers.onChatId(data)
  } else if (event === 'message') {
    handlers.onMessage && handlers.onMessage(data)
  } else if (event === 'done') {
    // done 事件 body 是 [DONE]，onDone 在流结束时触发
  }
}
```

---

### 2.2 对话面板组件 `src/components/AiChatPanel.vue`

```vue
<template>
  <div v-if="visible" class="ai-panel">
    <!-- 头部 -->
    <div class="ai-header">
      <span>🤖 AI 志愿助手</span>
      <button class="ai-close" @click="$emit('close')">✕</button>
    </div>

    <!-- 消息列表 -->
    <div class="ai-body" ref="bodyRef">
      <div
        v-for="(msg, i) in messages"
        :key="i"
        :class="['ai-row', msg.role]"
      >
        <div class="ai-avatar">{{ msg.role === 'user' ? '🧑' : '🤖' }}</div>
        <div class="ai-bubble">
          <!-- 用 white-space:pre-wrap 保留 AI 输出的换行 -->
          {{ msg.content }}
        </div>
      </div>

      <!-- 流式回复中的 typing 动画 -->
      <div v-if="streaming" class="ai-row assistant">
        <div class="ai-avatar">🤖</div>
        <div class="ai-bubble typing">
          <i class="dot"></i><i class="dot"></i><i class="dot"></i>
        </div>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="ai-footer">
      <textarea
        v-model="input"
        placeholder="输入问题，如：广东物理类5000位次推荐哪些学校？"
        @keydown.enter.exact.prevent="send"
        :disabled="streaming"
        rows="1"
      />
      <button @click="send" :disabled="streaming || !input.trim()">
        {{ streaming ? '回答中…' : '发送' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { createSSEConnection } from '@/utils/sse.js'

const props = defineProps({ visible: Boolean })
const emit = defineEmits(['close'])

const messages = ref([
  {
    role: 'assistant',
    content: '你好！我是高考志愿 AI 助手，可以帮你分析位次、推荐院校、制定冲稳保方案。请输入你的问题~'
  }
])
const input = ref('')
const streaming = ref(false)
const chatId = ref('')   // 会话 ID，后端首次返回后保存
const bodyRef = ref(null)
let abortCtrl = null      // 当前 SSE AbortController

function send() {
  const text = input.value.trim()
  if (!text || streaming.value) return

  // 追加用户消息
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  streaming.value = true
  scroll()

  // 占位 AI 消息，流式追加内容
  const aiIndex = messages.value.length
  messages.value.push({ role: 'assistant', content: '' })
  scroll()

  abortCtrl = createSSEConnection(
    'http://localhost:8080/api/agent/chat/sse',
    { message: text, chatId: chatId.value || undefined },
    {
      onChatId(id) {
        chatId.value = id
      },
      onMessage(chunk) {
        messages.value[aiIndex].content += chunk
        scroll()
      },
      onDone() {
        streaming.value = false
      },
      onError(err) {
        messages.value[aiIndex].content = '⚠️ 出错了：' + err.message
        streaming.value = false
      }
    }
  )
}

function scroll() {
  nextTick(() => {
    const el = bodyRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

// 发送新消息时中断上一条流
function stop() {
  if (abortCtrl) abortCtrl.abort()
  streaming.value = false
}
</script>

<style scoped>
.ai-panel {
  position: fixed;
  bottom: 100px;
  right: 32px;
  width: 380px;
  height: 540px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 48px rgba(0,0,0,0.18);
  display: flex;
  flex-direction: column;
  z-index: 10000;
  animation: slideUp 0.25s ease;
}
@keyframes slideUp {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}
.ai-header {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 16px 16px 0 0;
  font-size: 15px;
  font-weight: 600;
}
.ai-close {
  background: none;
  border: none;
  color: #fff;
  font-size: 18px;
  cursor: pointer;
  line-height: 1;
}
.ai-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
}
.ai-row {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
  align-items: flex-start;
}
.ai-row.user {
  flex-direction: row-reverse;
}
.ai-avatar {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  font-size: 20px;
  line-height: 32px;
  text-align: center;
}
.ai-bubble {
  max-width: 260px;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}
.ai-row.assistant .ai-bubble {
  background: #fff;
  color: #1a1a1a;
}
.ai-row.user .ai-bubble {
  background: #667eea;
  color: #fff;
}
/* typing 三点动画 */
.typing .dot {
  display: inline-block;
  width: 6px; height: 6px;
  border-radius: 50%;
  background: #bbb;
  margin: 0 2px;
  animation: blink 1.4s infinite both;
}
.typing .dot:nth-child(2) { animation-delay: 0.2s; }
.typing .dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes blink {
  0%, 100% { opacity: 0.2; }
  20%      { opacity: 1; }
}

.ai-footer {
  display: flex;
  padding: 12px;
  gap: 8px;
  border-top: 1px solid #eee;
}
.ai-footer textarea {
  flex: 1;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 14px;
  resize: none;
  outline: none;
  font-family: inherit;
}
.ai-footer textarea:focus {
  border-color: #667eea;
}
.ai-footer button {
  background: #667eea;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 0 18px;
  cursor: pointer;
  font-size: 14px;
  white-space: nowrap;
}
.ai-footer button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
</style>
```

---

### 2.3 悬浮按钮组件 `src/components/AiFloatButton.vue`

```vue
<template>
  <div class="ai-fab" @click="$emit('click')">
    <svg viewBox="0 0 24 24" width="26" height="26" fill="#fff">
      <!-- 机器人图标 -->
      <path d="M12 2a2 2 0 012 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 017 7v1h2v2h-2v1a7 7 0 01-7 7h-2a7 7 0 01-7-7v-1H3v-2h2v-1a7 7 0 017-7h1V5.73A2 2 0 0112 2zm0 4a1 1 0 100 2 1 1 0 000-2zm0 12a5 5 0 100-10 5 5 0 000 10z"/>
    </svg>
  </div>
</template>

<script setup>
defineEmits(['click'])
</script>

<style scoped>
.ai-fab {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  box-shadow: 0 4px 20px rgba(102,126,234,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 9999;
  transition: transform 0.2s, box-shadow 0.2s;
}
.ai-fab:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 28px rgba(102,126,234,0.65);
}
</style>
```

---

### 2.4 入口挂载（在 `App.vue` 或布局组件中）

```vue
<template>
  <!-- 现有页面内容 -->

  <AiFloatButton @click="panelOpen = true" />
  <AiChatPanel :visible="panelOpen" @close="panelOpen = false" />
</template>

<script setup>
import { ref } from 'vue'
import AiFloatButton from '@/components/AiFloatButton.vue'
import AiChatPanel from '@/components/AiChatPanel.vue'

const panelOpen = ref(false)
</script>
```

---

## 三、多轮对话机制说明

```
用户                              后端 SSE 流
 │
 ├─ POST /chat/sse {message:"你好"} ──► 返回 event:chatId → 保存 chatId
 │                                        返回 event:message × N
 │                                        返回 event:done
 │
 ├─ POST /chat/sse {message:"推荐院校", chatId: "xxx"}
 │                                   ──► 后端用同一 chatId 查上下文
 │                                        返回关联上文的回答
 │
 └─ 同一 chatId 可一直复用，上下文最多保留 10 轮
    （后端 CHAT_MEMORY_RETRIEVE_SIZE_KEY = 10）
```

**前端要点：**
- 首次请求 **不传 `chatId`**，从 `event: chatId` 事件中获取
- 后续所有请求 **必须带上 `chatId`**，否则后端会当成新会话
- `chatId` 建议存到 `localStorage`，页面刷新后仍能恢复会话

```js
// 持久化 chatId
const CHAT_ID_KEY = 'pathfinder_ai_chat_id'

function loadChatId() {
  return localStorage.getItem(CHAT_ID_KEY) || ''
}
function saveChatId(id) {
  chatId.value = id
  localStorage.setItem(CHAT_ID_KEY, id)
}
// 在 onChatId 回调中调用 saveChatId(id)
```

---

## 四、AI 输出内容说明

AI 回复为纯文本，但包含结构化标记，前端可直接显示或做简单解析：

```
【冲】（建议冲刺的院校）
中山大学、华南理工大学…

【稳】（较稳妥的院校）
暨南大学、深圳大学…

【保】（保底院校）
广东工业大学、广州大学…

⚠️ 风险提示：
…
```

如需更美观的展示，建议引入 `marked` 让 AI 输出 Markdown 格式，或按 `【冲】`、`【稳】`、`【保】` 做分段渲染。

---

## 五、生产环境对接注意事项

| 项目 | 开发环境 | 生产环境 |
|------|---------|---------|
| 接口地址 | `http://localhost:8080/api/agent/chat/sse` | `https://your-domain.com/api/agent/chat/sse` |
| 跨域 | 后端已全局 CORS 放行 | 建议改为具体域名白名单 |
| 鉴权 | 无 | 在 fetch headers 加 `Authorization: Bearer <token>` |
| SSE 超时 | 后端设 `0L`（无限） | 生产建议加 `SseEmitter(60000L)` 超时 |

---

## 六、调试方法

1. 启动后端：`mvn spring-boot:run`（端口 8080）
2. 用 curl 测试 SSE 流：
```bash
curl -N -X POST http://localhost:8080/api/agent/chat/sse \
  -H "Content-Type: application/json" \
  -d '{"message":"广东物理类位次5000能报哪些学校？"}'
```
3. 看到不断输出 `data:` 片段即接口正常

---

*文档版本：v1.1 | 确认对接接口：`POST /api/agent/chat/sse`*
