package ru.fortushin.web.dto;

import java.time.LocalDateTime;

public record QuizSubmissionDto(
        Long id,
        Long quizId,
        String quizTitle,
        Long studentId,
        String studentName,
        int score,
        LocalDateTime takenAt
) {}