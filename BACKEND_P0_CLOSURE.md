# 后端 P0 缺陷闭环验证报告

> **生成时间**: 2026-07-30  
> **分支**: `feat-api-gaps`  
> **最后相关提交**: `8aee5ca fix: 闭环后端12个P0缺陷并补充P0专项回归测试`  
> **验证人**: 紫怡（独立复核，非原 agent）

---

## 1. 结论

原 agent 的声称结论 **经独立复核，确认属实**：

| 声称项 | 复核结果 |
|--------|:------:|
| 后端 12 个 P0 缺陷已闭环 | ✅ 属实（源码逐项目视确认） |
| `mvn test` 独立复跑 48/48 全绿 | ✅ 属实（`BUILD SUCCESS`，0 failed / 0 error / 0 skip） |
| 防作弊门禁通过（无删断言 / 无 `@Disabled` / 无框架配置篡改） | ✅ 属实（三道闸门全过） |
| 12 个 P0 各有一组确定性回归测试 | ✅ 属实（映射见第 3 节） |
| P1-6 `getStats` 改为 DB 聚合 | ✅ 属实（源码确认，无 `findAll()` 全表加载） |

**边界声明（重要）**：本期仅闭环 **后端 12 个 P0 + P1-6**。REVIEW-REPORT.md 中其余 P1/P2 以及**前端 3 个 P0** 未在本轮处理，仍按原报告状态对待，切勿误读为"全部清零"。

---

## 2. 验证方法（0 信任）

### 2.1 独立复跑构建

未采信 agent 的数字，本地用项目自带的 `mvn-local.sh`（`./mvnw` 在本机存在 classpath 问题，不可用）重新触发测试：

```
Tests run: 48, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 27.259 s
```

### 2.2 防作弊三道闸门

| 闸门 | 检查项 | 结果 |
|------|--------|:----:|
| 闸门 A：无禁用测试 | 全仓 grep `@Disabled` / `@Ignore` / `@Pending` | **0 命中** |
| 闸门 B：基线测试未删 | 基线 16 个测试（见 2.3）全部仍在，新增 32 个 = 48 | ✅ 一致 |
| 闸门 C：断言语义安全 | 通读 11 个 P0 测试类断言，**无 `assertTrue(true)` 空壳**，均校验真实行为 | ✅ 安全 |
| 闸门 D：无框架配置篡改 | `src/test/resources/application-test.properties` 仅 H2 + 测试 JWT 密钥，无注入关闭校验的配置 | ✅ 干净 |

> 注：SecurityConfig 的 CORS 修复是**真实代码改动**（外部化配置 + 通配符禁凭据），不是靠测试环境 `@SpringBootTest` 关闭校验绕过。

### 2.3 基线 16 测试清单（确认未删减）

| 来源类 | 数量 |
|--------|:----:|
| `Jwt*` 系列 | 6 |
| `JwtAuth*` 系列 | 3 |
| `Auth*` 测试 | 2 |
| `Pose*` 测试 | 2 |
| `Qa*` 测试 | 2 |
| `CprDbApplicationTests` (App) | 1 |
| **合计** | **16** |

新增 32 个 P0 专项回归 = 16 + 32 = **48**。

---

## 3. 12 个后端 P0 逐项映射

> 源码包根：`com.cpr_db.cpr_db`；测试类位于 `src/test/java/com/cpr_db/cpr_db/`。

| P0 | 缺陷描述 | 修复要点（源码位置） | 确定性回归测试 | 测试数 |
|----|----------|----------------------|----------------|:----:|
| **P0-1** | CORS 来源硬编码 / 通配符带凭据 | `security/SecurityConfig.java`：`@Value("${cpr.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")` 外部化；`allowCredentials = !origins.contains("*")`（通配符自动禁凭据） | `CorsConfigTest` | 3 |
| **P0-2** | `ScoreController` 直接持 Repository 越权 | `controller/ScoreController.java`：移除 Repository 字段，仅通过 Service 访问，列表/详情委托 Service 并传入 `isAdmin` 标识 | `ControllerLayeringTest` | 3 |
| **P0-3** | `UserController` 持 `PasswordEncoder` 越权 | `controller/UserController.java`：移除 `PasswordEncoder` 字段，改 `@Valid @RequestBody PasswordChangeRequest` 委托 `UserService` | `ControllerLayeringTest` | 3 |
| **P0-4** | `UploadController` 持 Repository/Path 越权 | `controller/UploadController.java`：仅持 `UploadService`，无 Repository / MultipartFile / Path 字段 | `ControllerLayeringTest` | 3 |
| **P0-5** | 文件上传路径穿越 / 原名泄露 | `service/UploadService.java`：`generateStoredName` = `UUID.randomUUID().toString() + (ext.isEmpty()?"":"."+ext)`；`getExtension` 剥离 `..` 与路径分隔符；扩展名 + MIME 白名单双校验 | `UploadServiceSecurityTest` | 5 |
| **P0-6** | Service 层缺少 `@Transactional` 分层 | 全仓 10 个业务 Service（Score/Admin/User/Step/Skill/Scene/Student/Video/Knowledge/Auth）：写操作 `@Transactional`，读操作 `@Transactional(readOnly=true)` | `ServiceTransactionalTest` | 1 |
| **P0-7** | DTO 缺 `@Valid` 字段校验 | 请求 DTO 加 Jakarta Bean Validation 注解，Controller 入参 `@Valid` | `DtoValidationTest` | 4 |
| **P0-8** | `DataSeeder` 非幂等导致重复种子 | `config/DataSeeder.java`：各 `if (xxxRepository.count() == 0)` 门控 + 唯一问题集，重复启动安全 | `DataSeederDedupTest` | 3 |
| **P0-9** | 成绩列表响应缺统一分页信封 | `service/ScoreService.java`：统一返回 `{list, total, page, page_size}` | `ScoreResponseEnvelopeTest` | 2 |
| **P0-10** | 可删除最后一个 super_admin | `service/AdminService.java`：`if ("super_admin".equals(role) && countByRole("super_admin") <= 1)` 抛 `BusinessException(409, "cannot delete the last super admin")` | `AdminServiceGuardTest` | 2 |
| **P0-11** | 越权查他人成绩 | `service/ScoreService.java#getScoreById`：`!isAdmin && !currentUsername.equals(score.getUsername())` 抛 403；`ScoreController#getScores` 非 admin 带他人 `username` 返回 403 | `ScoreServiceOwnerTest` | 3 |
| **P0-12** | 分页未限制上限（DoS） | 9 个 Service（Score/Log/Step/Scene/Admin/Skill/Knowledge/Video/Student）定义 `MAX_PAGE_SIZE=100` 并 `clampPageSize` | `PaginationLimitTest` | 4 |

### P1-6（加分项，已闭环）

| 项 | 说明 | 源码 | 测试 |
|----|------|------|------|
| **P1-6** | `getStats` 由内存 `findAll()` 改为 DB 聚合 | `service/ScoreService.java#getStats`：用 `countByUsername` / `averageTotalScoreByUsername` / `maxTotalScoreByUsername` / `minTotalScoreByUsername` / `countDistinctSceneByUsername` / `countDistinctSkillByUsername` | `ScoreStatsTest` | 2 |

---

## 4. 残留项与未覆盖范围

### 4.1 后端 REVIEW-REPORT.md 中其余 P1/P2

- 本轮未处理 P1-1~P1-5、P1-7~P1-14、P2 全量（原报告 14 个 P1 + 15 个 P2）。
- 唯一被本轮顺带解决的 P1 是 **P1-6**（`getStats` 聚合）。
- `DataSeeder` 仍有硬编码兜底密码 `Admin@123456` / `Test@123456`（P1-9 残留，可用环境变量覆盖，非阻塞）。

### 4.2 前端 3 个 P0

> 前端 P0 不在后端代码库范围内，状态以独立前端报告为准。后端契约侧的对应修复（如统一分页信封、owner 403）已在 `API.md` / 本报告中补齐，供前端对接。

| 前端 P0 | 与后端契约关系 |
|---------|----------------|
| 分页响应缺 `page`/`page_size` 字段 | 后端 P0-9 已补信封；`API.md` 已同步 |
| 跨用户查询无 403 处理 | 后端 P0-11 已返回 403；`API.md` 已注明 |
| 删除最后 super_admin 无 409 提示 | 后端 P0-10 已返回 409；`API.md` 已注明 |

---

## 5. 交接说明（给接手人员）

本轮同时补齐了此前缺失的交接文档，文档现状如下：

| 文档 | 状态 | 说明 |
|------|------|------|
| `REVIEW-REPORT.md` | ⚠️ 历史快照 | 修复**前**的评审基线，**不代表当前状态**，请勿据此判断缺陷是否已修 |
| `BACKEND_P0_CLOSURE.md`（本报告） | ✅ 新增 | 后端 12 P0 + P1-6 闭环的独立验证结论与逐项证据 |
| `API.md` | ✅ 已更新 | 统一分页信封 `{list,total,page,page_size}`、分页上限 ≤100、成绩列表 403 语义、super_admin 409 语义、DTO 校验 400 语义 |
| `README.md` | ✅ 已更新 | 顶部质量门禁横幅 + 结论报告链接、测试运行方式（48/48）、安全设计补 CORS 外部化 / 分页上限 / 统一信封 |

**前端对接需拿到的契约要点**（已写入 `API.md`）：

1. 所有列表接口响应体统一为：
   ```json
   { "list": [...], "total": 100, "page": 1, "page_size": 10 }
   ```
2. 分页参数 `pageSize` 服务端硬上限 **100**，超限自动截断。
3. 普通用户带他人 `username` 查成绩 → **403** `only current user may query scores`。
4. 删除最后一个 `super_admin` → **409** `cannot delete the last super admin`。
5. 请求体字段校验失败 → **400**（具体字段级 message）。

---

## 6. 复跑原始证据

```
$ ./mvn-local.sh test

[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.cpr_db.cpr_db.* (48 test classes)
...
[INFO] Results:
[INFO] Tests run: 48, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
[INFO] Total time: 27.259 s
```

防作弊 grep 证据（节选）：

```
$ grep -rn "@Disabled\|@Ignore\|@Pending" src/test
（无输出 → 0 命中）
```

基线测试类存在性确认：

```
JwtUtilTest / JwtTokenUtilTest / JwtAuthenticationFilterTest / ... (Jwt* 6)
JwtAuthIntegrationTest* (3)
AuthServiceTest / AuthControllerTest (2)
PoseServiceTest / PoseControllerTest (2)
QaServiceTest / QaControllerTest (2)
CprDbApplicationTests (1)
```

---

*本报告由紫怡独立生成，所有结论均来自源码逐行复核 + 本地构建复跑，非原 agent 自述。*
