# CloudSafe – Cloud Backup & Disaster Recovery System

[![Java Version](https://img.shields.io/badge/Java-25-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Supported-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](#)

**CloudSafe** là hệ thống sao lưu và khôi phục dữ liệu tự động, bảo mật và đa phiên bản trên nền tảng điện toán đám mây (Cloud). Hệ thống hỗ trợ kết nối nhiều máy khách Windows/Linux (Client Agent) để sao lưu dữ liệu lên Google Cloud Storage hoặc AWS S3, bảo vệ dữ liệu trước các sự cố phần cứng hoặc mã độc tống tiền (Ransomware).

---

## 🚀 Các tính năng nổi bật (Core Features)

*   **Mã hóa Phía Máy Khách (Client-Side Encryption)**: Dữ liệu được nén và mã hóa bằng thuật toán đối xứng **AES-256** ngay tại máy người dùng trước khi tải lên Cloud. Khóa giải mã được giữ cục bộ, đảm bảo tính riêng tư tuyệt đối.
*   **Sao lưu Gia tăng (Incremental Backup)**: Tiết kiệm băng thông tối đa. Sau lần sao lưu đầu tiên (Full Backup), hệ thống chỉ sao lưu và tải lên các tệp tin mới tạo hoặc bị chỉnh sửa dựa trên dấu thời gian và mã băm **SHA-256**.
*   **Quản lý Đa Phiên bản (Multi-versioning)**: Lưu trữ lịch sử thay đổi của tệp qua các phiên bản. Cho phép khôi phục chính xác trạng thái tệp tin tại một mốc thời gian cụ thể (Point-in-Time Restore).
*   **Độc lập Nhà cung cấp Đám mây (Cloud Agnostic)**: Thiết kế với cơ chế Adapter trừu tượng hóa, dễ dàng chuyển đổi cấu hình lưu trữ giữa **Google Cloud Storage (GCS)** và **Amazon S3** thông qua file cấu hình.
*   **Gửi Email Báo cáo Tự động**: Tích hợp email thông báo tự động (Spring Mail / SendGrid API) gửi báo cáo trạng thái ngay khi phiên backup hoàn tất hoặc gặp sự cố.

---

## 👥 Hướng dẫn dành cho Người dùng cuối (Non-Developer Guide)

Nếu bạn là người dùng cuối và muốn sử dụng **CloudSafe** để bảo vệ dữ liệu trên máy tính cá nhân của mình, hãy thực hiện theo các bước cực kỳ đơn giản sau:

### 1. Cài đặt & Đăng nhập
1. Tải và cài đặt ứng dụng **CloudSafe Agent** trên máy tính của bạn (được cung cấp bởi quản trị viên hệ thống).
2. Mở ứng dụng lên, đăng ký tài khoản mới (hoặc đăng nhập bằng tài khoản email được cấp).

### 2. Thiết lập thư mục bảo vệ
1. Trên giao diện ứng dụng, bấm **"Chọn Thư mục"** để chỉ định thư mục quan trọng bạn muốn sao lưu (ví dụ: `D:\Tai_Lieu_Quan_Trong`).
2. **Đặt lịch tự động**: Chọn khung giờ bạn muốn ứng dụng tự động chạy sao lưu (ví dụ: *12:00 mỗi đêm* hoặc *hàng giờ*). 
3. Từ bây giờ, bạn có thể hoàn toàn yên tâm làm việc. Mỗi khi bạn thêm mới tài liệu hoặc chỉnh sửa file, CloudSafe sẽ tự động phát hiện và đồng bộ lên đám mây một cách âm thầm mà không làm ảnh hưởng đến hiệu năng máy tính.

### 3. Khôi phục dữ liệu khi gặp sự cố (Restore)
Nếu máy tính của bạn bị hỏng, bị nhiễm virus mã hóa, hoặc bạn lỡ tay xóa mất tệp tin quan trọng:
1. Mở ứng dụng **CloudSafe Agent** (trên máy tính cũ hoặc sau khi cài lại máy mới).
2. Đi tới tab **"Khôi phục" (Restore)**.
3. Chọn **Thiết bị** và **Phiên bản sao lưu** bạn muốn lấy lại (ví dụ bản sao lưu lúc *23:00 tối qua*).
4. Bấm **"Khôi phục ngay"**. Hệ thống sẽ tự động tải dữ liệu an toàn từ Cloud về, tự giải mã và trả lại file gốc nguyên vẹn vào máy tính của bạn.

> [!IMPORTANT]
> **Cam kết bảo mật tuyệt đối**: Dữ liệu của bạn được mã hóa bằng chìa khóa bảo mật riêng (AES-256) ngay tại máy tính của bạn trước khi gửi lên mạng. Do đó, ngay cả nhà quản lý Cloud (Google, AWS) hay quản trị viên hệ thống của chúng tôi cũng **không thể đọc được** nội dung file của bạn. Dữ liệu của bạn là của riêng bạn.

---

## 🛠️ Công nghệ sử dụng

*   **Ngôn ngữ**: Java 25
*   **Framework chính**: Spring Boot 3, Spring Security, Spring Data JPA
*   **Cơ sở dữ liệu**: PostgreSQL (hỗ trợ JSONB lưu trữ metadata động)
*   **Bảo mật**: JWT (JSON Web Tokens), BCrypt, AES-256, SHA-256
*   **Cloud Storage**: Google Cloud Storage Client / AWS S3 SDK
*   **Triển khai & Proxy**: Docker, Docker Compose, Nginx Reverse Proxy

---

## 📁 Thư mục dự án

```text
cloud-backup-system/
├── src/                    # Mã nguồn backend Spring Boot (Java)
├── nginx/                  # File cấu hình Nginx Reverse Proxy
├── infrastructure/         # File docker-compose, script khởi tạo DB, file .env
│   ├── docker-compose.yml  # File compose chính (App, DB, Nginx)
│   ├── Dockerfile          # Hướng dẫn build image cho Spring Boot app
│   ├── .env.docker         # File cấu hình môi trường mẫu
│   └── init-db.sql         # Script thiết lập quyền database Postgres
├── docs/                   # Thư mục chứa tài liệu đặc tả dự án
│   ├── erd/                # Thiết kế database chi tiết và sơ đồ ERD Mermaid
│   ├── system-design.md    # Thiết kế kiến trúc hệ thống hợp nhất
│   ├── setup-guide.md      # Hướng dẫn cài đặt chi tiết
│   └── team-playbook.md    # Sổ tay quy chuẩn code và Git cho team dev
└── README.md               # File tài liệu giới thiệu chính
```

---

## 📖 Hướng dẫn sử dụng nhanh (Quick Usage Guide)

Dưới đây là luồng nghiệp vụ thực tế khi sử dụng hệ thống thông qua các RESTful API:

### Bước 1: Đăng ký & Đăng nhập (Xác thực JWT)
Trước tiên, bạn đăng ký tài khoản và đăng nhập để nhận chuỗi JWT Token làm chìa khóa gọi các API bảo mật.
*   **API Đăng nhập**: `POST /api/auth/login`
*   **Dữ liệu gửi đi (JSON Request)**:
    ```json
    {
      "email": "user@example.com",
      "password": "SecurePassword123"
    }
    ```
*   **Dữ liệu nhận về (JSON Response)**: Nhận về `accessToken` dạng Bearer Token để gắn vào Header `Authorization: Bearer <token>` ở các request tiếp theo.

### Bước 2: Đăng ký thiết bị (Device Registration)
Tải Agent về máy khách và thực hiện đăng ký thiết bị vào hệ thống, chỉ định thư mục cục bộ cần giám sát.
*   **API Đăng ký Thiết bị**: `POST /api/devices`
*   **JSON Request**:
    ```json
    {
      "name": "Laptop-Dev-Work",
      "os": "WINDOWS",
      "watchPath": "D:/MyImportantData"
    }
    ```

### Bước 3: Chạy Job Sao lưu (Initiate Backup)
Kích hoạt tiến trình sao lưu thư mục. Hệ thống sẽ tạo một Job để ghi nhận tiến độ.
*   **API Kích hoạt**: `POST /api/backups/manual`
*   **JSON Request**:
    ```json
    {
      "deviceId": "c3b07384-d113-41c3-a3d8-5b128bb2081f",
      "backupType": "INCREMENTAL"
    }
    ```
> [!NOTE]
> Sau bước này, **Client Agent** sẽ tiến hành quét thư mục cục bộ, so sánh mã băm SHA-256 để tìm các file thay đổi, nén zip, mã hóa AES-256 rồi tải thẳng lên Cloud Storage (GCS/S3) và cập nhật báo cáo hoàn tất về Server.

### Bước 4: Khôi phục dữ liệu (Disaster Recovery)
Khi muốn lấy lại dữ liệu từ một phiên bản backup cũ do máy khách bị mất dữ liệu:
*   **API Yêu cầu Restore**: `POST /api/restore/full`
*   **JSON Request**:
    ```json
    {
      "backupVersionId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
      "destinationDeviceId": "c3b07384-d113-41c3-a3d8-5b128bb2081f"
    }
    ```
*   **Xử lý**: Server cấp quyền tải xuống (Presigned URL) các tệp mã hóa. Client Agent tải về, giải mã bằng khóa AES cục bộ và giải nén lại vào thư mục máy khách.

---

## ⚡ Khởi chạy dự án nhanh (Quick Start)

### Chạy trực tiếp trên máy local (Không dùng Docker):
1.  **Cơ sở dữ liệu**: Cài đặt PostgreSQL, tạo DB `cloudbackup` và chạy script [init-db.sql](file:///c:/Users/N07/Documents/GitHub/cloud-backup-system/infrastructure/init-db.sql) để phân quyền.
2.  **Cấu hình**: Chỉnh sửa file cấu hình kết nối DB tại `src/main/resources/application.yml`.
3.  **Khởi động**: Chạy lệnh Maven tại root:
    ```powershell
    mvn spring-boot:run -Dspring-boot.run.profiles=dev
    ```

### Chạy trọn bộ bằng Docker Compose:
1.  Điều hướng vào thư mục Docker:
    ```powershell
    cd infrastructure
    ```
2.  Copy file môi trường mẫu và cấu hình:
    ```powershell
    copy .env.docker .env
    ```
3.  Khởi chạy containers:
    ```powershell
    docker compose up -d
    ```
4.  Truy cập Swagger UI để test API tại: **`http://localhost:8080/swagger-ui.html`** (hoặc `http://localhost/swagger-ui.html` thông qua Nginx).

---

## 📚 Tài liệu chi tiết của dự án

Để tìm hiểu sâu hơn về kiến trúc và cách phát triển dự án, vui lòng đọc các tài liệu:
- 📊 **[Thiết kế Hệ thống (System Design)](file:///c:/Users/N07/Documents/GitHub/cloud-backup-system/docs/system-design.md)**: Đặc tả chi tiết kiến trúc phân tầng, luồng dữ liệu của các phiên backup/restore và ước tính chi phí Cloud.
- 🗄️ **[Thiết kế Cơ sở Dữ liệu & Sơ đồ ERD](file:///c:/Users/N07/Documents/GitHub/cloud-backup-system/docs/erd/README.md)**: Sơ đồ ERD trực quan vẽ bằng Mermaid và mô tả cấu trúc chi tiết từng bảng dữ liệu (khóa chính, khóa ngoại, kiểu dữ liệu, index).
- 🛠️ **[Hướng dẫn Cài đặt & Khởi chạy (Setup Guide)](file:///c:/Users/N07/Documents/GitHub/cloud-backup-system/docs/setup-guide.md)**: Hướng dẫn cài đặt chi tiết cho dev từ môi trường phát triển cục bộ tới production.
- 👥 **[Sổ tay Lập trình Đội ngũ (Team Playbook)](file:///c:/Users/N07/Documents/GitHub/cloud-backup-system/docs/team-playbook.md)**: Định nghĩa các quy chuẩn đặt tên, code style, xử lý exception và quy trình phối hợp làm việc trên Git.
