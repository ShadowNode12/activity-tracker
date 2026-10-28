package com.example.activitytracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.activitytracker.model.AIUserDailyUsage;
import com.example.activitytracker.model.AIUserDailyUsageId;

public interface AIUserDailyUsageRepository
        extends JpaRepository<AIUserDailyUsage, AIUserDailyUsageId> {

    @Modifying
    @Query(value = """
            INSERT INTO ai_user_daily_usage
                (user_id, usage_date, request_count)
            VALUES (:userId, :usageDate, 0)
            ON DUPLICATE KEY UPDATE request_count = request_count
            """, nativeQuery = true)
    void ensureDailyRowExists(
            @Param("userId") Long userId,
            @Param("usageDate") java.time.LocalDate usageDate);

    @Modifying
    @Query(value = """
            UPDATE ai_user_daily_usage
            SET request_count = request_count + 1
            WHERE user_id = :userId
              AND usage_date = :usageDate
              AND request_count < :dailyLimit
            """, nativeQuery = true)
    int reserveRequest(
            @Param("userId") Long userId,
            @Param("usageDate") java.time.LocalDate usageDate,
            @Param("dailyLimit") int dailyLimit);
}
