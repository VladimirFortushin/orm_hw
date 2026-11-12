package ru.fortushin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.fortushin.entity.*;
import ru.fortushin.entity.enums.*;
import ru.fortushin.exception.BusinessException;
import ru.fortushin.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class CourseWorkflowTest extends AbstractIntegrationTest {

    @Autowired UserService userService;
    @Autowired CategoryService categoryService;
    @Autowired CourseService courseService;
    @Autowired EnrollmentService enrollmentService;
    @Autowired AssignmentService assignmentService;
    @Autowired SubmissionService submissionService;
    @Autowired QuizService quizService;
    @Autowired QuizSubmissionService quizSubmissionService;
    @Autowired CourseReviewService courseReviewService;

    @Test
    void fullCourseScenario_endToEnd() {
        User teacher = userService.create("Test Teacher", "tt-" + System.nanoTime() + "@x", Role.TEACHER);
        User student = userService.create("Test Student", "ts-" + System.nanoTime() + "@x", Role.STUDENT);

        Category cat = categoryService.create("TestCat-" + System.nanoTime());
        Course course = courseService.create(
                "Test Course", "desc", "2 weeks", LocalDate.now(),
                cat.getId(), teacher.getId(), Set.of("tag1", "tag2"));

        CourseModule module = courseService.addModule(course.getId(), "Mod 1", "d", 1);
        Lesson lesson = courseService.addLesson(module.getId(), "Lesson 1", "content", null);

        Assignment hw = assignmentService.create(lesson.getId(),
                "HW", "Do it", LocalDateTime.now().plusDays(7), 100);
        Submission sub = submissionService.submit(hw.getId(), student.getId(), "my work");
        assertEquals(SubmissionStatus.SUBMITTED, sub.getStatus());

        submissionService.grade(sub.getId(), 90, "Well done");
        assertEquals(SubmissionStatus.GRADED, sub.getStatus());
        assertEquals(90, sub.getScore());

        assertThrows(BusinessException.class,
                () -> submissionService.grade(sub.getId(), 200, "too much"));

        assertThrows(BusinessException.class,
                () -> submissionService.submit(hw.getId(), student.getId(), "again"));

        Enrollment enrollment = enrollmentService.enroll(course.getId(), student.getId());
        assertEquals(EnrollmentStatus.ACTIVE, enrollment.getStatus());

        assertThrows(BusinessException.class,
                () -> enrollmentService.enroll(course.getId(), student.getId()));

        assertThrows(BusinessException.class,
                () -> enrollmentService.enroll(course.getId(), teacher.getId()));

        Quiz quiz = quizService.createForModule(module.getId(), "Quiz 1", 10);
        Question q = quizService.addQuestion(quiz.getId(), "1+1?", QuestionType.SINGLE_CHOICE);
        AnswerOption correct = quizService.addOption(q.getId(), "2", true);
        AnswerOption wrong = quizService.addOption(q.getId(), "3", false);

        QuizSubmission qs = quizSubmissionService.takeQuiz(
                quiz.getId(), student.getId(),
                Map.of(q.getId(), List.of(correct.getId())));
        assertEquals(100, qs.getScore());

        QuizSubmission qs2 = quizSubmissionService.takeQuiz(
                quiz.getId(), student.getId(),
                Map.of(q.getId(), List.of(wrong.getId())));
        assertEquals(0, qs2.getScore());

        CourseReview review = courseReviewService.addReview(
                course.getId(), student.getId(), 5, "Nice");
        assertEquals(5, review.getRating());

        User outsider = userService.create("Outsider", "out-" + System.nanoTime() + "@x", Role.STUDENT);
        assertThrows(BusinessException.class,
                () -> courseReviewService.addReview(course.getId(), outsider.getId(), 5, "spam"));
    }

    @Test
    void courseUpdate_usesDirtyChecking() {
        User teacher = userService.create("T2", "t2-" + System.nanoTime() + "@x", Role.TEACHER);
        Course course = courseService.create("Old Title", "old", null, null,
                null, teacher.getId(), null);

        courseService.update(course.getId(), "New Title", "new", "5w", LocalDate.now());

        Course reloaded = courseService.getById(course.getId());
        assertEquals("New Title", reloaded.getTitle());
        assertEquals("new", reloaded.getDescription());
    }
}