package ru.fortushin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.fortushin.entity.CourseModule;

import java.util.List;
import java.util.Optional;

public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {

    List<CourseModule> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    @Query("""
            SELECT DISTINCT m FROM CourseModule m
            LEFT JOIN FETCH m.lessons
            WHERE m.id = :id
            """)
    Optional<CourseModule> findByIdWithLessons(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT m FROM CourseModule m
        LEFT JOIN FETCH m.lessons
        WHERE m.id = :id
        """)
    Optional<CourseModule> findByIdWithLessonsAndAssignments(@Param("id") Long id);
}