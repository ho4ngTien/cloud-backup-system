# Đặc tả Cơ sở Dữ liệu: Phân hệ Thiết bị máy khách (Devices)

Phân hệ này quản lý thông tin các thiết bị máy khách (Client Agent) đã cài đặt ứng dụng và được liên kết với tài khoản người dùng.

---

## 1. Bảng `devices`
Lưu trữ thông tin chi tiết về từng thiết bị cục bộ của người dùng.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK, Default: gen_random_uuid() | Khóa chính |
| `user_id` | UUID | FK -> `users(id)` ON DELETE CASCADE | Chủ sở hữu thiết bị này |
| `name` | VARCHAR(100) | NOT NULL | Tên định danh thiết bị do người dùng đặt |
| `os` | VARCHAR(50) | NOT NULL | Hệ điều hành (`WINDOWS`, `LINUX`, `MAC`) |
| `watch_path` | VARCHAR(500) | NOT NULL | Đường dẫn thư mục cục bộ cần theo dõi sao lưu |
| `online` | BOOLEAN | NOT NULL, Default: false | Trạng thái kết nối hiện tại của Agent |
| `created_at` | TIMESTAMP | NOT NULL, Default: NOW() | Thời điểm đăng ký thiết bị vào hệ thống |
