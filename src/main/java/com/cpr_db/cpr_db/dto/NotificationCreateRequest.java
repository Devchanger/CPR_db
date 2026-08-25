package com.cpr_db.cpr_db.dto;

import jakarta.validation.constraints.NotBlank;

public class NotificationCreateRequest {

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "contentMd is required")
    private String contentMd;

    private String status;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContentMd() { return contentMd; }
    public void setContentMd(String contentMd) { this.contentMd = contentMd; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
