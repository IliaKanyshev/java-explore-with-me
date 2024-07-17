package ru.practicum.ewm.exception;

public class EventDateBeforePublishException extends RuntimeException {
    public EventDateBeforePublishException(String message) {
        super(message);
    }
}
