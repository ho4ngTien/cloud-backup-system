# Đặc tả Cơ sở Dữ liệu: Phân hệ Khôi phục dữ liệu (Restores)

Phân hệ này theo dõi tiến trình và nhật ký của các yêu cầu khôi phục dữ liệu (Restore) từ Cloud về máy khách.

---

## 1. Bảng `restore_history`
Ghi nhận chi tiết từng yêu cầu khôi phục của người dùng.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK | Khóa chính |
| `backup_version_id`| UUID | FK -> `backup_versions(id)` | Phiên bản sao lưu nguồn dùng để khôi phục |
| `user_id` | UUID | FK -> `users(id)` | Người dùng gửi yêu cầu khôi phục |
| `device_id` | UUID | FK -> `devices(id)` | Thiết bị máy khách nhận file khôi phục |
| `restore_type` | VARCHAR(30) | NOT NULL | Kiểu khôi phục (`FULL`, `SELECTIVE_FILES`) |
| `status` | VARCHAR(30) | NOT NULL | Trạng thái khôi phục (`PENDING`, `PROCESSING`, `SUCCESS`, `FAILED`) |
| `requested_at` | TIMESTAMP | NOT NULL, Default: NOW() | Thời gian bắt đầu gửi yêu cầu |
| `completed_at` | TIMESTAMP | | Thời gian hoàn thành giải mã và ghi đè file về máy |
