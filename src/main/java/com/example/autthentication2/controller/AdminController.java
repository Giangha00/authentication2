package com.example.autthentication2.controller;

import com.example.autthentication2.dto.AdminStatDto;
import com.example.autthentication2.service.AssignmentService;
import com.example.autthentication2.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AssignmentService assignmentService;
    private final UserService userService;

    public AdminController(AssignmentService assignmentService, UserService userService) {
        this.assignmentService = assignmentService;
        this.userService = userService;
    }

    @GetMapping
    public String dashboard(Model model) {
        AdminStatDto stats = assignmentService.getAdminStatistics();
        model.addAttribute("stats", stats);
        model.addAttribute("assignments", assignmentService.getAllAssignmentsForAdmin(0));
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/{id}/roles")
    public String assignRole(
            @PathVariable Long id,
            @RequestParam("roleName") String roleName,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.assignRole(id, roleName);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật vai trò thành công!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }
}
