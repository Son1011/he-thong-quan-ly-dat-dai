## 07 — Backend (Spring Boot + JPA)

### 7.1 Công nghệ

- **Spring Boot**: REST API, Spring Security, OpenAPI
- **Spring Data JPA**: ORM cho MySQL
- **MySQL**: DB chính, quản lý quan hệ dữ liệu
- **Flyway**: migration schema và seed dữ liệu
- **JWT**: auth stateless
- **BCrypt**: hash mật khẩu
- **multipart file upload**: upload/download tài liệu

### 7.2 Auth & session

- Login trả `access_token` JWT (payload có `sub=user_id`, `iat`, `exp`).
- Frontend gửi token trong `Authorization: Bearer ...`.
- `/me` trả `role`, `unit_id`, `must_change_password` để frontend render đúng UI/guard.

### 7.3 Quy tắc scope dữ liệu (high-level)

- Xã: xem hồ sơ có `origin_unit_id == unit_id`
- Tỉnh: xem hồ sơ `origin_unit_id` là tỉnh mình hoặc xã/phường trực thuộc
- Trung ương:
  - **Xem được mọi hồ sơ** (quyền giám sát)
  - Mặc định list theo inbox để tránh quá nhiều dữ liệu
  - Có thể browse theo unit selection (endpoint list dossiers có `unit_id`)
  - Có endpoint riêng cho “Danh sách hồ sơ do TW quyết”: `GET /dossiers/central/decisions`
- Admin: xem/tác động theo API admin

### 7.4 Workflow “inbox routing” bằng `assigned_to_unit_id`

MVP coi “ai được xử lý tiếp theo” là nguồn sự thật:

- Inbox = `assigned_to_unit_id == me.unit_id`
- Khi action được thực hiện, backend cập nhật `assigned_to_unit_id` sang đơn vị kế tiếp.

### 7.5 Attachments

- Upload file:
  - Backend tạo filename bằng uuid để tránh trùng
  - Lưu file tại `UPLOADS_DIR`
  - Lưu metadata ở `dossierattachment`
- Download:
  - Check quyền xem dossier trước
  - Trả file qua `FileResponse`

### 7.6 Stats

Thống kê dùng query SQL (aggregate) cho:

- Central/Admin: theo tỉnh (tổng hồ sơ, approved/returned/pending, tổng attachments)
- Province: theo xã/phường trực thuộc

### 7.7 Schema & Migration

Backend Java dùng **Flyway** để quản lý schema và seed dữ liệu. Mọi thay đổi bảng được áp dụng qua migration script, không dùng cơ chế `ALTER TABLE` runtime đơn giản.

Production khuyến nghị:

- Duy trì Flyway migration cho mọi thay đổi schema
- Sử dụng MySQL hoặc một database quan hệ tương thích sản xuất
