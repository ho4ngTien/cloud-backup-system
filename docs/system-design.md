# Thiết kế hệ thống: Cloud Backup & Disaster Recovery System

Tài liệu này mô tả chi tiết thiết kế kiến trúc, tính năng, sơ đồ luồng dữ liệu, API RESTful và phương án triển khai đám mây cho dự án CloudSafe.

---

## 1. Mục tiêu & Phạm vi dự án
Xây dựng hệ thống sao lưu và khôi phục dữ liệu tự động trên nền tảng điện toán đám mây với các khả năng cốt lõi:
- **Hỗ trợ đa nền tảng**: Cho phép nhiều máy khách Windows/Linux (Client Agent) kết nối và sao lưu dữ liệu.
- **Bảo mật tối đa**: Mã hóa dữ liệu bằng thuật toán mã hóa đối xứng AES-256 ở mức tệp tin ngay trước khi tải lên Cloud.
- **Tối ưu hóa dung lượng**: Hỗ trợ sao lưu toàn bộ (Full Backup) và sao lưu gia tăng (Incremental Backup), kiểm soát đa phiên bản và khôi phục linh hoạt theo phiên bản.
- **Kiến trúc Cloud native**: Tương thích tốt với các dịch vụ lưu trữ đám mây lớn (Google Cloud Storage / Amazon S3) và có khả năng chuyển đổi linh hoạt.

---

## 2. Đối tượng sử dụng (Actors) & Chức năng hệ thống

### 2.1 Đối tượng sử dụng (Actors)
1. **User (Người dùng cá nhân / doanh nghiệp)**:
   - Đăng ký, đăng nhập tài khoản và quản lý hồ sơ cá nhân.
   - Đăng ký thiết bị client và liên kết thư mục cần sao lưu.
   - Thiết lập lịch tự động sao lưu hoặc thực hiện sao lưu thủ công.
   - Xem lịch sử các phiên backup, khôi phục tệp/thư mục từ một phiên bản cụ thể.
   - Theo dõi không gian lưu trữ và nhận báo cáo/thông báo qua Email.
2. **Administrator (Quản trị viên)**:
   - Quản lý người dùng (khoá/mở tài khoản, phân quyền Admin/User).
   - Giám sát trạng thái hoạt động của toàn bộ thiết bị client trong hệ thống.
   - Thiết lập các chính sách sao lưu toàn cục và kiểm soát quota dung lượng.
   - Truy cập Audit Log (nhật ký hệ thống) để kiểm tra bảo mật.
3. **Backup Agent (Ứng dụng máy khách)**:
   - Chạy ngầm để theo dõi thay đổi tệp tin trong thư mục watch path.
   - Nén dữ liệu, băm SHA-256 để kiểm tra tính toàn vẹn và mã hóa AES-256.
   - Tải tệp lên Storage Bucket và đồng bộ trạng thái về Server.
   - Thực thi yêu cầu tải xuống và giải mã khi có lệnh Restore.

### 2.2 Các tính năng chính của hệ thống

#### 1. Quản lý người dùng (User Management)
- Đăng ký tài khoản.
- Đăng nhập, đăng xuất.
- Quản lý hồ sơ cá nhân.
- Đổi mật khẩu.
- Phân quyền (Admin/User).

#### 2. Quản lý thiết bị (Device Management)
- Đăng ký máy tính hoặc máy chủ vào hệ thống.
- Quản lý nhiều thiết bị trên cùng một tài khoản.
- Hiển thị trạng thái Online/Offline của từng thiết bị.
- Xóa hoặc vô hiệu hóa thiết bị.

#### 3. Quản lý thư mục sao lưu
- Chọn thư mục cần sao lưu.
- Thêm hoặc xóa thư mục khỏi danh sách sao lưu.
- Thiết lập quy tắc loại trừ (ví dụ: bỏ qua file tạm, file log).
- Thiết lập dung lượng tối đa cho mỗi lần sao lưu.

#### 4. Sao lưu dữ liệu (Backup)
- Sao lưu toàn bộ (Full Backup).
- Sao lưu gia tăng (Incremental Backup).
- Sao lưu thủ công.
- Sao lưu tự động theo lịch (Scheduler).
- Theo dõi tiến trình sao lưu.

#### 5. Mã hóa và nén dữ liệu
- Nén dữ liệu trước khi tải lên.
- Mã hóa dữ liệu bằng AES-256.
- Kiểm tra tính toàn vẹn của dữ liệu bằng Hash (SHA-256).

#### 6. Lưu trữ trên Cloud
- Upload dữ liệu lên Google Cloud Storage hoặc Amazon S3.
- Quản lý các phiên bản sao lưu.
- Theo dõi dung lượng lưu trữ đã sử dụng.
- Xóa các bản sao lưu cũ theo chính sách lưu trữ.

#### 7. Khôi phục dữ liệu (Restore)
- Khôi phục toàn bộ dữ liệu.
- Khôi phục từng tệp hoặc thư mục.
- Chọn phiên bản sao lưu để khôi phục.
- Xem lịch sử khôi phục.

#### 8. Đồng bộ dữ liệu (Synchronization)
- Đồng bộ dữ liệu giữa nhiều thiết bị.
- Phát hiện thay đổi dữ liệu.
- Đồng bộ các tệp mới hoặc đã chỉnh sửa.
- Xử lý xung đột phiên bản khi nhiều thiết bị cùng chỉnh sửa.

#### 9. Thông báo (Notification)
- Gửi email khi sao lưu thành công.
- Gửi email khi sao lưu thất bại.
- Cảnh báo khi dung lượng lưu trữ gần đầy.
- Thông báo khi có thiết bị mới đăng nhập.

#### 10. Dashboard và thống kê
- Tổng số thiết bị đang quản lý.
- Tổng số bản sao lưu.
- Dung lượng đã sử dụng.
- Dung lượng còn trống.
- Biểu đồ thống kê số lần sao lưu theo ngày/tháng.
- Nhật ký hoạt động của hệ thống.

#### 11. Nhật ký hệ thống (Audit Log)
- Ghi nhận lịch sử đăng nhập.
- Ghi nhận lịch sử sao lưu.
- Ghi nhận lịch sử khôi phục.
- Ghi nhận các thao tác của người dùng.

#### 12. Quản trị hệ thống (Admin)
- Quản lý người dùng.
- Quản lý thiết bị.
- Quản lý dung lượng lưu trữ.
- Theo dõi trạng thái hệ thống.
- Cấu hình chính sách sao lưu.

---

## 3. Kiến trúc hệ thống & Luồng dữ liệu

### 3.1 Mô hình Kiến trúc Phân tầng (Layered Architecture)
Ứng dụng API Spring Boot được tổ chức theo các phân tầng chuẩn Enterprise:
- **Presentation / API Layer**: Xử lý các request RESTful, tích hợp OpenAPI/Swagger để làm tài liệu giao tiếp API.
- **Controller Layer (`@RestController`)**: Điều hướng request, validate đầu vào (`Jakarta Validation`) và chuyển đổi dữ liệu qua DTO.
- **Security Layer (Spring Security + JWT)**: Kiểm soát xác thực người dùng và phân quyền dựa trên vai trò (RBAC - Role-Based Access Control).
- **Service Layer (Business Logic)**: Chứa toàn bộ nghiệp vụ xử lý logic (điều phối phiên backup/restore, kiểm tra dung lượng sử dụng, đồng bộ thiết bị).
- **Repository Layer (Spring Data JPA + Hibernate)**: Thực hiện truy vấn dữ liệu quan hệ trên PostgreSQL.
- **Infrastructure / Integrations Layer**:
  - *Cloud Storage Adapter*: Giao diện trừu tượng hóa cho phép kết nối đến GCS (Google Cloud Storage) hoặc AWS S3.
  - *Scheduler*: Spring Scheduler chạy ngầm để quét lịch biểu và dọn dẹp các phiên bản backup đã hết hạn.
  - *Notification*: Tích hợp Spring Mail (gửi trực tiếp qua SMTP hoặc SendGrid).

### 3.2 Sơ đồ luồng dữ liệu (Data Flow)

```
┌─────────────────────────────────────────┐
│         Nginx (Reverse Proxy)           │
│              (Port 80/443)              │
└──────────────────┬──────────────────────┘
                   │
         ┌─────────────────────┐
         │ Cloud Backup App    │
         │ (Port 8080)         │
         │ Spring Boot         │
         └──────────┬──────────┘
                    │
         ┌──────────────────────┐
         │  PostgreSQL DB       │
         │  (Port 5432)         │
         └──────────────────────┘
```

#### Luồng sao lưu (Backup Flow):
1. **Client Agent** gửi yêu cầu khởi tạo backup lên API Spring Boot kèm thông tin metadata của các file cần backup (kích thước, hash SHA-256).
2. **API Server** xác thực JWT, kiểm tra quota dung lượng và ghi nhận trạng thái Job là `IN_PROGRESS`, tạo các bản ghi Metadata phiên bản mới.
3. **Client Agent** thực hiện nén, mã hóa AES-256 cục bộ và tải trực tiếp file lên **Cloud Storage (GCS/S3)**.
4. Sau khi tải lên thành công, **Client Agent** gửi request hoàn tất (commit) kèm theo các `storage_key` của Cloud.
5. **API Server** xác thực, cập nhật trạng thái Job thành `COMPLETED` và gửi email thông báo cho User.

#### Luồng khôi phục (Restore Flow):
1. **User** yêu cầu Restore một file hoặc một phiên bản qua giao diện Web hoặc Agent.
2. **API Server** kiểm tra quyền truy cập của User đối với file/phiên bản đó, lấy `storage_key` và tạo URL tải xuống có thời hạn (Presigned URL) hoặc tải file trung gian qua API Server.
3. **Client Agent** nhận dữ liệu, thực hiện giải mã AES-256 bằng khóa bảo mật cục bộ của người dùng và giải nén tệp về thư mục đích.

---

## 4. Thiết kế REST API

### 4.1 Authentication & User API
- `POST /api/auth/register` - Đăng ký người dùng mới.
- `POST /api/auth/login` - Đăng nhập, trả về Access Token (JWT) và Refresh Token.
- `POST /api/auth/refresh` - Refresh Access Token từ Refresh Token.
- `GET /api/users/me` - Lấy thông tin tài khoản hiện tại.
- `PUT /api/users/me` - Cập nhật thông tin tài khoản.

### 4.2 Device API
- `POST /api/devices` - Đăng ký thiết bị máy khách mới.
- `GET /api/devices` - Liệt kê danh sách thiết bị của User hiện tại.
- `GET /api/devices/{id}` - Xem thông tin chi tiết một thiết bị.
- `PUT /api/devices/{id}` - Cập nhật cấu hình thiết bị (ví dụ: watch path).
- `DELETE /api/devices/{id}` - Hủy liên kết thiết bị.
- `POST /api/devices/{id}/status` - Cập nhật trạng thái heartbeat (Online/Offline) từ Agent.

### 4.3 Backup & Storage API
- `POST /api/backups/manual` - Kích hoạt một phiên backup thủ công.
- `POST /api/backups/incremental` - Báo cáo metadata và khởi động phiên backup gia tăng.
- `GET /api/backups` - Xem lịch sử tất cả phiên backup.
- `GET /api/backups/{id}/versions` - Xem danh sách các phiên bản của một Job backup.
- `GET /api/backups/{id}/files` - Liệt kê danh sách tệp tin trong phiên backup.
- `GET /api/storage/usage` - Xem dung lượng sử dụng thực tế và quota còn lại.

### 4.4 Restore API
- `POST /api/restore/full` - Yêu cầu khôi phục toàn bộ một phiên bản.
- `POST /api/restore/file` - Yêu cầu khôi phục một hoặc vài file cụ thể.
- `GET /api/restore/history` - Xem lịch sử các yêu cầu khôi phục.

### 4.5 Dashboard & Statistics (Dành cho Admin & User)
- `GET /api/dashboard/summary` - Lấy thông tin tổng hợp nhanh (số thiết bị, tổng dung lượng, phiên backup gần nhất).
- `GET /api/dashboard/storage/statistics` - Dữ liệu biểu đồ dung lượng lưu trữ theo thời gian.

---

## 5. Triển khai Cloud & Ước tính chi phí

### 5.1 Các dịch vụ đám mây đề xuất
- **Máy chủ ứng dụng (Compute)**: Google Compute Engine (GCE) hoặc AWS EC2 chạy Docker với Nginx làm Reverse Proxy.
- **Cơ sở dữ liệu (Database)**: Cloud SQL PostgreSQL (GCP) hoặc Amazon RDS PostgreSQL (AWS).
- **Lưu trữ đối tượng (Object Storage)**: Google Cloud Storage (GCS) hoặc Amazon S3 (sử dụng tính năng Object Versioning).
- **Hệ thống gửi thư (Email)**: SendGrid API hoặc Amazon SES.

### 5.2 Ước tính chi phí hàng tháng (Cơ bản)

#### Google Cloud Platform (GCP):
- **GCE instance (e2-micro)**: ~$7.00
- **Cloud Storage (50GB - Standard)**: ~$1.20
- **Cloud SQL PostgreSQL (db-f1-micro)**: ~$7.00
- **Băng thông mạng & Khác**: ~$1.00
- **Tổng cộng**: **~$16.20 - $20.00 / tháng**

#### Amazon Web Services (AWS):
- **EC2 instance (t3.micro)**: ~$8.00
- **Amazon S3 (50GB)**: ~$1.50
- **RDS PostgreSQL (db.t3.micro)**: ~$15.00
- **CloudWatch & Băng thông**: ~$1.50
- **Tổng cộng**: **~$26.00 - $30.00 / tháng**

#### Hướng triển khai miễn phí (Free Tier):
- Tận dụng chương trình **Google Cloud Free Tier** (1 VM f1-micro miễn phí vĩnh viễn, 5GB Cloud Storage) hoặc **AWS Free Tier** 12 tháng đầu để thử nghiệm và chạy demo.
- Sử dụng Database cục bộ và Mock Email service trong quá trình phát triển để tối ưu hóa chi phí.
