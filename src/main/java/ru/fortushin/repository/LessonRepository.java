package ru.fortushin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.fortushin.entity.Lesson;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByModuleIdOrderByIdAsc(Long moduleId);

    @Query("""
            SELECT DISTINCT l FROM Lesson l
            LEFT JOIN FETCH l.assignments
            WHERE l.id = :id
            """)
    Optional<Lesson> findByIdWithAssignments(@Param("id") Long id);
}