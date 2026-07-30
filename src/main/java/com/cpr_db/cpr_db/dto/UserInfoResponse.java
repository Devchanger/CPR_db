package com.cpr_db.cpr_db.dto;

import java.time.LocalDateTime;

public class UserInfoResponse {

    private Long id;
    private String username;
<<<<<<< HEAD
=======
    private String role;
    private String realName;
    private String avatar;
>>>>>>> 193e2be (feat: complete all missing backend API endpoints)
    private LocalDateTime createdAt;

    public UserInfoResponse() {
    }

<<<<<<< HEAD
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
=======
    public UserInfoResponse(Long id, String username, String role, String realName,
                            String avatar, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.realName = realName;
        this.avatar = avatar;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
>>>>>>> 193e2be (feat: complete all missing backend API endpoints)
}
