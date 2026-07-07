# CloudSafe – Kịch bản Demo End-to-End (Hoàn thiện Toàn bộ)

Tài liệu này mô tả kịch bản demo hoàn chỉnh cho toàn bộ hệ thống bao gồm cả Đăng xuất, Lưu trữ S3, File Exclusions, Chạy ngầm Daemon và Đồng bộ hóa 2 chiều.

---

## Điều kiện tiên quyết

| Yêu cầu | Kiểm tra |
|---|---|
| Server đang chạy | curl http://localhost:8080/actuator/health |
| PostgreSQL online | docker ps |
| CLI đã build | java -jar cloudsafe.jar --version |
| Thư mục test tồn tại | C:\TestData\ |

---

## Bước 1 – Chuẩn bị dữ liệu test

```bash
# Tạo thư mục test với một số file
mkdir -p C:\TestData\documents C:\TestData\photos

echo "Hello CloudSafe!" > C:\TestData\documents\readme.txt
echo "Project notes"   > C:\TestData\documents\notes.txt
echo "Temporary logs"   > C:\TestData\documents\temp.log
```

---

## Bước 2 – Đăng ký tài khoản & Đăng nhập CLI

### 1. Đăng ký tài khoản (qua Swagger UI hoặc cURL)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@cloudsafe.io",
    "password": "Demo@12345",
    "displayName": "CloudSafe Demo User"
  }'
```

### 2. Đăng nhập qua CLI
```bash
java -jar cloudsafe.jar login \
  --email demo@cloudsafe.io \
  --password "Demo@12345" \
  --server http://localhost:8080
```

---

## Bước 3 – Đăng ký thiết bị (kèm File Exclusions)

Gọi API đăng ký thiết bị với watchPath và cấu hình loại bỏ các file `.log` (excluded_patterns = *.log):
```bash
curl -X POST http://localhost:8080/api/devices \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My-Laptop",
    "operatingSystem": "Windows 11",
    "watchPath": "C:\\TestData",
    "autoBackupEnabled": true,
    "excludedPatterns": "*.log"
  }'
```
*Lưu lại deviceId trả về trong JSON response.*

---

## Bước 4 – Chạy Backup thủ công (Kiểm tra Exclusions)

```bash
java -jar cloudsafe.jar backup C:\TestData \
  --device <deviceId> \
  --type FULL
```
**Kết quả xác minh:**
- Quét qua thư mục `C:\TestData`.
- Tệp `temp.log` sẽ bị bỏ qua vì khớp với cấu hình loại trừ `*.log`.
- Chỉ tải lên `readme.txt` và `notes.txt`.

---

## Bước 5 – Đồng bộ hóa dữ liệu 2 chiều (Sync Command)

1. Xóa tệp cục bộ `readme.txt`.
2. Tạo thêm tệp mới cục bộ `newfile.txt`:
   ```bash
   echo "New file content" > C:\TestData\documents\newfile.txt
   ```
3. Chạy lệnh đồng bộ hóa:
   ```bash
   java -jar cloudsafe.jar sync C:\TestData --device <deviceId>
   ```
**Kết quả xác minh:**
- Hệ thống phát hiện thiếu `readme.txt` cục bộ -> Tự động tải từ Cloud về giải mã khôi phục.
- Hệ thống phát hiện `newfile.txt` mới ở cục bộ -> Tự động kích hoạt incremental backup đẩy lên Cloud.

---

## Bước 6 – Chạy ngầm tự động (Daemon Mode)

```bash
java -jar cloudsafe.jar daemon --device <deviceId> --interval 10
```
**Kết quả xác minh:**
- CLI khởi động tiến trình ngầm, gửi heartbeat báo trạng thái Online sau mỗi 10 giây.
- Tạo một Job backup PENDING từ Server API (hoặc Spring Boot Scheduler).
- CLI Daemon tự động nhận biết, tải và sao lưu thư mục watchPath lên Cloud Storage mà không cần can thiệp thủ công.

---

## Bước 7 – Đăng xuất & Thu hồi Token (Security check)

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <token>"
```
**Xác minh Bảo mật:**
- Sau khi gọi API Logout, dùng lại token cũ để gọi `/api/devices` hoặc `/api/storage/usage` sẽ nhận ngay mã lỗi 403 Forbidden / 401 Unauthorized vì token đã bị lưu vào Blacklist của Server.

---

## Bước 8 – Xem dung lượng sử dụng & Lịch sử
```bash
java -jar cloudsafe.jar storage
java -jar cloudsafe.jar history
```
Dung lượng lưu trữ và số file hiển thị chính xác theo dữ liệu đồng bộ.
