package com.joyson.ai_life_tracker.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "app_category",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_app_category_package_name",
            columnNames = "package_name"
        )
    }
)
public class AppCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_name", nullable = false, unique = true)
    private String packageName;

    @Column(name = "app_name")
    private String appName;

    @Column(nullable = false, length = 50)
    private String category = "OTHER";

    public AppCategory() {
    }

    public AppCategory(String packageName, String appName, String category) {
        this.packageName = packageName;
        this.appName = appName;
        this.category = category != null ? category.trim().toUpperCase() : "OTHER";
    }

    @PrePersist
    @PreUpdate
    public void normalize() {
        if (packageName != null) {
            packageName = packageName.trim();
        }
        if (appName != null) {
            appName = appName.trim();
        }
        if (category == null || category.trim().isEmpty()) {
            category = "OTHER";
        } else {
            category = category.trim().toUpperCase();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
