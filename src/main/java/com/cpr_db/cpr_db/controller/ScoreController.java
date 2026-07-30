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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public ResponseEntity<ApiResponse<?>> getScores(Authentication authentication,
                                                    @RequestParam(name = "username", required = false) String username,
                                                    @RequestParam(name = "all", defaultValue = "false") boolean all,
                                                    @RequestParam(name = "page", defaultValue = "1") int page,
                                                    @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        String currentUsername = authentication.getName();
        boolean isAdmin = isAdmin(authentication);
        if (all && isAdmin) {
            return ResponseEntity.ok(ApiResponse.success(scoreService.getAllScores(page, pageSize)));
        }
        if (username != null && !username.isBlank() && isAdmin) {
            return ResponseEntity.ok(ApiResponse.success(scoreService.getUserScores(username)));
        }
        if (username != null && !username.isBlank() && !username.equals(currentUsername) && !isAdmin) {
            return ResponseEntity.status(403).body(ApiResponse.fail(403, "only current user may query scores"));
        }
        return ResponseEntity.ok(ApiResponse.success(scoreService.getUserScores(currentUsername)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScoreDto>> getScoreById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(scoreService.getScoreById(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin') or hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Void>> deleteScore(@PathVariable Long id) {
        scoreService.deleteScore(id);
        return ResponseEntity.ok(ApiResponse.success(null, "deleted"));
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<ScoreDto>> getLatestScore(Authentication authentication,
                                                                @RequestParam(name = "username", required = false) String username) {
        String currentUsername = authentication.getName();
        String queryUsername = (username == null || username.isBlank()) ? currentUsername : username;
        if (!queryUsername.equals(currentUsername) && !isAdmin(authentication)) {
            return ResponseEntity.status(403).body(ApiResponse.fail(403, "only current user may query latest score"));
        }
        ScoreDto latest = scoreService.getLatestScore(queryUsername);
        return ResponseEntity.ok(ApiResponse.success(latest));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ScoreStatsResponse>> getStats(Authentication authentication) {
        String username = authentication.getName();
        ScoreStatsResponse stats = scoreService.getStats(username);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet())
                .stream()
                .anyMatch(a -> "admin".equals(a) || "super_admin".equals(a));
    }
}
