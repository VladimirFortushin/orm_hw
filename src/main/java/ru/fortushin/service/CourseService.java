package ru.fortushin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fortushin.entity.*;
import ru.fortushin.entity.enums.Role;
import ru.fortushin.exception.BusinessException;
import ru.fortushin.exception.NotFoundException;
import ru.fortushin.repository.*;
import ru.fortushin.web.dto.CourseDto;
import ru.fortushin.web.dto.DtoMapper;
import ru.fortushin.web.dto.LessonDto;
import ru.fortushin.web.dto.ModuleDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final LessonRepository lessonRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseReviewRepository courseReviewRepository;

    @Transactional
    public Course create(String title, String description, String duration,
                         LocalDate startDate, Long categoryId, Long teacherId,
                         Set<String> tagNames) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new NotFoundException("User (teacher)", teacherId));
        if (teacher.getRole() != Role.TEACHER && teacher.getRole() != Role.ADMIN) {
            throw new BusinessException("User " + teacherId + " is not a teacher/admin");
        }

        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category", categoryId));
        }

        Course course = Course.builder()
                .title(title)
                .description(description)
                .duration(duration)
                .startDate(startDate)
                .category(category)
                .teacher(teacher)
                .build();

        if (tagNames != null && !tagNames.isEmpty()) {
            for (String name : tagNames) {
                Tag tag = tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));
                course.getTags().add(tag);
            }
        }

        Course saved = courseRepository.save(course);
        log.info("Created course id={} title='{}' teacherId={}", saved.getId(), title, teacherId);
        return saved;
    }

    @Transactional
    public CourseModule addModule(Long courseId, String title, String description, int orderIndex) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course", courseId));
        CourseModule module = CourseModule.builder()
                .title(title)
                .description(description)
                .orderIndex(orderIndex)
                .build();
        course.addModule(module);
        return courseModuleRepository.saveAndFlush(module);
    }

    @Transactional
    public Lesson addLesson(Long moduleId, String title, String content, String videoUrl) {
        CourseModule module = courseModuleRepository.findById(moduleId)
                .orElseThrow(() -> new NotFoundException("Module", moduleId));
        Lesson lesson = Lesson.builder()
                .title(title)
                .content(content)
                .videoUrl(videoUrl)
                .build();
        module.addLesson(lesson);
        return lessonRepository.saveAndFlush(lesson);
    }

    public Course getById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course", id));
    }

    public Course getWithStructure(Long id) {
        return courseRepository.findByIdWithModulesAndLessons(id)
                .orElseThrow(() -> new NotFoundException("Course", id));
    }

    public Course getWithCategoryAndTeacher(Long id) {
        return courseRepository.findByIdWithCategoryAndTeacher(id)
                .orElseThrow(() -> new NotFoundException("Course", id));
    }

    public List<Course> findByCategory(Long categoryId) {
        return courseRepository.findByCategoryId(categoryId);
    }

    public List<Course> findByTeacher(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    public List<Course> search(String fragment) {
        return courseRepository.findByTitleContainingIgnoreCase(fragment);
    }

    @Transactional
    public Course update(Long id, String title, String description,
                         String duration, LocalDate startDate) {
        Course c = courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course", id));
        if (title != null) c.setTitle(title);
        if (description != null) c.setDescription(description);
        if (duration != null) c.setDuration(duration);
        if (startDate != null) c.setStartDate(startDate);
        return c;
    }

    @Transactional
    public void delete(Long id) {
        Course c = courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course", id));
        courseRepository.delete(c);
        log.info("Deleted course id={}", id);
    }

    @Transactional(readOnly = true)
    public List<CourseDto> listDto() {
        return courseRepository.findAll().stream()
                .map(c -> DtoMapper.toSummary(
                        c,
                        enrollmentRepository.countByCourseId(c.getId()),
                        Optional.ofNullable(courseReviewRepository.avgRatingByCourse(c.getId())).orElse(0.0)))
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseDto getFullDto(Long id) {
        Course c = courseRepository.findByIdWithModulesAndLessons(id)
                .orElseThrow(() -> new NotFoundException("Course", id));
        long count = enrollmentRepository.countByCourseId(id);
        double avg = Optional.ofNullable(courseReviewRepository.avgRatingByCourse(id)).orElse(0.0);
        return DtoMapper.toFull(c, count, avg);
    }

    @Transactional
    public CourseDto createDto(String title, String description, String duration,
                               LocalDate startDate, Long categoryId, Long teacherId,
                               Set<String> tagNames) {
        Course saved = create(title, description, duration, startDate, categoryId, teacherId, tagNames);
        return DtoMapper.toSummary(saved, 0L, 0.0);
    }

    @Transactional
    public CourseDto updateDto(Long id, String title, String description,
                               String duration, LocalDate startDate) {
        Course updated = update(id, title, description, duration, startDate);
        long count = enrollmentRepository.countByCourseId(id);
        double avg = Optional.ofNullable(courseReviewRepository.avgRatingByCourse(id)).orElse(0.0);
        return DtoMapper.toSummary(updated, count, avg);
    }

    @Transactional
    public ModuleDto addModuleDto(Long courseId, String title, String description, int orderIndex) {
        CourseModule m = addModule(courseId, title, description, orderIndex);
        return DtoMapper.toDto(m);
    }

    @Transactional
    public LessonDto addLessonDto(Long moduleId, String title, String content, String videoUrl) {
        Lesson l = addLesson(moduleId, title, content, videoUrl);
        return DtoMapper.toDto(l);
    }
}