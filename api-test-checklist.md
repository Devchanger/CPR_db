# CPR_DB API 接口验证清单（curl 命令）

> **用途**：部署到 ECS 后，逐条执行以下 curl 命令验证接口可用性
> **生成日期**：2026-07-30
> **审查人**：QA 工程师 严过关
> **后端基线**：feat-api-gaps 分支

---

## 0. 环境准备

```bash
# === 变量设置 ===
export BASE_URL="http://localhost:8080/api"   # 本地测试
# export BASE_URL="http://<ECS_IP>:8080/api"  # ECS 测试

# === 登录获取 Token ===
# 管理员登录（super_admin）
ADMIN_RESP=$(curl -s -X POST "$BASE_URL/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123456"}')
echo "$ADMIN_RESP" | jq .
export ADMIN_TOKEN=$(echo "$ADMIN_RESP" | jq -r '.data.token')
echo "Admin Token: $ADMIN_TOKEN"

# 学生登录
STUDENT_RESP=$(curl -s -X POST "$BASE_URL/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test@123456"}')
echo "$STUDENT_RESP" | jq .
export STUDENT_TOKEN=$(echo "$STUDENT_RESP" | jq -r '.data.token')
echo "Student Token: $STUDENT_TOKEN"
```

---

## 1. 认证接口 — `/api/v1/auth`

### 1.1 注册 — POST /v1/auth/register
```bash
# ✅ 成功场景
curl -s -X POST "$BASE_URL/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"newstudent001","password":"Test@123456"}' | jq .

# ❌ 失败：用户名已存在
curl -s -X POST "$BASE_URL/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Test@123456"}' | jq .

# ❌ 失败：缺 username
curl -s -X POST "$BASE_URL/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"password":"Test@123456"}' | jq .

# ❌ 失败：缺 password
curl -s -X POST "$BASE_URL/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"newstudent002"}' | jq .
```

### 1.2 登录 — POST /v1/auth/login
```bash
# ✅ 成功场景
curl -s -X POST "$BASE_URL/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123456"}' | jq .

# ❌ 失败：密码错误
curl -s -X POST "$BASE_URL/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrongpassword"}' | jq .

# ❌ 失败：用户不存在
curl -s -X POST "$BASE_URL/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"nouser","password":"anypassword"}' | jq .

# ❌ 失败：请求体缺失
curl -s -X POST "$BASE_URL/v1/auth/login" | jq .
```

---

## 2. 用户信息接口 — `/api/v1/user`

### 2.1 获取用户信息 — GET /v1/user/info
```bash
# ✅ 成功场景（管理员）
curl -s -X GET "$BASE_URL/v1/user/info" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ✅ 成功场景（学生）
curl -s -X GET "$BASE_URL/v1/user/info" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq .

# ❌ 失败：未携带 Token
curl -s -X GET "$BASE_URL/v1/user/info" | jq .

# ❌ 失败：无效 Token
curl -s -X GET "$BASE_URL/v1/user/info" \
  -H "Authorization: Bearer invalid_token_here" | jq .

# ⚠️ 验证点：响应 data 中必须包含 role, real_name, avatar 字段
# 期望：{"code":200,"message":"success","data":{"id":1,"username":"admin","role":"super_admin","real_name":"超级管理员","avatar":null,"created_at":"..."}}
```

### 2.2 修改密码 — PUT /v1/user/password
```bash
# ✅ 成功场景
curl -s -X PUT "$BASE_URL/v1/user/password" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"oldPassword":"Test@123456","newPassword":"NewTest@123456"}' | jq .

# ⚠️ 如果改了密码，用新密码重新登录获取 Token
curl -s -X PUT "$BASE_URL/v1/user/password" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"oldPassword":"NewTest@123456","newPassword":"Test@123456"}' | jq .  # 改回去

# ❌ 失败：旧密码不正确
curl -s -X PUT "$BASE_URL/v1/user/password" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"oldPassword":"wrongold","newPassword":"newpass"}' | jq .

# ❌ 失败：缺 oldPassword
curl -s -X PUT "$BASE_URL/v1/user/password" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"newPassword":"newpass"}' | jq .

# ❌ 失败：未携带 Token
curl -s -X PUT "$BASE_URL/v1/user/password" \
  -H "Content-Type: application/json" \
  -d '{"oldPassword":"old","newPassword":"new"}' | jq .

# ❌ 失败：前端用 POST 调用（HTTP 方法不匹配）
# 注意：前端 auth.js:25 使用 request.post() 发送 POST，但后端是 PUT
# 此命令模拟前端行为，预期 405
curl -s -X POST "$BASE_URL/v1/user/password" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"oldPassword":"old","newPassword":"new"}' | jq .
```

### 2.3 管理员列表 — GET /v1/user/admins
```bash
# ✅ 成功场景（super_admin）
curl -s -X GET "$BASE_URL/v1/user/admins" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ✅ 带关键词和分页
curl -s -X GET "$BASE_URL/v1/user/admins?keyword=admin&page=1&pageSize=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ❌ 失败：学生无权限（403）
curl -s -X GET "$BASE_URL/v1/user/admins" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq .

# ❌ 失败：未携带 Token（401）
curl -s -X GET "$BASE_URL/v1/user/admins" | jq .
```

### 2.4 创建管理员 — POST /v1/user/admins
```bash
# ✅ 成功场景
curl -s -X POST "$BASE_URL/v1/user/admins" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"newadmin001","password":"Admin@123456","role":"admin"}' | jq .

# ❌ 失败：用户名已存在（409）
curl -s -X POST "$BASE_URL/v1/user/admins" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123456","role":"admin"}' | jq .

# ❌ 失败：缺 username
curl -s -X POST "$BASE_URL/v1/user/admins" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"password":"Admin@123456","role":"admin"}' | jq .

# ❌ 失败：非法 role
curl -s -X POST "$BASE_URL/v1/user/admins" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"badadmin","password":"Admin@123456","role":"student"}' | jq .

# ❌ 失败：学生无权限（403）
curl -s -X POST "$BASE_URL/v1/user/admins" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"hackadmin","password":"Admin@123456","role":"super_admin"}' | jq .
```

### 2.5 修改用户角色 — PUT /v1/user/{id}/role
```bash
# ✅ 成功场景
curl -s -X PUT "$BASE_URL/v1/user/2/role" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role":"admin"}' | jq .

# ❌ 失败：用户不存在（404）
curl -s -X PUT "$BASE_URL/v1/user/99999/role" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role":"admin"}' | jq .

# ❌ 失败：学生无权限（403）
curl -s -X PUT "$BASE_URL/v1/user/2/role" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role":"admin"}' | jq .
```

### 2.6 删除用户 — DELETE /v1/user/{id}
```bash
# ✅ 成功场景（先创建再删）
curl -s -X DELETE "$BASE_URL/v1/user/3" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ❌ 失败：用户不存在（404）
curl -s -X DELETE "$BASE_URL/v1/user/99999" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ❌ 失败：删除自己（400）
# 需要知道 admin 的 id，假设为 1
curl -s -X DELETE "$BASE_URL/v1/user/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ❌ 失败：学生无权限（403）
curl -s -X DELETE "$BASE_URL/v1/user/1" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq .
```

---

## 3. 个人资料接口 — `/api/v1/profile`

### 3.1 获取个人资料 — GET /v1/profile
```bash
# ✅ 成功场景
curl -s -X GET "$BASE_URL/v1/profile" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ❌ 失败：未携带 Token
curl -s -X GET "$BASE_URL/v1/profile" | jq .
```

### 3.2 更新个人资料 — PUT /v1/profile
```bash
# ✅ 成功场景
curl -s -X PUT "$BASE_URL/v1/profile" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"realName":"张三","gender":"male","phone":"13800138000","className":"急救1班"}' | jq .

# ✅ 部分更新（只改一个字段）
curl -s -X PUT "$BASE_URL/v1/profile" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"realName":"李四"}' | jq .

# ❌ 失败：未携带 Token
curl -s -X PUT "$BASE_URL/v1/profile" \
  -H "Content-Type: application/json" \
  -d '{"realName":"李四"}' | jq .
```

### 3.3 上传头像 — POST /v1/profile/avatar
```bash
# ✅ 成功场景
curl -s -X POST "$BASE_URL/v1/profile/avatar" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -F "file=@/tmp/test_avatar.jpg" | jq .

# ❌ 失败：不支持格式
curl -s -X POST "$BASE_URL/v1/profile/avatar" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -F "file=@/tmp/test_avatar.gif" | jq .

# ❌ 失败：未携带 Token
curl -s -X POST "$BASE_URL/v1/profile/avatar" \
  -F "file=@/tmp/test_avatar.jpg" | jq .
```

---

## 4. 场景管理接口 — `/api/v1/scenes`

### 4.1 场景列表 — GET /v1/scenes
```bash
# ✅ 获取全部场景（无分页）
curl -s -X GET "$BASE_URL/v1/scenes" | jq .

# ✅ 分页列表（注意：需用 /scenes/list 端点）
curl -s -X GET "$BASE_URL/v1/scenes/list?page=1&pageSize=10&keyword=CPR" | jq .

# ✅ 按关键词搜索
curl -s -X GET "$BASE_URL/v1/scenes/list?keyword=成人" | jq .

# ⚠️ 验证点：GET /v1/scenes 返回 List<Scene>（无分页）
#            GET /v1/scenes/list 返回 {list, total}
# 前端 training.js 调用的是 GET /v1/scenes（带 params），可能不匹配
```

### 4.2 场景详情 — GET /v1/scenes/{id}
```bash
# ✅ 成功场景
curl -s -X GET "$BASE_URL/v1/scenes/1" | jq .

# ❌ 失败：不存在（404）
curl -s -X GET "$BASE_URL/v1/scenes/99999" | jq .
```

### 4.3 创建场景 — POST /v1/scenes
```bash
# ✅ 成功场景
curl -s -X POST "$BASE_URL/v1/scenes" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"新场景","description":"测试场景","type":"basic","icon":"heart","sortOrder":99}' | jq .

# ❌ 失败：缺 name
curl -s -X POST "$BASE_URL/v1/scenes" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"description":"test"}' | jq .

# ❌ 失败：学生无权限（403）
curl -s -X POST "$BASE_URL/v1/scenes" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"hack"}' | jq .
```

### 4.4 更新场景 — PUT /v1/scenes/{id}
```bash
# ✅ 成功场景
curl -s -X PUT "$BASE_URL/v1/scenes/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"更新场景名"}' | jq .

# ❌ 失败：不存在（404）
curl -s -X PUT "$BASE_URL/v1/scenes/99999" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"test"}' | jq .
```

### 4.5 删除场景 — DELETE /v1/scenes/{id}
```bash
# ✅ 成功场景
curl -s -X DELETE "$BASE_URL/v1/scenes/6" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ❌ 失败：不存在（404）
curl -s -X DELETE "$BASE_URL/v1/scenes/99999" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
```

### 4.6 修改场景状态 — PATCH /v1/scenes/{id}/status
```bash
# ✅ 成功场景
curl -s -X PATCH "$BASE_URL/v1/scenes/1/status" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"active"}' | jq .
```

---

## 5. 技能管理接口 — `/api/v1/skills`

### 5.1 技能列表 — GET /v1/skills
```bash
# ✅ 成功场景
curl -s -X GET "$BASE_URL/v1/skills?page=1&pageSize=10" | jq .

# ✅ 带关键词
curl -s -X GET "$BASE_URL/v1/skills?keyword=按压" | jq .

# ✅ 按状态筛选
curl -s -X GET "$BASE_URL/v1/skills?status=active" | jq .

# ✅ 空值处理：keyword 为空
curl -s -X GET "$BASE_URL/v1/skills?keyword=" | jq .

# ✅ 边界值：page 越界
curl -s -X GET "$BASE_URL/v1/skills?page=99999&pageSize=10" | jq .

# ⚠️ 验证点：响应应为 {list: [...], total: N}
```

### 5.2 技能详情 — GET /v1/skills/{id}
```bash
# ✅ 成功场景
curl -s -X GET "$BASE_URL/v1/skills/1" | jq .

# ❌ 失败：不存在（404）
curl -s -X GET "$BASE_URL/v1/skills/99999" | jq .
```

### 5.3 创建技能 — POST /v1/skills
```bash
# ✅ 成功场景
curl -s -X POST "$BASE_URL/v1/skills" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"胸外按压","description":"心肺复苏核心技能","icon":"/uploads/skills/cpr.png","sceneId":1,"status":"active","sortOrder":1}' | jq .

# ❌ 失败：缺 name
curl -s -X POST "$BASE_URL/v1/skills" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"description":"test"}' | jq .

# ❌ 失败：名称重复（409）
curl -s -X POST "$BASE_URL/v1/skills" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"胸外按压"}' | jq .

# ❌ 失败：学生无权限（403）
curl -s -X POST "$BASE_URL/v1/skills" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"hack"}' | jq .
```

### 5.4 更新技能 — PUT /v1/skills/{id}
```bash
# ✅ 成功场景
curl -s -X PUT "$BASE_URL/v1/skills/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"description":"更新描述"}' | jq .

# ❌ 失败：不存在（404）
curl -s -X PUT "$BASE_URL/v1/skills/99999" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"test"}' | jq .
```

### 5.5 删除技能 — DELETE /v1/skills/{id}
```bash
# ✅ 成功场景
curl -s -X DELETE "$BASE_URL/v1/skills/2" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ❌ 失败：不存在（404）
curl -s -X DELETE "$BASE_URL/v1/skills/99999" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
```

### 5.6 修改技能状态 — PATCH /v1/skills/{id}/status
```bash
# ✅ 成功场景
curl -s -X PATCH "$BASE_URL/v1/skills/1/status" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"inactive"}' | jq .
```

---

## 6. 步骤管理接口 — `/api/v1/steps`

### 6.1 步骤列表 — GET /v1/steps
```bash
# ✅ 成功场景
curl -s -X GET "$BASE_URL/v1/steps?page=1&pageSize=10" | jq .

# ✅ 按技能筛选
curl -s -X GET "$BASE_URL/v1/steps?skillId=1" | jq .

# ✅ 按状态筛选
curl -s -X GET "$BASE_URL/v1/steps?status=active" | jq .

# ✅ 边界值：page 越界
curl -s -X GET "$BASE_URL/v1/steps?page=99999" | jq .
```

### 6.2 步骤详情 — GET /v1/steps/{id}
```bash
# ✅ 成功场景
curl -s -X GET "$BASE_URL/v1/steps/1" | jq .

# ❌ 失败：不存在（404）
curl -s -X GET "$BASE_URL/v1/steps/99999" | jq .
```

### 6.3 创建步骤 — POST /v1/steps
```bash
# ✅ 成功场景
curl -s -X POST "$BASE_URL/v1/steps" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"确认环境安全","description":"检查周围环境","skillId":1,"status":"active","stepOrder":1}' | jq .

# ❌ 失败：缺 title
curl -s -X POST "$BASE_URL/v1/steps" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"description":"test"}' | jq .

# ❌ 失败：学生无权限（403）
curl -s -X POST "$BASE_URL/v1/steps" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"hack"}' | jq .
```

### 6.4 更新步骤 — PUT /v1/steps/{id}
```bash
# ✅ 成功场景
curl -s -X PUT "$BASE_URL/v1/steps/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"更新标题"}' | jq .

# ❌ 失败：不存在（404）
curl -s -X PUT "$BASE_URL/v1/steps/99999" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"test"}' | jq .
```

### 6.5 删除步骤 — DELETE /v1/steps/{id}
```bash
# ✅ 成功场景
curl -s -X DELETE "$BASE_URL/v1/steps/2" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ❌ 失败：不存在（404）
curl -s -X DELETE "$BASE_URL/v1/steps/99999" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
```

### 6.6 修改步骤状态 — PATCH /v1/steps/{id}/status
```bash
# ✅ 成功场景
curl -s -X PATCH "$BASE_URL/v1/steps/1/status" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"inactive"}' | jq .
```

### 6.7 步骤排序 — PUT /v1/steps/{id}/reorder
```bash
# ✅ 成功场景（上移）
curl -s -X PUT "$BASE_URL/v1/steps/2/reorder" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"direction":"up"}' | jq .

# ✅ 成功场景（下移）
curl -s -X PUT "$BASE_URL/v1/steps/1/reorder" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"direction":"down"}' | jq .

# ❌ 失败：非法 direction
curl -s -X PUT "$BASE_URL/v1/steps/1/reorder" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"direction":"left"}' | jq .

# ❌ 失败：已在顶部
# 需要对第一个步骤执行 up
curl -s -X PUT "$BASE_URL/v1/steps/1/reorder" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"direction":"up"}' | jq .

# ❌ 失败：不存在（404）
curl -s -X PUT "$BASE_URL/v1/steps/99999/reorder" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"direction":"up"}' | jq .

# ❌ 失败：学生无权限（403）
curl -s -X PUT "$BASE_URL/v1/steps/1/reorder" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"direction":"up"}' | jq .
```

---

## 7. 视频管理接口 — `/api/v1/videos`

### 7.1 视频列表 — GET /v1/videos
```bash
# ✅ 成功场景
curl -s -X GET "$BASE_URL/v1/videos?page=1&pageSize=10" | jq .

# ✅ 带关键词
curl -s -X GET "$BASE_URL/v1/videos?keyword=CPR" | jq .

# ✅ 按技能筛选
curl -s -X GET "$BASE_URL/v1/videos?skillId=1" | jq .

# ✅ 按状态筛选
curl -s -X GET "$BASE_URL/v1/videos?status=published" | jq .

# ⚠️ 验证点：此端点需登录（SecurityConfig 中 videos 在 authenticated 块）
# 未登录应返回 401
curl -s -X GET "$BASE_URL/v1/videos" | jq .
```

### 7.2 单个视频 — GET /v1/videos/{videoId}
```bash
# ✅ 成功场景
curl -s -X GET "$BASE_URL/v1/videos/video1" | jq .

# ❌ 失败：不存在（404）
curl -s -X GET "$BASE_URL/v1/videos/nonexistent" | jq .
```

### 7.3 创建视频 — POST /v1/videos
```bash
# ✅ 成功场景
curl -s -X POST "$BASE_URL/v1/videos" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"CPR教学视频","url":"https://example.com/test.mp4","durationSeconds":120,"skillId":1,"status":"published"}' | jq .

# ❌ 失败：缺 title
curl -s -X POST "$BASE_URL/v1/videos" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.com/test.mp4"}' | jq .

# ❌ 失败：缺 url
curl -s -X POST "$BASE_URL/v1/videos" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"test"}' | jq .

# ❌ 失败：学生无权限（403）
curl -s -X POST "$BASE_URL/v1/videos" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"hack","url":"https://example.com/hack.mp4"}' | jq .
```

### 7.4 更新视频 — PUT /v1/videos/{id}
```bash
# ✅ 成功场景（注意：路径参数是数据库 id，不是 videoId 字符串）
curl -s -X PUT "$BASE_URL/v1/videos/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"更新标题"}' | jq .

# ❌ 失败：不存在（404）
curl -s -X PUT "$BASE_URL/v1/videos/99999" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"test"}' | jq .
```

### 7.5 删除视频 — DELETE /v1/videos/{id}
```bash
# ✅ 成功场景
curl -s -X DELETE "$BASE_URL/v1/videos/3" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ❌ 失败：不存在（404）
curl -s -X DELETE "$BASE_URL/v1/videos/99999" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
```

### 7.6 修改视频状态 — PATCH /v1/videos/{id}/status
```bash
# ✅ 成功场景
curl -s -X PATCH "$BASE_URL/v1/videos/1/status" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"draft"}' | jq .
```

---

## 8. 文件上传接口 — `/api/v1/upload`

### 8.1 图片上传 — POST /v1/upload/image
```bash
# ✅ 成功场景
curl -s -X POST "$BASE_URL/v1/upload/image" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -F "file=@/tmp/test_image.jpg" | jq .

# ❌ 失败：不支持格式
curl -s -X POST "$BASE_URL/v1/upload/image" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -F "file=@/tmp/test_image.gif" | jq .

# ❌ 失败：未携带 Token
curl -s -X POST "$BASE_URL/v1/upload/image" \
  -F "file=@/tmp/test_image.jpg" | jq .
```

### 8.2 视频上传 — POST /v1/upload/video
```bash
# ✅ 成功场景（管理员）
curl -s -X POST "$BASE_URL/v1/upload/video" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -F "file=@/tmp/test_video.mp4" | jq .

# ❌ 失败：不支持格式
curl -s -X POST "$BASE_URL/v1/upload/video" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -F "file=@/tmp/test_video.avi" | jq .

# ❌ 失败：学生无权限（403）
curl -s -X POST "$BASE_URL/v1/upload/video" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -F "file=@/tmp/test_video.mp4" | jq .
```

---

## 9. 知识库接口 — `/api/v1/knowledge`

### 9.1 知识库列表 — GET /v1/knowledge
```bash
# ✅ 成功场景（全部）
curl -s -X GET "$BASE_URL/v1/knowledge" | jq .

# ✅ 按分类筛选
curl -s -X GET "$BASE_URL/v1/knowledge?category=基础" | jq .

# ⚠️ 验证点：返回 List<Knowledge>（无分页 {list, total}）
# 前端可能期望 {list, total} 结构
```

### 9.2 知识库详情 — GET /v1/knowledge/{id}
```bash
# ✅ 成功场景
curl -s -X GET "$BASE_URL/v1/knowledge/1" | jq .

# ❌ 失败：不存在（404）
curl -s -X GET "$BASE_URL/v1/knowledge/99999" | jq .
```

### 9.3 创建知识 — POST /v1/knowledge
```bash
# ✅ 成功场景
curl -s -X POST "$BASE_URL/v1/knowledge" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"question":"测试问题","answer":"测试答案","category":"基础","tags":"测试"}' | jq .

# ❌ 失败：缺 question
curl -s -X POST "$BASE_URL/v1/knowledge" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"answer":"test"}' | jq .

# ❌ 失败：学生无权限（403）
curl -s -X POST "$BASE_URL/v1/knowledge" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"question":"hack","answer":"hack"}' | jq .
```

### 9.4 更新知识 — PUT /v1/knowledge/{id}
```bash
# ✅ 成功场景
curl -s -X PUT "$BASE_URL/v1/knowledge/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"answer":"更新答案"}' | jq .

# ❌ 失败：不存在（404）
curl -s -X PUT "$BASE_URL/v1/knowledge/99999" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"question":"test"}' | jq .
```

### 9.5 删除知识 — DELETE /v1/knowledge/{id}
```bash
# ✅ 成功场景
curl -s -X DELETE "$BASE_URL/v1/knowledge/2" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ❌ 失败：不存在（404）
curl -s -X DELETE "$BASE_URL/v1/knowledge/99999" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
```

---

## 10. 成绩接口 — `/api/v1/scores`

### 10.1 提交成绩 — POST /v1/scores
```bash
# ✅ 成功场景
curl -s -X POST "$BASE_URL/v1/scores" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"scene":"CPR急救","skill":"胸外按压","totalScore":85.5,"compressionDepthAvg":5.2,"compressionRateAvg":110,"errorCount":2,"stepDetails":"{}"}' | jq .

# ❌ 失败：未携带 Token
curl -s -X POST "$BASE_URL/v1/scores" \
  -H "Content-Type: application/json" \
  -d '{"scene":"CPR","skill":"按压","totalScore":80}' | jq .
```

### 10.2 查询成绩 — GET /v1/scores
```bash
# ✅ 学生查询自己的成绩
curl -s -X GET "$BASE_URL/v1/scores" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq .

# ✅ 管理员查询全部成绩（分页）
curl -s -X GET "$BASE_URL/v1/scores?all=true&page=1&pageSize=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ✅ 管理员查询特定用户成绩
curl -s -X GET "$BASE_URL/v1/scores?username=testuser" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ❌ 失败：学生查询他人成绩（403）
curl -s -X GET "$BASE_URL/v1/scores?username=admin" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq .

# ❌ 失败：未携带 Token
curl -s -X GET "$BASE_URL/v1/scores" | jq .

# ⚠️ 验证点：学生查自己返回 List<ScoreDto>（数组），管理员查全部返回 {list, total}
#            响应结构不一致！前端需适配
```

### 10.3 成绩详情 — GET /v1/scores/{id}
```bash
# ✅ 成功场景
curl -s -X GET "$BASE_URL/v1/scores/1" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq .

# ❌ 失败：不存在（404）
curl -s -X GET "$BASE_URL/v1/scores/99999" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq .
```

### 10.4 删除成绩 — DELETE /v1/scores/{id}
```bash
# ✅ 成功场景（管理员）
curl -s -X DELETE "$BASE_URL/v1/scores/2" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ❌ 失败：不存在（404）
curl -s -X DELETE "$BASE_URL/v1/scores/99999" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ❌ 失败：学生无权限（403）
curl -s -X DELETE "$BASE_URL/v1/scores/1" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq .
```

### 10.5 最新成绩 — GET /v1/scores/latest
```bash
# ✅ 学生查询自己的最新成绩
curl -s -X GET "$BASE_URL/v1/scores/latest" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq .

# ✅ 管理员查询特定用户最新成绩
curl -s -X GET "$BASE_URL/v1/scores/latest?username=testuser" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ❌ 失败：学生查询他人（403）
curl -s -X GET "$BASE_URL/v1/scores/latest?username=admin" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq .
```

### 10.6 成绩统计 — GET /v1/scores/stats
```bash
# ✅ 成功场景
curl -s -X GET "$BASE_URL/v1/scores/stats" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq .

# ✅ 管理员也可查自己的统计
curl -s -X GET "$BASE_URL/v1/scores/stats" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
```

---

## 11. 学生管理接口 — `/api/v1/students`

### 11.1 学生列表 — GET /v1/students
```bash
# ✅ 成功场景
curl -s -X GET "$BASE_URL/v1/students?page=1&pageSize=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ✅ 带关键词
curl -s -X GET "$BASE_URL/v1/students?keyword=张" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ✅ 按状态筛选
curl -s -X GET "$BASE_URL/v1/students?status=active" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
```

### 11.2 学生详情 — GET /v1/students/{id}
```bash
# ✅ 成功场景
curl -s -X GET "$BASE_URL/v1/students/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ❌ 失败：不存在（404）
curl -s -X GET "$BASE_URL/v1/students/99999" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
```

### 11.3 创建学生 — POST /v1/students
```bash
# ✅ 成功场景
curl -s -X POST "$BASE_URL/v1/students" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","phone":"13800138001","email":"zhangsan@test.com","groupName":"急救1班","certStatus":"待考证","status":"active"}' | jq .

# ❌ 失败：缺 name
curl -s -X POST "$BASE_URL/v1/students" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138001"}' | jq .

# ❌ 失败：学生无权限（403）
curl -s -X POST "$BASE_URL/v1/students" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"hack"}' | jq .
```

### 11.4 更新学生 — PUT /v1/students/{id}
```bash
# ✅ 成功场景
curl -s -X PUT "$BASE_URL/v1/students/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"更新姓名"}' | jq .
```

### 11.5 删除学生 — DELETE /v1/students/{id}
```bash
# ✅ 成功场景
curl -s -X DELETE "$BASE_URL/v1/students/2" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .
```

### 11.6 修改学生状态 — PATCH /v1/students/{id}/status
```bash
# ✅ 成功场景
curl -s -X PATCH "$BASE_URL/v1/students/1/status" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"inactive"}' | jq .
```

---

## 12. 操作日志接口 — `/api/v1/logs`

### 12.1 日志列表 — GET /v1/logs
```bash
# ✅ 成功场景（super_admin）
curl -s -X GET "$BASE_URL/v1/logs?page=1&pageSize=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ✅ 按操作人筛选
curl -s -X GET "$BASE_URL/v1/logs?adminId=1" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ✅ 按操作类型筛选
curl -s -X GET "$BASE_URL/v1/logs?action=create" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ✅ 按目标类型筛选
curl -s -X GET "$BASE_URL/v1/logs?targetType=skill" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ✅ 按日期范围筛选
curl -s -X GET "$BASE_URL/v1/logs?startDate=2026-01-01T00:00:00&endDate=2026-12-31T23:59:59" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq .

# ❌ 失败：学生无权限（403）
curl -s -X GET "$BASE_URL/v1/logs" \
  -H "Authorization: Bearer $STUDENT_TOKEN" | jq .

# ❌ 失败：未携带 Token（401）
curl -s -X GET "$BASE_URL/v1/logs" | jq .

# ⚠️ 验证点：日志表可能为空，因为 LogService.log() 从未被任何 Controller 调用
```

---

## 13. QA 智能问答接口 — `/api/v1/qa`

### 13.1 预设问题 — GET /v1/qa/presets
```bash
# ✅ 成功场景（无需登录）
curl -s -X GET "$BASE_URL/v1/qa/presets" | jq .
```

### 13.2 提问 — POST /v1/qa
```bash
# ✅ 成功场景
curl -s -X POST "$BASE_URL/v1/qa" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"question":"心肺复苏的步骤是什么？"}' | jq .

# ❌ 失败：缺 question
curl -s -X POST "$BASE_URL/v1/qa" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}' | jq .
```

---

## 14. 姿态检测接口 — `/api/v1/pose`

### 14.1 姿态检测 — POST /v1/pose/detect
```bash
# ✅ 成功场景
curl -s -X POST "$BASE_URL/v1/pose/detect" \
  -H "Authorization: Bearer $STUDENT_TOKEN" \
  -F "image=@/tmp/test_pose.jpg" | jq .

# ❌ 失败：未携带 Token
curl -s -X POST "$BASE_URL/v1/pose/detect" \
  -F "image=@/tmp/test_pose.jpg" | jq .
```

---

## 15. 静态资源访问 — `/uploads/**`

```bash
# ✅ 成功场景（无需登录）
curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/uploads/avatars/test.jpg"

# ✅ 访问不存在的文件（应返回 404 而非 401）
curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/uploads/nonexistent.jpg"
```

---

## 附录：已知问题清单

> 以下问题在代码审查中发现，部署前应修复 P0 项，P1 项建议修复。

| # | 级别 | 模块 | 问题描述 | 影响 |
|---|------|------|----------|------|
| 1 | **P0** | auth.js / UserController | 前端 `changePassword` 用 POST，后端是 PUT，HTTP 方法不匹配 | 修改密码功能 405 不可用 |
| 2 | **P0** | admin.js / admin.vue | 前端 api/admin.js 调 `/v1/admins`，admin.vue 直接调 `/v1/user/admins`，路径不一致 | api 层函数全部 404 |
| 3 | **P0** | auth.js / UserController | 前端 `updateUserInfo` 调 `PUT /v1/user/info`，后端无此端点 | 更新用户信息功能 404 |
| 4 | **P1** | ScoreDto | `scene`/`skill` 字段经 SNAKE_CASE 后仍为 `scene`/`skill`，前端期望 `scene_name`/`skill_name` | 成绩列表场景名/技能名显示空 |
| 5 | **P1** | dashboard.vue | 前端用 `compression_depth`/`compression_rate`，后端输出 `compression_depth_avg`/`compression_rate_avg` | 仪表盘按压深度/频率显示空 |
| 6 | **P1** | SceneController | `GET /v1/scenes` 返回 List（无分页），前端期望 {list, total} | 场景列表分页失效 |
| 7 | **P1** | KnowledgeController | `GET /v1/knowledge` 返回 List（无分页），前端期望 {list, total} | 知识库列表分页失效 |
| 8 | **P1** | ScoreController | 学生查成绩返回 List 数组，管理员查全部返回 {list, total}，响应结构不一致 | 前端需做两套适配 |
| 9 | **P1** | LogService | `log()` 方法定义但无任何 Controller 调用，日志永远不会被记录 | 操作日志页面永远空 |
| 10 | **P1** | DataSeeder | 默认密码 Admin@123456 / Test@123456 硬编码在源码中 | 安全风险：默认凭证可被猜测 |
| 11 | **P1** | SecurityConfig | /api/v1/videos/** 从 permitAll 改为 authenticated，与 GAPS 文档 0.4 节描述不一致 | 文档与实现不同步（功能正确） |
| 12 | **P2** | VideoController | GET /videos/{videoId} 用 String videoId，PUT/DELETE 用 Long id，路径参数类型混用 | 可能造成前端困惑 |
| 13 | **P2** | UploadController | 视频上传 durationSeconds 硬编码为 0，未解析真实时长 | 视频时长信息缺失 |
| 14 | **P2** | DataSeeder | knowledge 种子数据有 `=======` 冲突标记残留 | Git 合并冲突未清理 |
