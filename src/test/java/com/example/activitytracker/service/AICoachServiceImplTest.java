
package com.example.activitytracker.service;

import com.example.activitytracker.DTO.AICoachResponseDTO;
import com.example.activitytracker.DTO.ProgressResponseDTO;
import com.example.activitytracker.exception.AIConfigurationException;
import com.example.activitytracker.exception.AIRateException;
import com.example.activitytracker.exception.AIResponseException;
import com.example.activitytracker.exception.AIServiceUnavailableException;
import com.example.activitytracker.model.TaskEntry;
import com.example.activitytracker.model.User;
import com.example.activitytracker.repository.TaskEntryRepository;
import com.example.activitytracker.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.errors.ApiException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    private User user;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test@example.com", null
                )
        );

        user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void stubEmptyTaskHistory() {
        when(taskEntryRepository.findByUser(user))
                .thenReturn(List.of());

        when(taskEntryRepository.findByUser(
                eq(user),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        when(progressService.calculateProgress(anyList()))
                .thenReturn(new ProgressResponseDTO());
    }

    @Test
    void shouldThrowAIResponseExceptionWhenResponseIsInvalid()
            throws Exception {

        stubEmptyTaskHistory();

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

        stubEmptyTaskHistory();

        ApiException apiException = mock(ApiException.class);
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

        stubEmptyTaskHistory();

        ApiException apiException = mock(ApiException.class);
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

        stubEmptyTaskHistory();

        ApiException apiException = mock(ApiException.class);
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

        stubEmptyTaskHistory();

        ApiException apiException = mock(ApiException.class);

        when(apiException.code()).thenReturn(400);
        when(apiException.getMessage()).thenReturn(
                "400 INVALID_ARGUMENT. API key not valid. "
                        + "Details: {\"reason\":\"API_KEY_INVALID\"}"
        );

        when(geminiClientService.generateContent(anyString()))
                .thenThrow(apiException);

        assertThrows(
                AIConfigurationException.class,
                () -> aiCoachService.getCoaching()
        );
    }

    @Test
    void shouldCalculateProgressFromFullHistoryAndUsePaginatedHistoryForPrompt()
            throws Exception {

        TaskEntry oldTask = new TaskEntry();
        oldTask.setTaskDescription("Older task");

        TaskEntry recentTask = new TaskEntry();
        recentTask.setTaskDescription("Recent task");

        List<TaskEntry> allTasks = List.of(oldTask, recentTask);
        List<TaskEntry> recentTasks = List.of(recentTask);

        when(taskEntryRepository.findByUser(user))
                .thenReturn(allTasks);

        when(taskEntryRepository.findByUser(
                eq(user),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(recentTasks));

        when(progressService.calculateProgress(allTasks))
                .thenReturn(new ProgressResponseDTO());

        when(geminiClientService.generateContent(anyString()))
                .thenReturn(
                        "{\"summary\":\"Summary\",\"feedback\":\"Feedback\","
                                + "\"suggestion\":\"Suggestion\","
                                + "\"motivation\":\"Motivation\"}"
                );

        when(objectMapper.readValue(
                anyString(),
                eq(AICoachResponseDTO.class)
        )).thenReturn(new AICoachResponseDTO());

        aiCoachService.getCoaching();

        // Progress calculation must receive the full task history.
        verify(progressService).calculateProgress(allTasks);

        // Gemini must receive the bounded task history.
        verify(geminiClientService).generateContent(argThat(prompt ->
                prompt.contains("Recent task")
                        && !prompt.contains("Older task")
        ));

        // Verify page size and deterministic sorting.
        verify(taskEntryRepository).findByUser(
                eq(user),
                argThat((Pageable pageable) ->
                        pageable.getPageNumber() == 0
                                && pageable.getPageSize() == 30
                                && pageable.getSort()
                                        .getOrderFor("date").getDirection()
                                        == Sort.Direction.DESC
                                && pageable.getSort()
                                        .getOrderFor("id").getDirection()
                                        == Sort.Direction.DESC
                )
        );
    }

    @Test
    void shouldKeepPromptWithinCharacterLimit() throws Exception {

        List<TaskEntry> tasks = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            TaskEntry task = new TaskEntry();
            task.setDate(LocalDate.now().minusDays(i));
            task.setTaskDescription(
                    "Task " + i + " " + "A".repeat(490)
            );
            task.setCompleted(false);
            tasks.add(task);
        }

        when(taskEntryRepository.findByUser(user))
                .thenReturn(tasks);

        when(taskEntryRepository.findByUser(
                eq(user),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(tasks));

        when(progressService.calculateProgress(tasks))
                .thenReturn(new ProgressResponseDTO());

        when(geminiClientService.generateContent(anyString()))
                .thenReturn("""
                        {
                          "summary": "Summary",
                          "feedback": "Feedback",
                          "suggestion": "Suggestion",
                          "motivation": "Motivation"
                        }
                        """);

        when(objectMapper.readValue(
                anyString(),
                eq(AICoachResponseDTO.class)
        )).thenReturn(new AICoachResponseDTO());

        aiCoachService.getCoaching();

        ArgumentCaptor<String> promptCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(geminiClientService)
                .generateContent(promptCaptor.capture());

        String prompt = promptCaptor.getValue();

        assertTrue(
                prompt.length() <= 12_000,
                "Prompt must not exceed 12,000 characters"
        );

        assertTrue(
                prompt.contains("Task 0"),
                "The newest task should be included"
        );

        assertFalse(
                prompt.contains("Task 29"),
                "Older tasks should be excluded when the limit is reached"
        );
    }
}
