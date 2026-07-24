# 2066 世界杯管理系统 (World Cup Manager)

基于 Spring Boot 3 + Vue 3 + Pinia + Element Plus 的全栈世界杯赛事管理系统。

## 🎯 项目功能

### 核心模块
- ✅ **球队管理** - 32支参赛球队的增删改查，支持按小组/大洲筛选
- ✅ **球员管理** - 每队23人大名单，支持能力值、位置、数据统计
- ✅ **小组赛** - 8组×4队单循环赛，实时积分榜计算
- ✅ **淘汰赛** - 16强→8强→4强→决赛，自动生成对阵树
- ✅ **比分录入** - 支持实时录入和修改，自动晋级胜者
- ✅ **赛程日历** - 所有比赛按阶段分类展示
- ✅ **仪表板** - 统计概览 + ECharts 图表（各洲分布、射手榜）
- ✅ **用户权限** - ADMIN/OPERATOR/VIEWER 三级权限控制
- ✅ **JWT 认证** - 无状态登录注册，路由守卫
- ✅ **WebSocket** - 预留实时比分推送接口

## 📦 技术栈

### 后端
- Spring Boot 3.2.5
- Spring Security + JWT (jjwt)
- WebSocket (STOMP + SockJS)
- Maven
- Java 17

### 前端
- Vue 3.4 (Composition API)
- Vite 5
- Pinia (状态管理)
- Vue Router 4
- Element Plus 2.6
- ECharts 5
- Axios

## 🚀 快速启动

### 前置要求
- Java 17+
- Node.js 18+
- Maven 3.8+

### 1. 启动后端

```bash
cd backend
# PowerShell；生产环境请使用独立生成的高强度密钥
$env:JWT_SECRET="replace-with-a-random-secret-at-least-32-characters"
mvn clean spring-boot:run
```

后端启动后访问 http://localhost:8080

启动时会自动初始化：
- 3个默认用户 (admin/operator/viewer)
- 32支球队（真实世界杯数据）
- 736名球员（每队23人，随机生成）
- 48场小组赛赛程

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动后访问 http://localhost:5173

### 3. 登录系统

**默认账号：**
- 管理员：`admin` / `admin123` （全部权限）
- 操作员：`operator` / `123456` （可编辑比分）
- 观众：`viewer` / `viewer123` （只读）

## 📖 使用流程

### 完整赛事流程

1. **查看球队** - 进入「球队管理」查看32支参赛球队
2. **查看球员** - 进入「球员管理」按球队筛选球员
3. **录入小组赛比分** - 进入「小组赛」，选择小组，录入每场比赛比分
4. **查看积分榜** - 积分榜自动更新（胜3平1负0）
5. **生成淘汰赛** - 小组赛全部结束后，点击「生成淘汰赛对阵」
6. **录入淘汰赛比分** - 进入「淘汰赛」，点击比赛录入比分，胜者自动晋级
7. **查看决赛** - 决赛结束后，冠军诞生！

### 权限说明

| 功能 | ADMIN | OPERATOR | VIEWER |
|------|-------|----------|--------|
| 查看数据 | ✅ | ✅ | ✅ |
| 录入比分 | ✅ | ✅ | ❌ |
| 编辑赛程 | ✅ | ✅ | ❌ |
| 增删球队/球员 | ✅ | ❌ | ❌ |
| 管理用户 | ✅ | ❌ | ❌ |

## 🗂️ 项目结构

```
D:\soccer\
├── backend/                          # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/worldcup/
│       ├── WorldCupApplication.java  # 启动类
│       ├── config/                   # 配置（JWT、Security、CORS、WebSocket）
│       ├── model/                    # 实体类 + 枚举
│       ├── service/                  # 业务逻辑层
│       ├── controller/               # REST 控制器
│       └── handler/                  # 全局异常处理
│
└── frontend/                         # Vue 3 前端
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.js                   # 入口
        ├── App.vue                   # 根组件
        ├── router/index.js           # 路由 + 守卫
        ├── stores/auth.js            # Pinia 状态管理
        ├── utils/                    # Axios + WebSocket 封装
        ├── components/Layout.vue     # 主布局（侧边栏+顶栏）
        └── views/                    # 8个页面视图
            ├── Login.vue
            ├── Register.vue
            ├── Dashboard.vue
            ├── TeamManager.vue
            ├── PlayerManager.vue
            ├── GroupStage.vue
            ├── Knockout.vue
            ├── Schedule.vue
            └── UserManager.vue
```

## 🔧 API 接口

所有 API 路径前缀：`/api`

### 认证
- `POST /api/auth/login` - 登录
- `POST /api/auth/register` - 注册
- `GET /api/auth/me` - 获取当前用户

### 球队
- `GET /api/teams` - 获取球队列表（支持 group/continent/keyword 筛选）
- `POST /api/teams` - 新增球队（ADMIN）
- `PUT /api/teams/{id}` - 更新球队（ADMIN）
- `DELETE /api/teams/{id}` - 删除球队（ADMIN）

### 球员
- `GET /api/players` - 获取球员列表（支持 teamId/position 筛选）
- `GET /api/players/top-scorers` - 射手榜
- `GET /api/players/top-assists` - 助攻榜

### 小组赛
- `GET /api/groups` - 获取所有小组
- `GET /api/groups/{groupLetter}/standings` - 小组积分榜
- `GET /api/groups/all-standings` - 所有小组积分榜
- `POST /api/groups/generate-knockout` - 生成淘汰赛（ADMIN）

### 比赛
- `GET /api/matches` - 获取比赛列表（支持 stage/group 筛选）
- `PUT /api/matches/{id}/score` - 录入比分（OPERATOR/ADMIN）

### 淘汰赛
- `GET /api/knockout/bracket` - 获取完整对阵树

### 仪表板
- `GET /api/dashboard/stats` - 统计数据
- `GET /api/dashboard/schedule` - 赛程日历

### 用户管理（ADMIN）
- `GET /api/users` - 用户列表
- `PUT /api/users/{id}` - 更新用户
- `PUT /api/users/{id}/reset-password` - 重置密码

## 🎨 界面预览

- **仪表板** - 统计卡片 + 射手榜表格 + ECharts 饼图
- **球队管理** - 表格展示，支持搜索/筛选/分页
- **小组赛** - 积分榜 + 比赛列表，支持比分录入弹窗
- **淘汰赛** - 树状对阵图，点击比赛录入比分
- **赛程日历** - 按阶段筛选所有比赛

## 📝 开发说明

### 积分排名算法
```
1. 胜 3 分，平 1 分，负 0 分
2. 积分相同 → 比较净胜球
3. 净胜球相同 → 比较进球数
4. 进球数相同 → 比较球队名称
```

### 淘汰赛对阵规则
```
16强交叉对阵（FIFA 标准）：
A1 vs B2    C1 vs D2    E1 vs F2    G1 vs H2
B1 vs A2    D1 vs C2    F1 vs E2    H1 vs G2

8强：相邻两场 16 强胜者对决
4强：相邻两场 8 强胜者对决
决赛：两场半决赛胜者对决
季军赛：两场半决赛负者对决
```

### 数据存储
- 使用 `ConcurrentHashMap` 内存存储
- 应用重启后数据重置（可替换为 JPA/H2 持久化）
- 所有 Service 类预留 JPA 替换接口

## 🐛 已知限制

1. **数据持久化** - 当前为内存存储，重启丢失（生产环境需接入数据库）
2. **WebSocket** - 代码已预留，默认使用轮询（取消注释即可启用）
3. **点球大战** - 淘汰赛平局暂不支持点球（可后续扩展）
4. **球员统计** - 进球/助攻需手动更新（可集成比赛事件系统）

## 📦 构建生产版本

### 后端
```bash
cd backend
mvn clean package
java -jar target/worldcup-manager-1.0.0.jar
```

### 前端
```bash
cd frontend
npm run build
# 生成文件在 dist/ 目录，可部署到 Nginx/CDN
```

## 📄 License

MIT - 仅供学习参考使用

---

**2066 世界杯管理系统** - 让赛事管理更高效！⚽🏆
