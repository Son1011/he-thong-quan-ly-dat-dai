-- =========================================================
-- V2__Seed_data.sql
-- Initial seed data for testing
-- =========================================================

-- Clear existing data (safe for initial setup)
DELETE FROM dossier_attachment;
DELETE FROM approval_history;
DELETE FROM dossier;
DELETE FROM user_account;
DELETE FROM administrative_unit;

-- =========================================================
-- Insert administrative units
-- =========================================================

INSERT INTO administrative_unit (id, name, unit_level, unit_kind, parent_id, is_active)
VALUES
(1, 'Trung ương', 'CENTRAL', NULL, NULL, 1),
(2, 'Tỉnh A', 'PROVINCE', NULL, 1, 1),
(3, 'Huyện B', 'COMMUNE', 'COMMUNE', 2, 1),
(4, 'Phường C', 'COMMUNE', 'WARD', 2, 1);

-- =========================================================
-- Insert user accounts
-- Passwords: 
-- - admin/123456
-- - commune_officer/123456
-- - province_officer/123456
-- - central_officer/123456
-- =========================================================

INSERT INTO user_account (id, username, password_hash, role_code, unit_id, is_active, must_change_password)
VALUES
(1, 'admin', '$2a$10$h1wNov8QxwpYGGZs9wwN4OmtekSUtZuKINAGCKblkv/Iwnfc5v97m', 'ADMIN', 1, 1, 0),
(2, 'commune_officer', '$2a$10$yd6fQ.l4ykAmUOF4eBUEqea1b4AHHQgLEzqoLYVL.JNn0Fh805ZbS', 'COMMUNE_OFFICER', 3, 1, 1),
(3, 'province_officer', '$2a$10$r9smsZuqb6xK/4Z6uwj1IOmsPN6ey6o0vu8juZuiYifgZhjcOC1jK', 'PROVINCE_OFFICER', 2, 1, 1),
(4, 'central_officer', '$2a$10$YHk5MJkLMRhlRUzyIfidS.Wi95T702AKvkEdR2y2Nd1Fgbm96c5Du', 'CENTRAL_OFFICER', 1, 1, 1);

-- =========================================================
-- Insert sample dossiers
-- =========================================================

INSERT INTO dossier (id, title, status_code, sent_to_central, origin_unit_id, created_by_user_id, assigned_to_unit_id)
VALUES
(1, 'Hồ sơ cấp đất', 'PENDING', 0, 3, 2, 3),
(2, 'Hồ sơ điều chuyển', 'ESCALATED', 1, 3, 2, 2);

-- =========================================================
-- Insert approval history
-- =========================================================

INSERT INTO approval_history (id, dossier_id, action_code, note, actor_user_id, actor_unit_id, from_unit_id, to_unit_id)
VALUES
(1, 1, 'CREATE', 'Khởi tạo hồ sơ', 2, 3, NULL, 3),
(2, 2, 'ESCALATE', 'Gửi lên tỉnh', 2, 3, 3, 2);

-- =========================================================
-- Insert sample attachments
-- =========================================================

INSERT INTO dossier_attachment (id, dossier_id, original_filename, content_type, storage_path, uploaded_by_user_id)
VALUES
(1, 1, 'giay_to.pdf', 'application/pdf', 'uploads/giay_to_1.pdf', 2);

-- Reset AUTO_INCREMENT
ALTER TABLE administrative_unit AUTO_INCREMENT = 5;
ALTER TABLE user_account AUTO_INCREMENT = 5;
ALTER TABLE dossier AUTO_INCREMENT = 3;
ALTER TABLE approval_history AUTO_INCREMENT = 3;
ALTER TABLE dossier_attachment AUTO_INCREMENT = 2;
