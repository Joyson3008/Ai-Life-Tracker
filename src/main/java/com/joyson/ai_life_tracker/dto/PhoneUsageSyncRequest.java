package com.joyson.ai_life_tracker.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Bulk synchronization request payload sent by the Android client or Web dashboard.
 * Contains the target date, optional screen time limit goal, and the complete list
 * of installed application usage statistics collected for that day.
 */
public class PhoneUsageSyncRequest {

    private LocalDate date;
    private Integer screenTimeLimit;
    private List<AppUsageData> apps = new ArrayList<>();

    public PhoneUsageSyncRequest() {
    }

    public PhoneUsageSyncRequest(LocalDate date, Integer screenTimeLimit, List<AppUsageData> apps) {
        this.date = date;
        this.screenTimeLimit = screenTimeLimit;
        this.apps = apps != null ? apps : new ArrayList<>();
    }

    // =========================================================
    // GETTERS & SETTERS
    // =========================================================

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getScreenTimeLimit() {
        return screenTimeLimit;
    }

    public void setScreenTimeLimit(Integer screenTimeLimit) {
        this.screenTimeLimit = screenTimeLimit;
    }

    public List<AppUsageData> getApps() {
        return apps;
    }

    public void setApps(List<AppUsageData> apps) {
        this.apps = apps != null ? apps : new ArrayList<>();
    }
}
