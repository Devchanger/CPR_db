package com.cpr_db.cpr_db.controller;

import com.cpr_db.cpr_db.common.ApiResponse;
import com.cpr_db.cpr_db.dto.ScoreDto;
import com.cpr_db.cpr_db.dto.ScoreStatsResponse;
import com.cpr_db.cpr_db.dto.ScoreSubmitRequest;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.repository.UserRepository;
import com.cpr_db.cpr_db.service.ScoreService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scores")
public class ScoreController {

    private final ScoreService scoreService;
    private final UserRepository userRepository;

    public ScoreController(ScoreService scoreService, UserRepository userRepository) {
        this.scoreService = scoreService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ScoreDto>> submitScore(Authentication authentication,
                                                             @Valid @RequestBody ScoreSubmitRequest request) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("current user not found"));
        ScoreDto saved = scoreService.saveScore(username, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ScoreDto>>> getScores(Authentication authentication,
                                                                 @RequestParam(name = "username", required = false) String username) {
        String currentUsername = authentication.getName();
        if (username == null || username.isBlank() || username.equals(currentUsername)) {
            return ResponseEntity.ok(ApiResponse.success(scoreService.getUserScores(currentUsername)));
        }
        return ResponseEntity.status(403).body(ApiResponse.fail(403, "only current user may query scores"));
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<ScoreDto>> getLatestScore(Authentication authentication,
                                                                @RequestParam(name = "username", required = false) String username) {
        String currentUsername = authentication.getName();
        String queryUsername = (username == null || username.isBlank()) ? currentUsername : username;
        if (!queryUsername.equals(currentUsername)) {
            return ResponseEntity.status(403).body(ApiResponse.fail(403, "only current user may query latest score"));
        }
        ScoreDto latest = scoreService.getLatestScore(currentUsername);
        return ResponseEntity.ok(ApiResponse.success(latest));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ScoreStatsResponse>> getStats(Authentication authentication) {
        String username = authentication.getName();
        ScoreStatsResponse stats = scoreService.getStats(username);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
