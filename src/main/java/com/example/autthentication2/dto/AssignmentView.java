package com.example.autthentication2.dto;

import com.example.autthentication2.entity.Assignment;
import com.example.autthentication2.entity.AssignmentStatus;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record AssignmentView(
        Long id,
        String title,
        String description,
        Instant deadline,
        String deadlineFormatted,
        AssignmentStatus status,
        String lecturerUsername,
        String lecturerFullName,
        int assignedCount,
        boolean isExpired
) {
    private static final DateTimeFormatter FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    public static AssignmentView from(Assignment a) {
        boolean expired = Instant.now().isAfter(a.getDeadline());
        return new AssignmentView(
                a.getId(),
                a.getTitle(),
                a.getDescription(),
                a.getDeadline(),
                FORMATTER.format(a.getDeadline()),
                a.getStatus(),
                a.getLecturer().getUsername(),
                a.getLecturer().getFullName() != null ? a.getLecturer().getFullName() : a.getLecturer().getUsername(),
                a.getAssignedStudents() != null ? a.getAssignedStudents().size() : 0,
                expired
        );
    }
}
