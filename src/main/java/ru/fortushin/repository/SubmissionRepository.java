package ru.fortushin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.fortushin.entity.Submission;
import ru.fortushin.entity.enums.SubmissionStatus;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByAssignmentId(Long assignmentId);
    List<Submission> findByStudentId(Long studentId);
    Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    List<Submission> findByStatus(SubmissionStatus status);

    @Query("""
            SELECT AVG(s.score) FROM Submission s
            WHERE s.assignment.id = :assignmentId AND s.score IS NOT NULL
            """)
    Double avgScoreByAssignment(@Param("assignmentId") Long assignmentId);

    @Query("""
            SELECT s FROM Submission s
            WHERE s.assignment.lesson.module.course.teacher.id = :teacherId
              AND s.score IS NULL
            """)
    List<Submission> findUngradedByTeacherId(@Param("teacherId") Long teacherId);
}