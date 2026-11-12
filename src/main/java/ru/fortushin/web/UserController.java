package ru.fortushin.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.fortushin.entity.enums.Role;
import ru.fortushin.service.EnrollmentService;
import ru.fortushin.service.SubmissionService;
import ru.fortushin.service.UserService;
import ru.fortushin.web.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EnrollmentService enrollmentService;
    private final SubmissionService submissionService;

    @GetMapping
    public List<UserDto> list() {
        return userService.listDto();
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) {
        return userService.getDto(id);
    }

    @PostMapping
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestParam String name,
                          @RequestParam String email,
                          @RequestParam Role role) {
        return userService.createDto(name, email, role);
    }

    @PutMapping("/{id}/profile")
    public ProfileDto upsertProfile(@PathVariable Long id,
                                    @RequestParam(required = false) String bio,
                                    @RequestParam(required = false) String avatarUrl) {
        return userService.upsertProfileDto(id, bio, avatarUrl);
    }

    @GetMapping("/{id}/enrollments")
    public List<EnrollmentDto> enrollments(@PathVariable Long id) {
        return enrollmentService.findByStudentDto(id);
    }

    @GetMapping("/{id}/submissions")
    public List<SubmissionDto> submissions(@PathVariable Long id) {
        return submissionService.findByStudentDto(id);
    }
}