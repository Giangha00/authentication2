package com.example.autthentication2.dto;

import com.example.autthentication2.entity.Submission;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record SubmissionView(
        Long id,
        Long assignmentId,
        String assignmentTitle,
        String studentUsername,
        String studentFullName,
        String content,
        Instant submittedAt,
        String submittedAtFormatted,
        BigDecimal score,
        String feedback,
        boolean isGraded
) {
    private static final DateTimeFormatter FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    public static SubmissionView from(Submission s) {
        return new SubmissionView(
                s.getId(),
                s.getAssignment().getId(),
                s.getAssignment().getTitle(),
                s.getStudent().getUsername(),
                s.getStudent().getFullName() != null ? s.getStudent().getFullName() : s.getStudent().getUsername(),
                s.getContent(),
                s.getSubmittedAt(),
                FORMATTER.format(s.getSubmittedAt()),
                s.getScore(),
                s.getFeedback(),
                s.isGraded()
        );
    }
}
