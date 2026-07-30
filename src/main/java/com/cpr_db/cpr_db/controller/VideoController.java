package com.cpr_db.cpr_db.controller;

import com.cpr_db.cpr_db.common.ApiResponse;
import com.cpr_db.cpr_db.dto.VideoResponse;
import com.cpr_db.cpr_db.service.VideoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<ApiResponse<VideoResponse>> getVideo(@PathVariable String videoId) {
        VideoResponse response = videoService.getVideo(videoId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVideoList(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "skillId", required = false) Long skillId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(videoService.getVideoList(keyword, skillId, status, page, pageSize)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createVideo(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.success(videoService.createVideo(body), "created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateVideo(@PathVariable Long id,
                                                                        @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(ApiResponse.success(videoService.updateVideo(id, body), "updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return ResponseEntity.ok(ApiResponse.success(null, "deleted"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateStatus(@PathVariable Long id,
                                                                         @RequestBody Map<String, Object> body) {
        String status = body.get("status") == null ? null : body.get("status").toString();
        return ResponseEntity.ok(ApiResponse.success(videoService.updateVideoStatus(id, status), "updated"));
    }
}
