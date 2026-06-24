package com.cpr_db.cpr_db.controller;

import com.cpr_db.cpr_db.common.ApiResponse;
import com.cpr_db.cpr_db.dto.PoseDetectResponse;
import com.cpr_db.cpr_db.service.PoseService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/pose")
public class PoseController {

    private final PoseService poseService;

    public PoseController(PoseService poseService) {
        this.poseService = poseService;
    }

    @PostMapping(value = "/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PoseDetectResponse>> detect(@RequestParam("image") MultipartFile image) throws IOException {
        PoseDetectResponse response = poseService.detect(image.getBytes());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
