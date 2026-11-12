package ru.fortushin.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.fortushin.entity.*;
import ru.fortushin.entity.enums.QuestionType;
import ru.fortushin.entity.enums.Role;
import ru.fortushin.exception.BusinessException;
import ru.fortushin.repository.UserRepository;
import ru.fortushin.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final QuizService quizService;
    private final QuizSubmissionService quizSubmissionService;
    private final CourseReviewService courseReviewService;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Demo data already present, skipping initializer");
            return;
        }
        log.info("Seeding demo data...");

        User alice = userService.create("Alice Ivanova", "alice@lms.local", Role.TEACHER);
        User bob   = userService.create("Bob Petrov",   "bob@lms.local",   Role.TEACHER);
        User charlie = userService.create("Charlie Sidorov", "charlie@lms.local", Role.STUDENT);
        User diana   = userService.create("Diana Smirnova",  "diana@lms.local",   Role.STUDENT);

        userService.upsertProfile(alice.getId(), "Senior Java instructor, 10+ years", null);
        userService.upsertProfile(charlie.getId(), "Third-year CS student", null);

        Category programming = categoryService.create("Programming");
        Category database    = categoryService.create("Database");

        Course hibernate = courseService.create(
                "Hibernate Basics",
                "Introduction to ORM and JPA with Hibernate",
                "8 weeks", LocalDate.now(),
                programming.getId(), alice.getId(),
                Set.of("Java", "Hibernate", "ORM", "Beginner"));

        CourseModule mod1 = courseService.addModule(hibernate.getId(), "Introduction to ORM",
                "What is ORM, why we need it", 1);
        Lesson l1 = courseService.addLesson(mod1.getId(), "What is ORM",
                "ORM is Object-Relational Mapping...", "https://youtube.com/...");
        Lesson l2 = courseService.addLesson(mod1.getId(), "JPA vs Hibernate",
                "JPA is a spec, Hibernate is implementation", null);

        CourseModule mod2 = courseService.addModule(hibernate.getId(), "Entity mapping",
                "Entities, relations, fetch types", 2);
        Lesson l3 = courseService.addLesson(mod2.getId(), "@Entity and @Table",
                "Basic entity annotations", null);

        Assignment hw1 = assignmentService.create(l1.getId(),
                "Describe ORM",
                "Write 200 words about ORM pros and cons",
                LocalDateTime.now().plusDays(7), 100);
        Assignment hw2 = assignmentService.create(l2.getId(),
                "Compare JPA and Hibernate",
                "Differences, when to use what",
                LocalDateTime.now().plusDays(14), 50);

        Course postgres = courseService.create(
                "PostgreSQL for Developers",
                "Deep dive into PostgreSQL",
                "6 weeks", LocalDate.now().plusWeeks(1),
                database.getId(), bob.getId(),
                Set.of("PostgreSQL", "SQL", "Database"));

        CourseModule pgMod = courseService.addModule(postgres.getId(), "Query optimization",
                "Indexes, EXPLAIN, performance", 1);
        courseService.addLesson(pgMod.getId(), "Indexes 101",
                "B-tree, hash, GIN, GiST", null);

        enrollmentService.enroll(hibernate.getId(), charlie.getId());
        enrollmentService.enroll(hibernate.getId(), diana.getId());
        enrollmentService.enroll(postgres.getId(), charlie.getId());

        Submission sub = submissionService.submit(
                hw1.getId(), charlie.getId(),
                "ORM maps objects to tables. Pros: less boilerplate, portability. Cons: leaky abstraction...");
        submissionService.grade(sub.getId(), 85, "Good overview, but missing N+1 discussion");

        Quiz quiz = quizService.createForModule(mod1.getId(), "Module 1 Quiz", 15);

        Question q1 = quizService.addQuestion(quiz.getId(),
                "What does JPA stand for?",
                QuestionType.SINGLE_CHOICE);
        AnswerOption a1 = quizService.addOption(q1.getId(), "Java Persistence API", true);
        quizService.addOption(q1.getId(), "Java Programming Architecture", false);
        quizService.addOption(q1.getId(), "Java Performance API", false);

        Question q2 = quizService.addQuestion(quiz.getId(),
                "Which of the following are JPA implementations?",
                QuestionType.MULTIPLE_CHOICE);
        AnswerOption hibernateOpt = quizService.addOption(q2.getId(), "Hibernate", true);
        AnswerOption eclipseOpt = quizService.addOption(q2.getId(), "EclipseLink", true);
        quizService.addOption(q2.getId(), "Spring JDBC", false);

        try {
            quizSubmissionService.takeQuiz(quiz.getId(), charlie.getId(), Map.of(
                    q1.getId(), List.of(a1.getId()),
                    q2.getId(), List.of(hibernateOpt.getId(), eclipseOpt.getId())
            ));
        } catch (BusinessException e) {
            log.warn("Quiz submission skipped: {}", e.getMessage());
        }

        try {
            courseReviewService.addReview(hibernate.getId(), charlie.getId(), 5,
                    "Great course, clear explanations");
        } catch (BusinessException e) {
            log.warn("Review skipped: {}", e.getMessage());
        }

        log.info("Demo data seeded successfully");
    }
}