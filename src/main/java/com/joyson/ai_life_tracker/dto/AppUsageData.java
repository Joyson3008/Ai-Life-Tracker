package com.joyson.ai_life_tracker.dto;

import com.joyson.ai_life_tracker.entity.PhoneAppUsage;

/**
 * Data transfer object representing the usage data of a single application.
 * Used in both sync requests from Android and responses to the React frontend.
 */
public class AppUsageData {

    private String appName;
    private String packageName;
    private Integer usageMinutes;
    private String category;

    public AppUsageData() {
    }

    public AppUsageData(String appName, String packageName, Integer usageMinutes, String category) {
        this.appName = appName;
        this.packageName = packageName;
        this.usageMinutes = usageMinutes;
        this.category = category;
    }

    public static AppUsageData fromEntity(PhoneAppUsage entity) {
        if (entity == null) {
            return null;
        }
        return new AppUsageData(
                entity.getAppName(),
                entity.getPackageName(),
                entity.getMinutes(),
                entity.getCategory()
        );
    }

    // =========================================================
    // GETTERS & SETTERS
    // =========================================================

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Integer getUsageMinutes() {
        return usageMinutes;
    }

    public void setUsageMinutes(Integer usageMinutes) {
        this.usageMinutes = usageMinutes;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
