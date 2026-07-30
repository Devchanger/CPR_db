# CPR DB 后端 API 文档

**服务地址**: `http://123.57.30.132:8080`  
**统一响应格式**: `{code, message, data}`  
**最新更新**: 2026-07-30  
**契约状态**: 已对齐后端 12 个 P0 闭环（统一分页信封 / 分页上限 100 / 403 越权 / 409 守卫），详见 [BACKEND_P0_CLOSURE.md](./BACKEND_P0_CLOSURE.md)

---

## 鉴权说明

| 符号 | 含义 |
|------|------|
| 🔓 | 公开接口，无需 token |
| 🔐 | 仅需登录（任意已登录用户，含 `student`，均可访问） |
| 👑 | 需 `admin` 或 `super_admin` 权限（管理 / 写操作） |
| 🔒 | 仅 `super_admin` 可访问 |

> 注：`🔐` 与 `👑` 常被混淆——`🔐` **只要求"已登录"，不要求 admin**。训练内容（场景 / 技能 / 步骤 / 知识 / 视频 / 成绩）的读取接口均为 `🔐`，学员端登录后即可调用，无需 admin 权限。

Token 通过登录接口获取，有效期 **24 小时**（86400000ms）。

**种子账号**:

| 账号 | 密码 | 角色 |
|------|------|------|
| `admin` | `Admin@123456` | super_admin |
| `testuser` | `Test@123456` | student |

---

## 一、认证模块 — `/api/v1/auth`

### 1.1 注册 🔓

```
POST /api/v1/auth/register
Content-Type: application/json
```

**请求体**:
```json
{ "username": "string (必填)", "password": "string (必填)" }
```

**成功** (200):
```json
{
  "code": 200, "message": "success",
  "data": { "token": "eyJ...", "expiresAt": 1784836055118 }
}
```

**失败**: `400` username already exists / username is required

### 1.2 登录 🔓

```
POST /api/v1/auth/login
Content-Type: application/json
```

请求体同注册。`expiresAt` 为 Unix 毫秒时间戳。

---

## 二、训练场景 — `/api/v1/scenes`

### 2.1 场景列表 🔐

```
GET /api/v1/scenes
```

返回所有场景（**纯 JSON 数组，非分页信封**——`data` 直接是数组，无 `list/total/page` 字段）。`type`: `basic` | `advanced`。

> 需要分页请改用 `GET /api/v1/scenes/list`（返回统一信封 `{list, total, page, page_size}`）。

### 2.2 场景分页列表 🔐

```
GET /api/v1/scenes/list?keyword=&page=1&pageSize=10
```

**响应**: `{list: [...], total: N, page: 1, page_size: 10}`

### 2.3 场景详情 🔐

```
GET /api/v1/scenes/{id}
```

> 🔐 仅需登录即可访问，任意已登录用户含 `student` 均可读取，非 admin 专属。

### 2.4 创建场景 👑

```
POST /api/v1/scenes
Content-Type: application/json
```

**请求体**: `{ "name": "...", "description": "...", "type": "basic", "icon": "heart", "sortOrder": 1 }`

### 2.5 更新场景 👑

```
PUT /api/v1/scenes/{id}
```

### 2.6 删除场景 👑

```
DELETE /api/v1/scenes/{id}
```

### 2.7 修改场景状态 👑

```
PATCH /api/v1/scenes/{id}/status
```

**请求体**: `{ "status": "published" }`

---

## 三、知识库 — `/api/v1/knowledge`

### 3.1 知识列表 🔐

```
GET /api/v1/knowledge
GET /api/v1/knowledge?category=AED
```

**分类**: 基础(8)、AED(5)、儿童CPR(3)、急救(6)、常见问题(5)，共 27 条。

### 3.2 知识详情 🔐

```
GET /api/v1/knowledge/{id}
```

> 🔐 仅需登录即可访问，任意已登录用户含 `student` 均可读取，非 admin 专属。

### 3.3 创建知识 👑

```
POST /api/v1/knowledge
```

**请求体**: `{ "question": "...", "answer": "...", "category": "...", "tags": "..." }`

### 3.4 更新知识 👑

```
PUT /api/v1/knowledge/{id}
```

### 3.5 删除知识 👑

```
DELETE /api/v1/knowledge/{id}
```

---

## 四、智能问答 — `/api/v1/qa`

### 4.1 预设问题 🔓

```
GET /api/v1/qa/presets
```

**响应**: `{ "presets": ["心肺复苏的正确步骤是什么？", ...] }`

### 4.2 提问 🔐

```
POST /api/v1/qa
Content-Type: application/json
```

**请求体**:
```json
{
  "question": "string (必填)",
  "history": [{ "role": "user", "content": "..." }, { "role": "assistant", "content": "..." }]
}
```

**响应**: `{ "answer": "AI 回答内容..." }`

> ⚠️ QA API Key 未配置时返回 fallback `"AI 服务返回异常，请稍后重试。"`

---

## 五、视频 — `/api/v1/videos`

### 5.1 视频列表 🔐

```
GET /api/v1/videos?keyword=&skillId=&status=&page=1&pageSize=10
```

**响应**: `{list: [...], total: N, page: 1, page_size: 10}`

### 5.2 获取单个视频 🔐

```
GET /api/v1/videos/{videoId}
```

`videoId` 为字符串（如 `video1` 或 `v1234567890`）。

### 5.3 创建视频 👑

```
POST /api/v1/videos
```

**请求体**: `{ "title": "...", "url": "...", "skillId": 1, "durationSeconds": 120 }`

### 5.4 更新视频 👑

```
PUT /api/v1/videos/{id}
```

### 5.5 删除视频 👑

```
DELETE /api/v1/videos/{id}
```

### 5.6 修改视频状态 👑

```
PATCH /api/v1/videos/{id}/status
```

**请求体**: `{ "status": "published" }`

---

## 六、技能 — `/api/v1/skills`

### 6.1 技能列表 🔐

```
GET /api/v1/skills?keyword=&status=&page=1&pageSize=10
```

### 6.2 技能详情 🔐

```
GET /api/v1/skills/{id}
```

> 🔐 仅需登录即可访问，任意已登录用户含 `student` 均可读取，非 admin 专属。

### 6.3 创建技能 👑

```
POST /api/v1/skills
```

**请求体**: `{ "name": "...", "description": "...", "icon": "...", "sceneId": 1, "sortOrder": 1 }`

### 6.4 更新技能 👑

```
PUT /api/v1/skills/{id}
```

### 6.5 删除技能 👑

```
DELETE /api/v1/skills/{id}
```

### 6.6 修改技能状态 👑

```
PATCH /api/v1/skills/{id}/status
```

**请求体**: `{ "status": "published" }`

---

## 七、训练步骤 — `/api/v1/steps`

### 7.1 步骤列表 🔐

```
GET /api/v1/steps?skillId=&status=&page=1&pageSize=10
```

### 7.2 步骤详情 🔐

```
GET /api/v1/steps/{id}
```

> 🔐 仅需登录即可访问，任意已登录用户含 `student` 均可读取，非 admin 专属。

### 7.3 创建步骤 👑

```
POST /api/v1/steps
```

**请求体**: `{ "skillId": 1, "title": "...", "description": "...", "order": 1 }`

### 7.4 更新步骤 👑

```
PUT /api/v1/steps/{id}
```

### 7.5 删除步骤 👑

```
DELETE /api/v1/steps/{id}
```

### 7.6 修改步骤状态 👑

```
PATCH /api/v1/steps/{id}/status
```

**请求体**: `{ "status": "active" }`

### 7.7 步骤排序 👑

```
PUT /api/v1/steps/{id}/reorder
```

**请求体**: `{ "direction": "up" }` 或 `{ "direction": "down" }`

---

## 八、成绩 — `/api/v1/scores`

### 8.1 提交成绩 🔐

```
POST /api/v1/scores
Content-Type: application/json
```

**请求体**:
```json
{
  "scene": "string (必填)",
  "skill": "string (必填)",
  "totalScore": 85.5,
  "compressionDepthAvg": 5.2,
  "compressionRateAvg": 110.0,
  "errorCount": 2,
  "stepDetails": "{\"steps\":[...]}"
}
```

### 8.2 成绩列表 🔐

```
GET /api/v1/scores?username=&all=&page=1&pageSize=10
```

- 普通用户：不带 `username` 返回本人成绩；带参数只能查自己
- admin/super_admin：`all=true` 查全部，`username=xxx` 查指定用户

**响应信封**:

```json
{
  "code": 200,
  "message": "success",
  "data": { "list": [...], "total": 12, "page": 1, "page_size": 10 }
}
```

> ⚠️ **403 越权**：非 admin 用户若携带他人 `username`（如 `?username=admin`）查询，返回 `403` + message `only current user may query scores`。前端须对此 403 做专门提示，而非通用"无权限"。

### 8.3 成绩详情 🔐

```
GET /api/v1/scores/{id}
```

> ⚠️ **403 越权**：非 admin 且非成绩本人时查询他人成绩详情，返回 `403`。前端在查详情前应确保当前用户有权限（本人或 admin）。

### 8.4 删除成绩 👑

```
DELETE /api/v1/scores/{id}
```

### 8.5 最新成绩 🔐

```
GET /api/v1/scores/latest?username=
```

权限规则同 8.2。返回单条或 `null`。

### 8.6 成绩统计 🔐

```
GET /api/v1/scores/stats
```

**响应**: `{ totalAttempts, averageScore, highestScore, lowestScore, scenesTrained, skillsTrained, recentScores[] }`

---

## 九、学员 — `/api/v1/students`

### 9.1 学员列表 🔐

```
GET /api/v1/students?keyword=&status=&page=1&pageSize=10
```

**响应**: `{list: [...], total: N, page: 1, page_size: 10}`

### 9.2 学员详情 🔐

```
GET /api/v1/students/{id}
```

### 9.3 创建学员 👑

```
POST /api/v1/students
```

**请求体**: `{ "name": "...", "phone": "...", "email": "...", "groupName": "...", "certStatus": "certified", "trainedAt": "..." }`

`certStatus`: `certified` | `training` | `expired`

> ⚠️ **字段映射（前后端对齐）**：后端字段名为 `name`（**必填**，`@NotBlank`）。前端当前若传 `username` / `role` / `password` 会被忽略或导致校验失败——请改为 `name`，且**不要**传 `role`、`password`（后端 `StudentCreateRequest` 无此字段）。`trainedAt` 为日期时间，如 `2026-07-30T10:00:00`。

### 9.4 更新学员 👑

```
PUT /api/v1/students/{id}
```

### 9.5 删除学员 👑

```
DELETE /api/v1/students/{id}
```

### 9.6 修改学员状态 👑

```
PATCH /api/v1/students/{id}/status
```

---

## 十、用户管理 — `/api/v1/user`

### 10.1 用户信息 🔐

```
GET /api/v1/user/info
```

**响应**: `{ "id": 1, "username": "testuser", "role": "student", "realName": "...", "avatar": "...", "createdAt": "..." }`

### 10.2 修改密码 🔐

```
PUT /api/v1/user/password
```

**请求体**: `{ "oldPassword": "...", "newPassword": "..." }`

### 10.3 管理员列表 🔒

```
GET /api/v1/user/admins?keyword=&page=1&pageSize=10
```

### 10.4 创建管理员 🔒

```
POST /api/v1/user/admins
```

**请求体**: `{ "username": "...", "password": "...", "role": "admin" }`

> 安全措施：不能删除自己、不能删除最后一个 super_admin

### 10.5 修改用户角色 🔒

```
PUT /api/v1/user/{id}/role
```

**请求体**: `{ "role": "admin" }`

### 10.6 删除用户 🔒

```
DELETE /api/v1/user/{id}
```

---

## 十一、个人信息 — `/api/v1/profile`

### 11.1 获取个人信息 🔐

```
GET /api/v1/profile
```

**响应**: `{ id, username, realName, role, avatar, gender, phone, studentId, className, createdAt }`

### 11.2 更新个人信息 🔐

```
PUT /api/v1/profile
```

**请求体**（只传需更新字段）: `{ "realName": "...", "gender": 1, "phone": "...", "studentId": "...", "className": "..." }`

手机号格式：`1[3-9]xxxxxxxxx`（11位）

### 11.3 上传头像 🔐

```
POST /api/v1/profile/avatar
Content-Type: multipart/form-data
```

**表单参数**: `file` — jpg/png/webp，≤ 2MB

**响应**: `{ "avatar_url": "/uploads/avatars/1_xxx.jpg" }`

---

## 十二、文件上传 — `/api/v1/upload`

### 12.1 上传图片 🔐

```
POST /api/v1/upload/image
Content-Type: multipart/form-data
```

**表单参数**: `file` — jpg/jpeg/png/webp，≤ 2MB

**响应**: `{ "url": "/uploads/images/yyyyMMdd/timestamp_filename.jpg" }`

### 12.2 上传视频 👑

```
POST /api/v1/upload/video
Content-Type: multipart/form-data
```

**表单参数**: `file` — mp4/webm/mov，≤ 500MB

**响应**: `{ "url": "...", "durationSeconds": 0 }`

---

## 十三、操作日志 — `/api/v1/logs`

### 13.1 日志查询 🔒

```
GET /api/v1/logs?adminId=&action=&targetType=&startDate=&endDate=&page=1&pageSize=10
```

**查询参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| adminId | Long | 管理员 ID |
| action | String | 操作类型 |
| targetType | String | 目标类型 |
| startDate | String | 起始日期 |
| endDate | String | 结束日期 |
| page | int | 页码，默认 1 |
| pageSize | int | 每页条数，默认 10 |

**响应**: `{list: [...], total: N, page: 1, page_size: 10}`

---

## 十四、姿态识别 — `/api/v1/pose`

### 14.1 姿态检测 🔐

```
POST /api/v1/pose/detect
Content-Type: multipart/form-data
```

**表单参数**: `image` — 图片文件

**响应**: `{ "angles": [...], "landmarks": [...] }`

---

## 附录

### 通用错误码

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 / token 无效或过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 405 | HTTP 方法不允许 |
| 409 | 数据冲突 |
| 415 | Content-Type 不支持 |
| 500 | 服务器内部错误 |

### 业务错误码补充（P0 闭环新增语义）

以下为本期闭环后**新增/强化的具体业务语义**，前端对接时须做差异化提示：

| HTTP | code | message 示例 | 触发场景 |
|:----:|------|--------------|----------|
| 400 | 400 | `<字段> 不能为空` / 格式错误 | 请求体 DTO `@Valid` 字段校验失败（如成绩提交缺 `scene`/`skill`） |
| 403 | 403 | `only current user may query scores` | 非 admin 用户越权查询他人成绩（P0-11） |
| 409 | 409 | `cannot delete the last super admin` | 试图删除最后一个 `super_admin`（P0-10） |

> 以上 403 / 409 在 REVIEW-REPORT.md 修复前未强制返回，前端旧逻辑可能未处理，请按新契约补齐。

### 统一响应结构

```typescript
interface ApiResponse<T> {
  code: number;
  message: string;
  data: T | null;
}
```

### 分页响应结构

> `pageSize` 服务端硬上限为 **100**，超过自动截断为 100。

```typescript
interface PageResponse<T> {
  list: T[];
  total: number;     // 符合条件的总记录数
  page: number;      // 当前页码，默认 1
  page_size: number; // 每页条数，默认 10，上限 100
}
```

### 列表接口返回格式说明（重要）

**并非所有 GET 列表都返回分页信封**，前端解析时需区分，切勿统一用 `data.list`：

| 返回格式 | 接口 | `data` 形态 |
|----------|------|-------------|
| **分页信封** `{list, total, page, page_size}` | `/scenes/list`、`/knowledge`、`/videos`、`/skills`、`/steps`、`/students`、`/logs`、`/scores` | 对象，含 `list` 数组 |
| **纯数组（无信封）** | `/scenes`（全量）、`/qa/presets` | 数组本身（或 `{presets:[...]}`） |
| **单资源 / 聚合对象** | 各 `/{id}` 详情、`/scores/latest`、`/scores/stats`、`/videos/{videoId}` | 单个对象或 `null` |

> 建议前端兼容解析：`const rows = Array.isArray(data) ? data : (data?.list ?? [])`。

### 角色权限说明

| 角色 | 权限 |
|------|------|
| `student` | 登录后查询训练数据（场景 / 技能 / 步骤 / 知识 / 视频 / 成绩）+ 管理自己的成绩 / 个人信息 |
| `admin` | student 权限 + 管理场景/知识/视频/技能/步骤/学员/上传视频/删除成绩 |
| `super_admin` | admin 权限 + 管理员管理 + 操作日志查看 |

### 完整接口速查表

| 模块 | 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|:--:|------|
| 认证 | POST | /api/v1/auth/register | 🔓 | 注册 |
| 认证 | POST | /api/v1/auth/login | 🔓 | 登录 |
| 场景 | GET | /api/v1/scenes | 🔐 | 场景列表 |
| 场景 | GET | /api/v1/scenes/list | 🔐 | 场景分页 |
| 场景 | GET | /api/v1/scenes/{id} | 🔐 | 场景详情 |
| 场景 | POST | /api/v1/scenes | 👑 | 创建场景 |
| 场景 | PUT | /api/v1/scenes/{id} | 👑 | 更新场景 |
| 场景 | DELETE | /api/v1/scenes/{id} | 👑 | 删除场景 |
| 场景 | PATCH | /api/v1/scenes/{id}/status | 👑 | 修改状态 |
| 知识库 | GET | /api/v1/knowledge | 🔐 | 知识列表 |
| 知识库 | GET | /api/v1/knowledge/{id} | 🔐 | 知识详情 |
| 知识库 | POST | /api/v1/knowledge | 👑 | 创建知识 |
| 知识库 | PUT | /api/v1/knowledge/{id} | 👑 | 更新知识 |
| 知识库 | DELETE | /api/v1/knowledge/{id} | 👑 | 删除知识 |
| 问答 | GET | /api/v1/qa/presets | 🔓 | 预设问题 |
| 问答 | POST | /api/v1/qa | 🔐 | 智能提问 |
| 视频 | GET | /api/v1/videos | 🔐 | 视频列表 |
| 视频 | GET | /api/v1/videos/{videoId} | 🔐 | 获取视频 |
| 视频 | POST | /api/v1/videos | 👑 | 创建视频 |
| 视频 | PUT | /api/v1/videos/{id} | 👑 | 更新视频 |
| 视频 | DELETE | /api/v1/videos/{id} | 👑 | 删除视频 |
| 视频 | PATCH | /api/v1/videos/{id}/status | 👑 | 修改状态 |
| 技能 | GET | /api/v1/skills | 🔐 | 技能列表 |
| 技能 | GET | /api/v1/skills/{id} | 🔐 | 技能详情 |
| 技能 | POST | /api/v1/skills | 👑 | 创建技能 |
| 技能 | PUT | /api/v1/skills/{id} | 👑 | 更新技能 |
| 技能 | DELETE | /api/v1/skills/{id} | 👑 | 删除技能 |
| 技能 | PATCH | /api/v1/skills/{id}/status | 👑 | 修改状态 |
| 步骤 | GET | /api/v1/steps | 🔐 | 步骤列表 |
| 步骤 | GET | /api/v1/steps/{id} | 🔐 | 步骤详情 |
| 步骤 | POST | /api/v1/steps | 👑 | 创建步骤 |
| 步骤 | PUT | /api/v1/steps/{id} | 👑 | 更新步骤 |
| 步骤 | DELETE | /api/v1/steps/{id} | 👑 | 删除步骤 |
| 步骤 | PATCH | /api/v1/steps/{id}/status | 👑 | 修改状态 |
| 步骤 | PUT | /api/v1/steps/{id}/reorder | 👑 | 步骤排序 |
| 成绩 | POST | /api/v1/scores | 🔐 | 提交成绩 |
| 成绩 | GET | /api/v1/scores | 🔐 | 成绩列表 |
| 成绩 | GET | /api/v1/scores/{id} | 🔐 | 成绩详情 |
| 成绩 | DELETE | /api/v1/scores/{id} | 👑 | 删除成绩 |
| 成绩 | GET | /api/v1/scores/latest | 🔐 | 最新成绩 |
| 成绩 | GET | /api/v1/scores/stats | 🔐 | 成绩统计 |
| 学员 | GET | /api/v1/students | 🔐 | 学员列表 |
| 学员 | GET | /api/v1/students/{id} | 🔐 | 学员详情 |
| 学员 | POST | /api/v1/students | 👑 | 创建学员 |
| 学员 | PUT | /api/v1/students/{id} | 👑 | 更新学员 |
| 学员 | DELETE | /api/v1/students/{id} | 👑 | 删除学员 |
| 学员 | PATCH | /api/v1/students/{id}/status | 👑 | 修改状态 |
| 用户 | GET | /api/v1/user/info | 🔐 | 用户信息 |
| 用户 | PUT | /api/v1/user/password | 🔐 | 修改密码 |
| 用户 | GET | /api/v1/user/admins | 🔒 | 管理员列表 |
| 用户 | POST | /api/v1/user/admins | 🔒 | 创建管理员 |
| 用户 | PUT | /api/v1/user/{id}/role | 🔒 | 修改角色 |
| 用户 | DELETE | /api/v1/user/{id} | 🔒 | 删除用户 |
| 个人信息 | GET | /api/v1/profile | 🔐 | 获取信息 |
| 个人信息 | PUT | /api/v1/profile | 🔐 | 更新信息 |
| 个人信息 | POST | /api/v1/profile/avatar | 🔐 | 上传头像 |
| 上传 | POST | /api/v1/upload/image | 🔐 | 上传图片 |
| 上传 | POST | /api/v1/upload/video | 👑 | 上传视频 |
| 日志 | GET | /api/v1/logs | 🔒 | 日志查询 |
| 姿态 | POST | /api/v1/pose/detect | 🔐 | 姿态检测 |
| 静态 | GET | /uploads/** | 🔓 | 静态文件 |
