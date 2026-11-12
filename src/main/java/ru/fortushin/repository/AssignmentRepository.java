package ru.fortushin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.fortushin.entity.Assignment;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByLessonId(Long lessonId);

    @Query("""
            SELECT DISTINCT a FROM Assignment a
            LEFT JOIN FETCH a.submissions
            WHERE a.id = :id
            """)
    Optional<Assignment> findByIdWithSubmissions(@Param("id") Long id);

    @Query("""
            SELECT a FROM Assignment a
            WHERE a.lesson.module.course.id = :courseId
              AND a.dueDate > CURRENT_TIMESTAMP
            """)
    List<Assignment> findActiveByCourseId(@Param("courseId") Long courseId);
}