# Hướng dẫn chạy project Land Management System

## 1. Mở terminal

Project gồm:
- backend = Spring Boot
- frontend = React Vite
- database = MySQL Docker

---

# 2. Chạy MySQL

## Kiểm tra container

```bash
docker ps
```
docker start mysql_land


Nếu chưa có mysql_land:

```bash
docker run -d \
  --name mysql_land \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=land_management_system \
  -p 3306:3306 \
  mysql:8
```

---

# 3. Import SQL

Đứng ở thư mục root:

```bash
cd /workspaces/he-thong-quan-l-dat-dai
```

Import:docker exec -it mysql_land mysql -uroot -proot

```bash
docker exec -i mysql_land mysql -uroot -proot land_management_system < land_management_system.sql
```

---

# 4. Cài Java 21

```bash
sudo apt update
sudo apt install openjdk-21-jdk -y
```

Set Java 21:

```bash
sudo update-alternatives --config java
```

Chọn dòng java-21.

Kiểm tra:

```bash
java -version
```
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

sdk install java 21.0.2-tem
sdk use java 21.0.2-tem
Phải ra:

```bash
openjdk version "21"
```

---

# 5. Chạy Backend

Mở terminal mới:

```bash
cd /workspaces/he-thong-quan-l-dat-dai/Land_Management_System/backend
mvn spring-boot:run
```

Backend chạy:
- http://localhost:8080

---

# 6. Chạy Frontend

Mở terminal mới:

```bash
cd /workspaces/he-thong-quan-l-dat-dai/Land_Management_System/frontend
npm install
npm run dev -- --host
```

Frontend chạy:
- http://localhost:5173

---

# 7. Tài khoản đăng nhập

 / admin1234

province01 / admin123

central01 / admin123

commune01 / admin123