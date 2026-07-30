package com.cpr_db.cpr_db.controller;

import com.cpr_db.cpr_db.common.ApiResponse;
import com.cpr_db.cpr_db.entity.Knowledge;
import com.cpr_db.cpr_db.service.KnowledgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Knowledge>>> getAll(
            @RequestParam(name = "category", required = false) String category) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success(knowledgeService.getByCategory(category)));
        }
        return ResponseEntity.ok(ApiResponse.success(knowledgeService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Knowledge>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(knowledgeService.getKnowledgeById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Knowledge>> create(@RequestBody Knowledge body) {
        return ResponseEntity.ok(ApiResponse.success(knowledgeService.createKnowledge(body), "created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Knowledge>> update(@PathVariable Long id, @RequestBody Knowledge body) {
        return ResponseEntity.ok(ApiResponse.success(knowledgeService.updateKnowledge(id, body), "updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        knowledgeService.deleteKnowledge(id);
        return ResponseEntity.ok(ApiResponse.success(null, "deleted"));
    }
}
