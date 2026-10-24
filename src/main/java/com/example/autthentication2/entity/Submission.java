package com.example.autthentication2.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "submissions",
        uniqueConstraints = @UniqueConstraint(name = "uq_submissions_assignment_student", columnNames = {"assignment_id", "student_id"}))
@Getter
@Setter
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false, updatable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, updatable = false)
    private AppUser student;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(precision = 4, scale = 2)
    private BigDecimal score;

    @Column(length = 2000)
    private String feedback;

    protected Submission() {}

    public Submission(Assignment assignment, AppUser student, String content) {
        this.assignment = assignment;
        this.student = student;
        this.content = content;
        this.submittedAt = Instant.now();
    }

    public boolean isGraded() {
        return this.score != null;
    }
}
