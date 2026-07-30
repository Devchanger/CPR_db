package com.cpr_db.cpr_db.controller;

import com.cpr_db.cpr_db.common.ApiResponse;
import com.cpr_db.cpr_db.entity.Scene;
import com.cpr_db.cpr_db.service.SceneService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/scenes")
public class SceneController {

    private final SceneService sceneService;

    public SceneController(SceneService sceneService) {
        this.sceneService = sceneService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Scene>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(sceneService.getAll()));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSceneList(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(sceneService.getSceneList(keyword, page, pageSize)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Scene>> getSceneById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(sceneService.getSceneById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Scene>> createScene(@RequestBody Scene body) {
        return ResponseEntity.ok(ApiResponse.success(sceneService.createScene(body), "created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Scene>> updateScene(@PathVariable Long id, @RequestBody Scene body) {
        return ResponseEntity.ok(ApiResponse.success(sceneService.updateScene(id, body), "updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Void>> deleteScene(@PathVariable Long id) {
        sceneService.deleteScene(id);
        return ResponseEntity.ok(ApiResponse.success(null, "deleted"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Scene>> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String status = body.get("status") == null ? null : body.get("status").toString();
        return ResponseEntity.ok(ApiResponse.success(sceneService.updateSceneStatus(id, status), "updated"));
    }
}
