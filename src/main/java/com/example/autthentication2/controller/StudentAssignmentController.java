package com.example.autthentication2.controller;

import com.example.autthentication2.dto.AssignmentView;
import com.example.autthentication2.dto.SubmissionForm;
import com.example.autthentication2.dto.SubmissionView;
import com.example.autthentication2.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student")
public class StudentAssignmentController {

    private final AssignmentService assignmentService;

    public StudentAssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @InitBinder("submissionForm")
    void initBinder(WebDataBinder binder) {
        // Prevent student from passing studentId, assignmentId or score in request body
        binder.setAllowedFields("content");
    }

    @GetMapping("/assignments")
    public String listAssignedAssignments(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("assignments", assignmentService.getOpenAssignmentsForStudent(page));
        return "student/assignments";
    }

    @GetMapping("/assignments/{id}")
    public String viewAssignmentAndSubmitForm(@PathVariable Long id, Model model) {
        AssignmentView assignment = assignmentService.getStudentAssignmentDetail(id);
        SubmissionView existingSubmission = assignmentService.getMySubmissionForAssignment(id);

        SubmissionForm form = new SubmissionForm();
        if (existingSubmission != null) {
            form.setContent(existingSubmission.content());
        }

        model.addAttribute("assignment", assignment);
        model.addAttribute("submission", existingSubmission);
        model.addAttribute("form", form);
        return "student/submit";
    }

    @PostMapping("/assignments/{id}/submit")
    public String submitOrResubmit(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") SubmissionForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (errors.hasErrors()) {
            AssignmentView assignment = assignmentService.getStudentAssignmentDetail(id);
            SubmissionView existingSubmission = assignmentService.getMySubmissionForAssignment(id);
            model.addAttribute("assignment", assignment);
            model.addAttribute("submission", existingSubmission);
            return "student/submit";
        }

        try {
            assignmentService.submitOrResubmit(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Nộp bài tập thành công!");
            return "redirect:/student/assignments/" + id;
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            AssignmentView assignment = assignmentService.getStudentAssignmentDetail(id);
            SubmissionView existingSubmission = assignmentService.getMySubmissionForAssignment(id);
            model.addAttribute("assignment", assignment);
            model.addAttribute("submission", existingSubmission);
            model.addAttribute("errorMessage", ex.getMessage());
            return "student/submit";
        }
    }

    @GetMapping("/history")
    public String viewHistory(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("submissions", assignmentService.getMySubmissionsHistory(page));
        return "student/history";
    }
}
