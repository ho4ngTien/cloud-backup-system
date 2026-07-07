# Đặc tả Cơ sở Dữ liệu: Phân hệ Sao lưu (Backups)

Phân hệ này lưu trữ nhật ký tiến trình sao lưu, thông tin các phiên bản và đường dẫn các file vật lý đã mã hóa lưu trên Cloud.

---

## 1. Bảng `backup_jobs`
Ghi nhận kết quả và trạng thái tổng quan của từng lượt kích hoạt sao lưu.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK | Khóa chính |
| `device_id` | UUID | FK -> `devices(id)` | Thiết bị thực hiện sao lưu |
| `user_id` | UUID | FK -> `users(id)` | Người dùng kích hoạt sao lưu |
| `type` | VARCHAR(30) | NOT NULL | Loại sao lưu (`FULL`, `INCREMENTAL`) |
| `status` | VARCHAR(30) | NOT NULL | Trạng thái Job (`IN_PROGRESS`, `COMPLETED`, `FAILED`) |
| `total_size` | BIGINT | Default: 0 | Tổng dung lượng tệp tin sao lưu của Job (bytes) |
| `version` | INTEGER | NOT NULL | Số thứ tự phiên bản backup |
| `created_at` | TIMESTAMP | NOT NULL, Default: NOW() | Thời điểm bắt đầu chạy Job |
| `completed_at`| TIMESTAMP | | Thời điểm Job kết thúc |

---

## 2. Bảng `backup_versions`
Quản lý lịch sử các phiên bản sao lưu để phục vụ tính năng phục hồi đa điểm thời gian (Point-in-Time Restore).

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK | Khóa chính |
| `backup_job_id`| UUID | FK -> `backup_jobs(id)` | Liên kết tới Job sao lưu sinh ra phiên bản này |
| `version_number`| INTEGER | NOT NULL | Số phiên bản của bản sao lưu |
| `full_backup` | BOOLEAN | NOT NULL | Đánh dấu đây là bản Full (true) hay Incremental (false) |
| `metadata` | JSONB | | Lưu trữ cấu trúc cây thư mục động tại thời điểm backup |
| `created_at` | TIMESTAMP | NOT NULL, Default: NOW() | Thời điểm tạo phiên bản |

---

## 3. Bảng `backup_files`
Đặc tả chi tiết thông tin các file vật lý đã được nén, mã hóa và đẩy lên Cloud Storage.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK | Khóa chính |
| `backup_version_id`| UUID | FK -> `backup_versions(id)` ON DELETE CASCADE | Thuộc về phiên bản sao lưu nào |
| `path` | VARCHAR(1000) | NOT NULL | Đường dẫn file gốc trên máy khách |
| `size_bytes` | BIGINT | NOT NULL | Kích thước file vật lý (bytes) |
| `storage_key` | VARCHAR(500) | UK, NOT NULL | Object key duy nhất đại diện cho file trên GCS/S3 |
| `encrypted` | BOOLEAN | NOT NULL, Default: true | Đánh dấu file đã được mã hóa AES-256 hay chưa |
| `created_at` | TIMESTAMP | NOT NULL, Default: NOW() | Thời điểm file được tải lên Cloud |
