package ru.fortushin.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.fortushin.entity.enums.EnrollmentStatus;

import java.time.LocalDate;

@Entity
@Table(name = "enrollments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_enrollment_student_course",
                columnNames = {"student_id", "course_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enroll_date", nullable = false)
    private LocalDate enrollDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status;

    @PrePersist
    void onCreate() {
        if (enrollDate == null) enrollDate = LocalDate.now();
        if (status == null) status = EnrollmentStatus.ACTIVE;
    }
}