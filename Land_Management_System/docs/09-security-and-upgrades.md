## 09 — Security (MVP) & hướng nâng cấp

### 9.1 Những gì MVP đang làm
- **Hash mật khẩu** bằng bcrypt (`passlib`).
- **JWT** cho authentication:
  - token có `exp` (hết hạn)
  - frontend gửi Bearer token qua axios interceptor
- **Phân quyền** backend:
  - endpoint admin chỉ cho `ADMIN`
  - thao tác hồ sơ chỉ cho unit đang assigned (tránh duyệt “nhầm tuyến”)
- **Bắt đổi mật khẩu**: `must_change_password`

### 9.2 Những gì MVP chưa làm (rủi ro)
- JWT secret đang hardcode (dev).
- Chưa có refresh token / revoke token.
- Chưa có rate limit / lockout login.
- Chữ ký chỉ là “ảnh vẽ” (không có giá trị chữ ký số pháp lý).
- Lưu chữ ký base64 trong DB có thể làm DB nặng.
- Upload chưa có antivirus scan / file type enforcement chặt.

### 9.3 Gợi ý nâng cấp (production)
- **Auth**
  - đưa JWT secret ra env + rotate key
  - refresh token + blacklist/revoke
  - rate limiting + audit IP/user-agent
- **RBAC/ABAC**
  - chuẩn hóa quyền theo policy
  - scope dữ liệu TW đầy đủ (browse theo tỉnh/xã + audit)
- **Chữ ký số**
  - PKI, timestamp, hash tài liệu, verify chain
  - render lên PDF biên bản
- **Storage**
  - lưu file/ảnh chữ ký ở S3/MinIO
  - DB chỉ lưu metadata + URL + checksum
- **Migration**
  - Alembic + DB Postgres


