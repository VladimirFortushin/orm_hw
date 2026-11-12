package ru.fortushin.web.dto;

import ru.fortushin.entity.enums.Role;

public record UserShortDto(Long id, String name, String email, Role role) {}