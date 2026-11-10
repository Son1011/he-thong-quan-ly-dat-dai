## 08 — Deployment / Java Backend

### 8.1 Backend Java

- Backend hiện tại là `backend-java/` dùng **Spring Boot + MySQL + Flyway**.
- Khởi chạy backend bằng Maven:

```bash
cd backend-java
mvn spring-boot:run
```

Mặc định API chạy tại `http://localhost:8080`.

### 8.2 Frontend

- Frontend vẫn nằm tại `frontend/` và chạy bằng Vite.

```bash
cd frontend
npm install
npm run dev
```

Mặc định frontend chạy tại `http://localhost:5173`.

### 8.3 Triển khai hiện tại

- Docker Compose deployment đã được loại bỏ khỏi branch này.
- Nếu cần deploy thực tế, xây dựng backend bằng Maven (`mvn package`) và cấu hình frontend/tài nguyên tĩnh riêng.
