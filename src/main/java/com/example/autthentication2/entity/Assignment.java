package com.example.autthentication2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "assignments")
@Getter
@Setter
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false)
    private Instant deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status = AssignmentStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecturer_id", nullable = false, updatable = false)
    private AppUser lecturer;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "assignment_student",
            joinColumns = @JoinColumn(name = "assignment_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id"))
    private Set<AppUser> assignedStudents = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Assignment() {}

    public Assignment(AppUser lecturer, String title, String description, Instant deadline) {
        this.lecturer = lecturer;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.status = AssignmentStatus.DRAFT;
        this.createdAt = Instant.now();
    }

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public boolean isOpen() {
        return AssignmentStatus.OPEN.equals(this.status);
    }

    public boolean isClosed() {
        return AssignmentStatus.CLOSED.equals(this.status);
    }

    public boolean isDraft() {
        return AssignmentStatus.DRAFT.equals(this.status);
    }

    public boolean canBeSubmittedAt(Instant now) {
        return isOpen() && !now.isAfter(deadline);
    }

    public boolean isAssignedTo(String username) {
        if (assignedStudents == null) return false;
        return assignedStudents.stream().anyMatch(u -> username.equals(u.getUsername()));
    }
}
