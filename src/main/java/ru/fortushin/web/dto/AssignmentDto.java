package ru.fortushin.web.dto;

import java.time.LocalDateTime;

public record AssignmentDto(
        Long id,
        String title,
        String description,
        LocalDateTime dueDate,
        int maxScore
) {}