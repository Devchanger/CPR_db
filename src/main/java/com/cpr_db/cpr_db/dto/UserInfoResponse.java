package com.cpr_db.cpr_db.dto;

import java.time.LocalDateTime;

public class UserInfoResponse {

    private Long id;
    private String username;
    private LocalDateTime createdAt;

    public UserInfoResponse() {
    }

    public UserInfoResponse(Long id, String username, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
