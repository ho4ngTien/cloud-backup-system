# Đặc tả Cơ sở Dữ liệu: Phân hệ Người dùng & Phân quyền (Users & Authentication)

Phân hệ này quản lý thông tin tài khoản người dùng, phân quyền truy cập dựa trên vai trò (Role-Based Access Control - RBAC).

---

## 1. Bảng `users`
Lưu trữ thông tin chi tiết về tài khoản người dùng và quản trị viên hệ thống.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK, Default: gen_random_uuid() | Khóa chính tự sinh |
| `email` | VARCHAR(255) | UK, NOT NULL | Email đăng nhập |
| `password_hash` | VARCHAR(255) | NOT NULL | Mật khẩu đã được mã hóa BCrypt |
| `display_name` | VARCHAR(100) | | Tên hiển thị của người dùng |
| `enabled` | BOOLEAN | NOT NULL, Default: true | Trạng thái kích hoạt tài khoản |
| `created_at` | TIMESTAMP | NOT NULL, Default: NOW() | Thời gian khởi tạo tài khoản |

---

## 2. Bảng `roles`
Định nghĩa danh sách các vai trò (roles) trong hệ thống.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | UUID | PK, Default: gen_random_uuid() | Khóa chính |
| `name` | VARCHAR(50) | UK, NOT NULL | Tên vai trò (ví dụ: `ROLE_USER`, `ROLE_ADMIN`) |

---

## 3. Bảng `user_roles`
Bảng trung gian thiết lập mối quan hệ nhiều - nhiều (many-to-many) giữa người dùng và vai trò.

| Tên trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `user_id` | UUID | PK, FK -> `users(id)` ON DELETE CASCADE | Liên kết đến ID người dùng |
| `role_id` | UUID | PK, FK -> `roles(id)` ON DELETE CASCADE | Liên kết đến ID vai trò |
