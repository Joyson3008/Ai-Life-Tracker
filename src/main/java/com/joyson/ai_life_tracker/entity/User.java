package com.joyson.ai_life_tracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;

    // 🔥 REQUIRED: DEFAULT CONSTRUCTOR
    public User() {}

    // 🔥 GETTERS & SETTERS

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {   // ✅ MUST EXIST
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) { // ✅ MUST EXIST
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) { // 🚨 THIS IS YOUR ISSUE
        this.password = password;
    }
}