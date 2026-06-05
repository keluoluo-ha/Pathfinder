# PathFinder 论坛模块 — 前端对接接口文档

> 基于当前后端实现（`ForumController` + WebSocket）整理，适用于 `pathfinder-fronted` 联调。  
> 用户复用 `student` 表，登录走现有 `/api/user/login`。

---

## 1. 基础约定

### 1.1 服务地址

| 环境 | HTTP Base URL | WebSocket |
|------|---------------|-----------|
| 本地开发 | `http://localhost:8080/api` | `ws://localhost:8080/ws/forum?token={JWT}` |
| 生产 | `https://{domain}/api` | `wss://{domain}/ws/forum?token={JWT}` |

前端环境变量示例：

```env
VITE_API_BASE=http://localhost:8080/api
```

**静态资料文件**（无 `/api` 前缀）：

```text
http://localhost:8080/forum-files/{filename}
```

若接口返回 `fileUrl` 为 `/forum-files/xxx.pdf`，完整下载地址为：

```text
http://localhost:8080/forum-files/xxx.pdf
```

---

### 1.2 统一响应结构 `Result<T>`

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

| code | 含义 | 前端处理 |
|------|------|----------|
| `0` | 成功 | 使用 `data` |
| `40100` | 未登录 / Token 失效 | 清 token，跳转登录页 |
| `40000` | 参数错误 | `Message.error(message)` |
| `40300` | 无权限 | 提示 `message` |
| `42001`~`42008` | 论坛业务错误 | 提示 `message` |
| `50000` | 系统异常 | 提示友好文案 |

**注意：** HTTP 状态码多为 `200`，必须以 **`response.data.code`** 判断，不能只看 HTTP 状态。

---

### 1.3 鉴权

论坛不单独注册，先调用：

| 接口 | 方法 | 路径 |
|------|------|------|
| 登录 | POST | `/api/user/login` |
| 注册 | POST | `/api/user/register` |

**登录成功 `data` 示例：**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expireAt": 1778392549206,
  "profile": {
    "id": 4,
    "name": "张三",
    "nickname": "小张",
    "avatarUrl": null,
    "subjectType": 1,
    "score": 528,
    "rankNo": 100000
  }
}
```

**所有 `/api/forum/**` 请求头：**

```http
Authorization: Bearer <token>
Content-Type: application/json
```

上传资料为 `multipart/form-data`，**不要**手动设置 `Content-Type`（由浏览器自动带 boundary）。

---

### 1.4 分页结构 `PageResult<T>`

```json
{
  "total": 100,
  "pageNo": 1,
  "pageSize": 10,
  "records": []
}
```

---

### 1.5 枚举说明

| 字段 | 值 | 含义 |
|------|-----|------|
| `gaokaoType` / `subjectType` | `1` | 物理类 |
| | `2` | 历史类 |
| `message.type` | `1` | 私信 |
| | `2` | 评论提醒 |
| | `3` | 新帖通知 |
| | `4` | 系统 |
| `isRead` | `0` | 未读 |
| | `1` | 已读 |

---

## 2. REST 接口明细

### 2.1 板块

#### GET `/api/forum/boards` — 板块列表

- **鉴权：** 需要登录
- **Query：** 无

**响应 `data`：** `ForumBoard[]`

```json
[
  {
    "id": 1,
    "name": "选科交流",
    "description": "3+1+2选科组合、科目搭配讨论",
    "sortOrder": 1,
    "status": 0,
    "createTime": "2026-05-19T10:00:00"
  }
]
```

---

### 2.2 帖子

#### POST `/api/forum/posts` — 发帖

**请求体：**

```json
{
  "boardId": 1,
  "title": "物化生选科求助",
  "content": "帖子正文...",
  "category": "选科",
  "grade": "高三",
  "gaokaoType": 1,
  "subjects": "物化生"
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| boardId | 是 | 板块 ID |
| title | 是 | 标题 |
| content | 是 | 正文 |
| category / grade / gaokaoType / subjects | 否 | 筛选展示用 |

**响应 `data`：** `Long`（新帖子 ID）

**副作用：** 在线用户会收到 WebSocket `NEW_POST` 推送。

---

#### GET `/api/forum/posts` — 帖子分页列表

**Query：**

| 参数 | 必填 | 默认 | 说明 |
|------|------|------|------|
| boardId | 否 | - | 板块 ID |
| category | 否 | - | 分类 |
| grade | 否 | - | 年级 |
| gaokaoType | 否 | - | 1 物理 / 2 历史 |
| subjects | 否 | - | 选科组合 |
| pageNo | 否 | 1 | 页码 |
| pageSize | 否 | 10 | 每页条数 |

**响应 `data`：** `PageResult<ForumPostVO>`

```json
{
  "total": 1,
  "pageNo": 1,
  "pageSize": 10,
  "records": [
    {
      "id": 1,
      "boardId": 1,
      "boardName": "选科交流",
      "userId": 4,
      "author": {
        "userId": 4,
        "nickname": "小张",
        "avatarUrl": null,
        "subjectType": 1
      },
      "title": "物化生选科求助",
      "content": "帖子正文...",
      "category": "选科",
      "grade": "高三",
      "gaokaoType": 1,
      "subjects": "物化生",
      "viewCount": 10,
      "likeCount": 2,
      "commentCount": 3,
      "liked": false,
      "createTime": "2026-05-19T11:00:00"
    }
  ]
}
```

---

#### GET `/api/forum/posts/{id}` — 帖子详情

- **路径参数：** `id` 帖子 ID
- **副作用：** `viewCount` 自动 +1

**响应 `data`：** `ForumPostVO`（结构同上，含完整 `content`）

---

#### DELETE `/api/forum/posts/{id}` — 删除帖子

- **权限：** 仅作者可删
- **响应 `data`：** `null`

---

### 2.3 评论

#### GET `/api/forum/posts/{id}/comments` — 评论列表

**Query：**

| 参数 | 默认 | 说明 |
|------|------|------|
| pageNo | 1 | 页码 |
| pageSize | 20 | 每页条数 |

**响应 `data`：** `PageResult<ForumCommentVO>`

```json
{
  "total": 1,
  "pageNo": 1,
  "pageSize": 20,
  "records": [
    {
      "id": 1,
      "postId": 1,
      "userId": 5,
      "author": {
        "userId": 5,
        "nickname": "小李",
        "avatarUrl": null,
        "subjectType": 2
      },
      "parentId": 0,
      "content": "建议看看往年录取位次",
      "createTime": "2026-05-19T11:05:00"
    }
  ]
}
```

---

#### POST `/api/forum/posts/{id}/comments` — 发表评论

**请求体：**

```json
{
  "content": "评论内容",
  "parentId": 0
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| content | 是 | 评论内容 |
| parentId | 否 | 父评论 ID，一级评论传 `0` 或不传 |

**响应 `data`：** `Long`（评论 ID）

**副作用：** 帖主收到 WebSocket `NEW_COMMENT`，未读数通过 `UNREAD_COUNT` 更新。

---

### 2.4 点赞

#### POST `/api/forum/posts/{id}/like` — 点赞

- **响应 `data`：** `null`
- **错误码 `42004`：** 已经点赞过了

#### DELETE `/api/forum/posts/{id}/like` — 取消点赞

- **错误码 `42005`：** 尚未点赞

---

### 2.5 学习资料

#### POST `/api/forum/materials` — 上传资料

- **Content-Type：** `multipart/form-data`
- **单文件上限：** 50MB（超限返回参数错误提示）
- **建议超时：** 120 秒

**Form 字段：**

| 字段 | 必填 | 说明 |
|------|------|------|
| title | 是 | 资料标题 |
| subject | 否 | 学科 |
| grade | 否 | 年级 |
| file | 是 | 文件 |

**前端示例：**

```javascript
const formData = new FormData()
formData.append('title', '2025数学一模')
formData.append('subject', '数学')
formData.append('grade', '高三')
formData.append('file', file, file.name)

await axios.post('http://localhost:8080/api/forum/materials', formData, {
  headers: { Authorization: `Bearer ${token}` },
  timeout: 120000,
})
```

**响应 `data`：** `Long`（资料 ID）

---

#### GET `/api/forum/materials` — 资料列表

**Query：**

| 参数 | 说明 |
|------|------|
| subject | 学科筛选 |
| grade | 年级筛选 |
| pageNo | 默认 1 |
| pageSize | 默认 10 |

**响应 `data`：** `PageResult<ForumMaterial>`

```json
{
  "total": 1,
  "pageNo": 1,
  "pageSize": 10,
  "records": [
    {
      "id": 1,
      "title": "2025数学一模",
      "fileUrl": "/forum-files/uuid.pdf",
      "subject": "数学",
      "grade": "高三",
      "userId": 4,
      "downloadCount": 5,
      "status": 0,
      "createTime": "2026-05-19T11:10:00"
    }
  ]
}
```

---

#### GET `/api/forum/materials/{id}` — 资料详情

**响应 `data`：** `ForumMaterial`（结构同上）

#### POST `/api/forum/materials/{id}/download` — 下载计数 +1

**响应 `data`：** 更新后的 `ForumMaterial`（`downloadCount` 已递增）

---

### 2.6 消息 / 私信

#### POST `/api/forum/messages/private` — 发送私信（REST）

**请求体：**

```json
{
  "toUserId": 5,
  "content": "你好，想请教志愿"
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| toUserId | 是 | 接收方 student.id |
| content | 是 | 消息内容 |

**响应 `data`：** `null`

**副作用：** 对方在线时 WebSocket 推送 `PRIVATE_MSG`。

---

#### GET `/api/forum/messages` — 消息列表

**Query：**

| 参数 | 说明 |
|------|------|
| type | 消息类型，私信传 `1` |
| peerUserId | 与某用户的会话（查私信时建议传） |
| pageNo | 默认 1 |
| pageSize | 默认 20 |

**响应 `data`：** `PageResult<ForumMessageVO>`

```json
{
  "total": 1,
  "pageNo": 1,
  "pageSize": 20,
  "records": [
    {
      "id": 10,
      "fromUserId": 4,
      "toUserId": 5,
      "fromAuthor": {
        "userId": 4,
        "nickname": "小张",
        "avatarUrl": null,
        "subjectType": 1
      },
      "toAuthor": {
        "userId": 5,
        "nickname": "小李",
        "avatarUrl": null,
        "subjectType": 2
      },
      "content": "你好",
      "type": 1,
      "isRead": 0,
      "refId": null,
      "createTime": "2026-05-19T11:20:00"
    }
  ]
}
```

---

#### GET `/api/forum/messages/unread-count` — 未读统计

**响应 `data`：**

```json
{
  "total": 3,
  "privateCount": 2,
  "notifyCount": 1
}
```

- `total`：总未读（建议用于论坛 Tab 角标）
- `privateCount`：私信未读
- `notifyCount`：通知类未读（评论提醒等）

---

#### PUT `/api/forum/messages/{id}/read` — 单条标已读

- **路径参数：** `id` 消息 ID
- **响应 `data`：** `null`
- **副作用：** WebSocket 推送最新 `UNREAD_COUNT`

#### PUT `/api/forum/messages/read-batch` — 批量标已读

**Query：**

| 参数 | 说明 |
|------|------|
| type | 可选，传 `1` 表示私信全部标已读 |

**示例：** `PUT /api/forum/messages/read-batch?type=1`

---

### 2.7 在线用户

#### GET `/api/forum/online-users` — 当前 WebSocket 在线用户

**响应 `data`：**

```json
[
  {
    "userId": 5,
    "nickname": "小李",
    "avatarUrl": null
  }
]
```

仅统计已建立 WebSocket 连接的用户。

---

## 3. WebSocket 对接

### 3.1 连接方式

```javascript
const token = localStorage.getItem('token')
const ws = new WebSocket(
  `ws://localhost:8080/ws/forum?token=${encodeURIComponent(token)}`
)
```

也可在握手 Header 中带：`Authorization: Bearer <token>`

- 握手失败：连接被拒绝（403），检查 token 是否有效或已过期
- 建议：用户登录后、进入主布局时建立连接

### 3.2 服务端 → 客户端消息格式

```json
{
  "type": "NEW_POST",
  "payload": {},
  "timestamp": 1716086400000
}
```

| type | payload 说明 | 前端建议 |
|------|----------------|----------|
| `NEW_POST` | `{ postId, title, fromUserId }` | 刷新帖子列表 |
| `NEW_COMMENT` | `{ postId, commentId, message }` | 详情页刷新评论；`message` 为 ForumMessageVO |
| `PRIVATE_MSG` | `ForumMessageVO` | 聊天窗口追加消息 |
| `UNREAD_COUNT` | `{ total, privateCount, notifyCount }` | 更新 Tab 角标 |
| `ONLINE_NOTIFY` | `{ userId, onlineCount }` | 刷新在线用户列表 |
| `PONG` | `{}` | 心跳响应 |
| `ERROR` | `{ message }` | 提示错误 |

### 3.3 客户端 → 服务端

**心跳（建议每 30 秒）：**

```json
{ "type": "PING", "payload": {} }
```

**私信（也可仅用 REST `POST /api/forum/messages/private`）：**

```json
{
  "type": "PRIVATE_MSG",
  "payload": {
    "toUserId": 5,
    "content": "你好"
  }
}
```

---

## 4. 论坛业务错误码

| code | message（示例） |
|------|-----------------|
| 42001 | 板块不存在 |
| 42002 | 帖子不存在 |
| 42003 | 评论不存在 |
| 42004 | 已经点赞过了 |
| 42005 | 尚未点赞 |
| 42006 | 资料不存在 |
| 42007 | 消息不存在 |
| 42008 | 无权操作 |

---

## 5. 公共数据结构

### ForumAuthorVO

```json
{
  "userId": 4,
  "nickname": "小张",
  "avatarUrl": null,
  "subjectType": 1
}
```

### ForumPostVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 帖子 ID |
| boardId | Long | 板块 ID |
| boardName | String | 板块名称 |
| userId | Long | 作者 student.id |
| author | ForumAuthorVO | 作者信息 |
| title | String | 标题 |
| content | String | 正文 |
| category | String | 分类 |
| grade | String | 年级 |
| gaokaoType | Integer | 科类 |
| subjects | String | 选科 |
| viewCount | Integer | 浏览量 |
| likeCount | Integer | 点赞数 |
| commentCount | Integer | 评论数 |
| liked | Boolean | 当前用户是否已点赞 |
| createTime | String | 创建时间（ISO 8601） |

---

## 6. 前端工程对照（pathfinder-fronted）

| 前端路由 | 主要接口 |
|----------|----------|
| `/forum` | boards、posts、materials、online-users、unread-count |
| `/forum/create` | POST `/api/forum/posts` |
| `/forum/post/:id` | GET post、comments、like/unlike、POST comments |
| `/forum/material/upload` | POST `/api/forum/materials`（multipart） |
| `/forum/chat` | GET messages |
| `/forum/chat/:userId` | GET messages?peerUserId=&type=1、POST private、WS |

| 前端文件 | 说明 |
|----------|------|
| `src/api/forum.js` | REST 封装 |
| `src/utils/forumWs.js` | WebSocket 工具 |
| `src/stores/forum.js` | 未读数、WS 连接 |

---

## 7. 联调检查清单

- [ ] 已执行 `src/main/resources/sql/forum_schema.sql`
- [ ] 已登录，请求带 `Authorization: Bearer <token>`
- [ ] 跨域预检 OPTIONS 已放行
- [ ] 上传资料使用 `FormData`，单文件 ≤ 50MB
- [ ] 资料下载地址为 `http://{host}/forum-files/...`（非 `/api` 前缀）
- [ ] WebSocket 连接 URL 带 `?token=`
- [ ] 业务成功判断：`code === 0`

---

## 8. 在线调试

- Knife4j / Swagger：`http://localhost:8080/doc.html`
- 在文档页配置 Bearer Token 后可在线调试 REST 接口

---

## 9. 接口速查表

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/forum/boards` | 板块列表 |
| POST | `/api/forum/posts` | 发帖 |
| GET | `/api/forum/posts` | 帖子分页 |
| GET | `/api/forum/posts/{id}` | 帖子详情 |
| DELETE | `/api/forum/posts/{id}` | 删帖 |
| GET | `/api/forum/posts/{id}/comments` | 评论列表 |
| POST | `/api/forum/posts/{id}/comments` | 发评论 |
| POST | `/api/forum/posts/{id}/like` | 点赞 |
| DELETE | `/api/forum/posts/{id}/like` | 取消赞 |
| POST | `/api/forum/materials` | 上传资料 |
| GET | `/api/forum/materials` | 资料列表 |
| GET | `/api/forum/materials/{id}` | 资料详情 |
| POST | `/api/forum/materials/{id}/download` | 下载计数 |
| POST | `/api/forum/messages/private` | 发私信 |
| GET | `/api/forum/messages` | 消息列表 |
| GET | `/api/forum/messages/unread-count` | 未读统计 |
| PUT | `/api/forum/messages/{id}/read` | 单条已读 |
| PUT | `/api/forum/messages/read-batch` | 批量已读 |
| GET | `/api/forum/online-users` | 在线用户 |
| WS | `/ws/forum?token=` | 实时消息 |
