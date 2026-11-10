-- V5__Sync_schema_with_current_entities.sql
-- Bring the database schema in line with current entity definitions.

SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- 1. Add dossier_type and seed a default type.
CREATE TABLE IF NOT EXISTS dossier_type (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    processing_days INT NOT NULL,
    description VARCHAR(500) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_dossier_type_code (code)
) ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4
COLLATE = utf8mb4_unicode_ci;

INSERT INTO dossier_type (code, name, processing_days, description)
SELECT 'LAND_ALLOCATION', 'Cấp đất', 10, 'Hồ sơ cấp đất'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM dossier_type WHERE code = 'LAND_ALLOCATION');

-- 2. Extend user_account to match UserAccount entity.
SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user_account'
    AND column_name = 'full_name'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE user_account ADD COLUMN full_name VARCHAR(255) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user_account'
    AND column_name = 'email'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE user_account ADD COLUMN email VARCHAR(255) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user_account'
    AND column_name = 'phone_number'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE user_account ADD COLUMN phone_number VARCHAR(50) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user_account'
    AND column_name = 'address'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE user_account ADD COLUMN address VARCHAR(500) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user_account'
    AND column_name = 'is_first_login'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE user_account ADD COLUMN is_first_login TINYINT(1) NOT NULL DEFAULT 1',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user_account'
    AND column_name = 'password_changed_at'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE user_account ADD COLUMN password_changed_at DATETIME(3) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user_account'
    AND column_name = 'password_expires_at'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE user_account ADD COLUMN password_expires_at DATETIME(3) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'user_account'
    AND column_name = 'last_login_at'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE user_account ADD COLUMN last_login_at DATETIME(3) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE user_account
SET full_name = COALESCE(NULLIF(full_name, ''), CONCAT(username, ' (user)')),
    email = COALESCE(NULLIF(email, ''), CONCAT(username, '@example.com'))
WHERE full_name IS NULL
   OR full_name = ''
   OR email IS NULL
   OR email = '';

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'user_account'
    AND index_name = 'uq_user_account_email'
);
SET @sql = IF(@index_exists = 0,
  'CREATE UNIQUE INDEX uq_user_account_email ON user_account(email)',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE user_account
    MODIFY COLUMN full_name VARCHAR(255) NOT NULL,
    MODIFY COLUMN email VARCHAR(255) NOT NULL;

-- 3. Extend dossier to match Dossier entity.
SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'dossier'
    AND column_name = 'dossier_code'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE dossier ADD COLUMN dossier_code VARCHAR(255) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'dossier'
    AND column_name = 'citizen_name'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE dossier ADD COLUMN citizen_name VARCHAR(255) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'dossier'
    AND column_name = 'citizen_phone'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE dossier ADD COLUMN citizen_phone VARCHAR(50) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'dossier'
    AND column_name = 'citizen_identity_number'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE dossier ADD COLUMN citizen_identity_number VARCHAR(100) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'dossier'
    AND column_name = 'citizen_address'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE dossier ADD COLUMN citizen_address VARCHAR(500) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'dossier'
    AND column_name = 'dossier_type_id'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE dossier ADD COLUMN dossier_type_id BIGINT NULL',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'dossier'
    AND column_name = 'priority'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE dossier ADD COLUMN priority VARCHAR(30) NOT NULL DEFAULT ''NORMAL''',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'dossier'
    AND column_name = 'current_step'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE dossier ADD COLUMN current_step VARCHAR(30) NOT NULL DEFAULT ''RECEIVE''',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'dossier'
    AND column_name = 'received_at'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE dossier ADD COLUMN received_at DATETIME(3) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'dossier'
    AND column_name = 'due_date'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE dossier ADD COLUMN due_date DATETIME(3) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'dossier'
    AND column_name = 'completed_at'
);
SET @sql = IF(@column_exists = 0,
  'ALTER TABLE dossier ADD COLUMN completed_at DATETIME(3) NULL',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE dossier
SET dossier_code = COALESCE(dossier_code, CONCAT('DS-', id)),
    citizen_name = COALESCE(citizen_name, 'Người dân'),
    dossier_type_id = COALESCE(dossier_type_id, (SELECT id FROM dossier_type WHERE code = 'LAND_ALLOCATION' LIMIT 1))
WHERE dossier_code IS NULL OR citizen_name IS NULL OR dossier_type_id IS NULL;

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'dossier'
    AND index_name = 'uq_dossier_code'
);
SET @sql = IF(@index_exists = 0,
  'CREATE UNIQUE INDEX uq_dossier_code ON dossier(dossier_code)',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_exists = (
  SELECT COUNT(*)
  FROM information_schema.REFERENTIAL_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND CONSTRAINT_NAME = 'fk_dossier_type'
);
SET @sql = IF(@fk_exists = 0,
  'ALTER TABLE dossier ADD CONSTRAINT fk_dossier_type FOREIGN KEY (dossier_type_id) REFERENCES dossier_type(id) ON UPDATE RESTRICT ON DELETE RESTRICT',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dossier' AND CONSTRAINT_NAME = 'chk_dossier_priority') = 0,
  'ALTER TABLE dossier ADD CONSTRAINT chk_dossier_priority CHECK (priority IN (''NORMAL'', ''URGENT'', ''EMERGENCY''))',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dossier' AND CONSTRAINT_NAME = 'chk_dossier_current_step') = 0,
  'ALTER TABLE dossier ADD CONSTRAINT chk_dossier_current_step CHECK (current_step IN (''RECEIVE'', ''VERIFY'', ''APPROVE'', ''COMPLETE''))',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'approval_history'
    AND index_name = 'idx_approval_action'
);
SET @sql = IF(@index_exists = 0,
  'CREATE INDEX idx_approval_action ON approval_history(action_code)',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'dossier_attachment'
    AND index_name = 'idx_attachment_uploaded'
);
SET @sql = IF(@index_exists = 0,
  'CREATE INDEX idx_attachment_uploaded ON dossier_attachment(uploaded_at)',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
