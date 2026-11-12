package ru.fortushin.web.dto;

import java.time.LocalDateTime;

public record CourseReviewDto(
        Long id,
        Long courseId,
        Long studentId,
        String studentName,
        int rating,
        String comment,
        LocalDateTime createdAt
) {}