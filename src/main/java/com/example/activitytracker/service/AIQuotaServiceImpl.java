package com.example.activitytracker.service;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.activitytracker.exception.AIQuotaExceededException;
import com.example.activitytracker.repository.AIGlobalDailyUsageRepository;
import com.example.activitytracker.repository.AIUserDailyUsageRepository;

@Service
public class AIQuotaServiceImpl implements AIQuotaService {

    private final AIUserDailyUsageRepository userUsageRepository;
    private final AIGlobalDailyUsageRepository globalUsageRepository;
    private final int perUserDailyLimit;
    private final int globalDailyLimit;
    private final ZoneId quotaZone;

    public AIQuotaServiceImpl(
            AIUserDailyUsageRepository userUsageRepository,
            AIGlobalDailyUsageRepository globalUsageRepository,
            @Value("${ai.coach.quota.per-user-daily-limit}") int perUserDailyLimit,
            @Value("${ai.coach.quota.global-daily-limit}") int globalDailyLimit,
            @Value("${ai.coach.quota.time-zone}") String quotaTimeZone) {

        this.userUsageRepository = userUsageRepository;
        this.globalUsageRepository = globalUsageRepository;
        this.perUserDailyLimit = perUserDailyLimit;
        this.globalDailyLimit = globalDailyLimit;
        this.quotaZone = ZoneId.of(quotaTimeZone);
    }

    @Override
    @Transactional
    public void reserveRequest(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        LocalDate today = LocalDate.now(quotaZone);

        userUsageRepository.ensureDailyRowExists(userId, today);
        globalUsageRepository.ensureDailyRowExists(today);

        int userRowsUpdated = userUsageRepository.reserveRequest(
                userId, today, perUserDailyLimit);

        if (userRowsUpdated == 0) {
            throw new AIQuotaExceededException(
                    "You have reached your daily AI Coach request limit.");
        }

        int globalRowsUpdated = globalUsageRepository.reserveRequest(
                today, globalDailyLimit);

        if (globalRowsUpdated == 0) {
            throw new AIQuotaExceededException(
                    "The daily AI Coach request limit has been reached. Please try again tomorrow.");
        }
    }
}
