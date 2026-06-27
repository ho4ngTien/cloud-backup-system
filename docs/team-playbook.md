# Sổ tay Lập trình Đội ngũ (Team Playbook)

Chào mừng bạn đến với đội ngũ phát triển **CloudSafe**! Tài liệu này định nghĩa các tiêu chuẩn kỹ thuật, quy tắc viết mã nguồn và quy trình phối hợp trên Git nhằm duy trì chất lượng mã nguồn cao nhất cho dự án.

---

## 1. Quy chuẩn Viết Code (Coding Standards)

Dự án sử dụng **Java 25** và framework **Spring Boot 3**. Mọi thành viên trong team cần tuân thủ các quy tắc sau:

### 1.1 Nguyên tắc Thiết kế phần mềm
- **SOLID**: Luôn áp dụng 5 nguyên tắc SOLID khi thiết kế class.
- **Clean Architecture & Modular Monolith**: Giữ các module chức năng (Authentication, User, Device, Backup, v.v.) tách biệt ở mức tối đa. Mỗi module chỉ nên giao tiếp qua API hoặc Event Listener, tránh tham chiếu vòng chéo nhau.
- **Single Responsibility**: Mỗi class, phương thức chỉ làm đúng một việc và làm thật tốt việc đó.

### 1.2 Quy chuẩn Java & Spring Boot
- **Đặt tên (Naming Conventions)**:
  - *Class/Interface*: CamelCase viết hoa chữ cái đầu (ví dụ: `BackupService`, `DeviceRepository`).
  - *Method/Variable*: camelCase viết thường chữ cái đầu (ví dụ: `createBackupJob()`, `totalSizeBytes`).
  - *Constant*: VIẾT HOA và phân cách bằng dấu gạch dưới (ví dụ: `MAX_RETRY_COUNT`).
  - *Package*: Viết thường toàn bộ, sử dụng danh từ số ít (ví dụ: `com.cloudsafe.backup.controller`).
- **DTO (Data Transfer Object)**: 
  - KHÔNG BAO GIỜ trả trực tiếp Entity JPA ra Controller (Presentation Layer). Hãy luôn map Entity sang DTO tương ứng bằng MapStruct hoặc mapper thủ công để tránh lộ thông tin nội bộ và cấu trúc DB.
- **Xử lý Exception (Exception Handling)**:
  - Sử dụng `@RestControllerAdvice` và `@ExceptionHandler` toàn cục để bắt lỗi và trả về JSON Error format chuẩn cho Client.
  - Sử dụng các Exception tự định nghĩa thừa kế từ `RuntimeException` (ví dụ: `ResourceNotFoundException`, `StorageLimitExceededException`).
- **Logging**:
  - Không sử dụng `System.out.println()`. Sử dụng annotation `@Slf4j` từ Lombok để ghi log.
  - Sử dụng phân cấp độ log hợp lý:
    - `INFO`: Ghi nhận các sự kiện quan trọng trong hệ thống (khởi động, luồng hoàn tất).
    - `DEBUG`: Ghi chi tiết tham số, giá trị phục vụ tìm lỗi khi phát triển.
    - `WARN`/`ERROR`: Ghi nhận các sự cố, lỗi phát sinh kèm StackTrace.

---

## 2. Quy trình làm việc với Git (Git Workflow)

Team sử dụng mô hình **Feature Branching Workflow** (GitHub Flow giản lược) để quản lý mã nguồn.

### 2.1 Nhánh chính (Protected Branches)
- `main`: Nhánh chạy production ổn định. Chỉ chấp nhận code từ PR (Pull Request) được duyệt và đã pass tất cả các bài kiểm tra tự động (CI).
- `develop`: Nhánh tích hợp chính cho dev. Tất cả các tính năng mới đều được merge vào đây trước khi release lên `main`.

### 2.2 Quy tắc đặt tên Nhánh phát triển (Branch Naming)
Mọi nhánh mới tách ra từ `develop` phải bắt đầu bằng tiền tố mô tả loại công việc:
- `feature/<tên-chức-năng>`: Thêm tính năng mới (ví dụ: `feature/jwt-auth`, `feature/s3-adapter`).
- `bugfix/<tên-lỗi>`: Sửa lỗi (ví dụ: `bugfix/connection-timeout`).
- `hotfix/<tên-lỗi>`: Sửa lỗi khẩn cấp trực tiếp từ nhánh production `main`.
- `refactor/<tên-refactor>`: Tái cấu trúc code nhưng không thay đổi chức năng.

### 2.3 Quy tắc viết Commit Message (Conventional Commits)
Thông điệp commit phải ngắn gọn, súc tích và tuân theo định dạng:
`type(scope): description`

Các loại `type` được chấp nhận:
- `feat`: Tính năng mới.
- `fix`: Sửa lỗi.
- `docs`: Chỉnh sửa tài liệu.
- `style`: Thay đổi định dạng code (khoảng trắng, format) không ảnh hưởng đến logic.
- `refactor`: Tái cấu trúc mã nguồn.
- `test`: Thêm hoặc sửa mã nguồn test.
- `chore`: Các việc vặt cấu hình build, dependencies.

*Ví dụ commit hợp lệ*:
- `feat(auth): implement jwt token generation on login`
- `fix(postgres): resolve database initial configuration connection timeout`
- `docs(readme): update deployment guide steps`

---

## 3. Quy trình phát triển một tính năng mới (Feature Lifecycle)

Khi bạn được giao phát triển một tính năng mới, hãy thực hiện theo chu trình chuẩn sau:

```
[Thiết kế Database] ──> [Đặc tả REST API Specs] ──> [Viết Mã Nguồn (Logic)] ──> [Viết Unit Test] ──> [Tạo Pull Request & Review]
```

1. **Thiết kế Database**:
   - Nếu tính năng yêu cầu bảng mới hoặc sửa đổi bảng hiện tại, hãy cập nhật thiết kế ERD trong [docs/erd/README.md](file:///c:/Users/N07/Documents/GitHub/cloud-backup-system/docs/erd/README.md) trước.
2. **Đặc tả REST API Specs**:
   - Thống nhất các endpoint, phương thức HTTP, cấu trúc Request Body và Response JSON trong tài liệu thiết kế.
3. **Viết Mã Nguồn (Implementation)**:
   - Viết code theo đúng phân tầng Layered Architecture. Chú ý validate dữ liệu đầu vào.
4. **Viết Unit/Integration Test**:
   - Đảm bảo độ bao phủ (test coverage) cho các Service xử lý nghiệp vụ chính đạt tối thiểu 80%.
5. **Tạo Pull Request (PR) & Review**:
   - Tạo PR từ nhánh của bạn vào `develop`. Ghi rõ mô tả thay đổi, đính kèm ảnh chụp/log test nếu có.
   - PR cần được ít nhất 1 thành viên khác trong team review và chấp thuận (Approve) trước khi merge.
