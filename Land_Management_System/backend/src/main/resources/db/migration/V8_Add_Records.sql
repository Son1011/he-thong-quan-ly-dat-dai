-- =========================
-- DOSSIER TYPE
-- =========================
INSERT INTO dossier_type (code, name, processing_days, description)
VALUES
('CHUYEN_NHUONG_DAT', 'Chuyển nhượng đất', 5, 'Hồ sơ chuyển nhượng quyền sử dụng đất')
ON DUPLICATE KEY UPDATE
name = VALUES(name),
processing_days = VALUES(processing_days),
description = VALUES(description);

-- =========================
-- USER ACCOUNT
-- =========================

UPDATE user_account
SET
    username = 'admin',
    password_hash = '$2a$10$mK2yC1j9bARDpjt14V7MQ.yh1HkGXQ6j6T7Of1kmXGcmpjinNff4G'
WHERE id = 1;

UPDATE user_account
SET
    username = 'commune01',
    password_hash = '$2a$10$T6ubiDRiYtvyJB.McrcIl.Qz1KLChu.WDjqQh2YNLpUefmsuwFGT6'
WHERE id = 2;

UPDATE user_account
SET
    username = 'province01',
    password_hash = '$2a$10$T6ubiDRiYtvyJB.McrcIl.Qz1KLChu.WDjqQh2YNLpUefmsuwFGT6'
WHERE id = 3;

UPDATE user_account
SET
    username = 'central01',
    password_hash = '$2a$10$T6ubiDRiYtvyJB.McrcIl.Qz1KLChu.WDjqQh2YNLpUefmsuwFGT6'
WHERE id = 4;