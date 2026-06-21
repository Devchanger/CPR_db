package com.cpr_db.cpr_db.dto;

import java.time.LocalDateTime;

public class ScoreDto {

    private Long id;
    private String username;
    private String scene;
    private String skill;
    private Float totalScore;
    private Float compressionDepthAvg;
    private Float compressionRateAvg;
    private Integer errorCount;
    private String stepDetails;
    private LocalDateTime createdAt;

    public ScoreDto() {
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

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public Float getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Float totalScore) {
        this.totalScore = totalScore;
    }

    public Float getCompressionDepthAvg() {
        return compressionDepthAvg;
    }

    public void setCompressionDepthAvg(Float compressionDepthAvg) {
        this.compressionDepthAvg = compressionDepthAvg;
    }

    public Float getCompressionRateAvg() {
        return compressionRateAvg;
    }

    public void setCompressionRateAvg(Float compressionRateAvg) {
        this.compressionRateAvg = compressionRateAvg;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public String getStepDetails() {
        return stepDetails;
    }

    public void setStepDetails(String stepDetails) {
        this.stepDetails = stepDetails;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
