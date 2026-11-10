# API Backend Land Management System

Tài liệu này liệt kê toàn bộ API hiện có trong backend để frontend dễ kết nối.

## 1. Thông tin chung

- Base URL local: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api/v3/api-docs`
- JSON naming: backend cấu hình Jackson `SNAKE_CASE`, nên field Java như `accessToken`, `unitId`, `mustChangePassword` sẽ ra/vào JSON là `access_token`, `unit_id`, `must_change_password`.
- Auth: hầu hết API cần header `Authorization: Bearer <access_token>`.
- Public: `POST /auth/login`, Swagger/OpenAPI, `GET /actuator/health/**`.
- Admin only: tất cả path bắt đầu bằng `/admin/**` yêu cầu role `ADMIN`.

Ví dụ gọi API sau khi đăng nhập:

```http
Authorization: Bearer eyJhbGciOi...
Content-Type: application/json
```

## 2. Enum dùng chung

### UserRole

- `ADMIN`
- `COMMUNE_OFFICER`
- `PROVINCE_OFFICER`
- `CENTRAL_OFFICER`

### UnitLevel

- `CENTRAL`
- `PROVINCE`
- `COMMUNE`

### UnitKind

- `COMMUNE`
- `WARD`

### DossierStatus

- `PENDING`
- `APPROVED`
- `RETURNED`
- `ESCALATED`

### ApprovalAction

- `CREATE`
- `APPROVE`
- `RETURN`
- `ESCALATE`
- `SUBMIT`
- `SEND_TO_CENTRAL`

## 3. Response mẫu dùng chung

### PaginatedResponse<T>

```json
{
  "data": [],
  "total": 0,
  "page": 0,
  "size": 20,
  "total_pages": 0,
  "has_next": false,
  "has_previous": false
}
```

### ApiErrorResponse

Khi lỗi validation/auth/business, backend có thể trả JSON dạng:

```json
{
  "code": "ERROR_CODE",
  "message": "Thông báo lỗi",
  "status": 400,
  "timestamp": "2026-05-22T10:00:00",
  "path": "/api/dossiers",
  "details": {},
  "trace_id": "..."
}
```

## 4. Auth APIs

| Method | Path | Auth | Chức năng |
| --- | --- | --- | --- |
| `POST` | `/auth/login` | Không | Đăng nhập và nhận JWT |
| `GET` | `/auth/me` | Có | Lấy thông tin user hiện tại |
| `POST` | `/auth/change-password` | Có | Đổi mật khẩu user hiện tại |
| `GET` | `/me` | Có | Alias lấy thông tin user hiện tại |

### POST /auth/login

Request:

```json
{
  "username": "admin",
  "password": "123456"
}
```

Response:

```json
{
  "access_token": "jwt-token",
  "token_type": "Bearer"
}
```

Frontend lưu `access_token` và gửi trong header `Authorization` cho các request sau.

### GET /auth/me

Response:

```json
{
  "id": 1,
  "username": "admin",
  "role": "ADMIN",
  "unit_id": 1,
  "must_change_password": false
}
```

### GET /me

Response giống `GET /auth/me`. Endpoint này tồn tại để frontend có thể gọi ngắn hơn.

### POST /auth/change-password

Request:

```json
{
  "old_password": "123456",
  "new_password": "new-password",
  "confirm_password": "new-password"
}
```

Response:

```json
{
  "ok": true
}
```

Lưu ý: `new_password` tối thiểu 6 ký tự và phải bằng `confirm_password`.

## 5. User Admin APIs

Base path: `/admin/users`. Tất cả API trong nhóm này chỉ dành cho `ADMIN`.

| Method | Path | Query/Body | Chức năng |
| --- | --- | --- | --- |
| `POST` | `/admin/users` | JSON body | Tạo tài khoản user |
| `GET` | `/admin/users` | `q`, `role`, `unitId`, `page`, `size` | Danh sách user có phân trang/lọc |
| `PUT` | `/admin/users/{userId}` | JSON body | Cập nhật user |

### POST /admin/users

Request:

```json
{
  "username": "province01",
  "password": "123456",
  "full_name": "Cán bộ tỉnh",
  "email": "province01@example.com",
  "phone_number": "0900000000",
  "address": "Địa chỉ",
  "role": "PROVINCE_OFFICER",
  "unit_id": 2,
  "must_change_password": true
}
```

Response:

```json
{
  "id": 10
}
```

### GET /admin/users

Query params:

- `q`: tìm kiếm theo text, optional.
- `role`: `ADMIN`, `COMMUNE_OFFICER`, `PROVINCE_OFFICER`, `CENTRAL_OFFICER`, optional.
- `unitId`: lọc theo đơn vị, optional. Lưu ý param này đang là camelCase theo code controller.
- `page`: mặc định `0`.
- `size`: mặc định `20`, tối đa xử lý theo `PaginationRequest` là `100`.

Response: `PaginatedResponse<UserResponse>`.

```json
{
  "data": [
    {
      "id": 10,
      "username": "province01",
      "role": "PROVINCE_OFFICER",
      "unit_id": 2,
      "is_active": true,
      "must_change_password": true
    }
  ],
  "total": 1,
  "page": 0,
  "size": 20,
  "total_pages": 1,
  "has_next": false,
  "has_previous": false
}
```

### PUT /admin/users/{userId}

Request: field nào cần đổi thì gửi field đó.

```json
{
  "full_name": "Tên mới",
  "email": "new@example.com",
  "phone_number": "0911111111",
  "address": "Địa chỉ mới",
  "role": "COMMUNE_OFFICER",
  "unit_id": 3,
  "is_active": true,
  "must_change_password": false
}
```

Response: `UserResponse`.

## 6. Unit APIs

| Method | Path | Auth | Chức năng |
| --- | --- | --- | --- |
| `POST` | `/admin/units` | `ADMIN` | Tạo đơn vị hành chính |
| `GET` | `/units/provinces` | Có | Lấy danh sách tỉnh/thành |
| `GET` | `/units/{unitId}/children` | Có | Lấy danh sách xã/phường con của một đơn vị |

### POST /admin/units

Request:

```json
{
  "name": "Tỉnh A",
  "level": "PROVINCE",
  "kind": null,
  "parent_id": 1
}
```

Với xã/phường:

```json
{
  "name": "Phường B",
  "level": "COMMUNE",
  "kind": "WARD",
  "parent_id": 2
}
```

Response:

```json
{
  "id": 5
}
```

### GET /units/provinces

Query params:

- `q`: tìm kiếm theo tên, optional.

Response:

```json
[
  {
    "id": 2,
    "name": "Tỉnh A",
    "level": "PROVINCE",
    "kind": null,
    "parent_id": 1
  }
]
```

### GET /units/{unitId}/children

Query params:

- `q`: tìm kiếm theo tên, optional.

Response: `UnitResponse[]`.

## 7. Dossier APIs

Base path: `/dossiers`. Các API hồ sơ đều cần đăng nhập.

| Method | Path | Chức năng |
| --- | --- | --- |
| `POST` | `/dossiers` | Tạo hồ sơ đất đai |
| `GET` | `/dossiers` | Danh sách hồ sơ có lọc/phân trang |
| `GET` | `/dossiers/{dossierId}` | Xem chi tiết hồ sơ |
| `POST` | `/dossiers/{dossierId}/actions` | Duyệt/trả/escalate/gửi lại hồ sơ |
| `GET` | `/dossiers/inbox` | Hồ sơ đang được giao cho đơn vị hiện tại |
| `GET` | `/dossiers/central/decisions` | Danh sách quyết định cấp trung ương |

### POST /dossiers

Request:

```json
{
  "title": "Hồ sơ cấp giấy chứng nhận quyền sử dụng đất",
  "citizen_name": "Nguyễn Văn A",
  "citizen_identity_number": "012345678901",
  "citizen_phone": "0900000000",
  "citizen_address": "Địa chỉ công dân",
  "dossier_type_id": 1,
  "priority": "NORMAL"
}
```

Response: `DossierResponse`.

```json
{
  "id": 100,
  "title": "Hồ sơ cấp giấy chứng nhận quyền sử dụng đất",
  "status": "PENDING",
  "sent_to_central": false,
  "origin_unit_id": 3,
  "created_by_user_id": 20,
  "assigned_to_unit_id": 2,
  "created_at": "2026-05-22T10:00:00",
  "updated_at": "2026-05-22T10:00:00"
}
```

Rule nghiệp vụ:

- `COMMUNE_OFFICER`: tạo hồ sơ từ xã/phường, hồ sơ `PENDING`, giao lên đơn vị tỉnh cha.
- `PROVINCE_OFFICER`: tạo hồ sơ từ tỉnh, hồ sơ `ESCALATED`, gửi lên trung ương.
- `CENTRAL_OFFICER` và `ADMIN`: không được tạo hồ sơ.

### GET /dossiers

Query params:

- `status`: `PENDING`, `APPROVED`, `RETURNED`, `ESCALATED`, optional.
- `unit_id`: lọc theo đơn vị tạo hồ sơ, optional.
- `include_children`: `true/false`, mặc định `false`; hữu ích cho tỉnh xem cả xã/phường con.
- `sent_to_central`: `true/false`, optional.
- `page`: mặc định `0`.
- `size`: mặc định `20`.

Response: `PaginatedResponse<DossierResponse>`.

Quyền xem:

- `COMMUNE_OFFICER`: chỉ xem hồ sơ của đơn vị mình.
- `PROVINCE_OFFICER`: xem hồ sơ của tỉnh mình và xã/phường con.
- `CENTRAL_OFFICER`: xem tất cả hồ sơ.
- `ADMIN`: xem tất cả hồ sơ.

### GET /dossiers/{dossierId}

Response: `DossierResponse`.

Backend kiểm tra quyền xem theo role và đơn vị trước khi trả dữ liệu.

### POST /dossiers/{dossierId}/actions

Request:

```json
{
  "action": "approve",
  "note": "Đã kiểm tra hợp lệ",
  "signature_base64_png": "data:image/png;base64,..."
}
```

Các `action` frontend gửi dạng chữ thường:

- `approve`: phê duyệt.
- `return`: trả lại; bắt buộc có `note`.
- `escalate`: gửi lên cấp cao hơn/trung ương.
- `submit`: xã/phường gửi lại hồ sơ đã bị trả.

Response: `DossierResponse` sau khi cập nhật.

Rule xử lý:

- Chỉ đơn vị đang được giao tại `assigned_to_unit_id` mới được thao tác.
- `COMMUNE_OFFICER`: chỉ được `submit` hồ sơ có trạng thái `RETURNED`.
- `PROVINCE_OFFICER`: với hồ sơ từ xã/phường con được `approve`, `return`, `escalate`; với hồ sơ do tỉnh tạo thì dùng `escalate` để gửi lại trung ương sau khi bị trả.
- `CENTRAL_OFFICER`: chỉ được `approve` hoặc `return`.
- `ADMIN`: không tham gia thao tác hồ sơ.

### GET /dossiers/inbox

Query params:

- `page`: mặc định `0`.
- `size`: mặc định `20`.

Response: `PaginatedResponse<DossierResponse>`.

Chức năng: lấy hồ sơ đang nằm ở `assigned_to_unit_id` của user hiện tại.

### GET /dossiers/central/decisions

Query params:

- `page`: mặc định `0`.
- `size`: mặc định `20`.

Response: `PaginatedResponse<DossierResponse>`.

Rule: chỉ `CENTRAL_OFFICER` được gọi.

## 8. Attachment APIs

| Method | Path | Chức năng |
| --- | --- | --- |
| `POST` | `/dossiers/{dossierId}/attachments` | Upload file vào hồ sơ |
| `GET` | `/dossiers/{dossierId}/attachments` | Danh sách file của hồ sơ |
| `GET` | `/dossiers/{dossierId}/attachments/{attachmentId}/download` | Download file theo hồ sơ |
| `GET` | `/attachments/{attachmentId}/download` | Download file route cũ/legacy |

### POST /dossiers/{dossierId}/attachments

Content-Type: `multipart/form-data`.

Form-data:

- `file`: file cần upload.

Response:

```json
{
  "id": 50,
  "dossier_id": 100,
  "original_filename": "giay-to.pdf",
  "content_type": "application/pdf",
  "uploaded_by_user_id": 20,
  "uploaded_at": "2026-05-22T10:10:00"
}
```

Lưu ý cấu hình upload:

- Thư mục: `./data/uploads`.
- Max file size app config: `52428800` bytes.
- MIME type cho phép: `application/pdf`, `image/jpeg`, `image/png`, `text/plain`.

### GET /dossiers/{dossierId}/attachments

Response: `AttachmentResponse[]`.

Backend kiểm tra user có quyền xem hồ sơ trước khi trả danh sách file.

### GET /dossiers/{dossierId}/attachments/{attachmentId}/download

Response: binary file với header:

- `Content-Disposition: attachment; filename="..."`
- `Content-Type: <content_type>`

Backend kiểm tra quyền xem hồ sơ và attachment phải thuộc đúng `dossierId`.

### GET /attachments/{attachmentId}/download

Route legacy để tương thích frontend cũ. Backend tự tìm hồ sơ của attachment rồi kiểm tra quyền xem.

## 9. Approval History APIs

| Method | Path | Chức năng |
| --- | --- | --- |
| `GET` | `/dossiers/{dossierId}/history` | Lịch sử xử lý hồ sơ |

### GET /dossiers/{dossierId}/history

Response:

```json
[
  {
    "id": 1,
    "dossier_id": 100,
    "action": "CREATE",
    "note": null,
    "signature_base64_png": null,
    "actor_user_id": 20,
    "actor_username": "commune01",
    "actor_unit_id": 3,
    "actor_unit_name": "Phường B",
    "from_unit_id": null,
    "to_unit_id": 2,
    "created_at": "2026-05-22T10:00:00"
  }
]
```

Backend kiểm tra quyền xem hồ sơ trước khi trả lịch sử.

## 10. Statistics APIs

| Method | Path | Chức năng |
| --- | --- | --- |
| `GET` | `/stats/provinces` | Thống kê theo tỉnh/thành |
| `GET` | `/stats/provinces/{provinceId}/children` | Thống kê các xã/phường con của tỉnh |

### GET /stats/provinces

Response:

```json
[
  {
    "unit_id": 2,
    "name": "Tỉnh A",
    "kind": null,
    "total_dossiers": 10,
    "pending": 3,
    "approved": 5,
    "returned": 2,
    "total_attachments": 8
  }
]
```

### GET /stats/provinces/{provinceId}/children

Response: `StatisticsResponse[]` cho từng xã/phường con.

## 11. Audit Log Admin APIs

Base path: `/admin/audit-logs`. Tất cả API trong nhóm này chỉ dành cho `ADMIN`.

| Method | Path | Query | Chức năng |
| --- | --- | --- | --- |
| `GET` | `/admin/audit-logs/dossier/{dossierId}` | `page`, `size` | Audit log theo hồ sơ |
| `GET` | `/admin/audit-logs/user/{userId}` | `page`, `size` | Audit log theo user |
| `GET` | `/admin/audit-logs/by-action/{actionType}` | `page`, `size` | Audit log theo loại hành động |

Response: `PaginatedResponse<AuditLog>`.

Query mặc định:

- `page`: `0`.
- `size`: `20`.

## 12. Actuator và docs

| Method | Path | Auth | Chức năng |
| --- | --- | --- | --- |
| `GET` | `/actuator/health/**` | Không | Health check |
| Any | `/actuator/**` | `ADMIN` | Metrics/info/actuator khác |
| `GET` | `/swagger-ui.html` | Không | Swagger UI |
| `GET` | `/v3/api-docs/**` | Không | OpenAPI docs |

## 13. Gợi ý kết nối frontend

1. Login bằng `POST /auth/login`.
2. Lưu `access_token`.
3. Tạo HTTP client tự thêm `Authorization: Bearer <token>`.
4. Gọi `GET /auth/me` hoặc `GET /me` để lấy role và `unit_id`.
5. Điều hướng UI theo role:
   - `ADMIN`: quản lý users, units, audit logs.
   - `COMMUNE_OFFICER`: tạo hồ sơ, xem hồ sơ đơn vị mình, gửi lại hồ sơ bị trả.
   - `PROVINCE_OFFICER`: xử lý inbox cấp tỉnh, duyệt/trả/gửi trung ương.
   - `CENTRAL_OFFICER`: xử lý hồ sơ trung ương, approve/return.
6. Với list API, dùng `page` bắt đầu từ `0` và đọc `total_pages`, `has_next` để phân trang.
