CREATE TABLE assignments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(150)  NOT NULL,
    description VARCHAR(4000) NOT NULL,
    deadline    DATETIME(6)   NOT NULL,
    status      VARCHAR(20)   NOT NULL,
    lecturer_id BIGINT        NOT NULL,
    created_at  DATETIME(6)   NOT NULL,
    CONSTRAINT fk_assignments_lecturer FOREIGN KEY (lecturer_id) REFERENCES users(id),
    CONSTRAINT ck_assignments_status CHECK (status IN ('DRAFT', 'OPEN', 'CLOSED'))
);

CREATE INDEX idx_assignments_lecturer ON assignments(lecturer_id);
CREATE INDEX idx_assignments_status ON assignments(status);

CREATE TABLE assignment_student (
    assignment_id BIGINT NOT NULL,
    student_id    BIGINT NOT NULL,
    PRIMARY KEY (assignment_id, student_id),
    CONSTRAINT fk_as_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_as_student FOREIGN KEY (student_id) REFERENCES users(id)
);

CREATE TABLE submissions (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT        NOT NULL,
    student_id    BIGINT        NOT NULL,
    content       VARCHAR(5000) NOT NULL,
    submitted_at  DATETIME(6)   NOT NULL,
    score         DECIMAL(4,2)  NULL,
    feedback      VARCHAR(2000) NULL,
    CONSTRAINT uq_submissions_assignment_student UNIQUE (assignment_id, student_id),
    CONSTRAINT fk_submissions_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_submissions_student FOREIGN KEY (student_id) REFERENCES users(id)
);

-- Seed Initial Demo Data per Section 17.3:
-- Assignment 1: DRAFT by lecturer1
INSERT INTO assignments (id, title, description, deadline, status, lecturer_id, created_at)
SELECT 1, 
       'Bài tập 1: Thiết kế REST API với Spring Boot (Bản thảo)', 
       'Xây dựng các endpoint RESTful tuân thủ chuẩn Richardson Maturity Level 2. Yêu cầu xử lý exception tập trung với @ControllerAdvice.', 
       DATE_ADD(NOW(6), INTERVAL 7 DAY), 
       'DRAFT', 
       u.id, 
       NOW(6)
FROM users u WHERE u.username = 'lecturer1';

-- Assignment 2: OPEN by lecturer1, assigned to student1 and student2
INSERT INTO assignments (id, title, description, deadline, status, lecturer_id, created_at)
SELECT 2, 
       'Bài tập 2: Xác thực và Phân quyền với Spring Security 7', 
       'Triển khai Form Login, BCrypt Password Encoder, Method Security (@EnableMethodSecurity) và Thymeleaf CSRF protection. Sinh viên nộp link GitHub repository hoặc nội dung báo cáo code.', 
       DATE_ADD(NOW(6), INTERVAL 3 DAY), 
       'OPEN', 
       u.id, 
       NOW(6)
FROM users u WHERE u.username = 'lecturer1';

INSERT INTO assignment_student (assignment_id, student_id)
SELECT 2, u.id FROM users u WHERE u.username IN ('student1', 'student2');

-- Assignment 3: CLOSED by lecturer2, assigned to student2 and student3, with a graded submission from student2
INSERT INTO assignments (id, title, description, deadline, status, lecturer_id, created_at)
SELECT 3, 
       'Bài tập 3: Tối ưu truy vấn dữ liệu với Spring Data JPA & Flyway', 
       'Sử dụng @EntityGraph để tránh N+1 query problem, cấu hình Flyway migration, kiểm thử tự động với MockMvc và H2 in-memory test database.', 
       DATE_SUB(NOW(6), INTERVAL 1 DAY), 
       'CLOSED', 
       u.id, 
       DATE_SUB(NOW(6), INTERVAL 5 DAY)
FROM users u WHERE u.username = 'lecturer2';

INSERT INTO assignment_student (assignment_id, student_id)
SELECT 3, u.id FROM users u WHERE u.username IN ('student2', 'student3');

-- Submission for Assignment 3 by student2 (Graded with 9.50 and feedback)
INSERT INTO submissions (assignment_id, student_id, content, submitted_at, score, feedback)
SELECT 3, 
       u.id, 
       'https://github.com/student2/jpa-flyway-optimization-assignment. Đã hoàn thành cấu hình EntityGraph, Flyway migration V1, V2 và viết 10 unit tests bao phủ các ca kiểm thử bảo mật.', 
       DATE_SUB(NOW(6), INTERVAL 2 DAY), 
       9.50, 
       'Bài làm rất tốt, cấu hình EntityGraph chuẩn xác loại bỏ được triệt để N+1 query. Code sạch và có đầy đủ test cases.'
FROM users u WHERE u.username = 'student2';
