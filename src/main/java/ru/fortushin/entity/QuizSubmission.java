package ru.fortushin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_submissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private int score;   // % правильных

    @Column(name = "taken_at", nullable = false)
    private LocalDateTime takenAt;

    @PrePersist
    void onCreate() {
        if (takenAt == null) takenAt = LocalDateTime.now();
    }
}