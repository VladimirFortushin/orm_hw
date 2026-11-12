package ru.fortushin.web.dto;

import java.time.LocalDate;
import java.util.List;

public record CourseDto(
        Long id,
        String title,
        String description,
        String duration,
        LocalDate startDate,
        CategoryDto category,
        UserShortDto teacher,
        List<TagDto> tags,
        List<ModuleDto> modules,
        long enrolledCount,
        double avgRating
) {}