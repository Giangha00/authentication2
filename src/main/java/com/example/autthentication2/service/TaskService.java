package com.example.autthentication2.service;

import com.example.autthentication2.dto.TaskForm;
import com.example.autthentication2.dto.TaskView;
import com.example.autthentication2.entity.Task;
import com.example.autthentication2.repository.TaskRepository;
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

@Service
@Validated
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository tasks;
    private final UserRepository users;

    public TaskService(TaskRepository tasks, UserRepository users) {
        this.tasks = tasks;
        this.users = users;
    }

    private String currentUsername() {
        return SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }

    private Pageable paging(int page) {
        return PageRequest.of(Math.max(0, page), 10,
                Sort.by(Sort.Order.asc("dueDate"), Sort.Order.desc("id")));
    }

    private Task owned(Long id) {
        return tasks.findByIdAndOwner_Username(id, currentUsername())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy công việc"));
    }

    public Page<TaskView> mine(int page) {
        return tasks.findByOwner_Username(currentUsername(), paging(page))
                .map(TaskView::from);
    }

    public TaskView getMine(Long id) {
        return TaskView.from(owned(id));
    }

    @Transactional
    public void create(@Valid TaskForm form) {
        var owner = users.findByUsername(currentUsername())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ"));
        tasks.save(new Task(owner, form));
    }

    @Transactional
    public void update(Long id, @Valid TaskForm form) {
        owned(id).apply(form);
    }

    @Transactional
    public void delete(Long id) {
        tasks.delete(owned(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<TaskView> all(int page) {
        return tasks.findAll(paging(page)).map(TaskView::from);
    }
}
