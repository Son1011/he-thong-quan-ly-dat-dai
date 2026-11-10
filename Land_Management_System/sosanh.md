# So sánh và checklist API backend

File này tóm tắt trạng thái backend Java hiện tại và các API frontend có thể sử dụng. Base URL local: `http://localhost:8080/api`.

## 1. Tổng quan hiện trạng

| Hạng mục | Trạng thái | Ghi chú |
| --- | --- | --- |
| Auth JWT | Đã có | `POST /auth/login`, Bearer token |
| Lấy user hiện tại | Đã có | Có cả `/auth/me` và `/me` |
| Đổi mật khẩu | Đã có | `/auth/change-password` |
| Quản lý user admin | Đã có | `/admin/users`, chỉ `ADMIN` |
| Quản lý đơn vị | Đã có | Tạo đơn vị admin, list tỉnh/xã cho frontend |
| Hồ sơ đất đai | Đã có | Tạo, list, detail, inbox, central decisions |
| Luồng duyệt hồ sơ | Đã có | `approve`, `return`, `escalate`, `submit` |
| File đính kèm | Đã có | Upload, list, download mới và legacy |
| Lịch sử xử lý | Đã có | `/dossiers/{dossierId}/history` |
| Thống kê | Đã có | Theo tỉnh và xã/phường con |
| Audit log | Đã có | `/admin/audit-logs/**`, chỉ `ADMIN` |
| Swagger/OpenAPI | Đã có | `/swagger-ui.html`, `/v3/api-docs` |
| JSON snake_case | Đã cấu hình | Frontend nên dùng `unit_id`, `access_token`, `must_change_password` |

## 2. Bảng API đầy đủ

### Auth

| Method | Path | Auth | Chức năng |
| --- | --- | --- | --- |
| `POST` | `/auth/login` | Không | Đăng nhập, nhận `access_token` |
| `GET` | `/auth/me` | Có | Thông tin user hiện tại |
| `GET` | `/me` | Có | Alias thông tin user hiện tại |
| `POST` | `/auth/change-password` | Có | Đổi mật khẩu |

### Admin Users

| Method | Path | Role | Chức năng |
| --- | --- | --- | --- |
| `POST` | `/admin/users` | `ADMIN` | Tạo tài khoản |
| `GET` | `/admin/users?q=&role=&unitId=&page=&size=` | `ADMIN` | Danh sách user, lọc và phân trang |
| `PUT` | `/admin/users/{userId}` | `ADMIN` | Cập nhật user |

### Units

| Method | Path | Role | Chức năng |
| --- | --- | --- | --- |
| `POST` | `/admin/units` | `ADMIN` | Tạo đơn vị hành chính |
| `GET` | `/units/provinces?q=` | Đăng nhập | Danh sách tỉnh/thành |
| `GET` | `/units/{unitId}/children?q=` | Đăng nhập | Danh sách đơn vị con |

### Dossiers

| Method | Path | Role | Chức năng |
| --- | --- | --- | --- |
| `POST` | `/dossiers` | `COMMUNE_OFFICER`, `PROVINCE_OFFICER` | Tạo hồ sơ |
| `GET` | `/dossiers?status=&unit_id=&include_children=&sent_to_central=&page=&size=` | Đăng nhập | Danh sách hồ sơ theo quyền |
| `GET` | `/dossiers/{dossierId}` | Đăng nhập | Chi tiết hồ sơ |
| `POST` | `/dossiers/{dossierId}/actions` | Đơn vị đang được giao | Duyệt/trả/gửi lên/gửi lại |
| `GET` | `/dossiers/inbox?page=&size=` | Đăng nhập | Hồ sơ đang giao cho đơn vị hiện tại |
| `GET` | `/dossiers/central/decisions?page=&size=` | `CENTRAL_OFFICER` | Hồ sơ/quyết định cấp trung ương |

### Attachments

| Method | Path | Role | Chức năng |
| --- | --- | --- | --- |
| `POST` | `/dossiers/{dossierId}/attachments` | Có quyền hồ sơ | Upload file, `multipart/form-data` field `file` |
| `GET` | `/dossiers/{dossierId}/attachments` | Có quyền hồ sơ | Danh sách file đính kèm |
| `GET` | `/dossiers/{dossierId}/attachments/{attachmentId}/download` | Có quyền hồ sơ | Download file theo hồ sơ |
| `GET` | `/attachments/{attachmentId}/download` | Có quyền hồ sơ | Download file route cũ |

### History, Stats, Audit

| Method | Path | Role | Chức năng |
| --- | --- | --- | --- |
| `GET` | `/dossiers/{dossierId}/history` | Có quyền hồ sơ | Lịch sử xử lý hồ sơ |
| `GET` | `/stats/provinces` | Đăng nhập | Thống kê theo tỉnh |
| `GET` | `/stats/provinces/{provinceId}/children` | Đăng nhập | Thống kê xã/phường con |
| `GET` | `/admin/audit-logs/dossier/{dossierId}?page=&size=` | `ADMIN` | Audit log theo hồ sơ |
| `GET` | `/admin/audit-logs/user/{userId}?page=&size=` | `ADMIN` | Audit log theo user |
| `GET` | `/admin/audit-logs/by-action/{actionType}?page=&size=` | `ADMIN` | Audit log theo action |

## 3. Request/response frontend cần nhớ

### Login

Request dùng camel/snake đều nên map được với Jackson, nhưng frontend nên thống nhất snake_case khi field có nhiều từ.

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

### Me

```json
{
  "id": 1,
  "username": "admin",
  "role": "ADMIN",
  "unit_id": 1,
  "must_change_password": false
}
```

### DossierResponse

```json
{
  "id": 100,
  "title": "Tên hồ sơ",
  "status": "PENDING",
  "sent_to_central": false,
  "origin_unit_id": 3,
  "created_by_user_id": 20,
  "assigned_to_unit_id": 2,
  "created_at": "2026-05-22T10:00:00",
  "updated_at": "2026-05-22T10:00:00"
}
```

### PaginatedResponse

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

## 4. Luồng frontend đề xuất

### Đăng nhập

1. Gọi `POST /auth/login`.
2. Lưu `access_token`.
3. Gắn `Authorization: Bearer <access_token>` cho request sau.
4. Gọi `GET /auth/me` để biết `role`, `unit_id`, `must_change_password`.

### Admin

1. Quản lý đơn vị: `POST /admin/units`, `GET /units/provinces`, `GET /units/{unitId}/children`.
2. Quản lý user: `POST /admin/users`, `GET /admin/users`, `PUT /admin/users/{userId}`.
3. Xem audit: `/admin/audit-logs/**`.

### Cán bộ xã/phường

1. Tạo hồ sơ: `POST /dossiers`.
2. Xem hồ sơ đơn vị mình: `GET /dossiers`.
3. Theo dõi hồ sơ bị trả: lọc `status=RETURNED`.
4. Gửi lại hồ sơ bị trả: `POST /dossiers/{id}/actions` với `action=submit`.
5. Upload/xem file: nhóm `/dossiers/{id}/attachments`.

### Cán bộ tỉnh

1. Xem inbox: `GET /dossiers/inbox`.
2. Duyệt hồ sơ xã/phường: `action=approve`.
3. Trả hồ sơ: `action=return` và bắt buộc `note`.
4. Gửi trung ương: `action=escalate`.
5. Xem thống kê: `/stats/provinces`, `/stats/provinces/{provinceId}/children`.

### Cán bộ trung ương

1. Xem inbox hoặc central decisions.
2. Duyệt hồ sơ: `action=approve`.
3. Trả hồ sơ: `action=return` và bắt buộc `note`.

## 5. Lưu ý quan trọng khi nối frontend

- Tất cả endpoint trong tài liệu phải cộng thêm prefix `/api` khi gọi local, ví dụ `/api/dossiers`.
- `page` bắt đầu từ `0`, không phải `1`.
- `GET /admin/users` dùng query `unitId` theo controller hiện tại, không phải `unit_id`.
- `GET /dossiers` dùng query `unit_id`, `include_children`, `sent_to_central`.
- Upload file phải dùng `multipart/form-data`, field tên chính xác là `file`.
- Khi `action=return`, luôn gửi `note` để tránh lỗi backend.
- Route download mới nên dùng `/dossiers/{dossierId}/attachments/{attachmentId}/download`; route `/attachments/{attachmentId}/download` chỉ để tương thích cũ.

## 6. Những phần nên bổ sung sau

| Việc nên làm | Lý do |
| --- | --- |
| Bổ sung test tự động | Hiện chưa thấy test source đầy đủ cho workflow chính |
| Chuẩn hóa query `unitId` thành `unit_id` hoặc document rõ | Tránh frontend nhầm giữa user API và dossier API |
| Bổ sung DTO chi tiết hồ sơ đầy đủ hơn | `DossierResponse` hiện chưa trả các field citizen/dossier type dù request có nhận |
| Bổ sung endpoint quản lý dossier type nếu frontend cần | `dossier_type_id` bắt buộc khi tạo hồ sơ nhưng chưa thấy API list dossier type |
| Chuẩn hóa lỗi business | Frontend dễ hiển thị lỗi thống nhất hơn |
