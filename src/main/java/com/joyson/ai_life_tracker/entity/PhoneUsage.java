package com.joyson.ai_life_tracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user's daily aggregated phone usage summary.
 *
 * Each record corresponds to exactly one calendar date per user,
 * storing overall screen time, breakdown by category, digital wellbeing score,
 * most-used application, and the list of individual app usage records.
 */
@Entity
@Table(
    name = "phone_usage",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_phone_usage_user_date",
            columnNames = {"user_id", "date"}
        )
    }
)
public class PhoneUsage {

    // =========================================================
    // PRIMARY KEY
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // USER RELATIONSHIP
    // =========================================================

    /**
     * The user who owns this daily phone usage record.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false
    )
    @JsonIgnore
    private User user;

    // =========================================================
    // DATE
    // =========================================================

    /**
     * Date for which the phone usage is recorded (YYYY-MM-DD).
     */
    @Column(nullable = false)
    private LocalDate date;

    // =========================================================
    // SCREEN TIME METRICS (in minutes)
    // =========================================================

    /**
     * Total phone screen time for the day in minutes.
     */
    @Column(name = "total_screen_time", nullable = false)
    private Integer totalScreenTime = 0;

    /**
     * Total time spent on productive applications in minutes.
     */
    @Column(name = "productive_time", nullable = false)
    private Integer productiveTime = 0;

    /**
     * Total time spent on distracting applications in minutes.
     */
    @Column(name = "distracting_time", nullable = false)
    private Integer distractingTime = 0;

    /**
     * User's daily screen time limit goal in minutes (default: 240 minutes = 4 hours).
     */
    @Column(name = "screen_time_limit", nullable = false)
    private Integer screenTimeLimit = 240;

    // =========================================================
    // DIGITAL WELLBEING & ANALYTICS
    // =========================================================

    /**
     * Digital wellbeing score computed automatically on the backend.
     * Scale: 0 to 100 (higher means healthier digital balance).
     */
    @Column(name = "wellbeing_score", nullable = false)
    private Integer wellbeingScore = 100;

    /**
     * Display name of the most-used application for the day.
     */
    @Column(name = "most_used_app")
    private String mostUsedApp;

    /**
     * AI-generated summary or recommendation for the day's usage.
     */
    @Column(name = "ai_insight", columnDefinition = "TEXT")
    private String aiInsight;

    // =========================================================
    // AUDIT TIMESTAMPS
    // =========================================================

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =========================================================
    // APP USAGE RELATIONSHIP
    // =========================================================

    /**
     * Individual application usage breakdown for this date.
     */
    @OneToMany(
        mappedBy = "phoneUsage",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JsonIgnore
    private List<PhoneAppUsage> appUsages = new ArrayList<>();

    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public PhoneUsage() {
    }

    public PhoneUsage(User user, LocalDate date) {
        this.user = user;
        this.date = date;
    }

    // =========================================================
    // LIFECYCLE HOOKS
    // =========================================================

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.totalScreenTime == null) this.totalScreenTime = 0;
        if (this.productiveTime == null) this.productiveTime = 0;
        if (this.distractingTime == null) this.distractingTime = 0;
        if (this.screenTimeLimit == null) this.screenTimeLimit = 240;
        if (this.wellbeingScore == null) this.wellbeingScore = 100;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public List<PhoneAppUsage> getAppUsages() {
        return appUsages;
    }

    public void setAppUsages(List<PhoneAppUsage> appUsages) {
        this.appUsages = appUsages;
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    public void addAppUsage(PhoneAppUsage appUsage) {
        if (appUsage == null) {
            return;
        }
        this.appUsages.add(appUsage);
        appUsage.setPhoneUsage(this);
    }

    public void removeAppUsage(PhoneAppUsage appUsage) {
        if (appUsage == null) {
            return;
        }
        this.appUsages.remove(appUsage);
        appUsage.setPhoneUsage(null);
    }

    public void clearAppUsages() {
        for (PhoneAppUsage appUsage : new ArrayList<>(this.appUsages)) {
            removeAppUsage(appUsage);
        }
    }
}