package ru.fortushin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "answer_options")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AnswerOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    // Lombok сгенерирует isCorrect()/setCorrect(...)
    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
}