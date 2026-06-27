# Hướng dẫn Cài đặt & Chạy dự án (Setup Guide)

Tài liệu này hướng dẫn chi tiết cách thiết lập môi trường phát triển cục bộ (local development) và chạy thử nghiệm hệ thống CloudSafe.

---

## 1. Yêu cầu Hệ thống & Công cụ (Prerequisites)

Trước khi bắt đầu, hãy đảm bảo máy tính của bạn đã cài đặt các công cụ sau:
- **Java Development Kit (JDK) 25**: Phiên bản JDK khuyên dùng là [Eclipse Temurin JDK 25](https://adoptium.net/).
- **Apache Maven 3.9+**: Công cụ quản lý dự án và build cho Java.
- **Docker & Docker Compose**: Để build và chạy hệ thống thông qua containers (Docker Desktop cho Windows/Mac).
- **Git**: Công cụ quản lý mã nguồn.
- **PostgreSQL Client (PgAdmin hoặc DBeaver)**: Để quản lý cơ sở dữ liệu cục bộ.

---

## 2. Thiết lập Môi trường Cục bộ (Local Development)

### Bước 2.1: Clone dự án
Mở terminal (PowerShell hoặc Command Prompt trên Windows) và thực hiện clone mã nguồn:
```bash
git clone <repository-url>
cd cloud-backup-system
```

### Bước 2.2: Thiết lập Cơ sở dữ liệu PostgreSQL cục bộ
1. Khởi động PostgreSQL cục bộ trên máy tính của bạn.
2. Tạo một cơ sở dữ liệu mới và người dùng tương tác:
   ```sql
   CREATE DATABASE cloudbackup;
   CREATE USER cloudbackup WITH PASSWORD 'cloudbackup123';
   GRANT ALL PRIVILEGES ON DATABASE cloudbackup TO cloudbackup;
   ```
3. Chạy script thiết lập quyền mặc định (nếu cần):
   - Mở client SQL của bạn (ví dụ: DBeaver), kết nối tới DB và chạy nội dung trong file [init-db.sql](file:///c:/Users/N07/Documents/GitHub/cloud-backup-system/infrastructure/init-db.sql) để cấu hình mở rộng UUID.

### Bước 2.3: Cấu hình ứng dụng Spring Boot
1. Đi tới thư mục cấu hình: `src/main/resources/`.
2. Mở file `application.yml` hoặc `application-dev.yml` (nếu có) để tùy chỉnh kết nối PostgreSQL cục bộ của bạn:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/cloudbackup
       username: cloudbackup
       password: cloudbackup123
   ```
3. Kiểm tra biến cấu hình nhà cung cấp lưu trữ Cloud Storage (`storage.provider` là `gcp` hoặc `aws`) và chuẩn bị thông tin tài khoản Cloud nếu bạn muốn test tính năng upload thực tế.

---

## 3. Khởi chạy Ứng dụng

### Cách 3.1: Chạy trực tiếp qua Maven (Không dùng Docker)
Phục vụ cho việc sửa đổi mã nguồn liên tục và debug trong IDE (IntelliJ IDEA, VS Code):
1. Chạy lệnh compile và khởi động từ thư mục gốc của project (nơi chứa file `pom.xml`):
   ```powershell
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```
2. Khi server khởi chạy thành công, log console sẽ xuất hiện dòng:
   `Started CloudBackupApplication in X seconds...`
3. Ứng dụng sẽ hoạt động tại địa chỉ: `http://localhost:8080`

### Cách 3.2: Chạy toàn bộ hệ thống bằng Docker Compose
Dành cho việc kiểm thử tích hợp môi trường hoàn chỉnh (bao gồm ứng dụng API, DB Postgres và Nginx Reverse Proxy):
1. Điều hướng vào thư mục chứa các file Docker:
   ```powershell
   cd infrastructure
   ```
2. Tạo file cấu hình môi trường `.env` từ file mẫu:
   ```powershell
   copy .env.docker .env
   ```
3. Khởi chạy Docker Compose ở chế độ chạy ngầm (detached):
   ```powershell
   docker compose up -d
   ```
4. Hệ thống sẽ tự động tải các image cần thiết, compile code Java bên trong container và khởi tạo dịch vụ.
5. Sau khi khởi chạy, bạn có thể truy cập qua Nginx Reverse Proxy ở cổng `80` mặc định:
   - API Server: `http://localhost/` hoặc `http://localhost:8080/`
   - Kiểm tra log ứng dụng: `docker compose logs -f cloud-backup-app`
   - Dừng hệ thống: `docker compose down`

---

## 4. Kiểm tra API qua Swagger UI

Khi ứng dụng Java đã chạy, bạn có thể kiểm tra danh sách API và test gửi nhận request thông qua giao diện tương tác Swagger UI:
- Đường dẫn truy cập: **`http://localhost:8080/swagger-ui.html`** hoặc qua cổng Nginx `http://localhost/swagger-ui.html` (khi chạy Docker).
- Tài liệu OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## 5. Khắc phục Sự cố Thường gặp (Troubleshooting)

- **Lỗi cổng 8080 hoặc 5432 bị chiếm dụng**: 
  - Hãy kiểm tra xem bạn có dịch vụ PostgreSQL hoặc Tomcat nào đang chạy sẵn trên máy. Cấu hình lại `APP_PORT` hoặc `DB_PORT` trong file `.env` (nếu chạy Docker) hoặc file `application.yml` (nếu chạy cục bộ).
- **Lỗi Maven không tìm thấy dependencies**:
  - Đảm bảo kết nối mạng ổn định và chạy lệnh `mvn clean install -U` để tải lại các thư viện Maven bị lỗi.
- **Docker Compose build bị đứng ở Maven Stage**:
  - Lần chạy đầu tiên có thể mất từ 3-5 phút do Maven cần tải toàn bộ thư viện cần thiết về local repository trong container. Điều này sẽ được cải thiện đáng kể ở các lần chạy sau nhờ cơ chế caching của Docker.
