## 03 — User stories (theo vai trò)

### Quy ước
- **US-XXX-YY**: mã user story
- **AC**: Acceptance Criteria

---

## A) ADMIN

### US-ADM-01 — Tạo user theo đúng cấp
Là admin, tôi muốn tạo user cán bộ theo đúng cấp (TW/Tỉnh/Xã-Phường) để phân quyền chính xác.

- **AC**
  - Không tạo được `PROVINCE_OFFICER` cho unit không phải `PROVINCE`
  - Không tạo được `COMMUNE_OFFICER` cho unit không phải `COMMUNE`
  - Không tạo được `CENTRAL_OFFICER` cho unit không phải `CENTRAL`

### US-ADM-02 — Khóa/mở user
Là admin, tôi muốn khóa user để ngăn đăng nhập khi cần.

- **AC**
  - `is_active=false` ⇒ login trả 401

### US-ADM-03 — Bắt đổi mật khẩu sau cấp tài khoản
Là admin, tôi muốn bật `must_change_password` để user phải đổi mật khẩu ở lần login đầu.

- **AC**
  - Sau khi user đổi mật khẩu thành công ⇒ `must_change_password=false`

---

## B) COMMUNE_OFFICER (Xã/Phường)

### US-COM-01 — Tạo hồ sơ và tự động chuyển tuyến
Là cán bộ xã/phường, tôi muốn tạo hồ sơ và hệ thống tự chuyển hồ sơ lên tỉnh để duyệt.

- **AC**
  - Tạo xong hồ sơ có `status=PENDING`
  - `assigned_to_unit_id` là tỉnh cha (parent unit)

### US-COM-02 — Xem danh sách hồ sơ của đơn vị mình
Là cán bộ xã/phường, tôi muốn xem danh sách và chi tiết các hồ sơ do đơn vị mình tạo.

- **AC**
  - Không thấy hồ sơ của đơn vị khác

### US-COM-03 — Nộp lại hồ sơ sau khi bị trả
Là cán bộ xã/phường, khi hồ sơ bị trả về, tôi muốn bổ sung và gửi lại lên tỉnh.

- **AC**
  - Chỉ hồ sơ `RETURNED` mới “submit” được
  - Submit xong `status=PENDING` và `assigned_to_unit_id` quay lại tỉnh cha

### US-COM-04 — Upload tài liệu
Là cán bộ xã/phường, tôi muốn đính kèm file (scan giấy tờ) vào hồ sơ.

- **AC**
  - Upload thành công tạo bản ghi `dossierattachment`
  - Có thể tải về đúng file đã upload

---

## C) PROVINCE_OFFICER (Tỉnh)

### US-PRO-01 — Inbox hồ sơ cần xử lý
Là cán bộ tỉnh, tôi muốn có inbox để thấy các hồ sơ đang chờ tỉnh xử lý.

- **AC**
  - Inbox = `assigned_to_unit_id == province_id`

### US-PRO-02 — Duyệt (approve) hồ sơ xã/phường
Là cán bộ tỉnh, tôi muốn duyệt hồ sơ và trả kết quả về xã/phường.

- **AC**
  - Approve ⇒ `status=APPROVED`, `assigned_to_unit_id=origin_unit_id`
  - Có lưu lịch sử `approvalhistory(action=approve)`

### US-PRO-03 — Trả (return) hồ sơ xã/phường
Là cán bộ tỉnh, tôi muốn trả hồ sơ về xã/phường và ghi rõ lý do.

- **AC**
  - Return bắt buộc có `note`
  - Return ⇒ `status=RETURNED`, `assigned_to_unit_id=origin_unit_id`

### US-PRO-04 — Gửi Trung ương (escalate)
Là cán bộ tỉnh, tôi muốn gửi hồ sơ lên Trung ương khi cần cấp trên quyết định.

- **AC**
  - Escalate ⇒ `status=ESCALATED`, `assigned_to_unit_id=CENTRAL`, `sent_to_central=true`

### US-PRO-05 — Tạo hồ sơ cấp tỉnh
Là cán bộ tỉnh, tôi muốn tạo hồ sơ cấp tỉnh và gửi thẳng lên Trung ương.

- **AC**
  - Tạo xong hồ sơ có `status=ESCALATED`, `assigned_to_unit_id=CENTRAL`, `sent_to_central=true`

### US-PRO-06 — Theo dõi hồ sơ đã gửi TW
Là cán bộ tỉnh, tôi muốn có danh sách các hồ sơ tỉnh đã gửi Trung ương.

- **AC**
  - Filter theo `sent_to_central=true` + scope theo tỉnh

### US-PRO-07 — Thống kê theo xã/phường
Là cán bộ tỉnh, tôi muốn xem thống kê số lượng hồ sơ theo từng xã/phường trực thuộc.

---

## D) CENTRAL_OFFICER (Trung ương)

### US-CEN-01 — Inbox hồ sơ cần xử lý
Là cán bộ Trung ương, tôi muốn thấy các hồ sơ đang chờ TW xử lý.

- **AC**
  - Inbox = `assigned_to_unit_id == CENTRAL`

### US-CEN-02 — Duyệt/trả hồ sơ (quyết định cuối)
Là cán bộ Trung ương, tôi muốn duyệt hoặc trả hồ sơ và **kết quả trả về đúng đơn vị tạo hồ sơ**.

- **AC**
  - Approve ⇒ `status=APPROVED`, `assigned_to_unit_id=origin_unit_id`
  - Return ⇒ `status=RETURNED`, `assigned_to_unit_id=origin_unit_id`

### US-CEN-03 — Danh sách hồ sơ (kết quả do TW quyết)
Là cán bộ Trung ương, tôi muốn có danh sách các hồ sơ mà thao tác cuối cùng là **Duyệt/Trả lại bởi TW** để tra cứu kết quả đã ban hành.

- **AC**
  - API trả về các dossier có latest action thuộc `{approve, return}` bởi user role `CENTRAL_OFFICER`
  - UI chỉ filter `APPROVED/RETURNED` (không có “đã gửi cấp trên” vì TW là cấp cao nhất)

### US-CEN-04 — Tra cứu toàn hệ thống theo tỉnh/xã
Là cán bộ Trung ương, tôi muốn xem toàn bộ hồ sơ theo tỉnh/xã (kể cả chưa duyệt) để giám sát tình hình.

- **AC**
  - Chọn tỉnh ⇒ xem hồ sơ của tỉnh + các xã/phường trực thuộc
  - Chọn xã/phường ⇒ xem hồ sơ của xã/phường đó

### US-CEN-05 — Thống kê theo tỉnh
Là cán bộ Trung ương, tôi muốn xem thống kê theo tỉnh (tổng hồ sơ/đính kèm…).


