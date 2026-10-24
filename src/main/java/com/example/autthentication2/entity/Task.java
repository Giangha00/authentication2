package com.example.autthentication2.entity;

import com.example.autthentication2.dto.TaskForm;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tasks")
@Getter
@Setter
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    private AppUser owner;

    protected Task() {}

    public Task(AppUser owner, TaskForm form) {
        this.owner = owner;
        apply(form);
    }

    public void apply(TaskForm form) {
        this.title = form.getTitle().strip();
        this.description = form.getDescription().strip();
        this.dueDate = form.getDueDate();
        this.status = form.getStatus();
    }
}
