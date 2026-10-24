# Cổng Giao Bài Tập Học Viên - FPT Aptech (Assignment Portal)
> **Spring Boot 4.1 · Spring Security 7 · Spring Data JPA · Flyway · Thymeleaf · MySQL 8.4**

Dự án mở rộng hoàn chỉnh theo tài liệu hướng dẫn và đề bài ứng dụng chấm điểm 100đ (Mục 17 & 18): Hệ thống phân quyền theo vai trò (Role-Based Access Control) và quyền sở hữu dữ liệu (Data Ownership) dành cho Học viên, Giảng viên và Quản trị viên.

---

## 1. Môi trường & Yêu cầu hệ thống

- **Ngôn ngữ:** Java 17 (tương thích hoàn toàn Java 25)
- **Framework:** Spring Boot 4.1.2 (Spring Framework 6.x / Spring Security 7)
- **Quản lý dữ liệu:** Spring Data JPA / Hibernate ORM
- **Template Engine:** Thymeleaf 3.x + `thymeleaf-extras-springsecurity6`
- **Database:** MySQL 8.4 (Chạy qua Docker Compose trên port `3307`)
- **Database Migration:** Flyway (`V1__init_schema.sql`, `V2__create_tasks.sql`, `V3__create_assignments.sql`)
- **Kiểm thử tự động:** JUnit 5, MockMvc, H2 in-memory test database (profile `test`)

---

## 2. Hướng dẫn khởi động ứng dụng

### Bước 1: Khởi động cơ sở dữ liệu MySQL 8.4

Chạy file `compose.yaml` có sẵn trong thư mục dự án:
```bash
docker compose up -d
```
*Ghi chú: Cơ sở dữ liệu chạy trên cổng `3307` (`127.0.0.1:3307`), Database name: `assignment_portal`, user: `root`, password: `root`.*

### Bước 2: Chạy kiểm thử tự động (Unit & Integration Security Tests)

```bash
./mvnw clean test
```
*Toàn bộ 20 kịch bản kiểm thử (TaskSecurityTest & AssignmentSecurityTest T01 - T10) sẽ chạy trên H2 in-memory database và phải đạt 100% SUCCESS.*

### Bước 3: Khởi chạy ứng dụng Web

```bash
./mvnw spring-boot:run
```

Sau khi ứng dụng khởi động thành công, truy cập trình duyệt tại:
👉 **[http://localhost:8080](http://localhost:8080)**

---

## 3. Danh sách tài khoản kiểm thử mẫu (Seed Accounts)

| STT | Tài khoản (Username) | Mật khẩu chung | Họ và tên | Vai trò (Role) | Chức năng chính |
|:---:|:---|:---|:---|:---|:---|
| 1 | **admin** | `Password@123` | Quản Trị Viên Hệ Thống | `ROLE_ADMIN` | Giám sát thống kê toàn trường, quản lý phân quyền tài khoản |
| 2 | **lecturer1** | `Password@123` | Giảng Viên Nguyễn Văn A | `ROLE_LECTURER` | Quản lý Bài tập 1 (DRAFT) và Bài tập 2 (OPEN) |
| 3 | **lecturer2** | `Password@123` | Giảng Viên Trần Thị B | `ROLE_LECTURER` | Quản lý Bài tập 3 (CLOSED), chấm điểm học viên |
| 4 | **student1** | `Password@123` | Học Viên Lê Hoàng | `ROLE_STUDENT` | Được giao Bài tập 2 (vào nộp bài trực tuyến) |
| 5 | **student2** | `Password@123` | Học Viên Phạm Thảo | `ROLE_STUDENT` | Được giao Bài 2 & 3, đã có bài nộp Bài 3 được chấm 9.50 |
| 6 | **student3** | `Password@123` | Học Viên Vũ Minh | `ROLE_STUDENT` | Được giao Bài tập 3 |

*Lưu ý bảo mật: Đăng ký tài khoản mới trên giao diện `/registration` sẽ tự động nhận vai trò `ROLE_STUDENT`. Không thể tự cấp quyền LECTURER hay ADMIN qua form đăng ký (chặn mass assignment).*

---

## 4. Ma trận URL × Vai trò × Phạm vi dữ liệu

| Method | URL | Mô tả nghiệp vụ | Quyền truy cập | Phạm vi dữ liệu (Data Scope) |
|:---|:---|:---|:---|:---|
| `GET` | `/` | Trang chủ giới thiệu hệ thống | Public | Công khai |
| `GET` | `/login` | Form đăng nhập | Public | Công khai |
| `POST` | `/login` | Xử lý đăng nhập bằng Spring Security FormLogin | Public + CSRF | Toàn hệ thống |
| `GET` | `/registration` | Form đăng ký học viên | Public | Công khai |
| `POST` | `/registration` | Đăng ký tài khoản mới (gán mặc định `ROLE_STUDENT`) | Public + CSRF | Tạo mới tài khoản học viên |
| `GET` | `/home` | Dashboard người dùng sau đăng nhập | Authenticated | Cá nhân hóa theo vai trò |
| `POST` | `/logout` | Đăng xuất, hủy phiên làm việc | Authenticated + CSRF | Phiên hiện tại |
| `GET` | `/student/assignments` | Danh sách bài tập đang mở được giao cho học viên | `STUDENT` | Chỉ các bài `OPEN` mà học viên được gán |
| `GET` | `/student/assignments/{id}` | Chi tiết bài tập & Form nộp bài | `STUDENT` | Chỉ bài tập được gán cho học viên |
| `POST` | `/student/assignments/{id}/submit` | Nộp hoặc nộp lại bài tập | `STUDENT` + CSRF | Cập nhật bản ghi Submission của chính mình |
| `GET` | `/student/history` | Lịch sử nộp bài, điểm số & nhận xét | `STUDENT` | Chỉ các bài nộp của học viên đó |
| `GET` | `/lecturer/assignments` | Danh sách bài tập do giảng viên phụ trách | `LECTURER` | Chỉ bài do giảng viên hiện tại tạo |
| `GET` | `/lecturer/assignments/new` | Form tạo bản thảo bài tập mới | `LECTURER` | N/A |
| `POST` | `/lecturer/assignments` | Lưu bài tập mới (DRAFT) | `LECTURER` + CSRF | Gán `lecturer_id` từ SecurityContext |
| `GET` | `/lecturer/assignments/{id}/edit` | Form sửa bài tập | `LECTURER` (Owner) | Chỉ chủ sở hữu bài tập (404 nếu không phải) |
| `POST` | `/lecturer/assignments/{id}` | Cập nhật thông tin bài tập | `LECTURER` (Owner) + CSRF | Chỉ chủ sở hữu bài tập |
| `GET` | `/lecturer/assignments/{id}/open` | Màn hình chọn học sinh & Mở bài | `LECTURER` (Owner) | Chỉ chủ sở hữu bài tập |
| `POST` | `/lecturer/assignments/{id}/open` | Mở bài tập (DRAFT -> OPEN) | `LECTURER` (Owner) + CSRF | Chỉ chủ sở hữu bài tập |
| `POST` | `/lecturer/assignments/{id}/close` | Đóng bài tập (OPEN -> CLOSED) | `LECTURER` (Owner) + CSRF | Chỉ chủ sở hữu bài tập |
| `GET` | `/lecturer/assignments/{id}/submissions` | Danh sách bài nộp của học viên | `LECTURER` (Owner) | Chỉ bài nộp thuộc bài tập do mình phụ trách |
| `GET` | `/lecturer/assignments/{id}/submissions/{subId}/grade` | Form chấm điểm bài nộp | `LECTURER` (Owner) | Chỉ sau khi bài CLOSED; chỉ chấm 1 lần |
| `POST` | `/lecturer/assignments/{id}/submissions/{subId}/grade` | Lưu điểm (0.00-10.00) & feedback | `LECTURER` (Owner) + CSRF | Chỉ chủ sở hữu bài tập |
| `GET` | `/admin` | Bảng giám sát thống kê toàn hệ thống | `ADMIN` | Toàn bộ số liệu bài tập & bài nộp |
| `GET` | `/admin/users` | Danh sách tài khoản người dùng | `ADMIN` | Toàn bộ tài khoản |
| `POST` | `/admin/users/{id}/roles` | Gán vai trò cho người dùng | `ADMIN` + CSRF | Quản lý vai trò có kiểm soát |
| `GET` | `/tasks` | Danh sách công việc cá nhân (Tutorial) | Authenticated | Chỉ công việc của người dùng hiện tại |
| `GET` | `/admin/tasks` | Giám sát toàn bộ công việc cá nhân | `ADMIN` | Toàn bộ công việc trong hệ thống |

---

## 5. Cấu trúc mô hình dữ liệu (Database Schema)

```
                 +-------------------+
                 |       users       |
                 +-------------------+
                 | id (PK)           |
                 | username (UQ)     |
                 | password_hash     |
                 | full_name         |
                 | email (UQ)        |
                 | enabled           |
                 | created_at        |
                 +-------------------+
                    | 1         1 |
                    |             |
+-------------------+ |         | +--------------------+
|    users_roles    | |         | |    assignments     |
+-------------------+ |         | +--------------------+
| user_id (FK)      |<+         | | id (PK)            |
| role_id (FK)      |           | | title              |
+-------------------+           | | description        |
        |                       | | deadline           |
        v                       | | status             |
+-------------------+           | | lecturer_id (FK)   |<--+ (Owner)
|       roles       |           | | created_at         |
+-------------------+           | +--------------------+
| id (PK)           |           |     | 1          | 1
| name (UQ)         |           |     |            |
+-------------------+           |     v            v
                                | +-------------------+  +-------------------+
                                | |assignment_student |  |    submissions    |
                                | +-------------------+  +-------------------+
                                | |assignment_id (FK) |  | id (PK)           |
                                | |student_id (FK)    |<-| assignment_id (FK)|
                                +>| [PK: (aid, sid)]  |  | student_id (FK)   |<--+ (Student)
                                  +-------------------+  | content           |
                                                         | submitted_at      |
                                                         | score (DECIMAL)   |
                                                         | feedback          |
                                                         | [UQ: (aid, sid)]  |
                                                         +-------------------+
```

---

## 6. Báo cáo kiểm thử tự động (Chứng minh các kịch bản T01 - T10)

Lệnh thực thi:
```bash
./mvnw -Dtest=AssignmentSecurityTest test
```

| Mã kịch bản | Kịch bản kiểm thử bảo mật | Kết quả xác minh |
|:---:|:---|:---:|
| **T01** | **STUDENT A không đọc hoặc sửa Submission của B bằng cách đổi ID:** Truy cập bài tập của sinh viên khác bị chặn với mã lỗi HTTP 404. | **PASSED** |
| **T02** | **LECTURER A không sửa/chấm bài của LECTURER B:** Giảng viên A cố gắng sửa hoặc chấm bài của Giảng viên B bị trả về HTTP 404, database không thay đổi. | **PASSED** |
| **T03** | **Học viên chưa được giao bài không nộp được dù đoán đúng assignmentId:** Hệ thống truy vấn theo cặp `(assignment_id, student_username)`, trả về HTTP 404 nếu chưa được phân công. | **PASSED** |
| **T04** | **Bài OPEN hết hạn hoặc CLOSED đều từ chối nộp; đúng deadline còn được nộp:** Kiểm tra nghiêm ngặt bằng đồng hồ server `Instant.now() > deadline` hoặc `status == CLOSED`, trả về HTTP 400 Bad Request. | **PASSED** |
| **T05** | **POST thiếu hoặc sai CSRF bị từ chối:** Spring Security chặn yêu cầu và trả về HTTP 403 Forbidden, dữ liệu trong database giữ nguyên vẹn. | **PASSED** |
| **T06** | **Nộp lại giữ đúng một Submission; đổi request studentId không chuyển chủ:** Sử dụng ràng buộc UNIQUE `(assignment_id, student_id)`, ghi đè nội dung và cập nhật `submitted_at` theo đồng hồ server; `student_id` lấy tuyệt đối từ SecurityContext. | **PASSED** |
| **T07** | **Điểm âm, >10 hoặc nhiều hơn 2 số lẻ không được lưu:** Bean Validation `@DecimalMin("0.00")`, `@DecimalMax("10.00")`, `@Digits(integer=2, fraction=2)` chặn ngay ở tầng DTO. | **PASSED** |
| **T08** | **STUDENT và ADMIN không gọi được chức năng chấm điểm:** Phân quyền URL và `@PreAuthorize("hasRole('LECTURER')")` từ chối truy cập với HTTP 403 Forbidden. Quản trị viên không can thiệp sửa điểm. | **PASSED** |
| **T09** | **Tiêu đề chứa HTML/script hiển thị dạng text:** Thymeleaf tự động escape dữ liệu với `th:text`, thẻ `<script>` hiển thị dạng chuỗi an toàn `&lt;script&gt;`, ngăn chặn hoàn toàn XSS. | **PASSED** |
| **T10** | **Form đăng ký không cấp được ROLE_ADMIN dù request gửi thêm roles:** Đăng ký công khai cố định cấp `ROLE_STUDENT`, sử dụng `@InitBinder` giới hạn trường cho phép để ngăn chặn triệt để lỗ hổng Mass Assignment. | **PASSED** |

---

## 7. Giải thích kỹ thuật: Danh tính Session, Chặn IDOR & Xử lý Hạn nộp

### 1. Cách lấy danh tính từ Session (Server-side Authentication)
Trong toàn bộ hệ thống, server **tuyệt đối không tin cậy** các tham số `userId`, `studentId`, `lecturerId`, hoặc `username` do client truyền lên qua form parameters, query strings hay hidden inputs:
```java
private String currentUsername() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
}
```
Tại tầng Service, đối tượng `AppUser` luôn được truy vấn từ cơ sở dữ liệu dựa trên danh tính đã được Spring Security xác thực trong Session (`currentUsername()`). Điều này đảm bảo kẻ tấn công không thể mạo danh bất kỳ tài khoản nào khác.

### 2. Kỹ thuật chặn lỗi IDOR (Insecure Direct Object Reference)
IDOR xảy ra khi ứng dụng chỉ tìm kiếm đối tượng theo `id` (`findById(id)`) mà không kiểm tra quyền sở hữu. 
Trong dự án này, mọi truy vấn dữ liệu nhạy cảm đều sử dụng truy vấn kép kết hợp ID và chủ sở hữu:
```java
// Giảng viên chỉ xem/sửa bài tập của chính mình:
assignmentRepository.findByIdAndLecturer_Username(id, currentUsername())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài tập"));

// Học viên chỉ truy cập bài tập được phân công:
assignmentRepository.findByIdAndAssignedStudent(id, currentUsername())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài tập được giao"));
```
*Lưu ý:* Khi không tìm thấy hoặc bản ghi thuộc về người khác, hệ thống đều trả về mã lỗi **HTTP 404 (Not Found)** thay vì 403, giúp ngăn chặn kẻ xấu dò quét xem ID đó có tồn tại trên hệ thống hay không (tránh Information Disclosure).

### 3. Xử lý hạn nộp (Deadline Enforcement)
- **Sử dụng chuẩn `Instant`:** Mọi thời điểm được lưu trữ bằng `Instant` (UTC/Epoch) với độ chính xác cao.
- **Hiển thị theo múi giờ chuẩn:** Giao diện Thymeleaf sử dụng `DateTimeFormatter` định dạng theo múi giờ `Asia/Ho_Chi_Minh`.
- **Kiểm tra theo đồng hồ server:** Khi học viên bấm gửi bài, server lấy thời gian thực `Instant now = Instant.now()`. Dù client có mở form trước hạn nhưng nếu gửi request sau thời điểm deadline, server lập tức từ chối với lỗi 400 Bad Request:
```java
Instant now = Instant.now();
if (now.isAfter(assignment.getDeadline())) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đã quá hạn nộp bài");
}
```
- **Thời gian nộp bài (`submitted_at`)** được gán trực tiếp từ đồng hồ server (`Instant.now()`), không nhận bất kỳ giá trị thời gian nào từ client gửi lên.
