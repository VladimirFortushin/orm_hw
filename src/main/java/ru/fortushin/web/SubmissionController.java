package ru.fortushin.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.fortushin.service.SubmissionService;
import ru.fortushin.web.dto.SubmissionDto;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/assignments/{assignmentId}/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionDto submit(@PathVariable Long assignmentId,
                                @RequestParam Long studentId,
                                @RequestParam String content) {
        return submissionService.submitDto(assignmentId, studentId, content);
    }

    @PutMapping("/submissions/{id}/grade")
    public SubmissionDto grade(@PathVariable Long id,
                               @RequestParam int score,
                               @RequestParam(required = false) String feedback) {
        return submissionService.gradeDto(id, score, feedback);
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    public List<SubmissionDto> byAssignment(@PathVariable Long assignmentId) {
        return submissionService.findByAssignmentDto(assignmentId);
    }
}