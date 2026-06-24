package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.dto.PoseDetectResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PoseServiceTest {

    @Test
    void shouldReturnLandmarksAndAngleAnalysis() {
        PoseService service = new PoseService();
        byte[] imageBytes = new byte[1024]; // dummy image

        PoseDetectResponse response = service.detect(imageBytes);

        assertNotNull(response);
        assertNotNull(response.getLandmarks());
        assertEquals(33, response.getLandmarks().size()); // MediaPipe Pose has 33 landmarks
        assertNotNull(response.getAngleAnalysis());
        assertTrue(response.getAngleAnalysis().getLeftArmAngle() > 0);
        assertTrue(response.getAngleAnalysis().getRightArmAngle() > 0);
    }

    @Test
    void shouldDetectVerticalArms() {
        PoseService service = new PoseService();

        PoseDetectResponse response = service.detect(new byte[1024]);

        assertTrue(response.getAngleAnalysis().getIsArmsVertical(),
                "Mock landmarks should show vertical arms (>160°)");
    }
}
