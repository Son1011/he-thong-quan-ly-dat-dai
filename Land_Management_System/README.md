# Land Management System (Quản lý hồ sơ đất đai) — MVP

Repo này đang được scaffold theo mô hình **hành chính 3 cấp**:

- **Trung ương** (chỉ duyệt)
- **Tỉnh** (duyệt hồ sơ xã, tạo hồ sơ tỉnh)
- **Xã** (tạo và theo dõi hồ sơ xã)

MVP hiện tại tập trung vào **backend-java**:

- Auth (JWT)
- Phân quyền theo **vai trò + đơn vị hành chính**
- Workflow duyệt hồ sơ theo cấp trên trực tiếp
- Trạng thái hồ sơ thống nhất: **Chờ duyệt / Đã duyệt / Bị trả lại / Đã gửi cấp trên**

## Chạy backend Java (Windows / PowerShell)

```bash
cd backend-java
mvn spring-boot:run
```

Mặc định API chạy tại `http://127.0.0.1:8080`.

## Chạy frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend mặc định chạy tại `http://127.0.0.1:5173`.

> Lưu ý: Docker Compose deployment đã bị loại bỏ trong phiên bản backend Java hiện tại. Nếu bạn cần triển khai trên môi trường khác, dùng `mvn package` để xây dựng backend và cấu hình frontend độc lập.

## Seed dữ liệu mẫu (dev)

Gọi endpoint seed (chỉ dùng dev) để tạo:

- 01 đơn vị **Trung ương**
- 01 đơn vị **Tỉnh A**
- 01 đơn vị **Xã A1** (thuộc Tỉnh A)
- 04 user: admin / cán bộ trung ương / cán bộ tỉnh / cán bộ xã

```bash
curl -X POST http://127.0.0.1:8000/dev/seed
```

## Luồng test nhanh (curl)

### 1) Login xã → tạo hồ sơ

```bash
curl -X POST http://127.0.0.1:8000/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"commune\",\"password\":\"commune\"}"
```

Copy `access_token`, rồi:

```bash
curl -X POST http://127.0.0.1:8000/dossiers ^
  -H "Authorization: Bearer <TOKEN>" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Hồ sơ xã A1 - thửa 123\"}"
```

### 2) Login tỉnh → xem hồ sơ xã cần duyệt → duyệt hoặc gửi trung ương

```bash
curl -X POST http://127.0.0.1:8000/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"province\",\"password\":\"province\"}"
```

```bash
curl http://127.0.0.1:8000/dossiers/inbox ^
  -H "Authorization: Bearer <TOKEN>"
```

Approve (trả kết quả về xã):

```bash
curl -X POST http://127.0.0.1:8000/dossiers/<ID>/actions ^
  -H "Authorization: Bearer <TOKEN>" ^
  -H "Content-Type: application/json" ^
  -d "{\"action\":\"approve\",\"note\":\"Đủ điều kiện\"}"
```

Escalate (gửi trung ương duyệt):

```bash
curl -X POST http://127.0.0.1:8000/dossiers/<ID>/actions ^
  -H "Authorization: Bearer <TOKEN>" ^
  -H "Content-Type: application/json" ^
  -d "{\"action\":\"escalate\",\"note\":\"Cần TW xem xét\"}"
```

### 2b) Nếu xã bị trả lại → xã bổ sung và gửi lại tỉnh

```bash
curl -X POST http://127.0.0.1:8000/dossiers/<ID>/actions ^
  -H "Authorization: Bearer <TOKEN_XA>" ^
  -H "Content-Type: application/json" ^
  -d "{\"action\":\"submit\",\"note\":\"Đã bổ sung\"}"
```

### 3) Login trung ương → duyệt hồ sơ được gửi lên

```bash
curl -X POST http://127.0.0.1:8000/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"central\",\"password\":\"central\"}"
```

```bash
curl http://127.0.0.1:8000/dossiers/inbox ^
  -H "Authorization: Bearer <TOKEN>"
```

Return về tỉnh:

```bash
curl -X POST http://127.0.0.1:8000/dossiers/<ID>/actions ^
  -H "Authorization: Bearer <TOKEN>" ^
  -H "Content-Type: application/json" ^
  -d "{\"action\":\"return\",\"note\":\"Thiếu tài liệu\"}"
```

## Tài liệu

- `docs/domain.md`: entity + workflow + quy tắc scope dữ liệu
- `docs/system-spec.md`: tài liệu đặc tả chi tiết (database, user story, chức năng, API, Mermaid diagrams, công nghệ)
- `docs/index.md`: mục lục tài liệu (bản tách nhỏ theo nhiều file)
