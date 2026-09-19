package com.joyson.ai_life_tracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Represents the usage breakdown for a single application on a specific day.
 *
 * Each record belongs to a parent PhoneUsage record and contains the application's
 * display name, Android package name, duration used in minutes, and productivity category.
 */
@Entity
@Table(
    name = "phone_app_usage",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_phone_app_usage_parent_package",
            columnNames = {"phone_usage_id", "package_name"}
        )
    }
)
public class PhoneAppUsage {

    // =========================================================
    // PRIMARY KEY
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // PARENT PHONE USAGE RELATIONSHIP
    // =========================================================

    /**
     * The daily PhoneUsage summary record that owns this application breakdown.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "phone_usage_id",
        nullable = false
    )
    @JsonIgnore
    private PhoneUsage phoneUsage;

    // =========================================================
    // APPLICATION DETAILS
    // =========================================================

    /**
     * Human-readable name of the application (e.g., "YouTube", "Instagram", "VS Code").
     */
    @Column(name = "app_name", nullable = false)
    private String appName;

    /**
     * Unique Android package identifier (e.g., "com.google.android.youtube").
     */
    @Column(name = "package_name")
    private String packageName;

    /**
     * Category of the application:
     * - PRODUCTIVE
     * - DISTRACTING
     * - OTHER
     */
    @Column(nullable = false, length = 50)
    private String category = "OTHER";

    /**
     * Usage duration in minutes for the given day.
     */
    @Column(nullable = false)
    private Integer minutes = 0;

    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public PhoneAppUsage() {
    }

    public PhoneAppUsage(String appName, String packageName, Integer minutes, String category) {
        this.appName = appName;
        this.packageName = packageName;
        this.minutes = minutes != null ? minutes : 0;
        this.category = category != null ? category.toUpperCase() : "OTHER";
    }

    public PhoneAppUsage(PhoneUsage phoneUsage, String appName, String packageName, Integer minutes, String category) {
        this.phoneUsage = phoneUsage;
        this.appName = appName;
        this.packageName = packageName;
        this.minutes = minutes != null ? minutes : 0;
        this.category = category != null ? category.toUpperCase() : "OTHER";
    }

    // =========================================================
    // LIFECYCLE HOOKS
    // =========================================================

    @PrePersist
    @PreUpdate
    protected void onSave() {
        if (this.minutes == null || this.minutes < 0) {
            this.minutes = 0;
        }
        if (this.category == null || this.category.trim().isEmpty()) {
            this.category = "OTHER";
        } else {
            this.category = this.category.trim().toUpperCase();
        }
        if (this.appName != null) {
            this.appName = this.appName.trim();
        }
        if (this.packageName != null) {
            this.packageName = this.packageName.trim();
        }
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

    public PhoneUsage getPhoneUsage() {
        return phoneUsage;
    }

    public void setPhoneUsage(PhoneUsage phoneUsage) {
        this.phoneUsage = phoneUsage;
    }

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }
}