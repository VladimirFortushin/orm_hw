package ru.fortushin.web.dto;

import java.util.List;
import java.util.Map;

public record TakeQuizRequest(Map<Long, List<Long>> answers) {}