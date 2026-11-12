package ru.fortushin.web.dto;

import ru.fortushin.entity.enums.QuestionType;

import java.util.List;

public record QuizDto(
        Long id,
        String title,
        int timeLimit,
        List<QuestionDto> questions
) {
    public record QuestionDto(
            Long id,
            String text,
            QuestionType type,
            List<AnswerOptionDto> options
    ) {}

    public record AnswerOptionDto(Long id, String text, boolean correct) {}
}