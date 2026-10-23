
package com.example.activitytracker.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_global_daily_usage")
public class AIGlobalDailyUsage {

    @Id
    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "request_count", nullable = false)
    private int requestCount;

    public AIGlobalDailyUsage() {
    }

    public LocalDate getUsageDate() {
        return usageDate;
    }

    public void setUsageDate(LocalDate usageDate) {
        this.usageDate = usageDate;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }
}
