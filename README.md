# CPR DB — 心肺复苏训练数据管理系统

基于 Spring Boot 4 构建的 CPR（Cardiopulmonary Resuscitation）训练数据后端服务，为 Unity VR 客户端及微信小程序提供用户认证、训练成绩管理、智能问答、知识库、个人信息管理等 RESTful API。

> **生产地址**: http://123.57.30.132:8080  
> **API 文档**: [API.md](./API.md)  
> **种子账号**: `testuser` / `Test@123456`

> 🟢 **质量门禁（2026-07-30）**：后端 12 个 P0 缺陷 + P1-6 已闭环，独立复跑 `mvn test` **48/48 全绿**（BUILD SUCCESS）。验证结论与逐项证据见 [BACKEND_P0_CLOSURE.md](./BACKEND_P0_CLOSURE.md)；契约更新见 [API.md](./API.md)。  
> ⚠️ `REVIEW-REPORT.md` 为**修复前**的评审基线快照，不代表当前状态，请勿据此判断缺陷是否已修。

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

| 符号 | 含义 |
|:--:|------|
| 🔓 | 公开，无需 token |
| 🔐 | 需登录 |
| 👑 | 需 admin 或 super_admin |
| 🔒 | 仅 super_admin |

| 模块 | 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|:--:|------|
| 认证 | `POST` | `/api/v1/auth/register` | 🔓 | 用户注册 |
| 认证 | `POST` | `/api/v1/auth/login` | 🔓 | 用户登录，返回 JWT |
| 场景 | `GET` | `/api/v1/scenes` | 🔐 | 场景列表（全量） |
| 场景 | `GET` | `/api/v1/scenes/list` | 🔐 | 场景分页 |
| 场景 | `GET/POST/PUT/DELETE/PATCH` | `/api/v1/scenes[/{id}[/status]]` | 🔐/👑 | 场景 CRUD |
| 知识库 | `GET` | `/api/v1/knowledge` | 🔐 | 知识列表（支持 `?category=`） |
| 知识库 | `GET/POST/PUT/DELETE` | `/api/v1/knowledge[/{id}]` | 🔐/👑 | 知识 CRUD |
| 问答 | `GET` | `/api/v1/qa/presets` | 🔓 | 预设问题 |
| 问答 | `POST` | `/api/v1/qa` | 🔐 | 智能提问 |
| 视频 | `GET` | `/api/v1/videos` | 🔐 | 视频列表 |
| 视频 | `GET/POST/PUT/DELETE/PATCH` | `/api/v1/videos[/{id}[/status]]` | 🔐/👑 | 视频 CRUD |
| 技能 | `GET` | `/api/v1/skills` | 🔐 | 技能列表 |
| 技能 | `GET/POST/PUT/DELETE/PATCH` | `/api/v1/skills[/{id}[/status]]` | 🔐/👑 | 技能 CRUD |
| 步骤 | `GET` | `/api/v1/steps` | 🔐 | 步骤列表 |
| 步骤 | `GET/POST/PUT/DELETE/PATCH` | `/api/v1/steps[/{id}[/status]]` | 🔐/👑 | 步骤 CRUD |
| 步骤 | `PUT` | `/api/v1/steps/{id}/reorder` | 👑 | 步骤排序 |
| 成绩 | `POST` | `/api/v1/scores` | 🔐 | 提交成绩 |
| 成绩 | `GET` | `/api/v1/scores` | 🔐 | 成绩列表（admin 支持 `?all=true`） |
| 成绩 | `GET/DELETE` | `/api/v1/scores/{id}` | 🔐/👑 | 成绩详情/删除 |
| 成绩 | `GET` | `/api/v1/scores/latest` | 🔐 | 最新成绩 |
| 成绩 | `GET` | `/api/v1/scores/stats` | 🔐 | 成绩统计 |
| 学员 | `GET` | `/api/v1/students` | 🔐 | 学员列表 |
| 学员 | `GET/POST/PUT/DELETE/PATCH` | `/api/v1/students[/{id}[/status]]` | 🔐/👑 | 学员 CRUD |
| 用户 | `GET` | `/api/v1/user/info` | 🔐 | 用户信息（含 role/realName/avatar） |
| 用户 | `PUT` | `/api/v1/user/password` | 🔐 | 修改密码 |
| 用户 | `GET/POST` | `/api/v1/user/admins` | 🔒 | 管理员列表/创建 |
| 用户 | `PUT` | `/api/v1/user/{id}/role` | 🔒 | 修改角色 |
| 用户 | `DELETE` | `/api/v1/user/{id}` | 🔒 | 删除用户 |
| 个人信息 | `GET/PUT` | `/api/v1/profile` | 🔐 | 个人信息 |
| 个人信息 | `POST` | `/api/v1/profile/avatar` | 🔐 | 上传头像 |
| 上传 | `POST` | `/api/v1/upload/image` | 🔐 | 上传图片（jpg/png/webp ≤2MB） |
| 上传 | `POST` | `/api/v1/upload/video` | 👑 | 上传视频（mp4/webm/mov ≤500MB） |
| 日志 | `GET` | `/api/v1/logs` | 🔒 | 操作日志查询 |
| 姿态 | `POST` | `/api/v1/pose/detect` | 🔐 | 姿态识别 |
| 静态 | `GET` | `/uploads/**` | 🔓 | 头像等静态文件 |

共 **14 个 Controller，约 60 个端点**。详细请求/响应格式见 [API.md](./API.md)。

## 项目结构

```
src/main/java/com/cpr_db/cpr_db/
├── CprDbApplication.java
├── common/
│   ├── ApiResponse.java              # 统一响应 {code, message, data}
│   ├── BusinessException.java        # 业务异常（支持自定义 HTTP code）
│   └── GlobalExceptionHandler.java   # 全局异常处理（10 种异常 → 精确 HTTP 状态码）
├── config/
│   ├── DataSeeder.java               # 启动种子数据（场景/知识库/视频/管理员/测试用户）
│   ├── JacksonConfig.java            # 全局 Jackson SNAKE_CASE 序列化
│   └── WebMvcConfig.java             # 静态文件映射 /uploads/**
├── controller/
│   ├── AuthController.java           # 注册 / 登录
│   ├── KnowledgeController.java      # 知识库 CRUD
│   ├── PoseController.java           # 姿态识别
│   ├── ProfileController.java        # 个人信息（含头像上传）
│   ├── QaController.java             # 智能问答
│   ├── SceneController.java          # 训练场景 CRUD + 状态管理
│   ├── ScoreController.java          # 成绩管理 + 统计 + admin 全量查询
│   ├── StudentController.java        # 学员 CRUD + 状态管理
│   ├── SkillController.java         # 技能 CRUD + 状态管理
│   ├── StepController.java           # 步骤 CRUD + 状态 + 排序
│   ├── UserController.java           # 用户信息 / 密码 / 管理员管理
│   ├── VideoController.java          # 视频 CRUD + 状态管理
│   ├── UploadController.java         # 图片/视频文件上传
│   └── LogController.java            # 操作日志查询
├── dto/
│   ├── AuthRequest / AuthResponse / RegisterRequest
│   ├── ProfileResponse / ProfileUpdateRequest
│   ├── ScoreDto / ScoreSubmitRequest / ScoreStatsResponse
│   ├── QaRequest / QaResponse / ChatMessage / PresetsResponse
│   ├── PoseDetectResponse / PoseLandmark / AngleAnalysis
│   ├── UserInfoResponse / VideoResponse
│   └── PasswordChangeRequest / AdminCreateRequest
├── entity/
│   ├── User.java                     # 用户（含 profile 字段）
│   ├── Score.java                    # 成绩
│   ├── Video.java                    # 视频
│   ├── Scene.java                    # 训练场景
│   ├── Student.java                  # 学员
│   ├── Knowledge.java                # 知识库
│   ├── Skill.java                    # 技能
│   ├── Step.java                     # 训练步骤
│   └── Log.java                      # 操作日志
├── repository/
│   ├── UserRepository.java
│   ├── ScoreRepository.java
│   ├── VideoRepository.java
│   ├── SceneRepository.java
│   ├── StudentRepository.java
│   ├── KnowledgeRepository.java
│   ├── SkillRepository.java
│   ├── StepRepository.java
│   └── LogRepository.java
├── security/
│   ├── SecurityConfig.java           # CORS + 三层权限 + @EnableMethodSecurity
│   ├── JwtTokenUtil.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java  # DB role 字段 → SimpleGrantedAuthority
└── service/
    ├── AuthService.java
    ├── KnowledgeService.java
    ├── PoseService.java
    ├── QaService.java
    ├── SceneService.java
    ├── ScoreService.java
    ├── StudentService.java
    ├── VideoService.java
    ├── SkillService.java
    ├── StepService.java
    ├── LogService.java
    └── AdminService.java
```

## 数据库表

| 表名 | 说明 | 记录数 |
|------|------|:--:|
| `users` | 用户（含个人资料字段、role） | 2（种子：admin + testuser） |
| `scores` | 训练成绩 | 按需 |
| `videos` | 视频资源 | 2（种子） |
| `scenes` | 训练场景 | 5（种子） |
| `knowledge` | 知识库 | 27（种子） |
| `students` | 学员 | 按需 |
| `skills` | 技能 | 按需 |
| `steps` | 训练步骤 | 按需 |
| `operation_logs` | 操作日志 | 按需 |

JPA `ddl-auto=update` 自动建表/加列，无需手动执行 DDL。

## 安全设计

- JWT 无状态认证（HMAC-SHA256），24 小时过期
- BCrypt 密码哈希存储
- **三层权限体系**：
  - `permitAll`：`/auth/**`、`/qa/presets`、`/uploads/**`
  - `authenticated`：videos、scenes、knowledge、skills、steps、scores、profile、students、user/info、user/password、upload/image、qa、pose
  - `@PreAuthorize` 方法级：admin/super_admin 管理写操作，super_admin 独占管理员管理和日志
- `@EnableMethodSecurity` 启用方法级权限注解
- `CustomUserDetailsService` 从 DB role 字段映射权限（`hasAuthority` 而非 `hasRole`）
- Jackson 全局 SNAKE_CASE 序列化（Java 驼峰 → JSON 下划线）
- 用户仅可查询本人成绩，admin 可通过 `?all=true` 查全部
- 管理员管理安全措施：不能删除自己、不能删除最后一个 super_admin
- 登录失败统一返回 401，防止用户枚举
- 全局异常处理覆盖 10 种异常类型，返回精确 HTTP 状态码
- 文件上传校验：图片 jpg/png/webp ≤2MB，视频 mp4/webm/mov ≤500MB
- 手机号/学号唯一性校验
- **CORS 来源外部化**：通过 `cpr.cors.allowed-origins` 配置（默认 `http://localhost:3000,http://localhost:5173`）；配置为 `*` 时自动禁用 credentials，避免通配符带凭据的安全风险
- **分页上限保护**：所有列表接口 `pageSize` 服务端硬上限 100，超限自动截断（防 DoS）
- **统一分页响应信封**：列表接口统一返回 `{list, total, page, page_size}`
- **请求体字段校验**：入参 DTO 使用 Jakarta `@Valid`，字段缺失/格式错误返回 400 并带字段级 message
- **文件上传防路径穿越**：存储文件名使用 UUID 重命名并剥离 `..` / 路径分隔符，杜绝目录穿越与原文件名泄露
- **最后 super_admin 守卫**：删除最后一个 `super_admin` 返回 409 `cannot delete the last super admin`

> ℹ️ 生产环境请替换 `jwt.secret` 为强随机密钥，通过环境变量注入。

## 测试

### 测试规则

| 规则 | 说明 |
|------|------|
| **Content-Type** | 所有 JSON 请求必须带 `Content-Type: application/json`，文件上传用 `multipart/form-data` |
| **Token 前缀** | 鉴权请求 Header 格式为 `Authorization: Bearer <token>` |
| **Token 有效期** | 24 小时，过期返回 401，需重新登录 |
| **成绩隔离** | 用户只能查询自己的成绩，查他人返回 403 |
| **个人信息隔离** | 只能查看/修改自己的 profile |
| **手机号唯一** | 同一手机号不能被两个用户使用，更新时排除自身 |
| **学号唯一** | 同学号规则同上 |
| **头像限制** | 仅 jpg/png/webp，≤ 2MB |
| **空数据** | `students` 无种子数据返回 `[]`；`stats` 无成绩时各字段为 0/空数组 |

### 运行单元测试

```bash
# 本仓库已验证可跑通的测试命令（./mvnw 在本机存在 classpath 问题，请用此脚本）
./mvn-local.sh test
```

结果：**Tests run: 48, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS**。  
其中基线 16 个 + P0 专项回归 32 个，无 `@Disabled` / 无删减断言。完整证据见 [BACKEND_P0_CLOSURE.md](./BACKEND_P0_CLOSURE.md)。

### 测试方法

#### 1. 公开接口（无需 token）

```bash
BASE="http://123.57.30.132:8080"

# 场景列表
curl -s $BASE/api/v1/scenes | python3 -m json.tool

# 知识库（全部 / 按分类）
curl -s "$BASE/api/v1/knowledge" | python3 -m json.tool
curl -s "$BASE/api/v1/knowledge?category=AED" | python3 -m json.tool

# 预设问题
curl -s $BASE/api/v1/qa/presets | python3 -m json.tool

# 视频
curl -s $BASE/api/v1/videos/video1 | python3 -m json.tool
```

#### 2. 登录获取 Token

```bash
# 登录
TOKEN=$(curl -s -X POST $BASE/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test@123456"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")

echo "Token: ${TOKEN:0:30}..."

# 注册新用户
curl -s -X POST $BASE/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","password":"Pass@123"}' | python3 -m json.tool
```

#### 3. 鉴权接口（需 Token）

```bash
AUTH="Authorization: Bearer $TOKEN"

# 个人信息
curl -s -H "$AUTH" $BASE/api/v1/profile | python3 -m json.tool

# 更新个人信息
curl -s -X PUT -H "Content-Type: application/json" -H "$AUTH" \
  -d '{"realName":"张三","gender":1,"phone":"13800138000","className":"护理2班"}' \
  $BASE/api/v1/profile | python3 -m json.tool

# 用户速查
curl -s -H "$AUTH" $BASE/api/v1/user/info | python3 -m json.tool

# 学员列表
curl -s -H "$AUTH" $BASE/api/v1/students | python3 -m json.tool

# 提交成绩
curl -s -X POST -H "Content-Type: application/json" -H "$AUTH" \
  -d '{"scene":"成人CPR训练","skill":"胸外按压","totalScore":85.5}' \
  $BASE/api/v1/scores | python3 -m json.tool

# 成绩列表 / 最新 / 统计
curl -s -H "$AUTH" $BASE/api/v1/scores | python3 -m json.tool
curl -s -H "$AUTH" $BASE/api/v1/scores/latest | python3 -m json.tool
curl -s -H "$AUTH" $BASE/api/v1/scores/stats | python3 -m json.tool

# 智能问答
curl -s -X POST -H "Content-Type: application/json" -H "$AUTH" \
  -d '{"question":"CPR的按压频率是多少？"}' \
  $BASE/api/v1/qa | python3 -m json.tool
```

#### 4. 文件上传

```bash
# 上传头像（有本地文件时）
curl -X POST -H "$AUTH" \
  -F "file=@/path/to/avatar.jpg" \
  $BASE/api/v1/profile/avatar

# 姿态识别
curl -X POST -H "$AUTH" \
  -F "image=@/path/to/pose.jpg" \
  $BASE/api/v1/pose/detect
```

#### 5. 错误场景测试

```bash
# 缺 Content-Type → 415
curl -s -X POST $BASE/api/v1/auth/login -d '{}'

# 缺 Token → 401
curl -s $BASE/api/v1/profile

# Token 过期/无效 → 401
curl -s -H "Authorization: Bearer invalid_token" $BASE/api/v1/profile

# 查他人成绩 → 403
curl -s -H "$AUTH" "$BASE/api/v1/scores?username=other_user"

# 错误方法 → 405
curl -s $BASE/api/v1/auth/login

# 手机号冲突 → 409
curl -s -X PUT -H "Content-Type: application/json" -H "$AUTH" \
  -d '{"phone":"13800138000"}' $BASE/api/v1/profile
```

### 常见错误码

| HTTP | code | message 示例 | 原因 | 解决方法 |
|:----:|------|------|------|------|
| 200 | 200 | success | 正常 | — |
| 400 | 400 | username is required | 缺少必填参数 | 检查请求体字段名和值 |
| 400 | 400 | 请求体缺失或格式错误 | 未传 body 或 JSON 格式错误 | 加 `Content-Type: application/json` 和正确 JSON |
| 400 | 400 | username already exists | 用户名重复 | 换一个用户名注册 |
| 400 | 400 | 不支持的文件格式 | 头像格式非 jpg/png/webp | 转换图片格式 |
| 400 | 400 | 文件大小不能超过 2MB | 头像过大 | 压缩图片 |
| 400 | 400 | 文件上传失败 | multipart 请求格式有误 | 确认表单字段名为 `file`，Content-Type 为 `multipart/form-data` |
| 400 | 400 | invalid phone number format | 手机号格式不符 | 使用 11 位中国大陆手机号 |
| 401 | 401 | Bad credentials | 用户名或密码错误 | 检查账号密码 |
| 401 | 401 | Full authentication is required | 缺少 Token | 先登录获取 Token |
| 401 | 401 | JWT expired / invalid | Token 过期或无效 | 重新登录 |
| 403 | 403 | Access denied | 无权限 | 确认路由是否有权访问 |
| 403 | 403 | only current user may query scores | 企图查他人成绩 | 去掉 `?username=` 参数 |
| 404 | 404 | score not found | 尚无成绩记录 | 先提交一条成绩 |
| 405 | 405 | 不支持的请求方法 | GET/POST 方法用错 | 检查 HTTP 方法 |
| 409 | 409 | 手机号已被其他用户使用 | 手机号冲突 | 换一个手机号 |
| 409 | 409 | 学号已被其他用户使用 | 学号冲突 | 换一个学号 |
| 415 | 415 | 不支持的 Content-Type | Content-Type 错误 | 设为 `application/json` 或 `multipart/form-data` |
| 500 | 500 | Internal server error | 未知服务器错误 | 查看 ECS 日志 `journalctl -u cpr-db -n 50` |
| 500 | 500 | 头像保存失败 | 磁盘写入异常 | 检查 `/opt/cpr-db/uploads/` 权限 |

> 提示：所有非 200 响应均包含 `"data": null`，前端可据此判断是否成功。

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
