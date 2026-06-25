# CPR DB — 心肺复苏训练数据管理系统

基于 Spring Boot 4 构建的 CPR（Cardiopulmonary Resuscitation）VR 训练数据后端服务，为 Unity VR 客户端及微信小程序提供用户认证、训练成绩管理、智慧问答与姿态识别等 RESTful API。

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

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+（或使用内置 Maven Wrapper）
- MySQL 8+（生产环境）/ 无需安装（开发环境使用 H2）

### 方式一：H2 开发模式（零配置，推荐开发调试）

无需安装 MySQL，直接启动即可：

```bash
./mvnw spring-boot:run
```

启动后访问 H2 控制台 `http://localhost:8080/h2-console` 查看数据库。

> **注意**：H2 是内存数据库，重启后数据丢失。仅用于开发调试。

### 方式二：MySQL 生产模式

#### 1. 创建数据库

```sql
CREATE DATABASE cpr_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 2. 配置

复制配置模板并修改：

```bash
cp src/main/resources/application.example.properties src/main/resources/application.properties
```

编辑 `application.properties`，注释掉 H2 配置，启用 MySQL 配置：

```properties
# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/cpr_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

#### 3. 启动

```bash
./mvnw spring-boot:run
```

### 构建可执行 JAR

```bash
./mvnw package
java -jar target/cpr_db-0.0.1-SNAPSHOT.jar
```

## API 接口

统一前缀 `/api/v1/`，返回格式 `{code, message, data}`。

### 认证接口

| 方法 | 路径                    | 说明             | 认证 |
| ---- | ----------------------- | ---------------- | ---- |
| POST | `/api/v1/auth/register` | 用户注册         | 否   |
| POST | `/api/v1/auth/login`    | 用户登录，返回 JWT | 否   |

### 成绩接口

| 方法 | 路径                    | 说明                 | 认证 |
| ---- | ----------------------- | -------------------- | ---- |
| POST | `/api/v1/scores`        | 提交 CPR 训练成绩     | JWT  |
| GET  | `/api/v1/scores`        | 查询当前用户成绩列表  | JWT  |
| GET  | `/api/v1/scores/{id}`   | 查询单条成绩详情      | JWT  |
| GET  | `/api/v1/scores/latest` | 查询当前用户最新成绩  | JWT  |

### 智慧问答接口

| 方法 | 路径                  | 说明                    | 认证 |
| ---- | --------------------- | ----------------------- | ---- |
| POST | `/api/v1/qa`          | 智慧急救问答（RAG 驱动） | JWT  |
| GET  | `/api/v1/qa/presets`  | 获取预设问题列表         | 否   |

**请求格式**：
```json
{
  "question": "心肺复苏的正确频率是多少？",
  "history": [
    {"role": "user", "content": "..."},
    {"role": "assistant", "content": "..."}
  ]
}
```

### 姿态识别接口

| 方法 | 路径                    | 说明                    | 认证 |
| ---- | ----------------------- | ----------------------- | ---- |
| POST | `/api/v1/pose/detect`   | 姿态识别（图片上传）     | JWT  |

**请求格式**：`multipart/form-data`，字段 `image`（jpg/png）

### 视频接口

| 方法 | 路径                       | 说明               | 认证 |
| ---- | -------------------------- | ------------------ | ---- |
| GET  | `/api/v1/videos/{videoId}` | 查询视频资源元数据  | 否   |

## 项目结构

```
src/main/java/com/cpr_db/cpr_db/
├── CprDbApplication.java          # 启动类
├── common/                        # 公共模块
│   ├── ApiResponse.java           # 统一响应包装器
│   ├── BusinessException.java     # 业务异常类
│   └── GlobalExceptionHandler.java # 全局异常处理器
├── controller/                    # 控制器层
│   ├── AuthController.java        # 认证（注册/登录）
│   ├── ScoreController.java       # 成绩管理
│   ├── QaController.java          # 智慧问答
│   ├── PoseController.java        # 姿态识别
│   └── VideoController.java       # 视频资源
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
    ├── AuthService.java           # 认证逻辑
    ├── ScoreService.java          # 成绩业务
    ├── QaService.java             # 智慧问答（对接大模型 API）
    ├── PoseService.java           # 姿态识别
    └── VideoService.java          # 视频查询
```

## 安全设计

- JWT 无状态认证（HMAC-SHA256），默认 24 小时过期
- BCrypt 密码哈希存储，不可逆
- 用户仅可查询本人成绩，通过比对 JWT 中的 username 实现隔离
- 登录失败统一返回 401，防止用户枚举攻击
- 问答接口 System Prompt 从后端配置，不暴露给前端
- 姿态识别接口限制上传文件类型为图片（jpg/png）

> **注意**：生产环境中务必替换 `jwt.secret` 为强随机密钥，并通过环境变量注入敏感配置。

## 相关项目

| 项目 | 仓库 |
| ---- | ---- |
| 微信小程序 | [Life-Guard-Mini-Program](https://github.com/ZhangJing-gugugaga/Life-Guard-Mini-Program) |
| Unity VR 端 | [VR-Security](https://github.com/guoguangxuan6-del/VR-Security) |
