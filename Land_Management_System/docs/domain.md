# Domain & Workflow (MVP)

## 1) Cấp hành chính

`AdministrativeUnit`

- `level`: `CENTRAL | PROVINCE | COMMUNE`
- Cấu trúc cây: `CENTRAL -> PROVINCE -> COMMUNE`
- Mỗi user thuộc đúng **1** đơn vị.

## 2) Vai trò

`User.role`

- `ADMIN`: quản trị hệ thống (không duyệt hồ sơ)
- `COMMUNE_OFFICER`: tạo & theo dõi hồ sơ xã
- `PROVINCE_OFFICER`: duyệt hồ sơ xã; tạo hồ sơ tỉnh
- `CENTRAL_OFFICER`: duyệt hồ sơ tỉnh & hồ sơ xã gửi lên

## 3) Trạng thái hồ sơ (dùng chung)

`DossierStatus`

- `PENDING` = Chờ duyệt
- `APPROVED` = Đã duyệt
- `RETURNED` = Bị trả lại
- `ESCALATED` = Đã gửi cấp trên

## 4) Mô hình xử lý (ai cần hành động)

Để backend quyết định “inbox” (hồ sơ cần xử lý), hồ sơ có:

- `assigned_to_unit_id`: đơn vị **được phân công xử lý tiếp theo**
- `origin_unit_id`: đơn vị tạo hồ sơ (xã hoặc tỉnh)
- `created_by_user_id`

Nguyên tắc:

- Đơn vị xử lý tiếp theo luôn là **cấp cao hơn**.
- Không cấp nào duyệt hồ sơ của chính mình (enforce bằng `assigned_to_unit_id` + check vai trò).

## 5) Workflow

### 5.1) Hồ sơ do xã tạo

1. Xã tạo hồ sơ:
   - `origin_unit = COMMUNE`
   - `status = PENDING`
   - `assigned_to = PROVINCE(parent)`
2. Tỉnh xử lý:
   - **approve**: `status = APPROVED`, `assigned_to = origin_unit` (trả kết quả về xã)
   - **return**: `status = RETURNED`, `assigned_to = origin_unit`
   - **escalate**: `status = ESCALATED`, `assigned_to = CENTRAL`
3. Trung ương xử lý (khi được gửi lên):
   - **approve**: `status = APPROVED`, `assigned_to = origin_unit` *(trả kết quả về đúng đơn vị tạo hồ sơ — xã/phường)*
   - **return**: `status = RETURNED`, `assigned_to = origin_unit` *(trả kết quả về đúng đơn vị tạo hồ sơ — xã/phường)*

4. Xã bổ sung hồ sơ khi bị trả lại:
   - **submit**: `status = PENDING`, `assigned_to = PROVINCE(parent)`

### 5.2) Hồ sơ do tỉnh tạo

1. Tỉnh tạo hồ sơ:
   - `origin_unit = PROVINCE`
   - `status = ESCALATED` (gửi thẳng cấp trên)
   - `assigned_to = CENTRAL`
2. Trung ương xử lý:
   - **approve/return** về tỉnh *(vì origin_unit = PROVINCE)*

## 6) Scope dữ liệu (load theo backend)

- Cán bộ xã: thấy hồ sơ có `origin_unit_id == unit_id`
- Cán bộ tỉnh: thấy hồ sơ có `origin_unit_id` thuộc (tỉnh mình) hoặc (các xã trực thuộc)
- Cán bộ trung ương:
  - **Xem được mọi hồ sơ** (quyền giám sát toàn hệ thống)
  - **Inbox** vẫn là: `assigned_to_unit_id == unit_id` (chỉ các hồ sơ TW cần xử lý)
- Admin: dùng API quản trị, không nằm trong workflow duyệt

MVP ưu tiên:

- **Inbox**: `assigned_to_unit_id == user.unit_id`
- **My dossiers**: theo scope ở trên (origin-based)


