package ru.fortushin.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
    public NotFoundException(String entity, Long id) {
        super(entity + " with id " + id + " not found");
    }
}