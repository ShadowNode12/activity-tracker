package com.example.activitytracker.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.activitytracker.model.AIGlobalDailyUsage;

public interface AIGlobalDailyUsageRepository
        extends JpaRepository<AIGlobalDailyUsage, LocalDate> {

    @Modifying
    @Query(value = """
            INSERT INTO ai_global_daily_usage
                (usage_date, request_count)
            VALUES (:usageDate, 0)
            ON DUPLICATE KEY UPDATE request_count = request_count
            """, nativeQuery = true)
    void ensureDailyRowExists(
            @Param("usageDate") LocalDate usageDate);

    @Modifying
    @Query(value = """
            UPDATE ai_global_daily_usage
            SET request_count = request_count + 1
            WHERE usage_date = :usageDate
              AND request_count < :dailyLimit
            """, nativeQuery = true)
    int reserveRequest(
            @Param("usageDate") LocalDate usageDate,
            @Param("dailyLimit") int dailyLimit);
}
