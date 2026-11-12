package ru.fortushin;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.fortushin.entity.*;
import ru.fortushin.repository.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Проверяет каскадные операции. Чистая БД между тестами.
 */
@Transactional
class CascadeIntegrationTest extends AbstractIntegrationTest {

    @Autowired CourseRepository courseRepo;
    @Autowired CourseModuleRepository moduleRepo;
    @Autowired LessonRepository lessonRepo;
    @Autowired AssignmentRepository assignmentRepo;
    @Autowired SubmissionRepository submissionRepo;
    @Autowired UserRepository userRepo;
    @Autowired EntityManager em;

    @Test
    void cascadePersist_savesModulesAndLessonsTogether() {
        Course course = Course.builder().title("Cascade Persist").build();
        CourseModule module = CourseModule.builder().title("M1").orderIndex(1).build();
        Lesson lesson = Lesson.builder().title("L1").build();

        course.addModule(module);
        module.addLesson(lesson);

        Course saved = courseRepo.save(course);
        em.flush();
        em.clear();

        Course reloaded = courseRepo.findByIdWithModulesAndLessons(saved.getId()).orElseThrow();
        assertEquals(1, reloaded.getModules().size());
        assertEquals(1, reloaded.getModules().get(0).getLessons().size());
        assertEquals("L1", reloaded.getModules().get(0).getLessons().get(0).getTitle());
    }

    @Test
    void cascadeDelete_deletesWholeSubtree() {
        User teacher = userRepo.save(User.builder()
                .name("T").email("t-" + System.nanoTime() + "@x").role(ru.fortushin.entity.enums.Role.TEACHER).build());
        User student = userRepo.save(User.builder()
                .name("S").email("s-" + System.nanoTime() + "@x").role(ru.fortushin.entity.enums.Role.STUDENT).build());

        Course course = Course.builder().title("To Delete").teacher(teacher).build();
        CourseModule module = CourseModule.builder().title("M").orderIndex(1).build();
        Lesson lesson = Lesson.builder().title("L").build();
        Assignment assignment = Assignment.builder().title("A").maxScore(100).build();

        course.addModule(module);
        module.addLesson(lesson);
        lesson.addAssignment(assignment);

        Submission sub = Submission.builder()
                .assignment(assignment).student(student).content("x").build();
        assignment.getSubmissions().add(sub);

        Course saved = courseRepo.save(course);
        em.flush();
        em.clear();

        Long courseId = saved.getId();
        Long moduleId = module.getId();
        Long lessonId = lesson.getId();
        Long assignmentId = assignment.getId();

        Course toDelete = courseRepo.findById(courseId).orElseThrow();
        courseRepo.delete(toDelete);
        em.flush();
        em.clear();

        assertTrue(courseRepo.findById(courseId).isEmpty());
        assertTrue(moduleRepo.findById(moduleId).isEmpty());
        assertTrue(lessonRepo.findById(lessonId).isEmpty());
        assertTrue(assignmentRepo.findById(assignmentId).isEmpty());
        assertEquals(0, submissionRepo.count());
    }

    @Test
    void removingFromCollection_orphanRemoval_deletesChild() {
        Course course = Course.builder().title("Orphan Test").build();
        CourseModule m1 = CourseModule.builder().title("M1").orderIndex(1).build();
        CourseModule m2 = CourseModule.builder().title("M2").orderIndex(2).build();
        course.addModule(m1);
        course.addModule(m2);
        courseRepo.save(course);
        em.flush();

        Long m2Id = m2.getId();
        course.removeModule(m2);
        em.flush();
        em.clear();

        assertTrue(moduleRepo.findById(m2Id).isEmpty());
    }
}