package com.example.autthentication2.controller;

import com.example.autthentication2.dto.AssignmentForm;
import com.example.autthentication2.dto.GradeForm;
import com.example.autthentication2.entity.Assignment;
import com.example.autthentication2.service.AssignmentService;
import com.example.autthentication2.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Controller
@RequestMapping("/lecturer/assignments")
public class LecturerAssignmentController {

    private final AssignmentService assignmentService;
    private final UserService userService;

    public LecturerAssignmentController(AssignmentService assignmentService, UserService userService) {
        this.assignmentService = assignmentService;
        this.userService = userService;
    }

    @InitBinder("assignmentForm")
    void initAssignmentBinder(WebDataBinder binder) {
        binder.setAllowedFields("title", "description", "deadline");
    }

    @InitBinder("gradeForm")
    void initGradeBinder(WebDataBinder binder) {
        binder.setAllowedFields("score", "feedback");
    }

    @GetMapping
    public String listAssignments(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("assignments", assignmentService.getLecturerAssignments(page));
        return "lecturer/assignments";
    }

    @GetMapping("/new")
    public String newAssignmentForm(Model model) {
        model.addAttribute("form", new AssignmentForm());
        model.addAttribute("actionUrl", "/lecturer/assignments");
        model.addAttribute("isEdit", false);
        return "lecturer/assignment-form";
    }

    @PostMapping
    public String createAssignment(
            @Valid @ModelAttribute("form") AssignmentForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (errors.hasErrors()) {
            model.addAttribute("actionUrl", "/lecturer/assignments");
            model.addAttribute("isEdit", false);
            return "lecturer/assignment-form";
        }
        try {
            assignmentService.createAssignment(form);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo bài tập bản thảo thành công!");
            return "redirect:/lecturer/assignments";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("actionUrl", "/lecturer/assignments");
            model.addAttribute("isEdit", false);
            return "lecturer/assignment-form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editAssignmentForm(@PathVariable Long id, Model model) {
        Assignment assignment = assignmentService.getLecturerAssignmentEntity(id);
        AssignmentForm form = new AssignmentForm();
        form.setTitle(assignment.getTitle());
        form.setDescription(assignment.getDescription());
        form.setDeadline(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(assignment.getDeadline()));

        model.addAttribute("form", form);
        model.addAttribute("actionUrl", "/lecturer/assignments/" + id);
        model.addAttribute("isEdit", true);
        model.addAttribute("assignmentId", id);
        return "lecturer/assignment-form";
    }

    @PostMapping("/{id}")
    public String updateAssignment(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") AssignmentForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (errors.hasErrors()) {
            model.addAttribute("actionUrl", "/lecturer/assignments/" + id);
            model.addAttribute("isEdit", true);
            model.addAttribute("assignmentId", id);
            return "lecturer/assignment-form";
        }
        try {
            assignmentService.updateAssignment(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật bài tập thành công!");
            return "redirect:/lecturer/assignments";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("actionUrl", "/lecturer/assignments/" + id);
            model.addAttribute("isEdit", true);
            model.addAttribute("assignmentId", id);
            return "lecturer/assignment-form";
        }
    }

    @GetMapping("/{id}/open")
    public String showOpenAssignmentForm(@PathVariable Long id, Model model) {
        Assignment assignment = assignmentService.getLecturerAssignmentEntity(id);
        model.addAttribute("assignment", assignment);
        model.addAttribute("allStudents", userService.findAllStudents());
        return "lecturer/assign-students";
    }

    @PostMapping("/{id}/open")
    public String openAssignment(
            @PathVariable Long id,
            @RequestParam(value = "studentIds", required = false) Set<Long> studentIds,
            RedirectAttributes redirectAttributes
    ) {
        try {
            assignmentService.openAssignment(id, studentIds);
            redirectAttributes.addFlashAttribute("successMessage", "Mở bài tập và giao cho học viên thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/lecturer/assignments";
    }

    @PostMapping("/{id}/close")
    public String closeAssignment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            assignmentService.closeAssignment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đóng bài tập thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/lecturer/assignments";
    }

    @GetMapping("/{id}/submissions")
    public String listSubmissions(@PathVariable Long id, Model model) {
        Assignment assignment = assignmentService.getLecturerAssignmentEntity(id);
        model.addAttribute("assignment", assignment);
        model.addAttribute("submissions", assignmentService.getSubmissionsForAssignment(id));
        return "lecturer/submissions";
    }

    @GetMapping("/{id}/submissions/{subId}/grade")
    public String showGradeForm(
            @PathVariable Long id,
            @PathVariable Long subId,
            Model model
    ) {
        Assignment assignment = assignmentService.getLecturerAssignmentEntity(id);
        model.addAttribute("assignment", assignment);
        model.addAttribute("submissionId", subId);
        model.addAttribute("form", new GradeForm());
        return "lecturer/grade-form";
    }

    @PostMapping("/{id}/submissions/{subId}/grade")
    public String submitGrade(
            @PathVariable Long id,
            @PathVariable Long subId,
            @Valid @ModelAttribute("form") GradeForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (errors.hasErrors()) {
            Assignment assignment = assignmentService.getLecturerAssignmentEntity(id);
            model.addAttribute("assignment", assignment);
            model.addAttribute("submissionId", subId);
            return "lecturer/grade-form";
        }
        try {
            assignmentService.gradeSubmission(id, subId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Chấm điểm bài nộp thành công!");
            return "redirect:/lecturer/assignments/" + id + "/submissions";
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            Assignment assignment = assignmentService.getLecturerAssignmentEntity(id);
            model.addAttribute("assignment", assignment);
            model.addAttribute("submissionId", subId);
            model.addAttribute("errorMessage", ex.getMessage());
            return "lecturer/grade-form";
        }
    }
}
