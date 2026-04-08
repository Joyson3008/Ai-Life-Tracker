package com.joyson.ai_life_tracker.entity;

import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @Column(nullable = false)
    private String password;

    // 🔥 RELATION WITH DAILY LOG (VERY IMPORTANT)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore  
    private List<DailyLog> logs;

    // ✅ DEFAULT CONSTRUCTOR
    public User() {}

    // ✅ GETTERS & SETTERS

    public Long getId() {
        return id;
    }

    // (optional but useful)
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
}