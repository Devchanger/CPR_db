package com.cpr_db.cpr_db.dto;

import java.util.List;

public class PoseDetectResponse {

    private List<PoseLandmark> landmarks;
    private AngleAnalysis angleAnalysis;

    public PoseDetectResponse() {
    }

    public PoseDetectResponse(List<PoseLandmark> landmarks, AngleAnalysis angleAnalysis) {
        this.landmarks = landmarks;
        this.angleAnalysis = angleAnalysis;
    }

    public List<PoseLandmark> getLandmarks() {
        return landmarks;
    }

    public void setLandmarks(List<PoseLandmark> landmarks) {
        this.landmarks = landmarks;
    }

    public AngleAnalysis getAngleAnalysis() {
        return angleAnalysis;
    }

    public void setAngleAnalysis(AngleAnalysis angleAnalysis) {
        this.angleAnalysis = angleAnalysis;
    }
}
