## 10 — Yêu cầu hệ thống (đồ án tốt nghiệp)

Tài liệu này mô tả **yêu cầu chức năng (FR)** và **yêu cầu phi chức năng (NFR)** cho hệ thống Quản lý hồ sơ đất đai theo mô hình hành chính 3 cấp.

---

## 10.1 Phạm vi & giả định

### Phạm vi nghiệp vụ

- Quản lý hồ sơ theo tuyến xử lý: **Xã/Phường → Tỉnh → Trung ương → trả kết quả**
- Quản trị tài khoản theo đơn vị hành chính và vai trò
- Lưu trữ tài liệu đính kèm, tải về
- Thống kê theo tỉnh / theo xã-phường

### Giả định

- Mỗi user thuộc đúng **1** đơn vị hành chính.
- Mỗi hồ sơ có đúng **1** đơn vị tạo (`origin_unit_id`) và **1** đơn vị đang xử lý (`assigned_to_unit_id`).
- Mỗi môi trường chỉ có **1** đơn vị Trung ương hoạt động (enforced).

---

## 10.2 FR — Yêu cầu chức năng

### FR-01 — Đăng nhập

- Người dùng đăng nhập bằng `username/password`.
- Hệ thống trả token phiên (JWT) để gọi API.

### FR-02 — Xem thông tin người dùng hiện tại

- Người dùng xem được `role`, `unit`, và trạng thái `must_change_password`.

### FR-03 — Đổi mật khẩu

- Người dùng đổi mật khẩu với xác nhận.
- Hệ thống có cơ chế bắt buộc đổi mật khẩu lần đầu (`must_change_password`).

### FR-04 — Quản trị tài khoản (ADMIN)

- Tạo user theo vai trò (TW/Tỉnh/Xã-Phường).
- Cập nhật quyền/đơn vị, bật tắt tài khoản.
- Tìm kiếm user theo username.

### FR-05 — Quản lý đơn vị hành chính

- Tạo đơn vị theo cấp và cây quan hệ.
- Tra cứu tỉnh; tra cứu xã/phường theo tỉnh.

### FR-06 — Tạo hồ sơ (Xã/Phường, Tỉnh)

- Xã/Phường tạo hồ sơ → tự động chuyển lên tỉnh.
- Tỉnh tạo hồ sơ → tự động chuyển lên Trung ương.

### FR-07 — Inbox xử lý hồ sơ

- Người dùng thấy danh sách hồ sơ cần xử lý khi `assigned_to_unit_id == unit_id`.

### FR-08 — Thao tác xử lý hồ sơ

- Tỉnh: `approve/return/escalate`
- Trung ương: `approve/return`
- Xã/Phường: `submit` (khi bị trả)
- `return` bắt buộc có `note`.

### FR-09 — Lịch sử xử lý (Audit trail)

- Mọi thao tác tạo/duyệt/trả/gửi… phải được ghi nhận trong lịch sử.
- Lịch sử có thể kèm **chữ ký** và ghi chú.

### FR-10 — Upload/Download tài liệu đính kèm

- Upload file cho hồ sơ.
- List file đã upload.
- Tải về file theo quyền xem hồ sơ.

### FR-11 — Thống kê

- TW/Admin: thống kê theo tỉnh (tổng hồ sơ, trạng thái, tổng file)
- Tỉnh: thống kê theo xã/phường trực thuộc

---

## 10.3 NFR — Yêu cầu phi chức năng

### NFR-01 — Bảo mật

- Mật khẩu lưu dạng hash (bcrypt).
- API phải enforce quyền ở backend (403).
- Token phải có hạn dùng (exp).

### NFR-02 — Toàn vẹn dữ liệu

- Không được phép tạo nhiều “Trung ương” hoạt động.
- Workflow phải đảm bảo “đúng tuyến”: chỉ đơn vị đang assigned mới thao tác.

### NFR-03 — Hiệu năng

- Inbox/list phải phản hồi ổn với dữ liệu lớn (cần index/aggregate hợp lý).
- Upload/download hỗ trợ file kích thước vừa phải (giới hạn cấu hình).

### NFR-04 — Tính sẵn sàng & deploy

- Backend phải chạy được bằng Maven / Spring Boot.
- Dữ liệu (DB, uploads) phải persistent.

### NFR-05 — Khả năng mở rộng

- Có thể nâng cấp DB sang Postgres.
- Có thể chuyển lưu file sang object storage (MinIO/S3).
- Có thể nâng cấp chữ ký “ảnh” thành chữ ký số (PKI).
