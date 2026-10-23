package com.example.activitytracker.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.activitytracker.model.AIGlobalDailyUsage;

public interface AIGlobalDailyUsageRepository
        extends JpaRepository<AIGlobalDailyUsage, LocalDate> {
}
