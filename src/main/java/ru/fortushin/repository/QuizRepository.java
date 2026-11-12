package ru.fortushin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.fortushin.entity.Quiz;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Optional<Quiz> findByModuleId(Long moduleId);

    @Query("""
        SELECT DISTINCT q FROM Quiz q
        LEFT JOIN FETCH q.questions
        WHERE q.id = :id
        """)
    Optional<Quiz> findByIdWithQuestionsAndOptions(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT q FROM Quiz q
            LEFT JOIN FETCH q.submissions
            WHERE q.id = :id
            """)
    Optional<Quiz> findByIdWithSubmissions(@Param("id") Long id);
}