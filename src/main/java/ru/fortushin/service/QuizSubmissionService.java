package ru.fortushin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fortushin.entity.*;
import ru.fortushin.exception.BusinessException;
import ru.fortushin.exception.NotFoundException;
import ru.fortushin.repository.*;
import ru.fortushin.web.dto.DtoMapper;
import ru.fortushin.web.dto.QuizSubmissionDto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class QuizSubmissionService {

    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    @Transactional
    public QuizSubmission takeQuiz(Long quizId, Long studentId, Map<Long, List<Long>> answers) {
        Quiz quiz = quizRepository.findByIdWithQuestionsAndOptions(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz", quizId));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("User", studentId));

        if (quiz.getQuestions().isEmpty()) {
            throw new BusinessException("Quiz " + quizId + " has no questions");
        }

        int correctCount = 0;
        for (Question question : quiz.getQuestions()) {
            Set<Long> selected = new HashSet<>(
                    answers.getOrDefault(question.getId(), List.of()));
            Set<Long> correctIds = question.getOptions().stream()
                    .filter(AnswerOption::isCorrect)
                    .map(AnswerOption::getId)
                    .collect(Collectors.toSet());
            if (selected.equals(correctIds)) {
                correctCount++;
            }
        }

        int score = (int) Math.round(100.0 * correctCount / quiz.getQuestions().size());

        QuizSubmission submission = QuizSubmission.builder()
                .quiz(quiz)
                .student(student)
                .score(score)
                .takenAt(LocalDateTime.now())
                .build();
        QuizSubmission saved = quizSubmissionRepository.save(submission);
        log.info("Quiz id={} taken by student={}, score={}%", quizId, studentId, score);
        return saved;
    }

    public List<QuizSubmission> findByStudent(Long studentId) {
        return quizSubmissionRepository.findByStudentId(studentId);
    }

    public List<QuizSubmission> findByQuiz(Long quizId) {
        return quizSubmissionRepository.findByQuizId(quizId);
    }

    public int bestScore(Long quizId, Long studentId) {
        return quizSubmissionRepository.findBestScore(quizId, studentId).orElse(0);
    }

    public double avgScore(Long quizId) {
        Double avg = quizSubmissionRepository.avgScoreByQuiz(quizId);
        return avg != null ? avg : 0.0;
    }

    @Transactional
    public QuizSubmissionDto takeQuizDto(Long quizId, Long studentId, Map<Long, List<Long>> answers) {
        return DtoMapper.toDto(takeQuiz(quizId, studentId, answers));
    }
}