package ru.fortushin;

import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.support.TransactionTemplate;
import ru.fortushin.entity.Course;
import ru.fortushin.entity.CourseModule;
import ru.fortushin.repository.CourseModuleRepository;
import ru.fortushin.repository.CourseRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Демонстрация проблемы lazy-загрузки и способов её решения.
 *
 * @DirtiesContext(AFTER_CLASS): после теста Spring пересоздаст контекст,
 * схема БД будет пересоздана — данные этого класса не протекут в следующие.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LazyLoadingTest extends AbstractIntegrationTest {

    @Autowired CourseRepository courseRepository;
    @Autowired CourseModuleRepository moduleRepository;
    @Autowired TransactionTemplate tx;

    @Test
    void accessingLazyCollectionOutsideTransaction_throwsLazyInitializationException() {
        Long courseId = tx.execute(status -> {
            Course c = Course.builder().title("Lazy Demo Course").build();
            c.addModule(CourseModule.builder().title("M1").orderIndex(1).build());
            return courseRepository.save(c).getId();
        });

        Course loaded = tx.execute(status ->
                courseRepository.findById(courseId).orElseThrow());

        assertNotNull(loaded);

        LazyInitializationException ex = assertThrows(
                LazyInitializationException.class,
                () -> loaded.getModules().size()
        );

        assertNotNull(ex.getMessage());
    }

    @Test
    void joinFetchLoadsCollection_eagerly_avoidsException() {
        Long courseId = tx.execute(status -> {
            Course c = Course.builder().title("JOIN FETCH Course").build();
            c.addModule(CourseModule.builder().title("M1").orderIndex(1).build());
            c.addModule(CourseModule.builder().title("M2").orderIndex(2).build());
            return courseRepository.save(c).getId();
        });

        Course loaded = tx.execute(status ->
                courseRepository.findByIdWithModules(courseId).orElseThrow());

        assertEquals(2, loaded.getModules().size());
        assertNotNull(loaded.getModules().get(0).getTitle());
    }

    @Test
    void accessingLazyCollectionInsideTransaction_works() {
        Long courseId = tx.execute(status -> {
            Course c = Course.builder().title("In-Tx Course").build();
            c.addModule(CourseModule.builder().title("M1").orderIndex(1).build());
            return courseRepository.save(c).getId();
        });

        List<String> titles = tx.execute(status -> {
            Course c = courseRepository.findById(courseId).orElseThrow();
            return c.getModules().stream()
                    .map(CourseModule::getTitle)
                    .toList();
        });

        assertEquals(List.of("M1"), titles);
    }
}