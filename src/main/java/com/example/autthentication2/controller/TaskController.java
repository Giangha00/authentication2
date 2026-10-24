package com.example.autthentication2.controller;

import com.example.autthentication2.dto.TaskForm;
import com.example.autthentication2.entity.TaskStatus;
import com.example.autthentication2.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @InitBinder("form")
    void initBinder(WebDataBinder binder) {
        binder.setAllowedFields("title", "description", "dueDate", "status");
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model m) {
        m.addAttribute("tasks", service.mine(page));
        return "tasks/list";
    }

    private String renderForm(Model m, TaskForm form, Long id) {
        m.addAttribute("form", form);
        m.addAttribute("taskId", id);
        m.addAttribute("actionUrl", id == null ? "/tasks" : "/tasks/" + id);
        m.addAttribute("statuses", TaskStatus.values());
        return "tasks/form";
    }

    @GetMapping("/new")
    public String newForm(Model m) {
        return renderForm(m, new TaskForm(), null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") TaskForm form,
                         BindingResult errors, Model m) {
        if (errors.hasErrors()) return renderForm(m, form, null);
        service.create(form);
        return "redirect:/tasks";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model m) {
        return renderForm(m, TaskForm.from(service.getMine(id)), id);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") TaskForm form,
                         BindingResult errors, Model m) {
        service.getMine(id); // kiểm tra quyền sở hữu trước cả khi form có lỗi
        if (errors.hasErrors()) return renderForm(m, form, id);
        service.update(id, form);
        return "redirect:/tasks";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/tasks";
    }
}
