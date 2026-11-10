-- =========================================================
-- V1__Create_tables.sql
-- Database schema for Land Management System
-- MySQL 8.0+
-- =========================================================

SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- =========================================================
-- 1. administrative_unit
-- =========================================================

CREATE TABLE IF NOT EXISTS administrative_unit (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    unit_level VARCHAR(30) NOT NULL,
    unit_kind VARCHAR(30) NULL,
    parent_id BIGINT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT chk_administrative_unit_level
        CHECK (unit_level IN ('CENTRAL', 'PROVINCE', 'COMMUNE')),
    CONSTRAINT chk_administrative_unit_kind
        CHECK (unit_kind IS NULL OR unit_kind IN ('COMMUNE', 'WARD')),
    CONSTRAINT fk_administrative_unit_parent
        FOREIGN KEY (parent_id)
        REFERENCES administrative_unit(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
) ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_administrative_unit_parent_id
ON administrative_unit(parent_id);

CREATE INDEX idx_administrative_unit_level
ON administrative_unit(unit_level);

CREATE INDEX idx_administrative_unit_active
ON administrative_unit(is_active);

-- =========================================================
-- 2. user_account
-- =========================================================

CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    unit_id BIGINT NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    must_change_password TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT uq_user_account_username
        UNIQUE (username),
    CONSTRAINT chk_user_account_role
        CHECK (role_code IN (
            'ADMIN',
            'COMMUNE_OFFICER',
            'PROVINCE_OFFICER',
            'CENTRAL_OFFICER'
        )),
    CONSTRAINT fk_user_account_unit
        FOREIGN KEY (unit_id)
        REFERENCES administrative_unit(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
) ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_user_account_unit_id
ON user_account(unit_id);

CREATE INDEX idx_user_account_role
ON user_account(role_code);

CREATE INDEX idx_user_account_active
ON user_account(is_active);

-- =========================================================
-- 3. dossier
-- =========================================================

CREATE TABLE IF NOT EXISTS dossier (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(500) NOT NULL,
    status_code VARCHAR(30) NOT NULL,
    sent_to_central TINYINT(1) NOT NULL DEFAULT 0,
    origin_unit_id BIGINT NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    assigned_to_unit_id BIGINT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT chk_dossier_status
        CHECK (status_code IN (
            'PENDING',
            'APPROVED',
            'RETURNED',
            'ESCALATED'
        )),
    CONSTRAINT fk_dossier_origin_unit
        FOREIGN KEY (origin_unit_id)
        REFERENCES administrative_unit(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    CONSTRAINT fk_dossier_assigned_unit
        FOREIGN KEY (assigned_to_unit_id)
        REFERENCES administrative_unit(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    CONSTRAINT fk_dossier_created_by_user
        FOREIGN KEY (created_by_user_id)
        REFERENCES user_account(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
) ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_dossier_status
ON dossier(status_code);

CREATE INDEX idx_dossier_origin_unit_id
ON dossier(origin_unit_id);

CREATE INDEX idx_dossier_assigned_to_unit_id
ON dossier(assigned_to_unit_id);

CREATE INDEX idx_dossier_created_by_user_id
ON dossier(created_by_user_id);

CREATE INDEX idx_dossier_sent_to_central
ON dossier(sent_to_central);

CREATE INDEX idx_dossier_created_at
ON dossier(created_at);

CREATE INDEX idx_dossier_inbox
ON dossier(assigned_to_unit_id, updated_at);

-- =========================================================
-- 4. approval_history
-- =========================================================

CREATE TABLE IF NOT EXISTS approval_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dossier_id BIGINT NOT NULL,
    action_code VARCHAR(50) NOT NULL,
    note TEXT NULL,
    signature_base64_png MEDIUMTEXT NULL,
    actor_user_id BIGINT NOT NULL,
    actor_unit_id BIGINT NOT NULL,
    from_unit_id BIGINT NULL,
    to_unit_id BIGINT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT chk_approval_history_action
        CHECK (action_code IN (
            'CREATE',
            'APPROVE',
            'RETURN',
            'ESCALATE',
            'SUBMIT',
            'SEND_TO_CENTRAL'
        )),
    CONSTRAINT fk_approval_history_dossier
        FOREIGN KEY (dossier_id)
        REFERENCES dossier(id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    CONSTRAINT fk_approval_history_actor_user
        FOREIGN KEY (actor_user_id)
        REFERENCES user_account(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    CONSTRAINT fk_approval_history_actor_unit
        FOREIGN KEY (actor_unit_id)
        REFERENCES administrative_unit(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    CONSTRAINT fk_approval_history_from_unit
        FOREIGN KEY (from_unit_id)
        REFERENCES administrative_unit(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    CONSTRAINT fk_approval_history_to_unit
        FOREIGN KEY (to_unit_id)
        REFERENCES administrative_unit(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
) ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_approval_history_dossier_id
ON approval_history(dossier_id);

CREATE INDEX idx_approval_history_actor_user_id
ON approval_history(actor_user_id);

CREATE INDEX idx_approval_history_actor_unit_id
ON approval_history(actor_unit_id);

CREATE INDEX idx_approval_history_created_at
ON approval_history(created_at);

-- =========================================================
-- 5. dossier_attachment
-- =========================================================

CREATE TABLE IF NOT EXISTS dossier_attachment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dossier_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    uploaded_by_user_id BIGINT NOT NULL,
    uploaded_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    CONSTRAINT fk_dossier_attachment_dossier
        FOREIGN KEY (dossier_id)
        REFERENCES dossier(id)
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    CONSTRAINT fk_dossier_attachment_uploaded_by_user
        FOREIGN KEY (uploaded_by_user_id)
        REFERENCES user_account(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
) ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_dossier_attachment_dossier_id
ON dossier_attachment(dossier_id);

CREATE INDEX idx_dossier_attachment_uploaded_by_user_id
ON dossier_attachment(uploaded_by_user_id);

CREATE INDEX idx_dossier_attachment_uploaded_at
ON dossier_attachment(uploaded_at);

CREATE INDEX idx_dossier_attachment_storage_path
ON dossier_attachment(storage_path);

