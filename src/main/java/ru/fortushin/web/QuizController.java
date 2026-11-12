package ru.fortushin.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.fortushin.service.QuizService;
import ru.fortushin.service.QuizSubmissionService;
import ru.fortushin.web.dto.QuizDto;
import ru.fortushin.web.dto.QuizSubmissionDto;
import ru.fortushin.web.dto.TakeQuizRequest;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuizSubmissionService quizSubmissionService;

    @GetMapping("/{id}")
    public QuizDto getById(@PathVariable Long id) {
        return quizService.getFullDto(id);
    }

    @PostMapping("/modules/{moduleId}")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizDto createForModule(@PathVariable Long moduleId,
                                   @RequestParam String title,
                                   @RequestParam(defaultValue = "15") int timeLimit) {
        return quizService.createForModuleDto(moduleId, title, timeLimit);
    }

    @PostMapping("/{id}/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizSubmissionDto submit(@PathVariable Long id,
                                    @RequestParam Long studentId,
                                    @RequestBody TakeQuizRequest req) {
        return quizSubmissionService.takeQuizDto(id, studentId, req.answers());
    }
}