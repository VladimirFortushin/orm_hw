package ru.fortushin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fortushin.entity.*;
import ru.fortushin.entity.enums.QuestionType;
import ru.fortushin.exception.BusinessException;
import ru.fortushin.exception.NotFoundException;
import ru.fortushin.repository.*;
import ru.fortushin.web.dto.DtoMapper;
import ru.fortushin.web.dto.QuizDto;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;

    @Transactional
    public Quiz createForModule(Long moduleId, String title, int timeLimit) {
        CourseModule module = courseModuleRepository.findById(moduleId)
                .orElseThrow(() -> new NotFoundException("Module", moduleId));
        if (module.getQuiz() != null) {
            throw new BusinessException("Module " + moduleId + " already has a quiz");
        }
        Quiz quiz = Quiz.builder()
                .title(title)
                .timeLimit(timeLimit)
                .module(module)
                .build();
        return quizRepository.save(quiz);
    }

    @Transactional
    public Question addQuestion(Long quizId, String text, QuestionType type) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz", quizId));
        Question q = Question.builder()
                .text(text)
                .type(type)
                .build();
        quiz.addQuestion(q);
        return questionRepository.saveAndFlush(q);
    }

    @Transactional
    public AnswerOption addOption(Long questionId, String text, boolean correct) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question", questionId));
        AnswerOption opt = AnswerOption.builder()
                .text(text)
                .correct(correct)
                .build();
        q.addOption(opt);
        return answerOptionRepository.saveAndFlush(opt);
    }

    public Quiz getById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quiz", id));
    }

    public Quiz getWithQuestionsAndOptions(Long id) {
        return quizRepository.findByIdWithQuestionsAndOptions(id)
                .orElseThrow(() -> new NotFoundException("Quiz", id));
    }

    public List<Quiz> findAll() { return quizRepository.findAll(); }
    @Transactional(readOnly = true)
    public QuizDto getFullDto(Long id) {
        Quiz q = quizRepository.findByIdWithQuestionsAndOptions(id)
                .orElseThrow(() -> new NotFoundException("Quiz", id));
        return DtoMapper.toFull(q);
    }

    @Transactional
    public QuizDto createForModuleDto(Long moduleId, String title, int timeLimit) {
        Quiz q = createForModule(moduleId, title, timeLimit);
        return new QuizDto(q.getId(), q.getTitle(), q.getTimeLimit(), List.of());
    }
}