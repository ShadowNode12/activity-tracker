package com.example.activitytracker.service;

import com.example.activitytracker.DTO.AICoachResponseDTO;
import com.example.activitytracker.DTO.ProgressResponseDTO;
import com.example.activitytracker.exception.AIConfigurationException;
import com.example.activitytracker.exception.AIRateException;
import com.example.activitytracker.exception.AIResponseException;
import com.example.activitytracker.exception.AIServiceUnavailableException;
import com.example.activitytracker.model.User;
import com.example.activitytracker.repository.TaskEntryRepository;
import com.example.activitytracker.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.mockito.ArgumentMatchers.anyList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AICoachServiceImplTest {

    @Mock
    private GeminiClientService geminiClientService;

    @Mock
    private ProgressService progressService;

    @Mock
    private TaskEntryRepository taskEntryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AICoachServiceImpl aiCoachService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test@example.com", null
                )
        );

        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(taskEntryRepository.findByUser(user))
                .thenReturn(List.of());

        when(progressService.calculateProgress(anyList()))
                .thenReturn(new ProgressResponseDTO());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldThrowAIResponseExceptionWhenResponseIsInvalid()
            throws Exception {

        when(geminiClientService.generateContent(anyString()))
                .thenReturn("invalid JSON");

        when(objectMapper.readValue(
                anyString(),
                eq(AICoachResponseDTO.class)
        )).thenThrow(new JsonProcessingException("Invalid JSON") {});

        assertThrows(
                AIResponseException.class,
                () -> aiCoachService.getCoaching()
        );
    }

    @Test
    void shouldThrowAIRateExceptionWhenGeminiRateLimitIsExceeded() {

        com.google.genai.errors.ApiException apiException =
                mock(com.google.genai.errors.ApiException.class);

        when(apiException.code()).thenReturn(429);

        when(geminiClientService.generateContent(anyString()))
                .thenThrow(apiException);

        assertThrows(
                AIRateException.class,
                () -> aiCoachService.getCoaching()
        );
    }

    @Test
    void shouldThrowAIServiceUnavailableExceptionWhenGeminiIsUnavailable() {

        com.google.genai.errors.ApiException apiException =
                mock(com.google.genai.errors.ApiException.class);

        when(apiException.code()).thenReturn(503);

        when(geminiClientService.generateContent(anyString()))
                .thenThrow(apiException);

        assertThrows(
                AIServiceUnavailableException.class,
                () -> aiCoachService.getCoaching()
        );
    }

    @Test
    void shouldThrowAIConfigurationExceptionWhenGeminiAuthenticationFails() {

        com.google.genai.errors.ApiException apiException =
                mock(com.google.genai.errors.ApiException.class);

        when(apiException.code()).thenReturn(401);

        when(geminiClientService.generateContent(anyString()))
                .thenThrow(apiException);

        assertThrows(
                AIConfigurationException.class,
                () -> aiCoachService.getCoaching()
        );
    }
    
    @Test
    void shouldThrowAIConfigurationExceptionWhenGeminiReturnsInvalidApiKey() {
        com.google.genai.errors.ApiException apiException =
                mock(com.google.genai.errors.ApiException.class);

        when(apiException.code()).thenReturn(400);
        when(apiException.getMessage()).thenReturn(
                "400 INVALID_ARGUMENT. API key not valid. " +
                "Details: {\"reason\":\"API_KEY_INVALID\"}"
        );

        when(geminiClientService.generateContent(anyString()))
                .thenThrow(apiException);

        assertThrows(
                AIConfigurationException.class,
                () -> aiCoachService.getCoaching()
        );
    }
    
}
