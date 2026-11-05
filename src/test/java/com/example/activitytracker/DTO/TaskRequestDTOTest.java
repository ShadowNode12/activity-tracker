package com.example.activitytracker.DTO;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class TaskRequestDTOTest {

    private static final ZoneId APP_ZONE =
            ZoneId.of("Asia/Kolkata");

    private static Validator validator;
    private static ValidatorFactory validatorFactory;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldAcceptTodayDate() {
        TaskRequestDTO request = createValidRequest();
        request.setDate(LocalDate.now(APP_ZONE));

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void shouldAcceptPastDate() {
        TaskRequestDTO request = createValidRequest();
        request.setDate(LocalDate.now(APP_ZONE).minusDays(1));

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectFutureDate() {
        TaskRequestDTO request = createValidRequest();
        request.setDate(LocalDate.now(APP_ZONE).plusDays(1));

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectNullDate() {
        TaskRequestDTO request = createValidRequest();
        request.setDate(null);

        assertFalse(validator.validate(request).isEmpty());
    }

    private TaskRequestDTO createValidRequest() {
        TaskRequestDTO request = new TaskRequestDTO();
        request.setDate(LocalDate.now(APP_ZONE));
        request.setTaskDescription("Complete DSA practice");
        request.setCompleted(false);

        return request;
    }
}