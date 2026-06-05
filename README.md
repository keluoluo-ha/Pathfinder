# 🎯 Pathfinder — 高考志愿填报智能咨询系统

基于 **Spring AI + Vue 3** 的高考志愿填报辅助平台，通过 AI 智能对话帮助考生查询位次对应的可选院校、模拟志愿方案，并提供论坛社区交流功能。

## ✨ 核心功能

| 模块 | 功能 |
|------|------|
| **AI 智能助手** | 基于 Spring AI Alibaba 的 ToolCallAgent，SSE 流式对话，支持院校推荐、联网搜索、人工介入 |
| **志愿推荐** | 基于位次/分数的智能院校推荐，支持多批次（本科一批/二批/专科） |
| **位次转换** | 分数 ↔ 位次双向映射，支持物理类/历史类 |
| **志愿模拟** | 志愿方案的保存、编辑、删除，完整 CRUD |
| **用户系统** | 注册 / 登录 / JWT 认证，个人信息管理 |
| **论坛社区** | 发帖 / 评论 / 点赞 / 私信，WebSocket 实时通信，网盘资料分享 |

## 🏗 技术架构

### 后端 (Spring Boot 3.5)

```
Spring Boot 3.5.14  +  Java 21
├── MyBatis-Plus 3.5.7        # ORM
├── Spring AI Alibaba 1.0.0-M6 # AI Agent
├── Redisson 3.40.0            # 分布式锁 / 缓存
├── Caffeine                   # 本地缓存 + BloomFilter
├── WebSocket                  # 论坛实时消息
├── Jsoup                      # 网页抓取
└── MySQL                      # 数据库
```

### 前端 (Vue 3)

```
Vue 3.5  +  Vite 6
├── Arco Design Vue 2.58       # UI 组件库
├── Pinia 3                    # 状态管理
├── Vue Router 5               # 路由
└── Axios                      # HTTP 客户端
```

## 📂 项目结构

```
src/main/java/com/hhk/pathfinderbacked/
├── ai/                        # AI 智能模块
│   ├── SimpleAgentApp.java    # Agent 主控
│   └── tools/                 # AI 工具集（推荐、搜索、爬取等）
├── controller/                # 接口层
│   ├── AiAgentController.java # SSE 流式对话
│   ├── RecommendController.java
│   ├── SimulationController.java
│   ├── UserController.java
│   └── forum/
│       └── ForumController.java
├── service/                   # 业务层（推荐、模拟、用户）
├── entity/                    # 数据实体
├── mapper/                    # MyBatis-Plus Mapper
├── config/                    # 配置类（Redis/CORS/Knife4j 等）
├── cache/                     # 缓存服务
├── forum/                     # 论坛社区模块（独立子包）
│   ├── websocket/             # WS 实时通信
│   ├── service/               # 论坛业务
│   └── vo/                    # 论坛视图对象
├── common/                    # 公共类（异常、响应封装）
├── dto/                       # 请求 DTO
├── vo/                        # 响应 VO
├── interceptor/               # 拦截器（JWT/限流）
└── utils/                     # 工具类（JWT/UserContext）
```

## 🚀 快速开始

### 环境要求

- **JDK** 21+
- **Maven** 3.8+
- **MySQL** 8.0+
- **Redis** 6.0+

### 启动步骤

```bash
# 1. 克隆项目
git clone https://github.com/keluoluo-ha/Pathfinder.git
cd Pathfinder

# 2. 导入数据库
mysql -u root -p < src/main/resources/sql/schema.sql
mysql -u root -p < src/main/resources/sql/forum_schema.sql

# 3. 修改配置文件
# 编辑 src/main/resources/application.yml，配置数据库连接和 Redis 地址

# 4. 构建并启动
./mvnw spring-boot:run
```

### API 文档

启动后访问：`http://localhost:8080/swagger-ui.html` （Knife4j/SpringDoc）

## 🔌 关键接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/agent/chat/sse` | POST | AI SSE 流式对话 |
| `/api/recommend/search` | POST | 院校推荐搜索 |
| `/api/rank/convert` | GET | 分数↔位次转换 |
| `/api/simulation/save` | POST | 保存志愿方案 |
| `/api/user/login` | POST | 用户登录 |
| `/api/forum/posts` | GET/POST | 论坛帖子列表/发布 |

详细接口说明见 [AI前端对接文档](./AI前端对接文档.md) 和 [论坛API文档](./src/main/resources/FORUM_API.md)。

## 📊 提交历史

| Commit | 说明 |
|--------|------|
| `e55a9ad` | feat: 项目脚手架与构建配置 |
| `5e5eecb` | feat: 公共基础设施层 |
| `fa9d40b` | feat: 数据层与缓存 |
| `d63b9ad` | feat: DTO/VO 传输层 |
| `b061218` | feat: 核心业务服务与控制器 |
| `ac040ad` | feat: AI 智能推荐模块 |
| `4d7b57a` | feat: 论坛社区模块 |
| `b36c147` | docs: 添加 AI 前端对接文档 |

## 📝 License

MIT License © 2026 keluoluo-ha
