package com.cpr_db.cpr_db.controller;

import com.cpr_db.cpr_db.common.ApiResponse;
import com.cpr_db.cpr_db.dto.PresetsResponse;
import com.cpr_db.cpr_db.dto.QaRequest;
import com.cpr_db.cpr_db.dto.QaResponse;
import com.cpr_db.cpr_db.service.QaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/qa")
public class QaController {

    private final QaService qaService;

    public QaController(QaService qaService) {
        this.qaService = qaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<QaResponse>> ask(@Valid @RequestBody QaRequest request) {
        QaResponse response = qaService.ask(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/presets")
    public ResponseEntity<ApiResponse<PresetsResponse>> presets() {
        PresetsResponse response = new PresetsResponse(qaService.getPresets());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
