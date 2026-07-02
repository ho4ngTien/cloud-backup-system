-- =============================================================
--  CloudSafe – PostgreSQL Database Schema
--  Giai đoạn 4: Khởi tạo database với đầy đủ bảng và indexes
-- =============================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================
-- 1. Roles & Users
-- =============================================================
CREATE TABLE IF NOT EXISTS roles (
    id   VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id                   VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    email                VARCHAR(256) NOT NULL UNIQUE,
    password_hash        VARCHAR(255) NOT NULL,
    display_name         VARCHAR(100) NOT NULL,
    enabled              BOOLEAN NOT NULL DEFAULT TRUE,
    storage_quota_bytes  BIGINT  NOT NULL DEFAULT 10737418240,  -- 10 GB
    storage_used_bytes   BIGINT  NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id VARCHAR(36) NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    role_id VARCHAR(36) NOT NULL REFERENCES roles(id)  ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- =============================================================
-- 2. Devices
-- =============================================================
CREATE TABLE IF NOT EXISTS devices (
    id                    VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    user_id               VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name                  VARCHAR(200) NOT NULL,
    operating_system      VARCHAR(100) NOT NULL,
    online                BOOLEAN NOT NULL DEFAULT FALSE,
    watch_path            VARCHAR(500),
    auto_backup_enabled   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_seen_at          TIMESTAMPTZ
);

-- =============================================================
-- 3. Backup Jobs, Versions, Files
-- =============================================================
CREATE TABLE IF NOT EXISTS backup_jobs (
    id               VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    user_id          VARCHAR(36) NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    device_id        VARCHAR(36) NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    backup_type      VARCHAR(20) NOT NULL CHECK (backup_type IN ('FULL','INCREMENTAL')),
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                          CHECK (status IN ('PENDING','IN_PROGRESS','COMPLETED','FAILED','CANCELLED')),
    total_size_bytes BIGINT NOT NULL DEFAULT 0,
    file_count       INT    NOT NULL DEFAULT 0,
    version          INT    NOT NULL DEFAULT 1,
    source_path      VARCHAR(500),
    error_message    TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at     TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS backup_versions (
    id                 VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    backup_job_id      VARCHAR(36) NOT NULL REFERENCES backup_jobs(id) ON DELETE CASCADE,
    version_number     INT    NOT NULL,
    full_backup        BOOLEAN NOT NULL DEFAULT TRUE,
    total_size_bytes   BIGINT  NOT NULL DEFAULT 0,
    file_count         INT     NOT NULL DEFAULT 0,
    encryption_key_ref VARCHAR(512),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS backup_files (
    id                VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    backup_version_id VARCHAR(36) NOT NULL REFERENCES backup_versions(id) ON DELETE CASCADE,
    relative_path     VARCHAR(1000) NOT NULL,
    size_bytes        BIGINT NOT NULL,
    sha256_hash       CHAR(64) NOT NULL,
    storage_key       VARCHAR(500) NOT NULL UNIQUE,
    encrypted         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================================
-- 4. Restore History
-- =============================================================
CREATE TABLE IF NOT EXISTS restore_history (
    id                  VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    backup_version_id   VARCHAR(36) NOT NULL REFERENCES backup_versions(id),
    user_id             VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_id           VARCHAR(36) NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    restore_type        VARCHAR(20) NOT NULL DEFAULT 'FULL'
                             CHECK (restore_type IN ('FULL','SELECTIVE')),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                             CHECK (status IN ('PENDING','IN_PROGRESS','COMPLETED','FAILED')),
    target_path         VARCHAR(500),
    selected_files      TEXT,
    error_message       TEXT,
    requested_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at        TIMESTAMPTZ
);

-- =============================================================
-- 5. Notifications
-- =============================================================
CREATE TABLE IF NOT EXISTS notifications (
    id          VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4()::text,
    user_id     VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_type  VARCHAR(50) NOT NULL,
    message     TEXT NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                     CHECK (status IN ('PENDING','SENT','FAILED')),
    metadata    TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sent_at     TIMESTAMPTZ
);

-- =============================================================
-- 6. Indexes (performance optimization)
-- =============================================================
CREATE INDEX IF NOT EXISTS idx_users_email            ON users(email);
CREATE INDEX IF NOT EXISTS idx_devices_user           ON devices(user_id);
CREATE INDEX IF NOT EXISTS idx_backup_jobs_user       ON backup_jobs(user_id);
CREATE INDEX IF NOT EXISTS idx_backup_jobs_device     ON backup_jobs(device_id);
CREATE INDEX IF NOT EXISTS idx_backup_jobs_status     ON backup_jobs(status);
CREATE INDEX IF NOT EXISTS idx_backup_versions_job    ON backup_versions(backup_job_id);
CREATE INDEX IF NOT EXISTS idx_backup_files_version   ON backup_files(backup_version_id);
CREATE INDEX IF NOT EXISTS idx_backup_files_key       ON backup_files(storage_key);
CREATE INDEX IF NOT EXISTS idx_backup_files_sha256    ON backup_files(sha256_hash);
CREATE INDEX IF NOT EXISTS idx_restore_user           ON restore_history(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user     ON notifications(user_id, status);

-- =============================================================
-- 7. Seed data – default roles
-- =============================================================
INSERT INTO roles (id, name) VALUES
    (uuid_generate_v4()::text, 'ROLE_USER'),
    (uuid_generate_v4()::text, 'ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;
