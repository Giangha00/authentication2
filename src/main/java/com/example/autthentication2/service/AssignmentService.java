package com.example.autthentication2.service;

import com.example.autthentication2.dto.*;
import com.example.autthentication2.entity.*;
import com.example.autthentication2.repository.AssignmentRepository;
import com.example.autthentication2.repository.SubmissionRepository;
import com.example.autthentication2.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Validated
@Transactional(readOnly = true)
public class AssignmentService {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATETIME_LOCAL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             SubmissionRepository submissionRepository,
                             UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private Pageable paging(int page) {
        return PageRequest.of(Math.max(0, page), 10,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
    }

    private Instant parseDeadline(String deadlineStr) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(deadlineStr.trim(), DATETIME_LOCAL_FORMATTER);
            return ldt.atZone(VIETNAM_ZONE).toInstant();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Định dạng thời hạn không hợp lệ");
        }
    }

    // ==================== LECTURER ACTIONS ====================

    @Transactional
    @PreAuthorize("hasRole('LECTURER')")
    public Assignment createAssignment(@Valid AssignmentForm form) {
        Instant deadline = parseDeadline(form.getDeadline());
        if (!deadline.isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thời hạn nộp bài phải ở thời điểm tương lai");
        }

        AppUser lecturer = userRepository.findByUsername(currentUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản không hợp lệ"));

        Assignment assignment = new Assignment(lecturer, form.getTitle().trim(), form.getDescription().trim(), deadline);
        return assignmentRepository.save(assignment);
    }

    @Transactional
    @PreAuthorize("hasRole('LECTURER')")
    public void updateAssignment(Long id, @Valid AssignmentForm form) {
        Assignment assignment = assignmentRepository.findByIdAndLecturer_Username(id, currentUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài tập"));

        if (assignment.isClosed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bài tập đã đóng, không thể sửa");
        }

        Instant deadline = parseDeadline(form.getDeadline());
        if (!deadline.isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thời hạn nộp bài phải ở thời điểm tương lai");
        }

        assignment.setTitle(form.getTitle().trim());
        assignment.setDescription(form.getDescription().trim());
        assignment.setDeadline(deadline);
    }

    @Transactional
    @PreAuthorize("hasRole('LECTURER')")
    public void openAssignment(Long id, Set<Long> studentIds) {
        Assignment assignment = assignmentRepository.findByIdAndLecturer_Username(id, currentUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài tập"));

        if (!assignment.getDeadline().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thời hạn bài tập đã qua, vui lòng cập nhật lại hạn trước khi mở");
        }

        if (studentIds != null && !studentIds.isEmpty()) {
            List<AppUser> students = userRepository.findAllById(studentIds);
            assignment.setAssignedStudents(new HashSet<>(students));
        }

        assignment.setStatus(AssignmentStatus.OPEN);
    }

    @Transactional
    @PreAuthorize("hasRole('LECTURER')")
    public void closeAssignment(Long id) {
        Assignment assignment = assignmentRepository.findByIdAndLecturer_Username(id, currentUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài tập"));

        assignment.setStatus(AssignmentStatus.CLOSED);
    }

    @PreAuthorize("hasRole('LECTURER')")
    public Page<AssignmentView> getLecturerAssignments(int page) {
        return assignmentRepository.findByLecturer_Username(currentUsername(), paging(page))
                .map(AssignmentView::from);
    }

    @PreAuthorize("hasRole('LECTURER')")
    public Assignment getLecturerAssignmentEntity(Long id) {
        return assignmentRepository.findByIdAndLecturer_Username(id, currentUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài tập"));
    }

    @PreAuthorize("hasRole('LECTURER')")
    public List<SubmissionView> getSubmissionsForAssignment(Long assignmentId) {
        // Ownership verification: ensures assignment belongs to current lecturer
        getLecturerAssignmentEntity(assignmentId);
        return submissionRepository.findByAssignment_Id(assignmentId).stream()
                .map(SubmissionView::from)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasRole('LECTURER')")
    public void gradeSubmission(Long assignmentId, Long submissionId, @Valid GradeForm form) {
        Assignment assignment = getLecturerAssignmentEntity(assignmentId);

        // F06: Chỉ được chấm sau khi bài tập đã đóng
        if (!assignment.isClosed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ được chấm điểm sau khi bài tập đã đóng (CLOSED)");
        }

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài nộp"));

        if (!submission.getAssignment().getId().equals(assignmentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài nộp không thuộc bài tập này");
        }

        // F06: Chấm điểm bản bắt buộc chỉ thực hiện một lần
        if (submission.isGraded()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bài nộp đã được chấm điểm, không được chấm lại");
        }

        // Validation for score
        if (form.getScore().compareTo(BigDecimal.ZERO) < 0 || form.getScore().compareTo(BigDecimal.TEN) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Điểm phải nằm trong thang điểm 0 - 10");
        }

        submission.setScore(form.getScore());
        submission.setFeedback(form.getFeedback().trim());
    }

    // ==================== STUDENT ACTIONS ====================

    @PreAuthorize("hasRole('STUDENT')")
    public Page<AssignmentView> getOpenAssignmentsForStudent(int page) {
        return assignmentRepository.findOpenAssignmentsForStudent(currentUsername(), paging(page))
                .map(AssignmentView::from);
    }

    @PreAuthorize("hasRole('STUDENT')")
    public AssignmentView getStudentAssignmentDetail(Long assignmentId) {
        Assignment assignment = assignmentRepository.findByIdAndAssignedStudent(assignmentId, currentUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài tập được giao"));
        return AssignmentView.from(assignment);
    }

    @Transactional
    @PreAuthorize("hasRole('STUDENT')")
    public void submitOrResubmit(Long assignmentId, @Valid SubmissionForm form) {
        // F03: Chỉ học viên được giao mới được nộp
        Assignment assignment = assignmentRepository.findByIdAndAssignedStudent(assignmentId, currentUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài tập hoặc bạn chưa được giao bài này"));

        // F03: Chỉ nộp khi trạng thái OPEN
        if (!assignment.isOpen()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bài tập không ở trạng thái mở");
        }

        // F03 / F04: Server clock check: tại đúng deadline vẫn cho nộp, sau deadline từ chối
        Instant now = Instant.now();
        if (now.isAfter(assignment.getDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đã quá hạn nộp bài");
        }

        // Student identity resolved strictly from SecurityContext (student_id cannot be spoofed)
        AppUser student = userRepository.findByUsername(currentUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản không hợp lệ"));

        // F04: Nộp lại cập nhật cùng một Submission, không sinh bản ghi trùng
        Submission submission = submissionRepository.findByAssignment_IdAndStudent_Username(assignmentId, currentUsername())
                .orElse(null);

        if (submission == null) {
            submission = new Submission(assignment, student, form.getContent().trim());
        } else {
            submission.setContent(form.getContent().trim());
            submission.setSubmittedAt(now); // updated from server clock
        }

        submissionRepository.save(submission);
    }

    @PreAuthorize("hasRole('STUDENT')")
    public SubmissionView getMySubmissionForAssignment(Long assignmentId) {
        return submissionRepository.findByAssignment_IdAndStudent_Username(assignmentId, currentUsername())
                .map(SubmissionView::from)
                .orElse(null);
    }

    @PreAuthorize("hasRole('STUDENT')")
    public Page<SubmissionView> getMySubmissionsHistory(int page) {
        return submissionRepository.findByStudent_Username(currentUsername(), paging(page))
                .map(SubmissionView::from);
    }

    // ==================== ADMIN ACTIONS ====================

    @PreAuthorize("hasRole('ADMIN')")
    public AdminStatDto getAdminStatistics() {
        long totalAssignments = assignmentRepository.count();
        long draft = assignmentRepository.countByStatus(AssignmentStatus.DRAFT);
        long open = assignmentRepository.countByStatus(AssignmentStatus.OPEN);
        long closed = assignmentRepository.countByStatus(AssignmentStatus.CLOSED);

        long totalSubmissions = submissionRepository.count();
        long graded = submissionRepository.countByScoreIsNotNull();
        long pending = totalSubmissions - graded;

        long totalUsers = userRepository.count();
        long totalStudents = userRepository.countByRoles_Name("ROLE_STUDENT");
        long totalLecturers = userRepository.countByRoles_Name("ROLE_LECTURER");

        return new AdminStatDto(totalAssignments, draft, open, closed,
                totalSubmissions, graded, pending,
                totalUsers, totalStudents, totalLecturers);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<AssignmentView> getAllAssignmentsForAdmin(int page) {
        return assignmentRepository.findAll(paging(page)).map(AssignmentView::from);
    }
}
