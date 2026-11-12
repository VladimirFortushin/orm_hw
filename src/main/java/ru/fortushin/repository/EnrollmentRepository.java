package ru.fortushin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fortushin.entity.Enrollment;
import ru.fortushin.entity.enums.EnrollmentStatus;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByCourseId(Long courseId);
    List<Enrollment> findByStudentId(Long studentId);
    Optional<Enrollment> findByCourseIdAndStudentId(Long courseId, Long studentId);
    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
    List<Enrollment> findByCourseIdAndStatus(Long courseId, EnrollmentStatus status);
    long countByCourseId(Long courseId);
}