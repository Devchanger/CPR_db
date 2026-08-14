package com.cpr_db.cpr_db.controller;

import com.cpr_db.cpr_db.common.ApiResponse;
import com.cpr_db.cpr_db.dto.SkillCreateRequest;
import com.cpr_db.cpr_db.dto.SkillUpdateRequest;
import com.cpr_db.cpr_db.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSkillList(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(skillService.getSkillList(keyword, status, page, pageSize)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSkillById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(skillService.getSkillById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSkill(@Valid @RequestBody SkillCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(skillService.createSkill(request), "created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateSkill(@PathVariable Long id,
                                                                       @Valid @RequestBody SkillUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(skillService.updateSkill(id, request), "updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.ok(ApiResponse.success(null, "deleted"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateStatus(@PathVariable Long id,
                                                                         @RequestBody Map<String, Object> body) {
        String status = body.get("status") == null ? null : body.get("status").toString();
        return ResponseEntity.ok(ApiResponse.success(skillService.updateSkillStatus(id, status), "updated"));
    }
}
