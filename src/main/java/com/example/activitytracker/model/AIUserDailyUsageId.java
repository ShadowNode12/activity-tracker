
package com.example.activitytracker.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AIUserDailyUsageId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    public AIUserDailyUsageId() {
    }

    public AIUserDailyUsageId(Long userId, LocalDate usageDate) {
        this.userId = userId;
        this.usageDate = usageDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getUsageDate() {
        return usageDate;
    }

    public void setUsageDate(LocalDate usageDate) {
        this.usageDate = usageDate;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof AIUserDailyUsageId other)) {
            return false;
        }

        return Objects.equals(userId, other.userId)
                && Objects.equals(usageDate, other.usageDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, usageDate);
    }
}
