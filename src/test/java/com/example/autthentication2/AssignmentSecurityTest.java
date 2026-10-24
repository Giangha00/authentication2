package com.example.autthentication2;

import com.example.autthentication2.entity.*;
import com.example.autthentication2.repository.AssignmentRepository;
import com.example.autthentication2.repository.RoleRepository;
import com.example.autthentication2.repository.SubmissionRepository;
import com.example.autthentication2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AssignmentSecurityTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired AssignmentRepository assignmentRepository;
    @Autowired SubmissionRepository submissionRepository;

    AppUser lecturerA;
    AppUser lecturerB;
    AppUser studentA;
    AppUser studentB;
    AppUser studentUnassigned;
    AppUser adminUser;

    Assignment assignmentA_Open;
    Assignment assignmentA_Closed;
    Assignment assignmentB_Closed;
    Submission submissionStudentA;

    @BeforeEach
    void setup() {
        Role roleStudent = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_STUDENT")));
        Role roleLecturer = roleRepository.findByName("ROLE_LECTURER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_LECTURER")));
        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

        // Create Users
        lecturerA = new AppUser("test_lecturer_a", "unused", "Giảng viên A", "lec_a@test.com");
        lecturerA.getRoles().add(roleLecturer);
        lecturerA = userRepository.saveAndFlush(lecturerA);

        lecturerB = new AppUser("test_lecturer_b", "unused", "Giảng viên B", "lec_b@test.com");
        lecturerB.getRoles().add(roleLecturer);
        lecturerB = userRepository.saveAndFlush(lecturerB);

        studentA = new AppUser("test_student_a", "unused", "Học viên A", "stu_a@test.com");
        studentA.getRoles().add(roleStudent);
        studentA = userRepository.saveAndFlush(studentA);

        studentB = new AppUser("test_student_b", "unused", "Học viên B", "stu_b@test.com");
        studentB.getRoles().add(roleStudent);
        studentB = userRepository.saveAndFlush(studentB);

        studentUnassigned = new AppUser("test_student_c", "unused", "Học viên Không được giao", "stu_c@test.com");
        studentUnassigned.getRoles().add(roleStudent);
        studentUnassigned = userRepository.saveAndFlush(studentUnassigned);

        adminUser = new AppUser("test_admin_user", "unused", "Quản trị viên", "admin@test.com");
        adminUser.getRoles().add(roleAdmin);
        adminUser = userRepository.saveAndFlush(adminUser);

        // Assignment A (OPEN, deadline future)
        assignmentA_Open = new Assignment(lecturerA, "Bài tập A Open", "Mô tả A", Instant.now().plus(2, ChronoUnit.DAYS));
        assignmentA_Open.setStatus(AssignmentStatus.OPEN);
        assignmentA_Open.getAssignedStudents().add(studentA);
        assignmentA_Open.getAssignedStudents().add(studentB);
        assignmentA_Open = assignmentRepository.saveAndFlush(assignmentA_Open);

        // Assignment A (CLOSED, past deadline)
        assignmentA_Closed = new Assignment(lecturerA, "Bài tập A Closed", "Mô tả A Closed", Instant.now().minus(1, ChronoUnit.DAYS));
        assignmentA_Closed.setStatus(AssignmentStatus.CLOSED);
        assignmentA_Closed.getAssignedStudents().add(studentA);
        assignmentA_Closed = assignmentRepository.saveAndFlush(assignmentA_Closed);

        // Assignment B (CLOSED) owned by Lecturer B
        assignmentB_Closed = new Assignment(lecturerB, "Bài tập B Closed", "Mô tả B Closed", Instant.now().minus(1, ChronoUnit.DAYS));
        assignmentB_Closed.setStatus(AssignmentStatus.CLOSED);
        assignmentB_Closed.getAssignedStudents().add(studentB);
        assignmentB_Closed = assignmentRepository.saveAndFlush(assignmentB_Closed);

        // Submission of Student A on Assignment A Closed
        submissionStudentA = new Submission(assignmentA_Closed, studentA, "Bài làm của Student A");
        submissionStudentA = submissionRepository.saveAndFlush(submissionStudentA);
    }

    // T01: STUDENT A không đọc hoặc sửa Submission của B bằng cách đổi id
    @Test
    void t01_studentCannotAccessOtherStudentSubmission() throws Exception {
        // Create submission for student B on assignment B
        Submission submissionB = new Submission(assignmentB_Closed, studentB, "Bài làm bí mật của Student B");
        submissionB = submissionRepository.saveAndFlush(submissionB);

        // Student A tries to submit or edit under Student B's assignment context
        mvc.perform(get("/student/assignments/{id}", assignmentB_Closed.getId())
                .with(user("test_student_a").roles("STUDENT")))
                .andExpect(status().isNotFound());
    }

    // T02: LECTURER A không sửa/chấm bài của LECTURER B
    @Test
    void t02_lecturerCannotEditOrGradeOtherLecturerAssignment() throws Exception {
        // Lecturer A tries to edit Assignment B
        mvc.perform(get("/lecturer/assignments/{id}/edit", assignmentB_Closed.getId())
                .with(user("test_lecturer_a").roles("LECTURER")))
                .andExpect(status().isNotFound());

        // Create submission on Assignment B
        Submission subB = new Submission(assignmentB_Closed, studentB, "Nội dung bài B");
        subB = submissionRepository.saveAndFlush(subB);

        // Lecturer A tries to grade Submission B
        mvc.perform(post("/lecturer/assignments/{id}/submissions/{subId}/grade", assignmentB_Closed.getId(), subB.getId())
                .with(user("test_lecturer_a").roles("LECTURER")).with(csrf())
                .param("score", "9.00")
                .param("feedback", "Hack grade"))
                .andExpect(status().isNotFound());

        // Verify score in database remains null
        assertNull(submissionRepository.findById(subB.getId()).orElseThrow().getScore());
    }

    // T03: Học viên chưa được giao bài không nộp được dù đoán đúng assignmentId
    @Test
    void t03_unassignedStudentCannotSubmit() throws Exception {
        mvc.perform(post("/student/assignments/{id}/submit", assignmentA_Open.getId())
                .with(user("test_student_c").roles("STUDENT")).with(csrf())
                .param("content", "Em xin nộp dù không được giao"))
                .andExpect(status().isNotFound());

        assertFalse(submissionRepository.findByAssignment_IdAndStudent_Username(assignmentA_Open.getId(), "test_student_c").isPresent());
    }

    // T04: Bài OPEN hết hạn hoặc CLOSED đều từ chối nộp; đúng deadline còn được nộp
    @Test
    void t04_closedOrExpiredAssignmentRejectsSubmission() throws Exception {
        // Submitting to CLOSED assignment
        mvc.perform(post("/student/assignments/{id}/submit", assignmentA_Closed.getId())
                .with(user("test_student_a").roles("STUDENT")).with(csrf())
                .param("content", "Cố tình nộp bài khi đã đóng"))
                .andExpect(status().isBadRequest());

        // Submitting to EXPIRED OPEN assignment
        Assignment expiredOpen = new Assignment(lecturerA, "Expired Open", "Desc", Instant.now().minusSeconds(60));
        expiredOpen.setStatus(AssignmentStatus.OPEN);
        expiredOpen.getAssignedStudents().add(studentA);
        expiredOpen = assignmentRepository.saveAndFlush(expiredOpen);

        mvc.perform(post("/student/assignments/{id}/submit", expiredOpen.getId())
                .with(user("test_student_a").roles("STUDENT")).with(csrf())
                .param("content", "Cố tình nộp khi quá hạn"))
                .andExpect(status().isBadRequest());
    }

    // T05: POST thiếu hoặc sai CSRF bị từ chối; database không đổi
    @Test
    void t05_missingOrInvalidCsrfIsRejected() throws Exception {
        long beforeCount = submissionRepository.count();

        // Without CSRF
        mvc.perform(post("/student/assignments/{id}/submit", assignmentA_Open.getId())
                .with(user("test_student_a").roles("STUDENT"))
                .param("content", "Nộp thiếu CSRF"))
                .andExpect(status().isForbidden());

        // With invalid CSRF
        mvc.perform(post("/student/assignments/{id}/submit", assignmentA_Open.getId())
                .with(user("test_student_a").roles("STUDENT"))
                .with(csrf().useInvalidToken())
                .param("content", "Nộp sai CSRF"))
                .andExpect(status().isForbidden());

        assertEquals(beforeCount, submissionRepository.count());
    }

    // T06: Nộp lại giữ đúng một Submission; đổi request studentId không chuyển chủ
    @Test
    void t06_resubmitUpdatesSameRecordAndPreservesOwner() throws Exception {
        // Student A submits to Assignment A Open
        mvc.perform(post("/student/assignments/{id}/submit", assignmentA_Open.getId())
                .with(user("test_student_a").roles("STUDENT")).with(csrf())
                .param("content", "Bản nộp 1"))
                .andExpect(status().is3xxRedirection());

        Submission firstSub = submissionRepository.findByAssignment_IdAndStudent_Username(assignmentA_Open.getId(), "test_student_a").orElseThrow();
        Long firstId = firstSub.getId();

        // Student A resubmits with attempt to inject studentId / studentUsername
        mvc.perform(post("/student/assignments/{id}/submit", assignmentA_Open.getId())
                .with(user("test_student_a").roles("STUDENT")).with(csrf())
                .param("studentId", "9999")
                .param("studentUsername", "hacked_user")
                .param("content", "Bản nộp 2 cập nhật"))
                .andExpect(status().is3xxRedirection());

        // Verify ID remains the same, content updated, owner remains test_student_a
        Submission updatedSub = submissionRepository.findByAssignment_IdAndStudent_Username(assignmentA_Open.getId(), "test_student_a").orElseThrow();
        assertEquals(firstId, updatedSub.getId());
        assertEquals("Bản nộp 2 cập nhật", updatedSub.getContent());
        assertEquals("test_student_a", updatedSub.getStudent().getUsername());
    }

    // T07: Điểm âm, >10 hoặc nhiều hơn 2 số lẻ không được lưu
    @Test
    void t07_invalidScoreIsRejected() throws Exception {
        // Negative score
        mvc.perform(post("/lecturer/assignments/{id}/submissions/{subId}/grade", assignmentA_Closed.getId(), submissionStudentA.getId())
                .with(user("test_lecturer_a").roles("LECTURER")).with(csrf())
                .param("score", "-1.5")
                .param("feedback", "Invalid"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "score"));

        // Greater than 10
        mvc.perform(post("/lecturer/assignments/{id}/submissions/{subId}/grade", assignmentA_Closed.getId(), submissionStudentA.getId())
                .with(user("test_lecturer_a").roles("LECTURER")).with(csrf())
                .param("score", "10.5")
                .param("feedback", "Invalid"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "score"));

        // More than 2 decimal digits
        mvc.perform(post("/lecturer/assignments/{id}/submissions/{subId}/grade", assignmentA_Closed.getId(), submissionStudentA.getId())
                .with(user("test_lecturer_a").roles("LECTURER")).with(csrf())
                .param("score", "8.125")
                .param("feedback", "Invalid"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "score"));

        assertNull(submissionRepository.findById(submissionStudentA.getId()).orElseThrow().getScore());
    }

    // T08: STUDENT và ADMIN không gọi được chức năng chấm điểm
    @Test
    void t08_studentAndAdminCannotGrade() throws Exception {
        // STUDENT tries to grade
        mvc.perform(post("/lecturer/assignments/{id}/submissions/{subId}/grade", assignmentA_Closed.getId(), submissionStudentA.getId())
                .with(user("test_student_a").roles("STUDENT")).with(csrf())
                .param("score", "10.00")
                .param("feedback", "Tự chấm"))
                .andExpect(status().isForbidden());

        // ADMIN tries to grade (Admin does NOT have ROLE_LECTURER, strictly forbidden per Section 17.1 & F09)
        mvc.perform(post("/lecturer/assignments/{id}/submissions/{subId}/grade", assignmentA_Closed.getId(), submissionStudentA.getId())
                .with(user("test_admin_user").roles("ADMIN")).with(csrf())
                .param("score", "10.00")
                .param("feedback", "Admin tự chấm"))
                .andExpect(status().isForbidden());
    }

    // T09: Tiêu đề chứa HTML/script hiển thị dạng text
    @Test
    void t09_xssEscapingInThymeleaf() throws Exception {
        String xssTitle = "<script>alert('xss')</script>";
        Assignment xssAssignment = new Assignment(lecturerA, xssTitle, "Desc", Instant.now().plus(2, ChronoUnit.DAYS));
        xssAssignment.setStatus(AssignmentStatus.OPEN);
        xssAssignment.getAssignedStudents().add(studentA);
        xssAssignment = assignmentRepository.saveAndFlush(xssAssignment);

        mvc.perform(get("/student/assignments")
                .with(user("test_student_a").roles("STUDENT")))
                .andExpect(status().isOk())
                // Verify raw script tag is escaped as &lt;script&gt;
                .andExpect(content().string(containsString("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;")));
    }

    // T10: Form đăng ký không cấp được ROLE_ADMIN dù request gửi thêm roles
    @Test
    void t10_registrationCannotGrantAdminRole() throws Exception {
        mvc.perform(post("/registration")
                .with(csrf())
                .param("username", "attacker")
                .param("fullName", "Attacker User")
                .param("email", "attacker@test.com")
                .param("password", "Password@123")
                .param("confirmPassword", "Password@123")
                .param("roles", "ROLE_ADMIN")
                .param("role", "ROLE_ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        AppUser created = userRepository.findByUsername("attacker").orElseThrow();
        // Attacker must ONLY have ROLE_STUDENT
        assertTrue(created.hasRole("ROLE_STUDENT"));
        assertFalse(created.hasRole("ROLE_ADMIN"));
        assertFalse(created.hasRole("ROLE_LECTURER"));
    }
}
