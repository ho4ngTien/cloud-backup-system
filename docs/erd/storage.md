# Đặc tả Cơ sở Dữ liệu: Phân hệ Dung lượng Lưu trữ (Storage Usage)

Phân hệ này kiểm soát hạn mức dung lượng lưu trữ (quota) của từng người dùng để tránh lãng phí tài nguyên Cloud.

---

## 1. Bảng `storage_usage`
Quản lý dung lượng đã dùng thực tế và quota tối đa được phép dùng của người dùng.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK | Khóa chính |
| `user_id` | UUID | FK -> `users(id)`, UK, NOT NULL | Liên kết duy nhất tới tài khoản người dùng |
| `total_bytes` | BIGINT | NOT NULL | Hạn mức dung lượng cho phép tối đa (quota) |
| `used_bytes` | BIGINT | NOT NULL, Default: 0 | Tổng dung lượng thực tế đã tải lên Cloud (bytes) |
| `last_updated` | TIMESTAMP | NOT NULL, Default: NOW() | Thời điểm cập nhật dữ liệu gần nhất |
