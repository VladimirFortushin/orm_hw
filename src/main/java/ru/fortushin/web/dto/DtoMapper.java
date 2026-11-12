package ru.fortushin.web.dto;

import ru.fortushin.entity.*;
import ru.fortushin.repository.EnrollmentRepository;

import java.util.List;

public final class DtoMapper {

    private DtoMapper() {}

    public static CategoryDto toDto(Category c) {
        return c == null ? null : new CategoryDto(c.getId(), c.getName());
    }

    public static TagDto toDto(Tag t) {
        return t == null ? null : new TagDto(t.getId(), t.getName());
    }

    public static UserShortDto toShort(User u) {
        return u == null ? null : new UserShortDto(u.getId(), u.getName(), u.getEmail(), u.getRole());
    }

    public static ProfileDto toDto(Profile p) {
        return p == null ? null : new ProfileDto(p.getId(), p.getBio(), p.getAvatarUrl());
    }

    public static UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getRole(),
                u.getProfile() == null ? null : toDto(u.getProfile()));
    }

    public static AssignmentDto toDto(Assignment a) {
        return new AssignmentDto(a.getId(), a.getTitle(), a.getDescription(),
                a.getDueDate(), a.getMaxScore());
    }

    public static LessonDto toDto(Lesson l) {
        List<AssignmentDto> assignments = l.getAssignments().stream()
                .map(DtoMapper::toDto)
                .toList();
        return new LessonDto(l.getId(), l.getTitle(), l.getContent(), l.getVideoUrl(), assignments);
    }

    public static QuizShortDto toShort(Quiz q) {
        return q == null ? null : new QuizShortDto(q.getId(), q.getTitle(), q.getTimeLimit());
    }

    public static ModuleDto toDto(CourseModule m) {
        List<LessonDto> lessons = m.getLessons().stream()
                .map(DtoMapper::toDto)
                .toList();
        return new ModuleDto(m.getId(), m.getTitle(), m.getOrderIndex(),
                m.getDescription(), lessons, toShort(m.getQuiz()));
    }

    public static CourseDto toSummary(Course c, long enrolledCount, double avgRating) {
        return new CourseDto(c.getId(), c.getTitle(), c.getDescription(),
                c.getDuration(), c.getStartDate(),
                toDto(c.getCategory()), toShort(c.getTeacher()),
                c.getTags().stream().map(DtoMapper::toDto).toList(),
                List.of(),
                enrolledCount, avgRating);
    }

    public static CourseDto toFull(Course c, long enrolledCount, double avgRating) {
        List<ModuleDto> modules = c.getModules().stream()
                .map(DtoMapper::toDto)
                .toList();
        return new CourseDto(c.getId(), c.getTitle(), c.getDescription(),
                c.getDuration(), c.getStartDate(),
                toDto(c.getCategory()), toShort(c.getTeacher()),
                c.getTags().stream().map(DtoMapper::toDto).toList(),
                modules, enrolledCount, avgRating);
    }

    public static QuizDto toFull(Quiz q) {
        List<QuizDto.QuestionDto> questions = q.getQuestions().stream()
                .map(question -> new QuizDto.QuestionDto(
                        question.getId(),
                        question.getText(),
                        question.getType(),
                        question.getOptions().stream()
                                .map(o -> new QuizDto.AnswerOptionDto(o.getId(), o.getText(), o.isCorrect()))
                                .toList()))
                .toList();
        return new QuizDto(q.getId(), q.getTitle(), q.getTimeLimit(), questions);
    }

    public static EnrollmentDto toDto(Enrollment e) {
        return new EnrollmentDto(
                e.getId(),
                e.getCourse().getId(),
                e.getCourse().getTitle(),
                e.getStudent().getId(),
                e.getStudent().getName(),
                e.getEnrollDate(),
                e.getStatus());
    }

    public static SubmissionDto toDto(Submission s) {
        return new SubmissionDto(
                s.getId(),
                s.getAssignment().getId(),
                s.getAssignment().getTitle(),
                s.getStudent().getId(),
                s.getStudent().getName(),
                s.getSubmittedAt(),
                s.getContent(),
                s.getScore(),
                s.getFeedback(),
                s.getStatus());
    }

    public static QuizSubmissionDto toDto(QuizSubmission qs) {
        return new QuizSubmissionDto(
                qs.getId(),
                qs.getQuiz().getId(),
                qs.getQuiz().getTitle(),
                qs.getStudent().getId(),
                qs.getStudent().getName(),
                qs.getScore(),
                qs.getTakenAt());
    }

    public static CourseReviewDto toDto(CourseReview r) {
        return new CourseReviewDto(
                r.getId(),
                r.getCourse().getId(),
                r.getStudent().getId(),
                r.getStudent().getName(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt());
    }
}