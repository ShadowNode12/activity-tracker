
package com.example.activitytracker.service;

import com.example.activitytracker.DTO.ProgressResponseDTO;
import com.example.activitytracker.model.TaskEntry;
import com.example.activitytracker.model.User;
import com.example.activitytracker.repository.TaskEntryRepository;
import com.example.activitytracker.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressServiceImplTest {

    @Mock
    private TaskEntryRepository taskEntryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProgressServiceImpl progressService;

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

    @Test
    void shouldCalculateTaskStatisticsCorrectly() {
        LocalDate today = LocalDate.now();

        List<TaskEntry> tasks = List.of(
                createTask(today, true),
                createTask(today, false),
                createTask(today.minusDays(1), true),
                createTask(today.minusDays(1), false)
        );

        when(taskEntryRepository.findByUser(user))
                .thenReturn(tasks);

        ProgressResponseDTO result = progressService.getProgress();

        assertEquals(4, result.getTotalTasks());
        assertEquals(2, result.getCompletedTasks());
        assertEquals(50.0, result.getCompletionRate(), 0.001);
        assertEquals(2, result.getTasksToday());
        assertEquals(1, result.getCompletedToday());
        assertEquals(2, result.getCurrentStreak());
    }

    @Test
    void shouldReturnZeroStatisticsWhenUserHasNoTasks() {
        when(taskEntryRepository.findByUser(user))
                .thenReturn(List.of());

        ProgressResponseDTO result = progressService.getProgress();

        assertEquals(0, result.getTotalTasks());
        assertEquals(0, result.getCompletedTasks());
        assertEquals(0.0, result.getCompletionRate(), 0.001);
        assertEquals(0, result.getTasksToday());
        assertEquals(0, result.getCompletedToday());
        assertEquals(0, result.getCurrentStreak());
    }

    @Test
    void shouldCalculateConsecutiveCompletionStreak() {
        LocalDate today = LocalDate.now();

        List<TaskEntry> tasks = List.of(
                createTask(today, true),
                createTask(today.minusDays(1), true),
                createTask(today.minusDays(2), true),
                createTask(today.minusDays(3), false)
        );

        when(taskEntryRepository.findByUser(user))
                .thenReturn(tasks);

        ProgressResponseDTO result = progressService.getProgress();

        assertEquals(3, result.getCurrentStreak());
    }

    @Test
    void shouldReturnZeroStreakWhenNoTaskWasCompletedToday() {
        LocalDate today = LocalDate.now();

        List<TaskEntry> tasks = List.of(
                createTask(today.minusDays(1), true),
                createTask(today.minusDays(2), true)
        );

        when(taskEntryRepository.findByUser(user))
                .thenReturn(tasks);

        ProgressResponseDTO result = progressService.getProgress();

        assertEquals(0, result.getCurrentStreak());
    }

    private TaskEntry createTask(LocalDate date, boolean completed) {
        TaskEntry task = new TaskEntry();
        task.setDate(date);
        task.setCompleted(completed);
        return task;
    }
}
