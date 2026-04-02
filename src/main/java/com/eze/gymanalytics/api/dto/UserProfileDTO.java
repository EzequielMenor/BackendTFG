package com.eze.gymanalytics.api.dto;

import java.util.UUID;

public class UserProfileDTO {

    private UUID id;
    private String email;
    private String username;
    private String role;
    private String createdAt;

    public UserProfileDTO() {}

    public UserProfileDTO(UUID id, String email, String username, String role, String createdAt) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.role = role;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
