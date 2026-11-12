package ru.fortushin.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.fortushin.service.CourseReviewService;
import ru.fortushin.service.CourseService;
import ru.fortushin.service.EnrollmentService;
import ru.fortushin.web.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final CourseReviewService courseReviewService;

    @GetMapping
    public List<CourseDto> list() {
        return courseService.listDto();
    }

    @GetMapping("/{id}")
    public CourseDto getById(@PathVariable Long id) {
        return courseService.getFullDto(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseDto create(@RequestBody @Valid CreateCourseRequest req) {
        return courseService.createDto(
                req.title(), req.description(), req.duration(), req.startDate(),
                req.categoryId(), req.teacherId(), req.tags());
    }

    @PutMapping("/{id}")
    public CourseDto update(@PathVariable Long id, @RequestBody CreateCourseRequest req) {
        return courseService.updateDto(id, req.title(), req.description(),
                req.duration(), req.startDate());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        courseService.delete(id);
    }

    @PostMapping("/{id}/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentDto enroll(@PathVariable Long id, @RequestParam Long studentId) {
        return enrollmentService.enrollDto(id, studentId);
    }

    @GetMapping("/{id}/enrollments")
    public List<EnrollmentDto> enrollments(@PathVariable Long id) {
        return enrollmentService.findByCourseDto(id);
    }

    @PostMapping("/{id}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public ModuleDto addModule(@PathVariable Long id,
                               @RequestParam String title,
                               @RequestParam(required = false) String description,
                               @RequestParam(defaultValue = "1") int orderIndex) {
        return courseService.addModuleDto(id, title, description, orderIndex);
    }

    @PostMapping("/modules/{moduleId}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    public LessonDto addLesson(@PathVariable Long moduleId,
                               @RequestParam String title,
                               @RequestParam(required = false) String content,
                               @RequestParam(required = false) String videoUrl) {
        return courseService.addLessonDto(moduleId, title, content, videoUrl);
    }

    @PostMapping("/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseReviewDto addReview(@PathVariable Long id,
                                     @RequestParam Long studentId,
                                     @RequestParam int rating,
                                     @RequestParam(required = false) String comment) {
        return courseReviewService.addReviewDto(id, studentId, rating, comment);
    }

    @GetMapping("/{id}/reviews")
    public List<CourseReviewDto> reviews(@PathVariable Long id) {
        return courseReviewService.findByCourseDto(id);
    }
}