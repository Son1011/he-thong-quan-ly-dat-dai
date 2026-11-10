## 04 — API (Backend Java Spring Boot)

### Base URL

- Dev: `http://127.0.0.1:8080`

### Auth header

Hầu hết endpoint (trừ login/seed) yêu cầu:

- `Authorization: Bearer <access_token>`

---

## A) Auth

### `POST /auth/login`

Mục đích: đăng nhập, nhận JWT token.

- **Request**
  - `username: string`
  - `password: string`
- **Response**
  - `{ access_token: string, token_type: "bearer" }`
- **Lỗi**
  - 401 nếu sai user/pass hoặc user bị khóa (`is_active=false`)

### `GET /me`

Mục đích: lấy thông tin user hiện tại.

- **Response**
  - `{ id, username, role, unit_id, must_change_password }`

### `POST /auth/change-password`

Mục đích: đổi mật khẩu + tắt cờ `must_change_password`.

- **Request**
  - `old_password`
  - `new_password` (>= 6 ký tự)
  - `confirm_password`
- **Response**
  - `{ ok: true }`

---

## B) Admin

> Chỉ `ADMIN` được gọi các endpoint nhóm này.

### `POST /admin/units`

Tạo đơn vị hành chính.

- **Request**
  - `name`
  - `level: CENTRAL | PROVINCE | COMMUNE`
  - `kind?: COMMUNE | WARD` (chỉ khi level=COMMUNE)
  - `parent_id?: number` (bắt buộc nếu level != CENTRAL)
- **Response**: `{ id: number }`

### `POST /admin/users`

Tạo tài khoản.

- **Request**
  - `username`
  - `password`
  - `role`
  - `unit_id`
  - `must_change_password`
- **Response**: `{ id: number }`

### `GET /admin/users`

List user (search/filter).

- **Query**
  - `q?: string` (search username)
  - `role?: UserRole`
  - `unit_id?: number`
- **Response**: `UserRead[]`

### `PUT /admin/users/{user_id}`

Cập nhật user.

- **Body**: `role?`, `unit_id?`, `is_active?`, `must_change_password?`
- **Response**: `UserRead`

---

## C) Units

### `GET /units/provinces`

List tỉnh (Admin/Central).

- **Query**
  - `q?: string`
- **Response**: `UnitRead[]`

### `GET /units/{unit_id}/children`

List xã/phường thuộc tỉnh.

- **Query**
  - `q?: string`
- **Auth**
  - Province officer chỉ được xem tỉnh của mình
  - Admin/Central xem được mọi tỉnh

---

## D) Dossiers

### `POST /dossiers`

Tạo hồ sơ.

- **Request**
  - `{ title: string }`
- **Rule**
  - Xã tạo: `PENDING` + assigned lên Tỉnh cha
  - Tỉnh tạo: `ESCALATED` + assigned lên Trung ương + `sent_to_central=true`
  - Trung ương/Admin: bị chặn (403)
- **Response**: `DossierRead`

### `GET /dossiers`

List hồ sơ (kèm scope theo role).

- **Query**
  - `status?: DossierStatus`
  - `unit_id?: number`
  - `include_children?: boolean` (khi chọn tỉnh)
  - `sent_to_central?: boolean`
- **Rule scope (tóm tắt)**
  - Xã: chỉ origin = xã mình
  - Tỉnh: origin = tỉnh mình + các xã/phường thuộc tỉnh
  - TW: mặc định giống inbox (assigned_to=TW) nếu không chọn `unit_id` (để tránh list quá lớn)
    - Nếu muốn xem toàn bộ theo địa bàn, dùng `unit_id` (+ `include_children`)
  - Admin: all

### `GET /dossiers/central/decisions`

**(Mới)** Danh sách hồ sơ mà **thao tác cuối cùng** là **Duyệt/Trả lại bởi Trung ương**.

- **Mục đích UI**
  - Inbox = hồ sơ cần TW xử lý
  - Danh sách hồ sơ (TW) = kết quả do TW đã ra quyết định (APPROVED/RETURNED)
- **Auth**
  - `CENTRAL_OFFICER` hoặc `ADMIN`
- **Response**: `DossierRead[]`

### `GET /dossiers/inbox`

Inbox theo `assigned_to_unit_id == me.unit_id`.

### `GET /dossiers/{dossier_id}`

Chi tiết hồ sơ (có check quyền view).

### `POST /dossiers/{dossier_id}/actions`

Thao tác xử lý hồ sơ.

- **Request**
  - `action: "approve" | "return" | "escalate" | "submit"`
  - `note?: string` (return bắt buộc)
  - `signature_base64_png?: string` (data URL PNG)
- **Rule**
  - Chỉ unit đang assigned mới thao tác được (403 nếu khác)
  - Province: approve/return/escalate theo loại origin
  - Central: approve/return
  - Commune: submit (chỉ khi status=RETURNED)
- **Side effect**
  - Update dossier status + assigned_to_unit_id
  - Insert `approvalhistory`

---

## E) Attachments

### `GET /dossiers/{dossier_id}/attachments`

List file.

### `POST /dossiers/{dossier_id}/attachments`

Upload file (multipart).

- **Form field**: `file`
- **Side effect**: lưu file vào `UPLOADS_DIR`, insert `dossierattachment`

### `GET /attachments/{attachment_id}/download`

Tải file (FileResponse).

---

## F) Stats

### `GET /stats/provinces`

Thống kê theo tỉnh (Admin/Central).

### `GET /stats/provinces/{province_id}/children`

Thống kê theo xã/phường thuộc tỉnh (Province officer của tỉnh đó; hoặc Admin/Central).

---

## G) Dev utilities

> Chỉ dùng dev.

- `POST /dev/seed`
- `POST /dev/seed-vn`
- `POST /dev/cleanup-legacy-seed`
