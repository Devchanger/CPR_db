package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.dto.AngleAnalysis;
import com.cpr_db.cpr_db.dto.PoseDetectResponse;
import com.cpr_db.cpr_db.dto.PoseLandmark;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PoseService {

    public PoseDetectResponse detect(byte[] imageBytes) {
        List<PoseLandmark> landmarks = generateMockLandmarks();

        double leftArmAngle = calculateArmAngle(landmarks, 11, 13, 15);
        double rightArmAngle = calculateArmAngle(landmarks, 12, 14, 16);
        boolean isArmsVertical = leftArmAngle > 160 && rightArmAngle > 160;

        AngleAnalysis angleAnalysis = new AngleAnalysis(leftArmAngle, rightArmAngle, isArmsVertical);
        return new PoseDetectResponse(landmarks, angleAnalysis);
    }

    private List<PoseLandmark> generateMockLandmarks() {
        double[][] mockCoords = {
                {0.500, 0.050, -0.100, 0.95}, // 0: nose
                {0.500, 0.080, -0.110, 0.93}, // 1: left eye inner
                {0.510, 0.080, -0.110, 0.94}, // 2: left eye
                {0.505, 0.090, -0.115, 0.92}, // 3: left eye outer
                {0.490, 0.080, -0.110, 0.94}, // 4: right eye inner
                {0.480, 0.080, -0.110, 0.93}, // 5: right eye
                {0.495, 0.090, -0.115, 0.91}, // 6: right eye outer
                {0.500, 0.110, -0.120, 0.96}, // 7: left ear
                {0.490, 0.110, -0.120, 0.95}, // 8: right ear
                {0.500, 0.160, -0.130, 0.97}, // 9: mouth left
                {0.495, 0.160, -0.130, 0.96}, // 10: mouth right
                {0.520, 0.220, -0.140, 0.99}, // 11: left shoulder
                {0.480, 0.220, -0.140, 0.99}, // 12: right shoulder
                {0.560, 0.350, -0.150, 0.98}, // 13: left elbow
                {0.440, 0.350, -0.150, 0.98}, // 14: right elbow
                {0.600, 0.480, -0.160, 0.97}, // 15: left wrist
                {0.400, 0.480, -0.160, 0.97}, // 16: right wrist
                {0.530, 0.500, -0.150, 0.90}, // 17: left pinky
                {0.470, 0.500, -0.150, 0.90}, // 18: right pinky
                {0.540, 0.520, -0.155, 0.91}, // 19: left index
                {0.460, 0.520, -0.155, 0.91}, // 20: right index
                {0.510, 0.510, -0.160, 0.92}, // 21: left thumb
                {0.490, 0.510, -0.160, 0.92}, // 22: right thumb
                {0.510, 0.600, -0.170, 0.88}, // 23: left hip
                {0.490, 0.600, -0.170, 0.88}, // 24: right hip
                {0.520, 0.700, -0.180, 0.85}, // 25: left knee
                {0.480, 0.700, -0.180, 0.85}, // 26: right knee
                {0.525, 0.830, -0.190, 0.80}, // 27: left ankle
                {0.475, 0.830, -0.190, 0.80}, // 28: right ankle
                {0.530, 0.900, -0.200, 0.75}, // 29: left heel
                {0.470, 0.900, -0.200, 0.75}, // 30: right heel
                {0.520, 0.910, -0.210, 0.72}, // 31: left foot index
                {0.480, 0.910, -0.210, 0.72}, // 32: right foot index
        };

        List<PoseLandmark> landmarks = new ArrayList<>();
        for (double[] coord : mockCoords) {
            landmarks.add(new PoseLandmark(coord[0], coord[1], coord[2], coord[3]));
        }
        return landmarks;
    }

    private double calculateArmAngle(List<PoseLandmark> landmarks, int shoulderIdx, int elbowIdx, int wristIdx) {
        PoseLandmark shoulder = landmarks.get(shoulderIdx);
        PoseLandmark elbow = landmarks.get(elbowIdx);
        PoseLandmark wrist = landmarks.get(wristIdx);

        double v1x = shoulder.getX() - elbow.getX();
        double v1y = shoulder.getY() - elbow.getY();
        double v2x = wrist.getX() - elbow.getX();
        double v2y = wrist.getY() - elbow.getY();

        double dot = v1x * v2x + v1y * v2y;
        double mag1 = Math.sqrt(v1x * v1x + v1y * v1y);
        double mag2 = Math.sqrt(v2x * v2x + v2y * v2y);

        if (mag1 < 1e-6 || mag2 < 1e-6) {
            return 0;
        }

        double cosAngle = dot / (mag1 * mag2);
        cosAngle = Math.max(-1.0, Math.min(1.0, cosAngle));
        return Math.toDegrees(Math.acos(cosAngle));
    }
}
