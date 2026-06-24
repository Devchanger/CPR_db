package com.cpr_db.cpr_db.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scores")
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(length = 50)
    private String scene;

    @Column(length = 10)
    private String skill;

    @Column(name = "total_score")
    private Float totalScore;

    @Column(name = "compression_depth_avg")
    private Float compressionDepthAvg;

    @Column(name = "compression_rate_avg")
    private Float compressionRateAvg;

    @Column(name = "error_count")
    private Integer errorCount;

    @Lob
    @Column(name = "step_details")
    private String stepDetails;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Score() {
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
