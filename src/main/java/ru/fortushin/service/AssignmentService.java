package ru.fortushin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fortushin.entity.Assignment;
import ru.fortushin.entity.Lesson;
import ru.fortushin.exception.NotFoundException;
import ru.fortushin.repository.AssignmentRepository;
import ru.fortushin.repository.LessonRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public Assignment create(Long lessonId, String title, String description,
                             LocalDateTime dueDate, int maxScore) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson", lessonId));
        Assignment a = Assignment.builder()
                .title(title)
                .description(description)
                .dueDate(dueDate)
                .maxScore(maxScore)
                .lesson(lesson)
                .build();
        return assignmentRepository.save(a);
    }

    public Assignment getById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assignment", id));
    }

    public Assignment getWithSubmissions(Long id) {
        return assignmentRepository.findByIdWithSubmissions(id)
                .orElseThrow(() -> new NotFoundException("Assignment", id));
    }

    public List<Assignment> findByLesson(Long lessonId) {
        return assignmentRepository.findByLessonId(lessonId);
    }

    public List<Assignment> findActiveByCourse(Long courseId) {
        return assignmentRepository.findActiveByCourseId(courseId);
    }
}