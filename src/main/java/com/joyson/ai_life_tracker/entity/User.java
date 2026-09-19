package com.joyson.ai_life_tracker.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // Never serialize password to JSON output (protects BCrypt hash)
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    // Relationships with cascade delete to prevent foreign key constraint violations
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DailyLog> logs = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PhoneUsage> phoneUsages = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DailyLearning> dailyLearnings = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<MobileDevice> mobileDevices = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<MobileSyncStatus> mobileSyncStatuses = new ArrayList<>();

    // Default constructor
    public User() {}

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<DailyLog> getLogs() {
        return logs;
    }

    public void setLogs(List<DailyLog> logs) {
        this.logs = logs;
    }

    public List<PhoneUsage> getPhoneUsages() {
        return phoneUsages;
    }

    public void setPhoneUsages(List<PhoneUsage> phoneUsages) {
        this.phoneUsages = phoneUsages;
    }

    public List<DailyLearning> getDailyLearnings() {
        return dailyLearnings;
    }

    public void setDailyLearnings(List<DailyLearning> dailyLearnings) {
        this.dailyLearnings = dailyLearnings;
    }

    public List<MobileDevice> getMobileDevices() {
        return mobileDevices;
    }

    public void setMobileDevices(List<MobileDevice> mobileDevices) {
        this.mobileDevices = mobileDevices;
    }

    public List<MobileSyncStatus> getMobileSyncStatuses() {
        return mobileSyncStatuses;
    }

    public void setMobileSyncStatuses(List<MobileSyncStatus> mobileSyncStatuses) {
        this.mobileSyncStatuses = mobileSyncStatuses;
    }
}