package com.example.autthentication2.dto;

public record AdminStatDto(
        long totalAssignments,
        long draftAssignments,
        long openAssignments,
        long closedAssignments,
        long totalSubmissions,
        long gradedSubmissions,
        long pendingSubmissions,
        long totalUsers,
        long totalStudents,
        long totalLecturers
) {}
