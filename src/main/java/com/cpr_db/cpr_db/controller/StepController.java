package com.cpr_db.cpr_db.controller;

import com.cpr_db.cpr_db.common.ApiResponse;
import com.cpr_db.cpr_db.dto.StepCreateRequest;
import com.cpr_db.cpr_db.dto.StepUpdateRequest;
import com.cpr_db.cpr_db.service.StepService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/steps")
public class StepController {

    private final StepService stepService;

    public StepController(StepService stepService) {
        this.stepService = stepService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStepList(
            @RequestParam(name = "skillId", required = false) Long skillId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(stepService.getStepList(skillId, status, page, pageSize)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStepById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(stepService.getStepById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createStep(@Valid @RequestBody StepCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.success(stepService.createStep(req), "created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateStep(@PathVariable Long id,
                                                                      @Valid @RequestBody StepUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success(stepService.updateStep(id, req), "updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Void>> deleteStep(@PathVariable Long id) {
        stepService.deleteStep(id);
        return ResponseEntity.ok(ApiResponse.success(null, "deleted"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateStatus(@PathVariable Long id,
                                                                         @RequestBody Map<String, Object> body) {
        String status = body.get("status") == null ? null : body.get("status").toString();
        return ResponseEntity.ok(ApiResponse.success(stepService.updateStepStatus(id, status), "updated"));
    }

    @PutMapping("/{id}/reorder")
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reorderStep(@PathVariable Long id,
                                                                        @RequestBody Map<String, Object> body) {
        String direction = body.get("direction") == null ? null : body.get("direction").toString();
        return ResponseEntity.ok(ApiResponse.success(stepService.reorderStep(id, direction), "updated"));
    }
}
