package ru.fortushin.web.dto;

import ru.fortushin.entity.enums.EnrollmentStatus;

import java.time.LocalDate;

public record EnrollmentDto(
        Long id,
        Long courseId,
        String courseTitle,
        Long studentId,
        String studentName,
        LocalDate enrollDate,
        EnrollmentStatus status
) {}