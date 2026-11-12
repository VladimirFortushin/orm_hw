package ru.fortushin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // 1-N: уроки модуля
    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Lesson> lessons = new ArrayList<>();

    // 1-1 (опционально): тест модуля (владелец — Quiz.module)
    @OneToOne(mappedBy = "module", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Quiz quiz;

    // ===== helpers =====
    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
        lesson.setModule(this);
    }
    public void removeLesson(Lesson lesson) {
        lessons.remove(lesson);
        lesson.setModule(null);
    }
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
        if (quiz != null) quiz.setModule(this);
    }
}