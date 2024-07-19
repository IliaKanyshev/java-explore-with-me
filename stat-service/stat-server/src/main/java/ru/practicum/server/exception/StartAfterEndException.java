package ru.practicum.server.exception;

public class StartAfterEndException extends RuntimeException {
    public StartAfterEndException(String message) {
        super(message);
    }
}
