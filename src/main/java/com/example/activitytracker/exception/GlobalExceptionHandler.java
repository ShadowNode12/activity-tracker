package com.example.activitytracker.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.activitytracker.DTO.ErrorResponseDTO;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidCredentialsException(
            InvalidCredentialsException ex) {

        return buildError(
                HttpStatus.UNAUTHORIZED,
                "INVALID_CREDENTIALS",
                ex.getMessage()
        );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex) {

        return buildError(
                HttpStatus.CONFLICT,
                "EMAIL_ALREADY_EXISTS",
                ex.getMessage()
        );
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleTaskNotFoundException(
            TaskNotFoundException ex) {

        return buildError(
                HttpStatus.NOT_FOUND,
                "TASK_NOT_FOUND",
                ex.getMessage()
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFoundException(
            UserNotFoundException ex) {

        return buildError(
                HttpStatus.NOT_FOUND,
                "USER_NOT_FOUND",
                ex.getMessage()
        );
    }

    @ExceptionHandler(TaskAccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleTaskAccessDeniedException(
            TaskAccessDeniedException ex) {

        return buildError(
                HttpStatus.FORBIDDEN,
                "TASK_ACCESS_DENIED",
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.merge(
                        error.getField(),
                        error.getDefaultMessage(),
                        (existing, additional) ->
                                existing + "; " + additional
                )
        );

        ErrorResponseDTO response = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                "One or more fields are invalid.",
                errors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(AIResponseException.class)
    public ResponseEntity<ErrorResponseDTO> handleAIResponseException(
            AIResponseException ex) {

        return buildError(
                HttpStatus.BAD_GATEWAY,
                "AI_RESPONSE_ERROR",
                "The AI service returned an invalid response."
        );
    }

    @ExceptionHandler(AIRateException.class)
    public ResponseEntity<ErrorResponseDTO> handleAIRateException(
            AIRateException ex) {

        return buildError(
                HttpStatus.TOO_MANY_REQUESTS,
                "AI_RATE_LIMIT_EXCEEDED",
                "The AI service rate limit has been reached. Please try again later."
        );
    }

    @ExceptionHandler(AIServiceUnavailableException.class)
    public ResponseEntity<ErrorResponseDTO> handleAIServiceUnavailableException(
            AIServiceUnavailableException ex) {

        return buildError(
                HttpStatus.SERVICE_UNAVAILABLE,
                "AI_SERVICE_UNAVAILABLE",
                "The AI service is temporarily unavailable. Please try again later."
        );
    }

    @ExceptionHandler(AIConfigurationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAIConfigurationException(
            AIConfigurationException ex) {

        logger.error("AI service configuration error", ex);

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "AI_CONFIGURATION_ERROR",
                "The AI service is incorrectly configured. Please try again later."
        );
    }

    @ExceptionHandler(AIQuotaExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleAIQuotaExceededException(
            AIQuotaExceededException ex) {

        return buildError(
                HttpStatus.TOO_MANY_REQUESTS,
                "AI_QUOTA_EXCEEDED",
                ex.getMessage()
        );
    }

    @ExceptionHandler(DailyQuoteNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleDailyQuoteNotFoundException(
            DailyQuoteNotFoundException ex) {

        return buildError(
                HttpStatus.SERVICE_UNAVAILABLE,
                "DAILY_QUOTE_UNAVAILABLE",
                ex.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                ex.getMessage()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(
            RuntimeException ex) {

        logger.error("Unexpected runtime exception occurred", ex);

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred."
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpectedException(
            Exception ex) {

        logger.error("Unexpected exception occurred", ex);

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred."
        );
    }

    private ResponseEntity<ErrorResponseDTO> buildError(
            HttpStatus status,
            String error,
            String message) {

        ErrorResponseDTO response = new ErrorResponseDTO(
                status.value(),
                error,
                message
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}
