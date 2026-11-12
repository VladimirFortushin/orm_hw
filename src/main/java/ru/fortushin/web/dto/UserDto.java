package ru.fortushin.web.dto;

import ru.fortushin.entity.enums.Role;

public record UserDto(
        Long id,
        String name,
        String email,
        Role role,
        ProfileDto profile
) {}