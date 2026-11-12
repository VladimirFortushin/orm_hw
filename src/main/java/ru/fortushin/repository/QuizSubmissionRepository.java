package ru.fortushin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.fortushin.entity.QuizSubmission;

import java.util.List;
import java.util.Optional;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {

    List<QuizSubmission> findByStudentId(Long studentId);
    List<QuizSubmission> findByQuizId(Long quizId);

    @Query("""
            SELECT MAX(qs.score) FROM QuizSubmission qs
            WHERE qs.quiz.id = :quizId AND qs.student.id = :studentId
            """)
    Optional<Integer> findBestScore(@Param("quizId") Long quizId, @Param("studentId") Long studentId);

    @Query("SELECT AVG(qs.score) FROM QuizSubmission qs WHERE qs.quiz.id = :quizId")
    Double avgScoreByQuiz(@Param("quizId") Long quizId);
}