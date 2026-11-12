package ru.fortushin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fortushin.entity.AnswerOption;

import java.util.List;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {

    List<AnswerOption> findByQuestionId(Long questionId);
    List<AnswerOption> findByQuestionIdAndCorrectTrue(Long questionId);
    long countByQuestionIdAndCorrectTrue(Long questionId);
}