package ru.fortushin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.fortushin.entity.Question;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizId(Long quizId);

    @Query("""
            SELECT DISTINCT q FROM Question q
            LEFT JOIN FETCH q.options
            WHERE q.id = :id
            """)
    Optional<Question> findByIdWithOptions(@Param("id") Long id);
}