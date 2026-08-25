package com.cpr_db.cpr_db.dto;

public class AuthResponse {

    private String token;
    private long expiresAt;
    private boolean mustChangePassword;

    public AuthResponse() {
    }

    public AuthResponse(String token, long expiresAt) {
        this(token, expiresAt, false);
    }

    public AuthResponse(String token, long expiresAt, boolean mustChangePassword) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.mustChangePassword = mustChangePassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
}
