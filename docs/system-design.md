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
   - Quản lý người dùng (khoá/mở tài khoản, phân quyền Admin/User, thay đổi quota).
   - Giám sát trạng thái hoạt động của toàn bộ thiết bị client trong hệ thống.
   - Truy cập Audit Log (nhật ký hệ thống) để kiểm tra bảo mật.
3. **Backup Agent (Ứng dụng máy khách)**:
   - Chạy ngầm định kỳ (Daemon mode) để theo dõi và thực thi các Job sao lưu từ Server.
   - Nén dữ liệu, băm SHA-256 để kiểm tra tính toàn vẹn và mã hóa AES-256.
   - Tải tệp lên Storage Bucket và đồng bộ trạng thái về Server.
   - Thực thi yêu cầu tải xuống và giải mã khi có lệnh Restore hoặc Sync.

### 2.2 Các tính năng chính của hệ thống

#### 1. Quản lý người dùng (User Management)
- Đăng ký tài khoản.
- Đăng nhập, đăng xuất (Blacklist Token thu hồi JWT hoạt động).
- Phân quyền (Admin/User).

#### 2. Quản lý thiết bị (Device Management)
- Đăng ký máy tính hoặc máy chủ vào hệ thống.
- Cấu hình `watch_path`, `auto_backup` và quy tắc loại trừ `excluded_patterns`.
- Heartbeat báo cáo Online/Offline cục bộ.

#### 3. Quản lý thư mục sao lưu
- Thiết lập quy tắc loại trừ (ví dụ: `*.tmp`, `node_modules/*` dạng glob).

#### 4. Sao lưu dữ liệu (Backup)
- Sao lưu toàn bộ (Full Backup).
- Sao lưu gia tăng (Incremental Backup): So sánh SHA-256 và giữ nguyên khóa AES cũ của Job trước.
- Sao lưu thủ công và sao lưu tự động (Daemon agent).

#### 5. Mã hóa và nén dữ liệu
- Nén ZIP và mã hóa đối xứng AES-256 GCM cục bộ.

#### 6. Lưu trữ trên Cloud (Multi-Cloud)
- Lưu trữ linh hoạt trên Google Cloud Storage (GCS) hoặc Amazon S3.

#### 7. Khôi phục dữ liệu (Restore)
- Khôi phục dựa trên URL tải xuống (Signed URL) và khóa giải mã AES.

#### 8. Đồng bộ dữ liệu (Synchronization)
- Đồng bộ dữ liệu 2 chiều (`sync` command): tải file thiếu từ Cloud về local và sao lưu file mới từ local lên Cloud.

---

## 3. Kiến trúc hệ thống & Luồng dữ liệu

### 3.1 Mô hình Kiến trúc Modular Monolith phân lớp
Ứng dụng API Spring Boot được tổ chức theo cấu trúc Modular Monolith phân lớp nghiệp vụ rõ ràng trong folder `/backend/src/main/java/com/cloudsafe/`:
- **`com.cloudsafe.auth`** (User & Security):
  - `controller/`, `service/`, `repository/`, `entity/`, `dto/`
- **`com.cloudsafe.backup`** (Job, Version, File metadata):
  - `controller/`, `service/`, `repository/`, `entity/`, `dto/`
- **`com.cloudsafe.device`** (Thiết bị & Cấu hình):
  - `controller/`, `service/`, `repository/`, `entity/`, `dto/`
- **`com.cloudsafe.restore`** (Lịch sử khôi phục):
  - `controller/`, `service/`, `repository/`, `entity/`, `dto/`
- **`com.cloudsafe.storage`** (Lớp Adapter GCS/S3):
  - `controller/`, `service/`
- **`com.cloudsafe.notification`** (Mail SMTP):
  - `service/`, `repository/`, `entity/`
- **`com.cloudsafe.audit`** (Audit Logs):
  - `service/`, `repository/`, `entity/`
- **`com.cloudsafe.dashboard`** (Dashboard thống kê):
  - `controller/`, `service/`
- **`com.cloudsafe.common`** (Exception & Configuration):
  - `config/`, `exception/`

### 3.2 Sơ đồ luồng dữ liệu (Data Flow)

```
┌─────────────────────────────────────────┐
│         Nginx (Reverse Proxy)           │
│              (Port 80/443)              │
│                    │                    │
│   ┌────────────────▼────────────────┐   │
│   │        Cloudsafe Backend        │   │
│   │           (Port 8080)           │   │
│   └────┬───────────────────────┬────┘   │
└────────┼───────────────────────┼────────┘
         │                       │
┌────────▼─────────────┐   ┌─────▼───────────────┐
│     PostgreSQL       │   │ Object Storage      │
│     (Port 5432)      │   │ (AWS S3 / GCS)      │
└──────────────────────┘   └─────────────────────┘
```

---

## 4. Thiết kế REST API

### 4.1 Authentication & User API
- `POST /api/auth/register` - Đăng ký người dùng mới.
- `POST /api/auth/login` - Đăng nhập, trả về Access Token (JWT).
- `POST /api/auth/logout` - Đăng xuất (Ghi nhận JWT vào Blacklist).
- `GET /api/users/me` - Lấy thông tin tài khoản hiện tại.
- `PUT /api/users/me` - Cập nhật thông tin tài khoản.
- `PUT /api/users/{id}/quota` - [Admin] Cập nhật quota bộ nhớ (byte) cho User.

### 4.2 Device API
- `POST /api/devices` - Đăng ký thiết bị máy khách mới.
- `GET /api/devices` - Liệt kê danh sách thiết bị của User hiện tại.
- `GET /api/devices/{id}` - Xem thông tin chi tiết một thiết bị.
- `PUT /api/devices/{id}` - Cập nhật cấu hình thiết bị (watch path, excluded patterns).
- `DELETE /api/devices/{id}` - Hủy liên kết thiết bị.
- `POST /api/devices/{id}/heartbeat` - Cập nhật trạng thái heartbeat từ Agent.

### 4.3 Backup & Storage API
- `POST /api/backups/start` - Khởi động phiên backup (tạo Job, cấp excluded patterns).
- `POST /api/backups/commit` - Xác nhận hoàn tất upload tất cả file từ CLI.
- `POST /api/backups/{id}/fail` - Báo cáo thất bại từ CLI.
- `GET /api/backups/pending` - Thăm dò các Job tự động PENDING dành cho Daemon CLI.
- `GET /api/backups` - Xem lịch sử phiên backup.
- `GET /api/backups/{id}/versions` - Xem danh sách phiên bản của Job.
- `GET /api/backups/versions/{versionId}/files` - Liệt kê tệp tin của phiên bản.
- `GET /api/backups/latest-completed-files` - Lấy trạng thái phiên bản mới nhất để thực hiện Incremental/Sync.
- `GET /api/storage/usage` - Xem dung lượng sử dụng và quota.
- `GET /api/storage/signed-url` - Cấp Signed URL để CLI download/upload.

### 4.4 Restore API
- `POST /api/restore/start` - Yêu cầu khôi phục phiên bản (trả về Signed URLs tải xuống).

---

## 5. Triển khai Cloud & Ước tính chi phí
*(Tham khảo thông tin dịch vụ đám mây chi tiết tại hướng dẫn deployment của dự án).*
