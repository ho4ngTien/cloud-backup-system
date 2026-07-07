# CloudSafe – Cloud Backup & Disaster Recovery System

**CloudSafe** là hệ thống sao lưu và khôi phục dữ liệu tự động, bảo mật và đa phiên bản trên nền tảng điện toán đám mây (Cloud). Hệ thống hỗ trợ kết nối nhiều máy khách Windows/Linux (Client Agent) để sao lưu dữ liệu lên Google Cloud Storage hoặc AWS S3, bảo vệ dữ liệu trước các sự cố phần cứng hoặc mã độc tống tiền (Ransomware).

---

## Các tính năng nổi bật (Core Features)

*   **Mã hóa Phía Máy Khách (Client-Side Encryption)**: Dữ liệu được nén và mã hóa bằng thuật toán đối xứng **AES-256** ngay tại máy người dùng trước khi tải lên Cloud. Khóa giải mã được giữ cục bộ, đảm bảo tính riêng tư tuyệt đối.
*   **Sao lưu Gia tăng (Incremental Backup)**: Tiết kiệm băng thông tối đa. Sau lần sao lưu đầu tiên (Full Backup), hệ thống chỉ sao lưu và tải lên các tệp tin mới tạo hoặc bị chỉnh sửa dựa trên dấu thời gian và mã băm **SHA-256**.
*   **Quản lý Đa Phiên bản (Multi-versioning)**: Lịch sử thay đổi tệp tin được lưu trữ qua các phiên bản. Cho phép khôi phục chính xác trạng thái tại một mốc thời gian cụ thể (Point-in-Time Restore).
*   **Độc lập Nhà cung cấp Đám mây (Cloud Agnostic)**: Thiết kế với cơ chế Adapter trừu tượng hóa, dễ dàng chuyển đổi cấu hình lưu trữ giữa **Google Cloud Storage (GCS)** và **Amazon S3** thông qua file cấu hình.
*   **Gửi Email Báo cáo Tự động**: Tích hợp email thông báo tự động (Spring Mail / SendGrid API) gửi báo cáo trạng thái ngay khi phiên backup hoàn tất hoặc gặp sự cố.
*   **Đồng bộ 2 chiều (Synchronization)**: CLI Command tự động tải các file mới từ Cloud và sao lưu các file mới/bị sửa đổi từ máy khách lên.
*   **Daemon Agent chạy ngầm**: Hỗ trợ CLI Agent chạy ngầm định kỳ kéo các job tự động được tạo từ server.

---

## Hướng dẫn dành cho Người dùng cuối (Non-Developer Guide)

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

## Công nghệ sử dụng

*   **Ngôn ngữ**: Java 25
*   **Framework chính**: Spring Boot 3, Spring Security, Spring Data JPA, Picocli
*   **Cơ sở dữ liệu**: PostgreSQL
*   **Bảo mật**: JWT (JSON Web Tokens), BCrypt, AES-256, SHA-256, Token Blacklist
*   **Cloud Storage**: Google Cloud Storage Client / AWS S3 SDK
*   **Triển khai & Proxy**: Docker, Docker Compose, Nginx Reverse Proxy

---

## Thư mục dự án

```text
cloud-backup-system/
├── backend/                # Backend Spring Boot (Kiến trúc Modular Monolith)
├── cli/                    # Client Agent CLI Java (Picocli)
├── nginx/                  # File cấu hình Nginx Reverse Proxy
├── infrastructure/         # File docker-compose, script khởi tạo DB, file .env
│   ├── docker-compose.yml  # File compose chính (App, DB, Nginx)
│   ├── Dockerfile          # Hướng dẫn build image cho Spring Boot app
│   ├── .env.docker         # File cấu hình môi trường mẫu
│   └── init-db.sql         # Script thiết lập quyền database Postgres & bảng CSDL
└── docs/                   # Thư mục chứa tài liệu đặc tả dự án
    ├── erd/                # Thiết kế database chi tiết và sơ đồ ERD Mermaid
    ├── system-design.md    # Thiết kế kiến trúc hệ thống hợp nhất
    ├── setup-guide.md      # Hướng dẫn cài đặt chi tiết
    └── team-playbook.md    # Sổ tay quy chuẩn code và Git cho team dev
```

---

## Hướng dẫn sử dụng nhanh (Quick Usage Guide)

Hệ thống hoạt động dựa trên sự tương tác giữa API Server (Backend) và máy khách (CLI):

### 1. Phía API Server (Backend)
Hỗ trợ xác thực người dùng, đăng ký thiết bị, phân phối khóa AES và lưu trữ metadata phiên bản sao lưu.
-   **Đăng ký**: `POST /api/auth/register`
-   **Đăng nhập**: `POST /api/auth/login`
-   **Đăng xuất (Thu hồi Token)**: `POST /api/auth/logout`

### 2. Phía Client (CLI)
Sau khi build dự án, bạn sẽ có file `cli/target/cloudsafe.jar` chạy trực tiếp thông qua Java:

*   **Đăng nhập**:
    ```powershell
    java -jar cloudsafe.jar login --email <email>
    ```
*   **Thực hiện sao lưu**:
    ```powershell
    java -jar cloudsafe.jar backup <localFolder> --device <deviceId> --type INCREMENTAL
    ```
*   **Đồng bộ dữ liệu 2 chiều**:
    ```powershell
    java -jar cloudsafe.jar sync <localFolder> --device <deviceId>
    ```
*   **Khởi chạy ngầm Daemon tự động**:
    ```powershell
    java -jar cloudsafe.jar daemon --device <deviceId> --interval 30
    ```
*   **Khôi phục dữ liệu**:
    ```powershell
    java -jar cloudsafe.jar restore --job <jobId> --destination <localFolder>
    ```

---

## Khởi chạy dự án nhanh (Quick Start)

### Chạy trực tiếp trên máy local (Không dùng Docker):
1.  **Cơ sở dữ liệu**: Cài đặt PostgreSQL, tạo DB `cloudbackup` và chạy script [init-db.sql](file:///c:/Users/N07/Documents/GitHub/cloud-backup-system/infrastructure/init-db.sql) để phân quyền.
2.  **Cấu hình**: Chỉnh sửa cấu hình kết nối DB tại `backend/src/main/resources/application.yml`.
3.  **Khởi động Backend**: Chạy lệnh Spring Boot tại root:
    ```powershell
    mvn spring-boot:run -pl backend -Dspring-boot.run.profiles=dev
    ```
4.  **Đóng gói CLI**: Chạy lệnh Maven tại root:
    ```powershell
    mvn package -DskipTests
    ```
    Sử dụng tệp tin JAR được build ra tại `cli/target/cloudsafe.jar` để tương tác.

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

## Tài liệu chi tiết của dự án

Để tìm hiểu sâu hơn về kiến trúc và cách phát triển dự án, vui lòng đọc các tài liệu:
- **[Thiết kế Hệ thống (System Design)](file:///c:/Users/N07/Documents/GitHub/cloud-backup-system/docs/system-design.md)**: Đặc tả chi tiết kiến trúc Modular Monolith, luồng dữ liệu của các phiên backup/restore và ước tính chi phí Cloud.
- **[Thiết kế Cơ sở Dữ liệu & Sơ đồ ERD](file:///c:/Users/N07/Documents/GitHub/cloud-backup-system/docs/erd/README.md)**: Sơ đồ ERD trực quan vẽ bằng Mermaid và mô tả cấu trúc chi tiết từng bảng dữ liệu (khóa chính, khóa ngoại, kiểu dữ liệu, index).
- **[Hướng dẫn Cài đặt & Khởi chạy (Setup Guide)](file:///c:/Users/N07/Documents/GitHub/cloud-backup-system/docs/setup-guide.md)**: Hướng dẫn cài đặt chi tiết cho dev từ môi trường phát triển cục bộ tới production.
- **[Sổ tay Lập trình Đội ngũ (Team Playbook)](file:///c:/Users/N07/Documents/GitHub/cloud-backup-system/docs/team-playbook.md)**: Định nghĩa các quy chuẩn đặt tên, code style, xử lý exception và quy trình phối hợp làm việc trên Git.
