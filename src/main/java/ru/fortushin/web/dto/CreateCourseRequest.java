package ru.fortushin.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

public record CreateCourseRequest(
        @NotBlank String title,
        String description,
        String duration,
        LocalDate startDate,
        Long categoryId,
        @NotNull Long teacherId,
        Set<String> tags
) {}