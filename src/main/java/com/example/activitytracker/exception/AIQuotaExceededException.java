package com.example.activitytracker.exception;

public class AIQuotaExceededException extends RuntimeException {

    public AIQuotaExceededException(String message) {
        super(message);
    }
}
