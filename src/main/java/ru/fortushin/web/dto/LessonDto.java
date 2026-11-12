package ru.fortushin.web.dto;

import java.util.List;

public record LessonDto(
        Long id,
        String title,
        String content,
        String videoUrl,
        List<AssignmentDto> assignments
) {}