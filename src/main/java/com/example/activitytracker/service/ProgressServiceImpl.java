
package com.example.activitytracker.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.activitytracker.DTO.ProgressResponseDTO;
import com.example.activitytracker.model.TaskEntry;
import com.example.activitytracker.model.User;
import com.example.activitytracker.repository.TaskEntryRepository;
import com.example.activitytracker.repository.UserRepository;
import com.example.activitytracker.exception.UserNotFoundException;

@Service
public class ProgressServiceImpl implements ProgressService {

    private final TaskEntryRepository taskEntryRepository;
    private final UserRepository userRepository;

    public ProgressServiceImpl(
            TaskEntryRepository taskEntryRepository,
            UserRepository userRepository) {
        this.taskEntryRepository = taskEntryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ProgressResponseDTO getProgress() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<TaskEntry> tasks = taskEntryRepository.findByUser(user);

        return calculateProgress(tasks);
    }

    @Override
    public ProgressResponseDTO calculateProgress(List<TaskEntry> tasks) {

        long totalTasks = tasks.size();
        long completedTasks = 0;
        long tasksToday = 0;
        long completedToday = 0;

        LocalDate today = LocalDate.now();

        Set<LocalDate> completedDates = new HashSet<>();

        for (TaskEntry task : tasks) {

            if (task.isCompleted()) {
                completedTasks++;
                completedDates.add(task.getDate());
            }

            if (task.getDate().equals(today)) {
                tasksToday++;
            }

            if (task.getDate().equals(today) && task.isCompleted()) {
                completedToday++;
            }
        }

        long currentStreak = 0;
        LocalDate streakDate = today;

        while (completedDates.contains(streakDate)) {
            currentStreak++;
            streakDate = streakDate.minusDays(1);
        }

        double completionRate = 0;

        if (totalTasks > 0) {
            completionRate =
                    ((double) completedTasks / totalTasks) * 100;
        }

        ProgressResponseDTO response = new ProgressResponseDTO();

        response.setTotalTasks(totalTasks);
        response.setCompletedTasks(completedTasks);
        response.setCompletionRate(completionRate);
        response.setCurrentStreak(currentStreak);
        response.setTasksToday(tasksToday);
        response.setCompletedToday(completedToday);

        return response;
    }
}
