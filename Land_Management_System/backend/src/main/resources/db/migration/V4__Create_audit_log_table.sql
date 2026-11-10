-- V4__Create_audit_log_table.sql
-- Migration to create audit_log table for tracking sensitive operations

CREATE TABLE audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(255) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    dossier_id BIGINT,
    old_value LONGTEXT,
    new_value LONGTEXT,
    ip_address VARCHAR(45),
    user_agent LONGTEXT,
    status VARCHAR(20) NOT NULL,
    error_message LONGTEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description LONGTEXT,
    
    INDEX idx_audit_user_id (user_id),
    INDEX idx_audit_dossier_id (dossier_id),
    INDEX idx_audit_timestamp (created_at),
    INDEX idx_audit_action (action_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add foreign key constraints
ALTER TABLE audit_log 
ADD CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE SET NULL;

ALTER TABLE audit_log 
ADD CONSTRAINT fk_audit_dossier FOREIGN KEY (dossier_id) REFERENCES dossier(id) ON DELETE CASCADE;
