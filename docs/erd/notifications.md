# Đặc tả Cơ sở Dữ liệu: Phân hệ Cảnh báo & Thông báo (Notifications)

Phân hệ này quản lý và lưu trữ các thông báo, cảnh báo từ hệ thống gửi tới người dùng.

---

## 1. Bảng `notifications`
Ghi nhận các sự kiện cần gửi thông báo hoặc cảnh báo cho người dùng.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK, Default: gen_random_uuid() | Khóa chính tự sinh |
| `user_id` | UUID | FK -> `users(id)` ON DELETE CASCADE | Người nhận thông báo |
| `event_type` | VARCHAR(50) | NOT NULL | Loại sự kiện (`BACKUP_SUCCESS`, `BACKUP_FAILED`, `STORAGE_WARNING`, `NEW_DEVICE`) |
| `message` | TEXT | NOT NULL | Nội dung chi tiết của thông báo |
| `status` | VARCHAR(30) | NOT NULL, Default: 'UNREAD' | Trạng thái thông báo (`UNREAD`, `READ`) |
| `created_at` | TIMESTAMP | NOT NULL, Default: NOW() | Thời điểm phát sinh thông báo |
