package com.cpr_db.cpr_db.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ScoreSubmitRequest {

    @NotBlank(message = "scene is required")
    private String scene;

    @NotBlank(message = "skill is required")
    private String skill;

    @NotNull(message = "totalScore is required")
    @DecimalMin(value = "0.0", message = "totalScore must be >= 0")
    @DecimalMax(value = "100.0", message = "totalScore must be <= 100")
    private Float totalScore;

    @DecimalMin(value = "0.0", message = "compressionDepthAvg must be >= 0")
    @DecimalMax(value = "10.0", message = "compressionDepthAvg must be <= 10")
    private Float compressionDepthAvg;

    @DecimalMin(value = "0.0", message = "compressionRateAvg must be >= 0")
    @DecimalMax(value = "300.0", message = "compressionRateAvg must be <= 300")
    private Float compressionRateAvg;

    @Min(value = 0, message = "errorCount must be >= 0")
    @Max(value = 1000, message = "errorCount must be <= 1000")
    private Integer errorCount;

    private String stepDetails;

    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }
    public Float getTotalScore() { return totalScore; }
    public void setTotalScore(Float totalScore) { this.totalScore = totalScore; }
    public Float getCompressionDepthAvg() { return compressionDepthAvg; }
    public void setCompressionDepthAvg(Float compressionDepthAvg) { this.compressionDepthAvg = compressionDepthAvg; }
    public Float getCompressionRateAvg() { return compressionRateAvg; }
    public void setCompressionRateAvg(Float compressionRateAvg) { this.compressionRateAvg = compressionRateAvg; }
    public Integer getErrorCount() { return errorCount; }
    public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }
    public String getStepDetails() { return stepDetails; }
    public void setStepDetails(String stepDetails) { this.stepDetails = stepDetails; }
}
