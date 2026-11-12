package ru.fortushin.web.dto;

import java.util.List;

public record ModuleDto(
        Long id,
        String title,
        int orderIndex,
        String description,
        List<LessonDto> lessons,
        QuizShortDto quiz
) {}