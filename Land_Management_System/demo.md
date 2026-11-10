Bước 1: Vào thư mục backend
cd "C:\Users\Bui Chien\OneDrive\Desktop\Land_Management_System\Land_Management_System\backend"
Bước 2: Set JAVA_HOME
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.11"
Bước 3: Thêm Java vào Path
$env:Path="$env:JAVA_HOME\bin;$env:Path"
Bước 4: Kiểm tra Maven
mvn -version

Nếu thấy:

Java version: 21.0.11

là đúng.

Bước 5: Chạy backend
mvn spring-boot:run

admin (tk:admin mk:admin1234)
tk cấp tỉnh (tk: province01 mk:admin123)
tk cấp trung ương (tk: central01 mk:admin123)
yk cấp phường/xã (tk: commune01 mk:admin123)
