# CPR DB — 心肺复苏训练数据管理系统

基于 Spring Boot 4 构建的 CPR（Cardiopulmonary Resuscitation）VR 训练数据后端服务，为 Unity VR 客户端及 Web 前端提供用户认证、训练成绩管理与视频资源查询等 RESTful API。

## 技术栈

| 层次       | 技术选型                                         |
| ---------- | ------------------------------------------------ |
| 编程语言   | Java 17                                          |
| 应用框架   | Spring Boot 4.0.6（Spring Framework 7）           |
| Web 层     | Spring WebMVC                                    |
| 数据访问   | Spring Data JPA / Hibernate                      |
| 数据库     | MySQL 8+（utf8mb4 字符集）                        |
| 安全认证   | Spring Security + JWT（jjwt 0.11.5, HMAC-SHA256） |
| 密码加密   | BCrypt                                           |
| 参数校验   | Jakarta Bean Validation                          |
| 构建工具   | Apache Maven 3.9（Maven Wrapper）                 |

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8+
- Maven 3.9+（或使用内置 Maven Wrapper）

### 创建数据库

```sql
CREATE DATABASE cpr_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 配置

复制 `.env` 文件并修改数据库连接和 JWT 密钥：

```bash
cp .env.example .env
```

`.env` 内容示例：

```ini
DB_URL=jdbc:mysql://localhost:3306/cpr_db?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=你的密码
JWT_SECRET=替换为至少32字符的强随机密钥
JWT_EXPIRATION_MS=86400000
```

启动时通过 `spring-boot-maven-plugin` 或 `--env-file` 加载环境变量。

### 构建与运行

```bash
# 编译
./mvnw compile

# 启动应用（开发模式，端口 8080）
./mvnw spring-boot:run

# 构建可执行 JAR
./mvnw package
java -jar target/cpr_db-0.0.1-SNAPSHOT.jar
```

## API 接口

统一前缀 `/api/v1/`，返回格式 `{code, message, data}`。

### 认证接口

| 方法 | 路径                    | 说明             |
| ---- | ----------------------- | ---------------- |
| POST | `/api/v1/auth/register` | 用户注册         |
| POST | `/api/v1/auth/login`    | 用户登录，返回 JWT |

### 成绩接口

| 方法 | 路径                    | 说明                 |
| ---- | ----------------------- | -------------------- |
| POST | `/api/v1/scores`        | 提交 CPR 训练成绩     |
| GET  | `/api/v1/scores`        | 查询当前用户成绩列表  |
| GET  | `/api/v1/scores/latest` | 查询当前用户最新成绩  |

### 视频接口

| 方法 | 路径                       | 说明               |
| ---- | -------------------------- | ------------------ |
| GET  | `/api/v1/videos/{videoId}` | 查询视频资源元数据  |

## 项目结构

```
src/main/java/com/cpr_db/cpr_db/
├── CprDbApplication.java          # 启动类
├── common/                        # 公共模块
│   ├── ApiResponse.java           # 统一响应包装器
│   ├── BusinessException.java     # 业务异常类
│   └── GlobalExceptionHandler.java # 全局异常处理器
├── controller/                    # 控制器层
│   ├── AuthController.java
│   ├── ScoreController.java
│   └── VideoController.java
├── dto/                           # 数据传输对象
├── entity/                        # JPA 实体
│   ├── User.java
│   └── Score.java
├── repository/                    # 数据访问层
├── security/                      # 安全模块
│   ├── SecurityConfig.java
│   ├── JwtTokenUtil.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
└── service/                       # 业务逻辑层
    ├── AuthService.java
    ├── ScoreService.java
    └── VideoService.java
```

## 安全设计

- JWT 无状态认证（HMAC-SHA256），默认 24 小时过期
- BCrypt 密码哈希存储，不可逆
- 用户仅可查询本人成绩，通过比对 JWT 中的 username 实现隔离
- 登录失败统一返回 401，防止用户枚举攻击

> **注意**：生产环境中务必替换 `jwt.secret` 为强随机密钥，并通过环境变量注入敏感配置。
