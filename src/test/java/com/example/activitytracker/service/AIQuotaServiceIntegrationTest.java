package com.example.activitytracker.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.activitytracker.model.AIGlobalDailyUsage;
import com.example.activitytracker.model.AIUserDailyUsage;
import com.example.activitytracker.model.AIUserDailyUsageId;
import com.example.activitytracker.model.User;
import com.example.activitytracker.repository.AIGlobalDailyUsageRepository;
import com.example.activitytracker.repository.AIUserDailyUsageRepository;
import com.example.activitytracker.repository.UserRepository;

@SpringBootTest
class AIQuotaServiceIntegrationTest {

    @Autowired
    private AIQuotaService aiQuotaService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AIUserDailyUsageRepository userUsageRepository;

    @Autowired
    private AIGlobalDailyUsageRepository globalUsageRepository;

    private final ZoneId quotaZone = ZoneId.of("Asia/Kolkata");

    @Test
    void reserveRequest_shouldPersistUserAndGlobalQuota() {
        User user = new User();
        user.setName("Quota Integration Test");
        user.setEmail("quota-test-" + UUID.randomUUID() + "@example.com");
        user.setPassword("test-password");
        user.setRole("USER");

        user = userRepository.saveAndFlush(user);

        Long userId = user.getId();
        LocalDate today = LocalDate.now(quotaZone);

        AIUserDailyUsageId userUsageId =
                new AIUserDailyUsageId(userId, today);

        int userCountBefore = userUsageRepository.findById(userUsageId)
                .map(AIUserDailyUsage::getRequestCount)
                .orElse(0);

        int globalCountBefore = globalUsageRepository.findById(today)
                .map(AIGlobalDailyUsage::getRequestCount)
                .orElse(0);

        aiQuotaService.reserveRequest(userId);

        AIUserDailyUsage userUsageAfter =
                userUsageRepository.findById(userUsageId).orElseThrow();

        AIGlobalDailyUsage globalUsageAfter =
                globalUsageRepository.findById(today).orElseThrow();

        assertThat(userUsageAfter.getRequestCount())
                .isEqualTo(userCountBefore + 1);

        assertThat(globalUsageAfter.getRequestCount())
                .isEqualTo(globalCountBefore + 1);

        // Clean up test data
        userUsageRepository.deleteById(userUsageId);
        userRepository.deleteById(userId);
    }
}