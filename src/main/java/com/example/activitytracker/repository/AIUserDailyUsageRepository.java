package com.example.activitytracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.activitytracker.model.AIUserDailyUsage;
import com.example.activitytracker.model.AIUserDailyUsageId;

public interface AIUserDailyUsageRepository
        extends JpaRepository<AIUserDailyUsage, AIUserDailyUsageId> {
}
