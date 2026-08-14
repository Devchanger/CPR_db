package com.cpr_db.cpr_db.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StepCreateRequest {
    @NotNull(message = "skillId is required")
    private Long skillId;
    @NotBlank(message = "title is required")
    private String title;
    private String description;
    private String status;
    @Min(value = 0, message = "order must be >= 0")
    private Integer order;

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }
}
