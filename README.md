# CPR DB — 心肺复苏训练数据管理系统

基于 Spring Boot 4 构建的 CPR（Cardiopulmonary Resuscitation）训练数据后端服务，为 Unity VR 客户端及微信小程序提供用户认证、训练成绩管理、智能问答、知识库、个人信息管理等 RESTful API。

> **生产地址**: http://123.57.30.132:8080  
> **API 文档**: [API.md](./API.md)  
> **种子账号**: `testuser` / `Test@123456`

## 技术栈

| 层次       | 技术选型                                         |
| ---------- | ------------------------------------------------ |
| 编程语言   | Java 17                                          |
| 应用框架   | Spring Boot 4.0.6（Spring Framework 7）           |
| Web 层     | Spring WebMVC                                    |
| 数据访问   | Spring Data JPA / Hibernate                      |
| 数据库     | MySQL 8+（生产）/ H2 内存数据库（开发）            |
| 安全认证   | Spring Security + JWT（jjwt 0.11.5, HMAC-SHA256） |
| 密码加密   | BCrypt                                           |
| 参数校验   | Jakarta Bean Validation                          |
| 构建工具   | Apache Maven 3.9（Maven Wrapper）                 |
| 部署       | Ubuntu 24/26 LTS + systemd + MySQL               |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+（或使用内置 Maven Wrapper）
- MySQL 8+（生产环境）/ 无需安装（开发环境使用 H2）

### 方式一：H2 开发模式（零配置）

```bash
./mvnw spring-boot:run
```

启动后访问 H2 控制台 `http://localhost:8080/h2-console`。H2 为内存数据库，重启后数据丢失。

### 方式二：MySQL 生产模式

```sql
CREATE DATABASE cpr_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

编辑 `application.properties`，启用 MySQL 配置：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cpr_db?characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### 构建可执行 JAR

```bash
./mvnw package -DskipTests
java -jar target/cpr_db-0.0.1-SNAPSHOT.jar
```

## API 接口总览

统一前缀 `/api/v1/`，返回格式 `{code, message, data}`。鉴权方式：`Authorization: Bearer <token>`。

| 模块 | 方法 | 路径 | 认证 | 说明 |
|------|------|------|:--:|------|
| 认证 | `POST` | `/api/v1/auth/register` | 🔓 | 用户注册 |
| 认证 | `POST` | `/api/v1/auth/login` | 🔓 | 用户登录，返回 JWT |
| 场景 | `GET` | `/api/v1/scenes` | 🔓 | 训练场景列表（5 个） |
| 知识库 | `GET` | `/api/v1/knowledge` | 🔓 | 知识库列表（27 条，支持 `?category=`） |
| 问答 | `GET` | `/api/v1/qa/presets` | 🔓 | 预设问题 |
| 问答 | `POST` | `/api/v1/qa` | 🔐 | 智能提问 |
| 视频 | `GET` | `/api/v1/videos/{videoId}` | 🔓 | 获取视频 |
| 成绩 | `POST` | `/api/v1/scores` | 🔐 | 提交成绩 |
| 成绩 | `GET` | `/api/v1/scores` | 🔐 | 成绩列表 |
| 成绩 | `GET` | `/api/v1/scores/latest` | 🔐 | 最新成绩 |
| 成绩 | `GET` | `/api/v1/scores/stats` | 🔐 | 成绩统计 |
| 学员 | `GET` | `/api/v1/students` | 🔐 | 学员列表 |
| 用户 | `GET` | `/api/v1/user/info` | 🔐 | 用户速查（id/username/createdAt） |
| 个人信息 | `GET` | `/api/v1/profile` | 🔐 | 完整个人信息 |
| 个人信息 | `PUT` | `/api/v1/profile` | 🔐 | 更新个人信息 |
| 个人信息 | `POST` | `/api/v1/profile/avatar` | 🔐 | 上传头像 |
| 姿态 | `POST` | `/api/v1/pose/detect` | 🔐 | 姿态识别（图片上传） |
| 静态 | `GET` | `/uploads/**` | 🔓 | 头像等静态文件 |

详细请求/响应格式见 [API.md](./API.md)。

## 项目结构

```
src/main/java/com/cpr_db/cpr_db/
├── CprDbApplication.java
├── common/
│   ├── ApiResponse.java              # 统一响应 {code, message, data}
│   ├── BusinessException.java        # 业务异常（支持自定义 HTTP code）
│   └── GlobalExceptionHandler.java   # 全局异常处理（10 种异常 → 精确 HTTP 状态码）
├── config/
│   ├── DataSeeder.java               # 启动种子数据（场景/知识库/视频）
│   └── WebMvcConfig.java             # 静态文件映射 /uploads/**
├── controller/
│   ├── AuthController.java           # 注册 / 登录
│   ├── KnowledgeController.java      # 知识库查询
│   ├── PoseController.java           # 姿态识别
│   ├── ProfileController.java        # 个人信息（含头像上传）
│   ├── QaController.java             # 智能问答
│   ├── SceneController.java          # 训练场景
│   ├── ScoreController.java          # 成绩管理 + 统计
│   ├── StudentController.java        # 学员管理
│   ├── UserController.java           # 用户速查
│   └── VideoController.java          # 视频资源
├── dto/
│   ├── AuthRequest / AuthResponse / RegisterRequest
│   ├── ProfileResponse / ProfileUpdateRequest
│   ├── ScoreDto / ScoreSubmitRequest / ScoreStatsResponse
│   ├── QaRequest / QaResponse / ChatMessage / PresetsResponse
│   ├── PoseDetectResponse / PoseLandmark / AngleAnalysis
│   ├── UserInfoResponse / VideoResponse
├── entity/
│   ├── User.java                     # 用户（含 profile 字段）
│   ├── Score.java                    # 成绩
│   ├── Video.java                    # 视频
│   ├── Scene.java                    # 训练场景
│   ├── Student.java                  # 学员
│   └── Knowledge.java                # 知识库
├── repository/
│   ├── UserRepository.java
│   ├── ScoreRepository.java
│   ├── VideoRepository.java
│   ├── SceneRepository.java
│   ├── StudentRepository.java
│   └── KnowledgeRepository.java
├── security/
│   ├── SecurityConfig.java           # CORS + 公开/鉴权路由
│   ├── JwtTokenUtil.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
└── service/
    ├── AuthService.java
    ├── KnowledgeService.java
    ├── PoseService.java
    ├── QaService.java
    ├── SceneService.java
    ├── ScoreService.java
    ├── StudentService.java
    └── VideoService.java
```

## 数据库表

| 表名 | 说明 | 记录数 |
|------|------|:--:|
| `users` | 用户（含个人资料字段） | 1（种子） |
| `scores` | 训练成绩 | 按需 |
| `videos` | 视频资源 | 2（种子） |
| `scenes` | 训练场景 | 5（种子） |
| `knowledge` | 知识库 | 27（种子） |
| `students` | 学员 | 按需 |

JPA `ddl-auto=update` 自动建表/加列，无需手动执行 DDL。

## 安全设计

- JWT 无状态认证（HMAC-SHA256），24 小时过期
- BCrypt 密码哈希存储
- 用户仅可查询本人成绩，通过 JWT username 隔离
- **公开路由**: auth、scenes、knowledge、qa/presets、videos、uploads
- **鉴权路由**: scores、students、user、profile、pose、qa
- 登录失败统一返回 401，防止用户枚举
- 全局异常处理覆盖 10 种异常类型，返回精确 HTTP 状态码（400/401/403/404/405/409/415/500）
- 头像上传校验格式（jpg/png/webp）和大小（≤2MB），手机号/学号唯一性校验

> ℹ️ 生产环境请替换 `jwt.secret` 为强随机密钥，通过环境变量注入。

## 部署

ECS 部署地址 `123.57.30.132:8080`，systemd 服务 `cpr-db.service`。

```bash
# 一键部署
python deploy/deploy-to-ecs.py

# 或手动
cd /opt/cpr-db-src && git pull && mvn -DskipTests clean package -q
cp target/*.jar /opt/cpr-db/cpr_db-0.0.1-SNAPSHOT.jar
systemctl restart cpr-db
```

部署脚本位于 `deploy/` 目录，含 `.env`、`init.sql`、`cpr-db.service`、`setup-ecs.sh`。

## 相关项目

| 项目 | 仓库 |
| ---- | ---- |
| 微信小程序 | [Life-Guard-Mini-Program](https://github.com/ZhangJing-gugugaga/Life-Guard-Mini-Program) |
| Unity VR 端 | [VR-Security](https://github.com/guoguangxuan6-del/VR-Security) |
