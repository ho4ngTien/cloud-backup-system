# 🎬 CloudSafe – Kịch bản Demo End-to-End (Giai đoạn 12)

Tài liệu này mô tả kịch bản demo hoàn chỉnh theo đúng 12 bước từ đăng ký đến xem lịch sử backup.

---

## Điều kiện tiên quyết

| Yêu cầu | Kiểm tra |
|---|---|
| Server đang chạy | `curl http://localhost:8080/actuator/health` |
| PostgreSQL online | `docker ps \| grep postgres` |
| CLI đã build | `java -jar cloudsafe.jar --version` |
| Thư mục test tồn tại | `C:\TestData\` (Windows) hoặc `/tmp/testdata/` (Linux) |

---

## Bước 1 – Chuẩn bị dữ liệu test

```bash
# Tạo thư mục test với một số file
mkdir -p C:\TestData\documents C:\TestData\photos

echo "Hello CloudSafe!" > C:\TestData\documents\readme.txt
echo "Project notes"   > C:\TestData\documents\notes.txt
# Thêm 1 file ảnh nhỏ để demo
```

---

## Bước 2 – Đăng ký tài khoản

### Gọi API trực tiếp (hoặc qua Swagger UI)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@cloudsafe.io",
    "password": "Demo@12345",
    "displayName": "CloudSafe Demo User"
  }'
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "userId": "uuid-here",
  "email": "demo@cloudsafe.io",
  "displayName": "CloudSafe Demo User",
  "roles": ["ROLE_USER"]
}
```

---

## Bước 3 – Đăng nhập qua CLI

```bash
java -jar cloudsafe.jar login \
  --email demo@cloudsafe.io \
  --password "Demo@12345" \
  --server http://localhost:8080
```

**Output:**
```
✅ Login successful!
   Welcome, CloudSafe Demo User
   Email : demo@cloudsafe.io
   Token saved to ~/.cloudsafe/config.json
```

---

## Bước 4 – Đăng ký thiết bị

```bash
curl -X POST http://localhost:8080/api/devices \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My-Laptop",
    "operatingSystem": "Windows 11",
    "watchPath": "C:\\TestData",
    "autoBackupEnabled": true
  }'
```

**Lưu lại `deviceId` từ response!**

---

## Bước 5 – Chọn thư mục và chạy Backup

```bash
java -jar cloudsafe.jar backup C:\TestData \
  --device <deviceId> \
  --type FULL
```

**Output:**
```
☁️  CloudSafe Backup
   Source : C:\TestData
   Type   : FULL

✅ Job created: abc-123-def-456
📂 Scanning: C:\TestData
   Found 2 files

   [1/2] documents/readme.txt
         ✅ SHA-256: a1b2c3...
   [2/2] documents/notes.txt
         ✅ SHA-256: d4e5f6...

✅ Backup completed successfully!
   Job ID : abc-123-def-456
   Check your email for a confirmation.
```

---

## Bước 6 – Upload lên Cloud Storage ✅

> *(Đây là bước tự động trong quá trình backup – CLI đã upload lên GCS)*

Kiểm tra trên GCS Console:
```bash
gsutil ls gs://cloudsafe-backup/<jobId>/
```

---

## Bước 7 – Xóa file local (để test restore)

```bash
# Windows
del "C:\TestData\documents\readme.txt"

# hoặc Linux
rm /tmp/testdata/documents/readme.txt
```

---

## Bước 8 – Restore từ Cloud

```bash
# Lấy versionId từ lịch sử
java -jar cloudsafe.jar history

# Restore toàn bộ
java -jar cloudsafe.jar restore \
  --version <versionId> \
  --device <deviceId> \
  --output C:\TestData\Restored \
  --type FULL
```

**Output:**
```
☁️  CloudSafe Restore
   Version ID : bcd-234-efg-567
   Target     : C:\TestData\Restored
   Type       : FULL

📥 Restoring 2 file(s) to: C:\TestData\Restored

   [1/2] documents/readme.txt
         ✅ Verified
   [2/2] documents/notes.txt
         ✅ Verified

✅ Restore complete: 2 succeeded, 0 failed
```

---

## Bước 9 – File quay trở lại ✅

```bash
# Kiểm tra file đã restore
cat "C:\TestData\Restored\documents\readme.txt"
# Output: Hello CloudSafe!
```

---

## Bước 10 – Kiểm tra Email

Kiểm tra hòm thư của `demo@cloudsafe.io` – bạn sẽ nhận được:

| Email | Nội dung |
|---|---|
| ✅ Backup thành công | Job ID, số file, dung lượng, thư mục nguồn |
| ✅ Restore thành công | Restore ID, thư mục đích, thời gian |

---

## Bước 11 – Xem lịch sử Backup

```bash
java -jar cloudsafe.jar history --count 5
```

**Output:**
```
☁️  CloudSafe Backup History
   Job ID                                Type          Status      Files    Created
   ------------------------------------------------------------------------------------------
   abc-123-def-456                       FULL          COMPLETED   2        2024-01-02T10:00
   ...

   Total records: 1
```

---

## Bước 12 – Xem dung lượng sử dụng

```bash
java -jar cloudsafe.jar storage
```

**Output:**
```
☁️  CloudSafe Storage
   Used      : 0.02 MB
   Quota     : 10.00 GB
   Available : 9.99 GB
   Used %   : 0.0%

   [░░░░░░░░░░░░░░░░░░░░] 0.0%
```

---

## API Swagger UI

Truy cập: **http://localhost:8080/swagger-ui.html**

Các endpoint chính:
- `POST /api/auth/register` – Đăng ký
- `POST /api/auth/login` – Đăng nhập
- `POST /api/devices` – Đăng ký thiết bị
- `POST /api/backups/start` – Bắt đầu backup
- `POST /api/backups/commit` – Hoàn tất backup
- `GET  /api/backups` – Lịch sử
- `POST /api/restore/start` – Bắt đầu restore
- `GET  /api/storage/usage` – Dung lượng

---

## Kiến trúc cuối cùng

```
                     USER
                      │
               CloudSafe CLI (Java + Picocli)
                      │ HTTPS (JWT Bearer)
             Spring Boot REST API (:8080)
                      │
      ┌───────────────┼────────────────┐
      │               │                │
 PostgreSQL    Google Cloud Storage   Gmail SMTP
      │               │                │
 Metadata      Encrypted Files     Notifications
 (backup_jobs  (AES-256/GCM)       (Thymeleaf HTML)
  backup_files  .enc files in
  users, etc.)  gs://bucket/jobId/
```

---

> **Demo thành công khi:**
> - ✅ File được backup lên GCS
> - ✅ File restore lại đúng nội dung (SHA-256 khớp)
> - ✅ Email nhận được sau backup và restore
> - ✅ Lịch sử backup hiển thị đúng
