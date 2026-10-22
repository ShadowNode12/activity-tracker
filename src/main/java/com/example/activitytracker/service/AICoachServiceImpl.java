package com.example.activitytracker.service;

import com.example.activitytracker.DTO.AICoachResponseDTO;
import com.example.activitytracker.DTO.ProgressResponseDTO;
import com.example.activitytracker.exception.AIConfigurationException;
import com.example.activitytracker.exception.AIRateException;
import com.example.activitytracker.exception.AIResponseException;
import com.example.activitytracker.exception.AIServiceUnavailableException;
import com.example.activitytracker.exception.UserNotFoundException;
import com.example.activitytracker.model.TaskEntry;
import com.example.activitytracker.model.User;
import com.example.activitytracker.repository.TaskEntryRepository;
import com.example.activitytracker.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.errors.ApiException;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
@Service
public class AICoachServiceImpl implements AICoachService {

    private final GeminiClientService geminiClientService;
    private final ProgressService progressService;
    private final TaskEntryRepository taskEntryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AICoachServiceImpl(
            GeminiClientService geminiClientService,
            ProgressService progressService,
            TaskEntryRepository taskEntryRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper) {

        this.geminiClientService = geminiClientService;
        this.progressService = progressService;
        this.taskEntryRepository = taskEntryRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public AICoachResponseDTO getCoaching() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<TaskEntry> allTasks = taskEntryRepository.findByUser(user);

        ProgressResponseDTO progress =
                progressService.calculateProgress(allTasks);

        List<TaskEntry> recentTasks = taskEntryRepository.findByUser(
                user,
                PageRequest.of(
                        0,
                        30,
                        Sort.by(
                                Sort.Order.desc("date"),
                                Sort.Order.desc("id")
                        )
                )
        ).getContent();

        String prompt = buildPrompt(progress, recentTasks);

        String response;

        try {
            response = geminiClientService.generateContent(prompt);

            
        } catch (ApiException e) {

            

            int statusCode = e.code();

            if (statusCode == 429) {
                throw new AIRateException(
                        "Gemini API rate limit exceeded", e);
            }

            if (statusCode == 401 || statusCode == 403
                    || (statusCode == 400
                    && e.getMessage() != null
                    && e.getMessage().contains("API_KEY_INVALID"))) {

                throw new AIConfigurationException(
                        "Gemini API authentication or configuration failed", e);
            }

            if (statusCode >= 500 && statusCode <= 599) {
                throw new AIServiceUnavailableException(
                        "Gemini service is temporarily unavailable", e);
            }

            throw e;
        }
        

        try {
            return objectMapper.readValue(
                    response,
                    AICoachResponseDTO.class
            );

        } catch (JsonProcessingException e) {
            throw new AIResponseException(
                    "Gemini returned an invalid response", e);
        }
    }

    private String buildPrompt(
            ProgressResponseDTO progress,
            List<TaskEntry> tasks) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                You are an AI productivity coach.

                Analyze the user's productivity data and provide practical,
                personalized feedback.

                User Progress:
                """);

        prompt.append("Total tasks: ")
                .append(progress.getTotalTasks())
                .append("\n");

        prompt.append("Completed tasks: ")
                .append(progress.getCompletedTasks())
                .append("\n");

        prompt.append("Completion rate: ")
                .append(String.format("%.2f", progress.getCompletionRate()))
                .append("%\n");

        prompt.append("Current streak: ")
                .append(progress.getCurrentStreak())
                .append(" days\n");

        prompt.append("Tasks today: ")
                .append(progress.getTasksToday())
                .append("\n");

        prompt.append("Completed today: ")
                .append(progress.getCompletedToday())
                .append("\n\n");

        prompt.append("Task History:\n");

        for (TaskEntry task : tasks) {
            prompt.append("- ")
                    .append(task.getDate())
                    .append(" | ")
                    .append(task.getTaskDescription())
                    .append(" | ")
                    .append(task.isCompleted() ? "Completed" : "Incomplete")
                    .append("\n");
        }

        prompt.append("""
                
                Based only on the information provided above, return a valid JSON object
                with exactly these four string fields:

                {
                  "summary": "A short summary of the user's productivity",
                  "feedback": "One useful observation about their progress",
                  "suggestion": "One practical action they can take",
                  "motivation": "One short motivational message"
                }

                Rules:
                - Return only the JSON object.
                - Do not include Markdown code fences.
                - All four fields must be strings.
                - Do not invent information that is not present in the data.
                """);

        return prompt.toString();
    }
}
