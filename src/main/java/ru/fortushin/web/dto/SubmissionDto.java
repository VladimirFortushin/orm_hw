package ru.fortushin.web.dto;

import ru.fortushin.entity.enums.SubmissionStatus;

import java.time.LocalDateTime;

public record SubmissionDto(
        Long id,
        Long assignmentId,
        String assignmentTitle,
        Long studentId,
        String studentName,
        LocalDateTime submittedAt,
        String content,
        Integer score,
        String feedback,
        SubmissionStatus status
) {}