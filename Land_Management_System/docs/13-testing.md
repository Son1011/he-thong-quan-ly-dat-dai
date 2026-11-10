## 13 — Kế hoạch kiểm thử (đồ án)

### 13.1 Mục tiêu kiểm thử

- Xác nhận đúng nghiệp vụ 3 cấp (workflow + assigned routing)
- Xác nhận phân quyền theo role/unit
- Xác nhận upload/download file hoạt động
- Xác nhận thống kê đúng phạm vi
- Xác nhận UI thay đổi theo role và không lộ chức năng sai

---

## 13.2 Chiến lược kiểm thử

### A) Kiểm thử API (manual + curl/Postman)

- Test auth: login, me, change-password
- Test dossier: create, list, inbox, actions
- Test attachments: upload + download
- Test stats: provinces + children
- Test admin: create/list/update user

### B) Kiểm thử UI (manual)

- Login theo 4 role
- Verify menu theo role
- Verify action buttons theo inbox routing
- Verify signature capture + lưu trong lịch sử

### C) Kiểm thử tích hợp (Local)

- Chạy backend Java bằng Maven: `cd backend-java && mvn spring-boot:run`
- Chạy frontend bằng Vite: `cd frontend && npm run dev`
- Kiểm tra upload/download và dữ liệu persistent trên storage backend.

---

## 13.3 Bộ test case mẫu

### TC-AUTH-01 — Login hợp lệ

- **Input**: username/password đúng
- **Expected**: trả token; `/me` trả đúng role/unit

### TC-AUTH-02 — Login user bị khóa

- **Pre**: `is_active=false`
- **Expected**: 401

### TC-WF-01 — Xã tạo hồ sơ → tỉnh thấy trong inbox

- **Steps**
  1. Login xã
  2. POST `/dossiers`
  3. Login tỉnh
  4. GET `/dossiers/inbox`
- **Expected**
  - hồ sơ xuất hiện trong inbox tỉnh

### TC-WF-02 — Tỉnh return bắt buộc note

- **Steps**
  - POST `/dossiers/{id}/actions` action=return note=""
- **Expected**
  - 400 (bắt buộc note)

### TC-WF-03 — Tỉnh escalate → TW thấy trong inbox

- **Steps**
  1. Tỉnh escalate
  2. Login TW
  3. GET `/dossiers/inbox`
- **Expected**
  - hồ sơ xuất hiện trong inbox TW

### TC-UP-01 — Upload & download file

- **Steps**
  1. POST `/dossiers/{id}/attachments`
  2. GET `/dossiers/{id}/attachments`
  3. GET `/attachments/{att_id}/download`
- **Expected**
  - file download đúng, mở được

### TC-UI-ROLE-01 — Menu theo role

- **Expected**
  - Admin thấy trang quản trị
  - Xã thấy tạo hồ sơ
  - Tỉnh thấy sent-to-central, stats
  - TW thấy stats theo tỉnh

---

## 13.4 Dữ liệu test (seed)

Khuyến nghị dùng:

- `POST /dev/seed-vn` để có đủ tỉnh/xã/phường và user mẫu.
