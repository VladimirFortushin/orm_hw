package ru.fortushin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fortushin.entity.Course;
import ru.fortushin.entity.CourseReview;
import ru.fortushin.entity.User;
import ru.fortushin.exception.BusinessException;
import ru.fortushin.exception.NotFoundException;
import ru.fortushin.repository.CourseRepository;
import ru.fortushin.repository.CourseReviewRepository;
import ru.fortushin.repository.EnrollmentRepository;
import ru.fortushin.repository.UserRepository;
import ru.fortushin.web.dto.CourseReviewDto;
import ru.fortushin.web.dto.DtoMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseReviewService {

    private final CourseReviewRepository courseReviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public CourseReview addReview(Long courseId, Long studentId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new BusinessException("Rating must be between 1 and 5");
        }
        if (courseReviewRepository.findByCourseIdAndStudentId(courseId, studentId).isPresent()) {
            throw new BusinessException(
                    "Student " + studentId + " already reviewed course " + courseId);
        }
        if (!enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new BusinessException("Student must be enrolled to review course");
        }
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("User", studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course", courseId));

        CourseReview review = CourseReview.builder()
                .course(course)
                .student(student)
                .rating(rating)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
        return courseReviewRepository.save(review);
    }

    public List<CourseReview> findByCourse(Long courseId) {
        return courseReviewRepository.findByCourseId(courseId);
    }

    public double avgRating(Long courseId) {
        Double avg = courseReviewRepository.avgRatingByCourse(courseId);
        return avg != null ? avg : 0.0;
    }

    @Transactional
    public CourseReviewDto addReviewDto(Long courseId, Long studentId, int rating, String comment) {
        return DtoMapper.toDto(addReview(courseId, studentId, rating, comment));
    }

    @Transactional(readOnly = true)
    public List<CourseReviewDto> findByCourseDto(Long courseId) {
        return courseReviewRepository.findByCourseId(courseId).stream()
                .map(DtoMapper::toDto)
                .toList();
    }
}