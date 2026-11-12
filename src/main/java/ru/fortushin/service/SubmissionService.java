package ru.fortushin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fortushin.entity.Assignment;
import ru.fortushin.entity.Submission;
import ru.fortushin.entity.User;
import ru.fortushin.entity.enums.SubmissionStatus;
import ru.fortushin.exception.BusinessException;
import ru.fortushin.exception.NotFoundException;
import ru.fortushin.repository.AssignmentRepository;
import ru.fortushin.repository.SubmissionRepository;
import ru.fortushin.repository.UserRepository;
import ru.fortushin.web.dto.DtoMapper;
import ru.fortushin.web.dto.SubmissionDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public Submission submit(Long assignmentId, Long studentId, String content) {
        if (submissionRepository.existsByAssignmentIdAndStudentId(assignmentId, studentId)) {
            throw new BusinessException(
                    "Student " + studentId + " already submitted assignment " + assignmentId);
        }
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment", assignmentId));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("User", studentId));

        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .content(content)
                .submittedAt(LocalDateTime.now())
                .status(SubmissionStatus.SUBMITTED)
                .build();
        Submission saved = submissionRepository.save(submission);
        log.info("Submission id={} submitted by student={} for assignment={}",
                saved.getId(), studentId, assignmentId);
        return saved;
    }

    @Transactional
    public Submission grade(Long submissionId, int score, String feedback) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission", submissionId));
        int max = submission.getAssignment().getMaxScore();
        if (score < 0 || score > max) {
            throw new BusinessException("Score must be between 0 and " + max + ", got " + score);
        }
        submission.setScore(score);
        submission.setFeedback(feedback);
        submission.setStatus(SubmissionStatus.GRADED);
        return submission;
    }

    public List<Submission> findByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    public List<Submission> findByStudent(Long studentId) {
        return submissionRepository.findByStudentId(studentId);
    }

    public List<Submission> findUngradedByTeacher(Long teacherId) {
        return submissionRepository.findUngradedByTeacherId(teacherId);
    }

    public double avgScore(Long assignmentId) {
        Double avg = submissionRepository.avgScoreByAssignment(assignmentId);
        return avg != null ? avg : 0.0;
    }

    @Transactional
    public SubmissionDto submitDto(Long assignmentId, Long studentId, String content) {
        return DtoMapper.toDto(submit(assignmentId, studentId, content));
    }

    @Transactional
    public SubmissionDto gradeDto(Long submissionId, int score, String feedback) {
        return DtoMapper.toDto(grade(submissionId, score, feedback));
    }

    @Transactional(readOnly = true)
    public List<SubmissionDto> findByAssignmentDto(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId).stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubmissionDto> findByStudentDto(Long studentId) {
        return submissionRepository.findByStudentId(studentId).stream()
                .map(DtoMapper::toDto)
                .toList();
    }
}