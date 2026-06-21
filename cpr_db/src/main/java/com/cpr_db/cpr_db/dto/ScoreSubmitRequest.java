package com.cpr_db.cpr_db.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ScoreSubmitRequest {

    @NotBlank(message = "scene is required")
    private String scene;

    @NotBlank(message = "skill is required")
    private String skill;

    @NotNull(message = "totalScore is required")
    private Float totalScore;

    private Float compressionDepthAvg;
    private Float compressionRateAvg;
    private Integer errorCount;
    private String stepDetails;

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
}
