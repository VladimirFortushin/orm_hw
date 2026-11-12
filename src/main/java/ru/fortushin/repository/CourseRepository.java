package ru.fortushin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.fortushin.entity.Course;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByCategoryId(Long categoryId);
    List<Course> findByTeacherId(Long teacherId);
    List<Course> findByTitleContainingIgnoreCase(String fragment);

    @Query("""
            SELECT c FROM Course c
            LEFT JOIN FETCH c.category
            LEFT JOIN FETCH c.teacher
            WHERE c.id = :id
            """)
    Optional<Course> findByIdWithCategoryAndTeacher(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT c FROM Course c
            LEFT JOIN FETCH c.modules
            WHERE c.id = :id
            """)
    Optional<Course> findByIdWithModules(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT c FROM Course c
        LEFT JOIN FETCH c.modules
        WHERE c.id = :id
        """)
    Optional<Course> findByIdWithModulesAndLessons(@Param("id") Long id);
}