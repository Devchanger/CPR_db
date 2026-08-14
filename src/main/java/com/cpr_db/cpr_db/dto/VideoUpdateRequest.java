package com.cpr_db.cpr_db.dto;

import jakarta.validation.constraints.Min;

public class VideoUpdateRequest {
    private String title;
    private String url;
    private Long skillId;
    @Min(value = 0, message = "durationSeconds must be >= 0")
    private Integer durationSeconds;
    private String status;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
