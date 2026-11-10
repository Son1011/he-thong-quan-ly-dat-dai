# Hệ thống Quản lý Đất đai (Land Management System)

Hệ thống quản lý hồ sơ đất đai theo mô hình **hành chính 3 cấp** (**Trung ương** → **Tỉnh** → **Xã/Phường**):

- **Backend**: Java 21 + Spring Boot 3.5.14 + Spring Security (JWT) + Spring Data JPA
- **Frontend**: React + TypeScript + Vite (phục vụ qua Nginx khi chạy Docker)
- **Database**: MySQL 8.0 (tự động seed từ [`land_management_system.sql`](./land_management_system.sql))

---

## 1. Tài khoản đăng nhập mẫu

Dữ liệu mẫu được tự động nạp từ [`land_management_system.sql`](./land_management_system.sql) trong lần khởi tạo MySQL đầu tiên:

| Username | Password | Vai trò (`role_code`) | Đơn vị hành chính |
| --- | --- | --- | --- |
| `admin` | `admin1234` | `ADMIN` (Quản trị hệ thống) | Trung ương |
| `central01` | `admin123` | `CENTRAL_OFFICER` (Cán bộ Trung ương) | Trung ương |
| `province01` | `admin123` | `PROVINCE_OFFICER` (Cán bộ Tỉnh) | Tỉnh Hà Nội |
| `commune01` | `admin123` | `COMMUNE_OFFICER` (Cán bộ Xã/Phường) | Xã Đông Anh |

---

## 2. Các câu lệnh Docker

> Yêu cầu: đã cài và mở **Docker Desktop**. Chạy các lệnh tại thư mục gốc của repository (nơi chứa [`docker-compose.yml`](./docker-compose.yml)).

### Khởi chạy hệ thống

```powershell
# Build và chạy toàn bộ (MySQL + Java Backend + Frontend) ở chế độ nền
docker compose up -d --build

# Hoặc chỉ chạy MySQL + Java Backend (nếu chạy Frontend riêng bằng npm run dev)
docker compose up -d --build mysql backend
```

### Kiểm tra trạng thái & logs

```powershell
# Xem trạng thái các container và healthcheck
docker compose ps

# Xem log theo thời gian thực
docker compose logs -f
docker compose logs -f backend
docker compose logs -f mysql
```

### Dừng, khởi động lại & rebuild

```powershell
# Dừng các container (giữ nguyên dữ liệu DB và file upload)
docker compose down

# Khởi động lại không cần build lại
docker compose up -d

# Build lại riêng Java backend sau khi sửa code
docker compose up -d --build backend
```

### Reset Database & seed lại từ `land_management_system.sql`

MySQL Docker chỉ chạy script trong `/docker-entrypoint-initdb.d/` ở lần đầu tiên khi volume `mysql_data` chưa tồn tại. Nếu muốn xóa toàn bộ dữ liệu cũ và nạp lại từ [`land_management_system.sql`](./land_management_system.sql):

```powershell
docker compose down -v
docker compose up -d --build
```

### Truy cập MySQL trong container

```powershell
docker exec -it land_mgmt_mysql mysql -uroot -proot land_management_system
```

### Biến môi trường hỗ trợ (tùy chọn)

Có thể truyền qua file `.env` ở thư mục gốc hoặc biến môi trường shell trước khi chạy `docker compose`:

| Biến | Mặc định | Mô tả |
| --- | --- | --- |
| `MYSQL_ROOT_PASSWORD` | `root` | Mật khẩu tài khoản `root` của MySQL |
| `MYSQL_DATABASE` | `land_management_system` | Tên database |
| `MYSQL_PORT` | `3306` | Port MySQL ánh xạ ra máy host |
| `BACKEND_PORT` | `8080` | Port Backend Java ánh xạ ra máy host |
| `FRONTEND_PORT` | `5173` | Port Frontend ánh xạ ra máy host |
| `JWT_SECRET` | `dev-secret-change-me-in-production-very-important-key-for-security` | Khóa bí mật ký JWT |
| `SPRING_FLYWAY_ENABLED` | `false` | Tắt Flyway mặc định do DB đã init từ `land_management_system.sql` |

---

## 3. Địa chỉ truy cập sau khi chạy

- **Frontend (Web UI)**: `http://localhost:5173`
- **Backend API Base URL**: `http://localhost:8080/api`
- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api/v3/api-docs`
- **Health Check**: `http://localhost:8080/api/actuator/health`
- **MySQL**: `localhost:3306` (`database`: `land_management_system`, `user`: `root`, `password`: `root`)

---

## 4. Danh sách API Endpoints

- Mọi endpoint dưới đây đều dùng Base URL `http://localhost:8080/api` (khi gọi qua Frontend Nginx cũng dùng prefix `/api`).
- Ngoại trừ nhóm **Public**, các API còn lại yêu cầu header:
  ```http
  Authorization: Bearer <access_token>
  ```
- JSON sử dụng chuẩn `snake_case` (ví dụ: `access_token`, `unit_id`, `status_code`).

### Hệ thống & Tài liệu API (Public)

| Method | Endpoint | Mô tả |
| --- | --- | --- |
| `GET` | `/api/actuator/health` | Kiểm tra trạng thái hoạt động của backend |
| `GET` | `/api/swagger-ui.html` | Giao diện tài liệu API Swagger UI |
| `GET` | `/api/v3/api-docs` | Đặc tả OpenAPI JSON |

### Xác thực & Tài khoản hiện tại (Auth)

| Method | Endpoint | Auth | Mô tả |
| --- | --- | --- | --- |
| `POST` | `/api/auth/login` | Public | Đăng nhập bằng `username` / `password`, trả về `access_token` |
| `GET` | `/api/auth/me` | Bearer | Lấy thông tin người dùng hiện tại |
| `GET` | `/api/me` | Bearer | Alias lấy thông tin người dùng hiện tại |
| `POST` | `/api/auth/change-password` | Bearer | Đổi mật khẩu cho tài khoản đang đăng nhập |

### Quản lý hồ sơ (Dossiers)

| Method | Endpoint | Auth | Mô tả |
| --- | --- | --- | --- |
| `POST` | `/api/dossiers` | Bearer | Tạo hồ sơ mới |
| `GET` | `/api/dossiers` | Bearer | Danh sách hồ sơ theo phân quyền đơn vị (hỗ trợ phân trang/lọc) |
| `GET` | `/api/dossiers/{dossierId}` | Bearer | Xem chi tiết hồ sơ |
| `POST` | `/api/dossiers/{dossierId}/actions` | Bearer | Thực hiện xử lý hồ sơ (`approve`, `return`, `escalate`, `submit`, ...) |
| `GET` | `/api/dossiers/inbox` | Bearer | Danh sách hồ sơ chờ xử lý của đơn vị hiện tại |
| `GET` | `/api/dossiers/central/decisions` | Bearer | Danh sách hồ sơ đã quyết định ở cấp Trung ương |
| `GET` | `/api/dossiers/{dossierId}/history` | Bearer | Xem lịch sử phê duyệt của hồ sơ |

### Tệp đính kèm (Attachments)

| Method | Endpoint | Auth | Mô tả |
| --- | --- | --- | --- |
| `POST` | `/api/dossiers/{dossierId}/attachments` | Bearer | Tải lên tệp đính kèm cho hồ sơ (`multipart/form-data`, tối đa 50MB) |
| `GET` | `/api/dossiers/{dossierId}/attachments` | Bearer | Danh sách tệp đính kèm của hồ sơ |
| `GET` | `/api/dossiers/{dossierId}/attachments/{attachmentId}/download` | Bearer | Tải xuống tệp đính kèm |
| `GET` | `/api/attachments/{attachmentId}/download` | Bearer | Tải xuống tệp đính kèm (legacy endpoint) |

### Đơn vị hành chính & Thống kê

| Method | Endpoint | Auth | Mô tả |
| --- | --- | --- | --- |
| `GET` | `/api/units/provinces` | Bearer | Danh sách đơn vị hành chính cấp Tỉnh |
| `GET` | `/api/units/{unitId}/children` | Bearer | Danh sách đơn vị hành chính trực thuộc `unitId` |
| `GET` | `/api/stats/provinces` | Bearer | Thống kê hồ sơ theo các Tỉnh |
| `GET` | `/api/stats/provinces/{provinceId}/children` | Bearer | Thống kê hồ sơ các đơn vị trực thuộc Tỉnh |

### Quản trị hệ thống (Chỉ role `ADMIN`)

| Method | Endpoint | Auth | Mô tả |
| --- | --- | --- | --- |
| `POST` | `/api/admin/users` | `ADMIN` | Tạo tài khoản người dùng mới |
| `GET` | `/api/admin/users` | `ADMIN` | Danh sách tài khoản người dùng |
| `PUT` | `/api/admin/users/{userId}` | `ADMIN` | Cập nhật thông tin/trạng thái người dùng |
| `POST` | `/api/admin/units` | `ADMIN` | Tạo đơn vị hành chính mới |
| `GET` | `/api/admin/audit-logs/dossier/{dossierId}` | `ADMIN` | Tra cứu audit log theo hồ sơ |
| `GET` | `/api/admin/audit-logs/user/{userId}` | `ADMIN` | Tra cứu audit log theo người dùng |
| `GET` | `/api/admin/audit-logs/by-action/{actionType}` | `ADMIN` | Tra cứu audit log theo loại hành động |

---

## 5. Ví dụ test nhanh API bằng `curl`

Đăng nhập với tài khoản cán bộ xã (`commune01`):

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"commune01\",\"password\":\"admin123\"}"
```

Lấy thông tin tài khoản hiện tại và danh sách hồ sơ:

```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <ACCESS_TOKEN>"

curl http://localhost:8080/api/dossiers \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

Chi tiết schema request/response xem thêm tại [`Land_Management_System/backend/API.md`](./Land_Management_System/backend/API.md) hoặc Swagger UI sau khi khởi chạy.
