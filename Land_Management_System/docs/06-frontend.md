## 06 — Frontend (React + Vite + MUI)

### 6.1 Công nghệ
- **React 18**: UI component-based
- **Vite**: build/dev nhanh
- **MUI (Material UI)**: UI kit
- **Axios**: gọi API + interceptor gắn token
- **Chart.js + react-chartjs-2**: biểu đồ thống kê
- **html2canvas + jsPDF**: export báo cáo PDF (client-side)
- **SVG map (Vietnam)**: bản đồ tương tác cho thống kê theo tỉnh

### 6.2 Cấu trúc chính
- `src/main.tsx`: Theme + `CssBaseline` + global background
- `src/App.tsx`: route + guard đăng nhập + redirect đổi mật khẩu
- `src/auth/AuthContext.tsx`: quản lý `me` + token
- `src/layout/AppShell.tsx`: layout + menu theo role
- `src/pages/*`: các màn hình nghiệp vụ

### 6.3 Role-based UI (tự hiện giao diện theo vai trò)
Menu được filter theo `me.role`, giúp user chỉ thấy chức năng phù hợp.

Ví dụ:
- `ADMIN` thấy trang “Quản trị”
- `COMMUNE_OFFICER` thấy “Tạo hồ sơ”
- `PROVINCE_OFFICER` thấy “Hồ sơ gửi lên Trung ương”
- `CENTRAL_OFFICER/PROVINCE_OFFICER` thấy “Thống kê”, “Tra cứu”

> Quan trọng: UI ẩn/hiện chỉ là UX; backend vẫn kiểm tra quyền (403) là chính.

### 6.4 Route guard & đổi mật khẩu bắt buộc
Luồng:
- Nếu không có token ⇒ redirect `/login`
- Nếu `must_change_password=true` ⇒ redirect `/change-password`

### 6.5 DossierDetail — action buttons theo inbox
Trong chi tiết hồ sơ:
- Chỉ khi `row.assigned_to_unit_id === me.unit_id` mới hiện nút thao tác.
- Action theo role:
  - Province: `approve/return/escalate`
  - Central: `approve/return`
  - Commune: `submit`

### 6.5.1 Semantics (đúng theo nghiệp vụ) cho Cấp Trung ương
- **Inbox**: chỉ để **thông báo hồ sơ cần TW duyệt** (assigned về TW).
- **Danh sách hồ sơ** (menu): chỉ hiển thị **hồ sơ đã được TW ra quyết định** (APPROVED/RETURNED).
  - Không có mục “đã gửi cấp trên” vì TW là cấp cao nhất.
  - Backend cung cấp endpoint: `GET /dossiers/central/decisions`.

### 6.6 Chữ ký (SignaturePad) dùng để làm gì?
`SignaturePad` là một canvas cho phép người xử lý ký trực tiếp bằng chuột/touch.

Luồng:
- User ký → canvas export ra PNG (`data:image/png;base64,...`)
- Frontend gửi lên backend trong `signature_base64_png`
- Backend lưu vào `approvalhistory.signature_base64_png`

Điểm hay trong MVP:
- Mỗi action có “dấu vết” trực quan (kèm note), phục vụ kiểm tra sau này.

Giới hạn MVP:
- Đây là chữ ký “hình vẽ”, chưa phải chữ ký số (PKI) có giá trị pháp lý.
- Lưu base64 trong DB có thể nặng; production nên chuyển sang lưu file/URL + hash.

### 6.7 Asset hình ảnh (logo/background)
Ảnh để trong `frontend/public/images/`.
Vì tên file có dấu/space, frontend dùng `encodeURI()` khi build URL để tránh lỗi serve tĩnh.

### 6.8 Thống kê — bản đồ tương tác + export PDF
- Bản đồ SVG Việt Nam cho phép:
  - Hover hiện tooltip theo tỉnh
  - Click để drill-down danh sách thống kê (tuỳ role)
- Nút “Xuất PDF” chụp vùng report và render ra PDF (dùng `html2canvas` + `jsPDF`).


