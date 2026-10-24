CREATE TABLE roles (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    full_name     VARCHAR(100) NULL,
    email         VARCHAR(120) NULL UNIQUE,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6)  NOT NULL
);

CREATE TABLE users_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- Default Roles
INSERT INTO roles (name) VALUES 
('ROLE_ADMIN'), 
('ROLE_LECTURER'), 
('ROLE_STUDENT'), 
('ROLE_USER');

-- Demo Users (Password for all accounts is 'Password@123')
INSERT INTO users (username, password_hash, full_name, email, enabled, created_at) VALUES
('admin', '{bcrypt}$2a$10$g10CzKEohgC65pUnAWE3yepaXQ9PV3NtCGGcc12u7itGRUjnxfdq2', 'Quản Trị Viên Hệ Thống', 'admin@fptaptech.edu.vn', TRUE, NOW(6)),
('lecturer1', '{bcrypt}$2a$10$g10CzKEohgC65pUnAWE3yepaXQ9PV3NtCGGcc12u7itGRUjnxfdq2', 'Giảng Viên Nguyễn Văn A', 'lecturer1@fptaptech.edu.vn', TRUE, NOW(6)),
('lecturer2', '{bcrypt}$2a$10$g10CzKEohgC65pUnAWE3yepaXQ9PV3NtCGGcc12u7itGRUjnxfdq2', 'Giảng Viên Trần Thị B', 'lecturer2@fptaptech.edu.vn', TRUE, NOW(6)),
('student1', '{bcrypt}$2a$10$g10CzKEohgC65pUnAWE3yepaXQ9PV3NtCGGcc12u7itGRUjnxfdq2', 'Học Viên Lê Hoàng', 'student1@fptaptech.edu.vn', TRUE, NOW(6)),
('student2', '{bcrypt}$2a$10$g10CzKEohgC65pUnAWE3yepaXQ9PV3NtCGGcc12u7itGRUjnxfdq2', 'Học Viên Phạm Thảo', 'student2@fptaptech.edu.vn', TRUE, NOW(6)),
('student3', '{bcrypt}$2a$10$g10CzKEohgC65pUnAWE3yepaXQ9PV3NtCGGcc12u7itGRUjnxfdq2', 'Học Viên Vũ Minh', 'student3@fptaptech.edu.vn', TRUE, NOW(6));

-- Assign Roles
-- Admin
INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- Lecturer 1
INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'lecturer1' AND r.name = 'ROLE_LECTURER';

-- Lecturer 2
INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'lecturer2' AND r.name = 'ROLE_LECTURER';

-- Students
INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username IN ('student1', 'student2', 'student3') AND r.name = 'ROLE_STUDENT';
