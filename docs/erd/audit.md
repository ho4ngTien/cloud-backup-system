# Đặc tả Cơ sở Dữ liệu: Phân hệ Nhật ký Hệ thống (Audit Logs)

Phân hệ này lưu vết toàn bộ hoạt động nhạy cảm trên hệ thống để phục vụ công tác giám sát an ninh và quản trị.

---

## 1. Bảng `audit_logs`
Nhật ký chi tiết các thao tác của người dùng và quản trị viên.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK, Default: gen_random_uuid() | Khóa chính tự sinh |
| `user_id` | UUID | FK -> `users(id)` ON DELETE SET NULL | Người thực hiện thao tác (có thể null nếu là hệ thống tự chạy) |
| `event_type` | VARCHAR(50) | NOT NULL | Loại thao tác (`LOGIN_SUCCESS`, `LOGIN_FAILED`, `RESTORE_REQUEST`, `DELETE_BACKUP`, `USER_BLOCKED`) |
| `details` | TEXT | | Mô tả chi tiết hành động và tham số |
| `ip_address` | VARCHAR(45) | NOT NULL | Địa chỉ IP của máy khách gửi yêu cầu (IPv4 hoặc IPv6) |
| `created_at` | TIMESTAMP | NOT NULL, Default: NOW() | Thời điểm ghi nhận nhật ký |
