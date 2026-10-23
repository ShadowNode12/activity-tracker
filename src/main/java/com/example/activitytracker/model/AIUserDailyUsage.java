package com.example.activitytracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_user_daily_usage")
public class AIUserDailyUsage {

    @EmbeddedId
    private AIUserDailyUsageId id;

    @Column(name = "request_count", nullable = false)
    private int requestCount;

    public AIUserDailyUsage() {
    }

    public AIUserDailyUsageId getId() {
        return id;
    }

    public void setId(AIUserDailyUsageId id) {
        this.id = id;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }
}