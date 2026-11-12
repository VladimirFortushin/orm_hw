package ru.fortushin.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.fortushin.entity.enums.SubmissionStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "submissions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_submission_assignment_student",
                columnNames = {"assignment_id", "student_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(columnDefinition = "TEXT")
    private String content;

    // null пока не оценено
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubmissionStatus status;

    @PrePersist
    void onCreate() {
        if (submittedAt == null) submittedAt = LocalDateTime.now();
        if (status == null) status = SubmissionStatus.SUBMITTED;
    }
}