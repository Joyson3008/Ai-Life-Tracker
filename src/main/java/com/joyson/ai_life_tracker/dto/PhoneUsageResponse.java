package com.joyson.ai_life_tracker.dto;

import com.joyson.ai_life_tracker.entity.PhoneAppUsage;
import com.joyson.ai_life_tracker.entity.PhoneUsage;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Standard response DTO returned by the backend for phone usage summaries,
 * dashboard metrics, and sync confirmations.
 */
public class PhoneUsageResponse {

    private Long id;
    private Long userId;
    private LocalDate date;
    private Integer totalScreenTime;
    private Integer productiveTime;
    private Integer distractingTime;
    private Integer screenTimeLimit;
    private Integer wellbeingScore;
    private String mostUsedApp;
    private String aiInsight;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer appCount;
    private List<AppUsageData> apps = new ArrayList<>();

    public PhoneUsageResponse() {
    }

    public static PhoneUsageResponse fromEntity(PhoneUsage entity) {
        if (entity == null) {
            return null;
        }

        PhoneUsageResponse response = new PhoneUsageResponse();
        response.setId(entity.getId());
        if (Hibernate.isInitialized(entity.getUser()) && entity.getUser() != null) {
            response.setUserId(entity.getUser().getId());
        }
        response.setDate(entity.getDate());
        response.setTotalScreenTime(entity.getTotalScreenTime());
        response.setProductiveTime(entity.getProductiveTime());
        response.setDistractingTime(entity.getDistractingTime());
        response.setScreenTimeLimit(entity.getScreenTimeLimit());
        response.setWellbeingScore(entity.getWellbeingScore());
        response.setMostUsedApp(entity.getMostUsedApp());
        response.setAiInsight(entity.getAiInsight());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getAppUsages() != null) {
            response.setAppCount(entity.getAppUsages().size());
            response.setApps(
                    entity.getAppUsages().stream()
                            .map(AppUsageData::fromEntity)
                            .collect(Collectors.toList())
            );
        } else {
            response.setAppCount(0);
        }

        return response;
    }

    public static PhoneUsageResponse fromEntity(PhoneUsage entity, List<PhoneAppUsage> appList) {
        PhoneUsageResponse response = fromEntity(entity);
        if (response != null && appList != null) {
            response.setAppCount(appList.size());
            response.setApps(
                    appList.stream()
                            .map(AppUsageData::fromEntity)
                            .collect(Collectors.toList())
            );
        }
        return response;
    }

    // =========================================================
    // GETTERS & SETTERS
    // =========================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getTotalScreenTime() {
        return totalScreenTime;
    }

    public void setTotalScreenTime(Integer totalScreenTime) {
        this.totalScreenTime = totalScreenTime;
    }

    public Integer getProductiveTime() {
        return productiveTime;
    }

    public void setProductiveTime(Integer productiveTime) {
        this.productiveTime = productiveTime;
    }

    public Integer getDistractingTime() {
        return distractingTime;
    }

    public void setDistractingTime(Integer distractingTime) {
        this.distractingTime = distractingTime;
    }

    public Integer getScreenTimeLimit() {
        return screenTimeLimit;
    }

    public void setScreenTimeLimit(Integer screenTimeLimit) {
        this.screenTimeLimit = screenTimeLimit;
    }

    public Integer getWellbeingScore() {
        return wellbeingScore;
    }

    public void setWellbeingScore(Integer wellbeingScore) {
        this.wellbeingScore = wellbeingScore;
    }

    public String getMostUsedApp() {
        return mostUsedApp;
    }

    public void setMostUsedApp(String mostUsedApp) {
        this.mostUsedApp = mostUsedApp;
    }

    public String getAiInsight() {
        return aiInsight;
    }

    public void setAiInsight(String aiInsight) {
        this.aiInsight = aiInsight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getAppCount() {
        return appCount;
    }

    public void setAppCount(Integer appCount) {
        this.appCount = appCount;
    }

    public List<AppUsageData> getApps() {
        return apps;
    }

    public void setApps(List<AppUsageData> apps) {
        this.apps = apps;
    }
}
