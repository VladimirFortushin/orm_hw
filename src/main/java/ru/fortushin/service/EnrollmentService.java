package ru.fortushin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fortushin.entity.Course;
import ru.fortushin.entity.Enrollment;
import ru.fortushin.entity.User;
import ru.fortushin.entity.enums.EnrollmentStatus;
import ru.fortushin.entity.enums.Role;
import ru.fortushin.exception.BusinessException;
import ru.fortushin.exception.NotFoundException;
import ru.fortushin.repository.CourseRepository;
import ru.fortushin.repository.EnrollmentRepository;
import ru.fortushin.repository.UserRepository;
import ru.fortushin.web.dto.DtoMapper;
import ru.fortushin.web.dto.EnrollmentDto;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Transactional
    public Enrollment enroll(Long courseId, Long studentId) {
        if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new BusinessException(
                    "Student " + studentId + " already enrolled in course " + courseId);
        }
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("User", studentId));
        if (student.getRole() != Role.STUDENT) {
            throw new BusinessException("Only STUDENT can be enrolled, got: " + student.getRole());
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course", courseId));

        Enrollment e = Enrollment.builder()
                .student(student)
                .course(course)
                .enrollDate(LocalDate.now())
                .status(EnrollmentStatus.ACTIVE)
                .build();
        Enrollment saved = enrollmentRepository.save(e);
        log.info("Enrolled student {} -> course {}", studentId, courseId);
        return saved;
    }

    @Transactional
    public void unenroll(Long courseId, Long studentId) {
        Enrollment e = enrollmentRepository
                .findByCourseIdAndStudentId(courseId, studentId)
                .orElseThrow(() -> new BusinessException(
                        "Student " + studentId + " not enrolled in course " + courseId));
        enrollmentRepository.delete(e);
    }

    @Transactional
    public Enrollment updateStatus(Long enrollmentId, EnrollmentStatus status) {
        Enrollment e = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new NotFoundException("Enrollment", enrollmentId));
        e.setStatus(status);
        return e;
    }

    public List<Enrollment> findByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    public List<Enrollment> findByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    public long countByCourse(Long courseId) {
        return enrollmentRepository.countByCourseId(courseId);
    }

    @Transactional
    public EnrollmentDto enrollDto(Long courseId, Long studentId) {
        return DtoMapper.toDto(enroll(courseId, studentId));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDto> findByCourseDto(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDto> findByStudentDto(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(DtoMapper::toDto)
                .toList();
    }
}