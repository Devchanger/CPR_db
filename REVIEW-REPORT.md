# CPR_DB 后端 feat-api-gaps 分支审查报告

> **审查日期**: 2026-07-30
> **审查范围**: 51 文件, +3734/-394 行, 20 次提交
> **审查团**: 架构师(高见远) + 后端工程师(贝洛奇) + QA(严过关)
> **总裁决**: **FAIL — 不通过，需修复后重新审查**

---

## 一、三方审查汇总

| 专家 | verdict | P0 | P1 | P2 |
|------|---------|----|----|-----|
| 架构师 | fail | 3 | 7 | 4 |
| 后端工程师 | fail | 7 | 5 | 9 |
| QA (v2) | fail | 6 | 8 | 4 |
| **去重后合计** | **FAIL** | **12** | **14** | **15** |

---

## 二、三方一致发现（高置信度问题）

以下问题被 2-3 位专家独立报告，置信度最高：

| # | 问题 | 架构师 | 后端 | QA | 合并级别 |
|---|------|:------:|:----:|:--:|:--------:|
| A | CORS 全通配 + 允许凭证 | P1-6 | P0 | P0 | **P0** |
| B | LogService.log() 从未被调用 | P1-5 | P1 | P0 | **P0** |
| C | DataSeeder 种子数据重复 | P0 | P1 | P2 | **P0** |
| D | ScoreController 响应结构不一致 | P0 | P2 | P1 | **P0** |
| E | N+1 查询（列表接口逐条查关联名） | P1 | P1 | — | **P1** |
| F | 默认凭证硬编码 | P1 | P2 | P1 | **P1** |
| G | Map\<String,Object\> 代替 DTO 无校验 | P1 | P0 | — | **P0** |
| H | 全项目零 @Transactional | — | P0 | P1 | **P0** |
| I | Controller 直连 Repository（跨层） | P1 | P0 | — | **P0** |
| J | 零测试覆盖（Controller/Repository 0%） | — | P2 | P0 | **P0** |
| K | ScoreController.getScoreById 无权限校验（越权） | — | — | P0 | **P0** |
| L | 文件上传路径遍历 | — | P0 | P0 | **P0** |
| M | 分页无上限（pageSize=999999） | — | P1 | P0 | **P0** |
| N | 上传路径硬编码 /opt/cpr-db/uploads/ | — | P2 | P0 | **P0** |

---

## 三、P0 缺陷清单（必须修复才能部署）

### 后端可修复的 P0（12 个）

| # | 缺陷 | 证据位置 | 期望 | 责任 |
|---|------|----------|------|------|
| P0-1 | CORS `allowedOriginPatterns("*")` + `allowCredentials(true)` | SecurityConfig.java L36,39 | 限制为已知前端域名，外部化到配置 | 后端 |
| P0-2 | Controller 直连 Repository（ScoreController 注入 UserRepository） | ScoreController.java L25-26 | 移到 Service 层，Controller 禁止直连 DB | 后端 |
| P0-3 | UserController.changePassword 业务逻辑在 Controller | UserController.java L47-59 | 密码比对/编码下沉到 UserService | 后端 |
| P0-4 | UploadController 全部 IO 在 Controller，无 Service 层 | UploadController.java L33-89 | 抽取 UploadService 处理文件保存 | 后端 |
| P0-5 | 文件上传路径遍历风险（getOriginalFilename 未消毒） | UploadController.java L43,68 | 对文件名做消毒，去除 `../` 等路径符，或用 UUID | 后端 |
| P0-6 | 全项目零 @Transactional 注解 | 全局 grep 0 结果 | 所有 Service 写操作加 @Transactional，尤其 StepService.reorderStep | 后端 |
| P0-7 | 多 Controller 零输入校验（Map 代替 DTO） | SkillController/StepController/VideoController/SceneController/StudentController | 改用强类型 DTO + @Valid + @NotBlank/@NotNull | 后端 |
| P0-8 | DataSeeder Knowledge 种子数据重复（4 条重复） | DataSeeder.java L74&120, L85&123, L105&126, L111&129 | 去重，补 Skill/Step/Student 种子数据 | 后端 |
| P0-9 | ScoreController 响应结构不一致（学生返回 List，管理员返回分页 Map） | ScoreController.java L43-60 | 统一为 `{list, total, page, pageSize}` | 后端 |
| P0-10 | AdminService 无"最后一个 super_admin"保护 | AdminService.deleteUser() | 增加校验 `countByRole("super_admin") <= 1` 时拒绝删除 | 后端 |
| P0-11 | ScoreController.getScoreById 无权限校验（越权遍历他人成绩） | ScoreController.java L62-64 | 非管理员只能查自己的成绩，加 owner 校验 | 后端 |
| P0-12 | 分页无上限（pageSize=999999 可导致 OOM） | 所有 Service 的 pageSize 检查 | 加 `if (pageSize > 100) pageSize = 100` 上限 | 后端 |

### 前端需配合的 P0（3 个 — 集成断裂）

| # | 缺陷 | 现状 | 期望 | 责任 |
|---|------|------|------|------|
| P0-F1 | changePassword 用 POST，后端是 PUT → 405 | 前端 auth.js:25 `request.post()` | 改为 `request.put()` | 前端 |
| P0-F2 | admin.js 调 /v1/admins，后端是 /v1/user/admins → 404 | 前端 api/admin.js | 改为 `/api/v1/user/admins` | 前端 |
| P0-F3 | updateUserInfo 调 PUT /v1/user/info，后端无此端点 → 404 | 前端 auth.js | 改为 `PUT /api/v1/profile` | 前端 |

---

## 四、P1 问题清单（建议修复）

| # | 问题 | 证据 | 专家 |
|---|------|------|------|
| P1-1 | Knowledge 实体字段三重不一致（Java字段=question, DB列=title, JSON=question） | Knowledge.java L16-22 | 架构师 |
| P1-2 | Scene/Knowledge 列表接口返回 List 无分页 | SceneController L23, KnowledgeController L22 | 架构师+QA |
| P1-3 | StepService.reorderStep 无事务（P0-6 覆盖，但此操作风险最高） | StepService.java L112-151 | 架构师+后端 |
| P1-4 | N+1 查询：Step/Skill/Video 列表逐条查关联名 | StepService L153, SkillService L126, VideoService L143 | 架构师+后端 |
| P1-5 | LogService.log() 从未被调用，操作日志形同虚设 | LogService.java L57-68 | 三方一致 |
| P1-6 | ScoreService.getStats 全量加载内存计算 | ScoreService.java L55-96 | 后端+QA |
| P1-7 | 分页无上限（pageSize 可传 999999） | 所有 Service pageSize 检查 | 后端+QA（已升为 P0-12） |
| P1-8 | ScoreDto 字段名不一致（compressionDepthAvg 驼峰 vs compression_depth_avg 蛇形） | ScoreDto.java | QA |
| P1-9 | 默认凭证硬编码（Admin@123456 / Test@123456） | DataSeeder.java L137,146 | 三方一致 |
| P1-10 | JWT 无 refresh token，access token 24h 过长 | application.example.properties | 后端 |
| P1-11 | ScoreSubmitRequest 无 @Min/@Max 校验分数范围 | ScoreSubmitRequest | QA |
| P1-12 | 无速率限制（登录/注册可被暴力枚举） | 全局无 RateLimiter | QA |
| P1-13 | H2 Console 在生产环境暴露 | application.example.properties L17 | QA |
| P1-14 | StepService.createStep 不校验 skillId 存在性 | StepService.java L53-72 | QA |

---

## 五、P2 问题清单（低优先级）

| # | 问题 | 证据 |
|---|------|------|
| P2-1 | VideoController 路径参数类型混用（GET 用 String，PUT/DELETE 用 Long） | VideoController L22, L44 |
| P2-2 | UploadController 视频时长硬编码为 0 | UploadController L79 |
| P2-3 | WebMvcConfig 静态资源路径硬编码 /opt/cpr-db/uploads/ | WebMvcConfig L13 |
| P2-4 | Score 实体 skill 字段长度仅 10 | Score.java L23 |
| P2-5 | JWT 默认密钥可预测（硬编码在 application.example.properties） | application.example.properties L26 |
| P2-6 | 重复工具方法 toLong/toInt（3 个 Service 各一份） | SkillService/StepService/VideoService |
| P2-7 | 响应格式不统一（Entity 直接返回 vs Map vs DTO） | 多个 Controller |
| P2-8 | 状态值无白名单校验（status 接受任意字符串） | 多个 Service |
| P2-9 | 文件上传仅校验扩展名不校验 MIME | UploadController |
| P2-10 | 无密码复杂度校验 | PasswordChangeRequest/AdminCreateRequest |
| P2-11 | 无 actuator/metrics/健康检查端点 | — |
| P2-12 | application-test.properties 硬编码 MySQL 连接信息 | application-test.properties |
| P2-13 | 无 CI/CD pipeline 配置 | — |
| P2-14 | 零集成测试 / 零 E2E 测试 | src/test/ 目录 |

---

## 六、已知问题验证结果（14 个）

| 状态 | 数量 | 编号 |
|------|------|------|
| 已修复 | 2 | P1#11 (videos 改 authenticated), P2#14 (冲突标记清理) |
| 仍存在 | 12 | P0#1-3, P1#4-10, P2#12-13 |

---

## 七、测试覆盖评估

| 指标 | 当前 | 目标 |
|------|------|------|
| 测试文件 | 6 | — |
| 测试用例 | 16 | — |
| Controller 测试 | 0/14 | 14 |
| Service 测试 | 3/12 | 12 |
| Repository 测试 | 0/9 | 9 |
| 集成测试 | 0 | ≥3 |
| E2E 测试 | 0 | ≥1 |
| 覆盖率估算 | <10% | >60% |

现有测试中：
- **JwtTokenUtilTest** (6 用例) — 良好，覆盖正常+异常+边界
- **JwtAuthenticationFilterTest** (3 用例) — 良好
- **AuthServiceTest** (2 用例) — 不足，缺异常路径
- **PoseServiceTest** (2 用例) — 一般
- **QaServiceTest** (2 用例) — 一般
- **CprDbApplicationTests** (1 用例) — 仅上下文加载，零断言

---

## 八、生产就绪评分

| 维度 | 档位 | 关键缺陷 |
|------|------|----------|
| 测试 + 回归 | **Bronze** | Controller/Repository 零测试，无集成/E2E |
| 契约 | **Bronze** | 12/14 已知问题仍存在，响应结构不统一 |
| 安全 | **Bronze** | CORS 全开，无速率限制，路径遍历，硬编码密码 |
| 性能 | **Bronze** | 无分页上限，N+1 查询，全量加载内存计算 |
| 可观测 | **Bronze** | LogService 形同虚设，无 metrics/actuator |
| 发布安全 | **Bronze** | 硬编码连接信息，无 CI/CD |
| **总档** | **Bronze** | **未达 Silver，不建议交付商业生产** |

---

## 九、安全审查结论

| 维度 | 状态 | 说明 |
|------|------|------|
| 密码加密 | ✅ PASS | BCryptPasswordEncoder |
| SQL 注入 | ✅ PASS | JPA 参数化查询 |
| 方法级授权 | ✅ PASS | @PreAuthorize 分级合理 |
| XSS | ✅ PASS | JSON API，无模板注入 |
| CSRF | ✅ PASS | JWT 无状态，CSRF 已禁用（合理） |
| CORS | ❌ **FAIL** | 全通配 + 凭证 |
| 输入校验 | ❌ **FAIL** | 多数端点 Map 无校验 |
| 文件上传 | ❌ **FAIL** | 路径遍历 + 仅校验扩展名 |
| JWT | ⚠️ WARN | 24h 过长，默认密钥可预测，无 refresh token |
| 速率限制 | ❌ **FAIL** | 无 |
| 默认凭证 | ⚠️ WARN | 硬编码在源码 |

---

## 十、修复优先级（建议执行顺序）

### 第一优先级 — P0（不修不能部署）

| 序号 | 缺陷 | 预估工作量 |
|------|------|-----------|
| 1 | 全项目加 @Transactional | 1h |
| 2 | Map → DTO + @Valid（5 个 Controller） | 3h |
| 3 | 抽取 UploadService + 文件名消毒 | 1.5h |
| 4 | Controller 去直连 Repository（ScoreController/UserController） | 1h |
| 5 | CORS 收紧 + 外部化 | 0.5h |
| 6 | DataSeeder 去重 + 补种子数据 | 1h |
| 7 | ScoreController 响应结构统一 | 1h |
| 8 | AdminService 加最后 super_admin 保护 | 0.5h |
| 9 | Knowledge 实体字段命名统一 | 0.5h |
| 10 | ScoreController.getScoreById 加 owner 权限校验 | 0.5h |
| 11 | 所有分页接口加 pageSize 上限 | 0.5h |
| 12 | 前端 3 个集成 P0（changePassword/admins/userInfo） | 1h |

### 第二优先级 — P1（建议本轮一起修）

| 序号 | 缺陷 |
|------|------|
| 11 | Step/Skill/Video 列表 N+1 改 JOIN |
| 12 | ScoreService.getStats 改数据库聚合 |
| 13 | 分页加 pageSize 上限（max=100） |
| 14 | 接入 LogService（AOP 或 Service 层注入） |
| 15 | 默认凭证改环境变量 |
| 16 | ScoreDto 字段名对齐 snake_case |
| 17 | ScoreSubmitRequest 加 @Min/@Max |
| 18 | Scene/Knowledge 列表加分页 |

### 第三优先级 — P2（下个迭代）

逐步补齐测试覆盖、CI/CD、actuator、密码复杂度校验等。

---

## 十一、改动总结

### 本次 feat-api-gaps 分支做了什么

1. **新增 3 实体 + 3 表**：Skill（技能）、Step（训练步骤）、Log（操作日志）
2. **新增 5 Controller + 5 Service**：Skill/Step/Log/Upload/Student(扩充)
3. **Knowledge 字段重构**：title→question, content→answer + 25 条种子数据
4. **API 从 ~20 端点扩展到 60+ 端点**，覆盖 14 个业务模块
5. **安全体系完善**：三层权限（public/authenticated/admin+super_admin）+ @EnableMethodSecurity
6. **全局配置**：JacksonConfig SNAKE_CASE + DataSeeder 种子账号
7. **文件上传**：图片/视频上传 + 头像上传 + 静态资源放行
8. **文档更新**：API.md 全量 60+ 端点 + README + api-test-checklist.md

### 改动质量评价

- **功能覆盖**：从 ~20 端点补到 60+，前端需要的接口基本都有了
- **代码质量**：分层基本存在但跨层违规严重（Controller 直连 DB、无 Service 层）
- **安全性**：基础认证授权到位，但 CORS/输入校验/文件上传有安全漏洞
- **测试覆盖**：严重不足，Controller/Repository 零测试，无集成/E2E
- **生产就绪**：Bronze 档，不建议直接交付商业生产

---

## 附录：三方审查原始结论

- **架构师**：verdict fail, 3 P0 + 7 P1 + 4 P2
- **后端工程师**：verdict fail, 7 P0 + 5 P1 + 9 P2
- **QA (v2)**：verdict fail, 6 P0 + 8 P1 + 4 P2, 生产就绪 Bronze

> P0 emoji 规则扫描：✅ PASS — 全局无 emoji 作功能图标
> P0 紫粉渐变：N/A — 后端项目无 UI
> P0 AI 模板味：✅ PASS — 无 Lorem ipsum / Welcome to / 空洞占位
