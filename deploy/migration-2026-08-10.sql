-- ============================================================================
-- Life Guard CPR_db 生产迁移 SQL（2026-08-10，2026-08-25 补 notifications）
-- 用途：补齐代码期望、但线上库缺失的表与列
--   skills / steps / operation_logs / notifications 四张新表 + videos / students 新列
-- 幂等设计：IF NOT EXISTS / 信息函数判断，可重复执行
-- 新列策略：NOT NULL 列先可空/给默认 → 回填 → 再收紧（避免对已有行失败）
-- 依据实体：Skill.java / Step.java / Log.java / Notification.java / Video.java / Student.java
-- ============================================================================

USE cpr_db;

-- ----------------------------------------------------------------------------
-- 1. skills（Skill.java：id/name/description/icon/scene_id/status/sort_order/created_at）
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skills (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    description TEXT         NULL,
    icon        VARCHAR(500) NULL,
    scene_id    BIGINT       NULL,
    status      VARCHAR(20)  NULL,
    sort_order  INT          NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 2. steps（Step.java：id/skill_id/title/description/step_order/status/created_at）
--    entity 字段 order 映射列 step_order
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS steps (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    skill_id    BIGINT       NULL,
    title       VARCHAR(200) NOT NULL,
    description TEXT         NULL,
    step_order  INT          NULL,
    status      VARCHAR(20)  NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_steps_skill_id (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 3. operation_logs（Log.java：id/admin_id/admin_username/action/target_type/target_id/detail/ip/created_at）
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS operation_logs (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    admin_id       BIGINT       NULL,
    admin_username VARCHAR(50)  NULL,
    action         VARCHAR(30)  NULL,
    target_type    VARCHAR(30)  NULL,
    target_id      BIGINT       NULL,
    detail         TEXT         NULL,
    ip             VARCHAR(50)  NULL,
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 3b. notifications（Notification.java：id/title/content_md/status/created_at/updated_at/published_at）
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    title        VARCHAR(200) NOT NULL,
    content_md   TEXT         NOT NULL,
    status       VARCHAR(20)  NULL,
    created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6)  NULL,
    published_at DATETIME(6)  NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 4. videos 补列（Video.java：title/skill_id/status/created_at）
--    title: NOT NULL 但给默认 '' 以兼容旧行；status 默认 active；created_at 默认当前时间
-- ----------------------------------------------------------------------------
SET @has_title := (SELECT COUNT(*) FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA='cpr_db' AND TABLE_NAME='videos' AND COLUMN_NAME='title');
SET @sql := IF(@has_title = 0,
    'ALTER TABLE videos ADD COLUMN title VARCHAR(200) NOT NULL DEFAULT ''''',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_skill_id := (SELECT COUNT(*) FROM information_schema.COLUMNS
                      WHERE TABLE_SCHEMA='cpr_db' AND TABLE_NAME='videos' AND COLUMN_NAME='skill_id');
SET @sql := IF(@has_skill_id = 0,
    'ALTER TABLE videos ADD COLUMN skill_id BIGINT NULL',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_status := (SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA='cpr_db' AND TABLE_NAME='videos' AND COLUMN_NAME='status');
SET @sql := IF(@has_status = 0,
    'ALTER TABLE videos ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT ''active''',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_created_at := (SELECT COUNT(*) FROM information_schema.COLUMNS
                        WHERE TABLE_SCHEMA='cpr_db' AND TABLE_NAME='videos' AND COLUMN_NAME='created_at');
SET @sql := IF(@has_created_at = 0,
    'ALTER TABLE videos ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------------------------
-- 5. students 补列（Student.java：status）
--    拍板：先可空（与 DataSeeder/Service 对齐后再决定是否收紧）
-- ----------------------------------------------------------------------------
SET @has_status := (SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA='cpr_db' AND TABLE_NAME='students' AND COLUMN_NAME='status');
SET @sql := IF(@has_status = 0,
    'ALTER TABLE students ADD COLUMN status VARCHAR(20) NULL',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------------------------
-- 6. 校验（部署后运行确认）
-- ----------------------------------------------------------------------------
-- SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA='cpr_db' AND TABLE_NAME IN ('skills','steps','operation_logs','notifications');
-- SHOW COLUMNS FROM videos; SHOW COLUMNS FROM students;
