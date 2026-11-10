## 01 — Tổng quan hệ thống

### Bài toán

Hệ thống quản lý hồ sơ đất đai theo mô hình hành chính 3 cấp:

- **Xã/Phường** tạo hồ sơ, bổ sung khi bị trả.
- **Tỉnh** duyệt hồ sơ từ Xã/Phường hoặc gửi lên Trung ương khi cần.
- **Trung ương** duyệt/hoàn trả các hồ sơ được gửi lên.

### Mục tiêu MVP

- **Workflow rõ ràng**: ai xử lý tiếp theo được quyết định bởi `assigned_to_unit_id`.
- **Phân quyền** theo **vai trò + đơn vị** (role + unit).
- **Audit trail**: lưu lịch sử xử lý trong `approvalhistory` (kèm note và chữ ký dạng ảnh).
- **File attachments**: upload và tải về tài liệu hồ sơ.
- **Thống kê**: tổng hợp theo tỉnh / theo xã-phường.
- **Backend Java**: `backend-java/` dùng Spring Boot + MySQL, chạy bằng Maven.

### Các module chính

- **Auth**: login JWT + `/me` + đổi mật khẩu.
- **Admin**: tạo/sửa user, quản trị đơn vị (MVP có endpoint tạo unit).
- **Dossiers**: tạo, list, inbox, detail, actions.
- **Attachments**: upload + download.
- **Units**: tra cứu tỉnh và xã/phường theo tỉnh.
- **Stats**: thống kê theo tỉnh / theo xã-phường.

### Vai trò

- `ADMIN`
- `COMMUNE_OFFICER`
- `PROVINCE_OFFICER`
- `CENTRAL_OFFICER`

### Tài liệu liên quan

- Chi tiết database: `docs/02-database.md`
- Chi tiết user stories: `docs/03-user-stories.md`
- Chi tiết API: `docs/04-api.md`
- Diagram (Mermaid): `docs/05-diagrams.md`
- Frontend: `docs/06-frontend.md`
- Backend: `docs/07-backend.md`
- Deploy: `docs/08-deployment.md`
- Security & nâng cấp: `docs/09-security-and-upgrades.md`
