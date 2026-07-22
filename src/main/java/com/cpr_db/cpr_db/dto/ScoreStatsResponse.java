package com.cpr_db.cpr_db.dto;

import java.util.List;

public class ScoreStatsResponse {

    private int totalAttempts;
    private double averageScore;
    private double highestScore;
    private double lowestScore;
    private int scenesTrained;
    private int skillsTrained;
    private List<ScoreDto> recentScores;

    public ScoreStatsResponse() {
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public double getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(double highestScore) {
        this.highestScore = highestScore;
    }

    public double getLowestScore() {
        return lowestScore;
    }

    public void setLowestScore(double lowestScore) {
        this.lowestScore = lowestScore;
    }

    public int getScenesTrained() {
        return scenesTrained;
    }

    public void setScenesTrained(int scenesTrained) {
        this.scenesTrained = scenesTrained;
    }

    public int getSkillsTrained() {
        return skillsTrained;
    }

    public void setSkillsTrained(int skillsTrained) {
        this.skillsTrained = skillsTrained;
    }

    public List<ScoreDto> getRecentScores() {
        return recentScores;
    }

    public void setRecentScores(List<ScoreDto> recentScores) {
        this.recentScores = recentScores;
    }
}
