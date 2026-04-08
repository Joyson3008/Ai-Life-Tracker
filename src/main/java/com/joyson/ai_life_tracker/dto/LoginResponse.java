package com.joyson.ai_life_tracker.dto;

public class LoginResponse {

    private Long id;
    private String email;

    public LoginResponse(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
}