package com.example.activitytracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.activitytracker.exception.AIQuotaExceededException;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${ai.coach.quota.global-daily-limit}")
    private int globalDailyLimit;

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

    @Test
    void reserveRequest_shouldRollbackUserQuota_whenGlobalQuotaIsExceeded() {

        LocalDate today = LocalDate.now(quotaZone);

        // Preserve the existing global quota count.
        Integer originalGlobalCount = globalUsageRepository.findById(today)
                .map(AIGlobalDailyUsage::getRequestCount)
                .orElse(null);

        User user = new User();
        user.setName("Quota Rollback Test");
        user.setEmail("quota-rollback-" + UUID.randomUUID() + "@example.com");
        user.setPassword("test-password");
        user.setRole("USER");

        user = userRepository.saveAndFlush(user);

        Long userId = user.getId();

        AIUserDailyUsageId userUsageId =
                new AIUserDailyUsageId(userId, today);

        try {
            // Force the global quota to its configured limit.
            jdbcTemplate.update("""
                    INSERT INTO ai_global_daily_usage (usage_date, request_count)
                    VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE request_count = ?
                    """,
                    today,
                    globalDailyLimit,
                    globalDailyLimit);

            assertThatThrownBy(() -> aiQuotaService.reserveRequest(userId))
                    .isInstanceOf(AIQuotaExceededException.class);

            // User reservation should have rolled back.
            int userCountAfter = userUsageRepository.findById(userUsageId)
                    .map(AIUserDailyUsage::getRequestCount)
                    .orElse(0);

            assertThat(userCountAfter).isZero();

            // Global quota should remain at the limit.
            assertThat(globalUsageRepository.findById(today)
                    .orElseThrow()
                    .getRequestCount())
                    .isEqualTo(globalDailyLimit);

        } finally {
            // Clean up user-related test data.
            userUsageRepository.deleteById(userUsageId);
            userRepository.deleteById(userId);

            // Restore the original global quota state.
            if (originalGlobalCount == null) {
                jdbcTemplate.update(
                        "DELETE FROM ai_global_daily_usage WHERE usage_date = ?",
                        today);
            } else {
                jdbcTemplate.update("""
                        UPDATE ai_global_daily_usage
                        SET request_count = ?
                        WHERE usage_date = ?
                        """,
                        originalGlobalCount,
                        today);
            }
        }
    }
}