package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.common.SecurityUtil;
import com.cpr_db.cpr_db.dto.ScoreDto;
import com.cpr_db.cpr_db.dto.ScoreStatsResponse;
import com.cpr_db.cpr_db.dto.ScoreSubmitRequest;
import com.cpr_db.cpr_db.entity.Score;
import com.cpr_db.cpr_db.repository.ScoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScoreService {

    private static final int MAX_PAGE_SIZE = 100;

    /**
     * BE-C-03 (S4): VR submits English scene/skill names; normalize to the canonical
     * Chinese values used by seeds/analysis. Unknown values fall back as-is.
     * Mapping content will be re-verified against the VR code when it becomes available.
     */
    private static final Map<String, String> SCENE_ALIASES = Map.of(
            "Subway", "地铁站",
            "Subway_Terminal", "地铁站",
            "Hospital_Corridor", "医院走廊",
            "Ruins", "废墟");

    private static final Map<String, String> SKILL_ALIASES = Map.of(
            "CPR", "成人胸外按压");

    private final ScoreRepository scoreRepository;
    private final LogService logService;

    public ScoreService(ScoreRepository scoreRepository, LogService logService) {
        this.scoreRepository = scoreRepository;
        this.logService = logService;
    }

    @Transactional
    public ScoreDto saveScore(String username, Long userId, ScoreSubmitRequest request) {
        Score score = new Score();
        score.setUserId(userId);
        score.setUsername(username);
        score.setScene(normalize(SCENE_ALIASES, request.getScene()));
        score.setSkill(normalize(SKILL_ALIASES, request.getSkill()));
        score.setTotalScore(request.getTotalScore());
        score.setCompressionDepthAvg(request.getCompressionDepthAvg());
        score.setCompressionRateAvg(request.getCompressionRateAvg());
        score.setErrorCount(request.getErrorCount());
        score.setStepDetails(request.getStepDetails());
        Score saved = scoreRepository.save(score);
        // Non-blocking audit log of the submission (review P1-5).
        try {
            logService.log(null, SecurityUtil.currentUsername(), "submit_score", "score",
                    saved.getId(), "submitted score total=" + saved.getTotalScore() + " scene=" + saved.getScene(),
                    SecurityUtil.currentIp());
        } catch (Exception ignored) {
            // logging must not break the submission flow
        }
        return toDto(saved);
    }

    // Paginated, unified response for a single user's scores (review P0-9 / P0-12).
    @Transactional(readOnly = true)
    public Map<String, Object> getUserScores(String username, int page, int pageSize) {
        page = clampPage(page);
        pageSize = clampPageSize(pageSize);
        Page<Score> result = scoreRepository.findByUsername(username,
                PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<ScoreDto> list = result.getContent().stream().map(this::toDto).collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", result.getTotalElements());
        map.put("page", result.getNumber() + 1);
        map.put("page_size", result.getSize());
        return map;
    }

    @Transactional(readOnly = true)
    public ScoreDto getLatestScore(String username) {
        return scoreRepository.findFirstByUsernameOrderByCreatedAtDesc(username)
                .map(this::toDto)
                .orElseThrow(() -> new BusinessException(404, "score not found"));
    }

    // DB-level aggregation instead of loading all rows into memory (review P1-6).
    @Transactional(readOnly = true)
    public ScoreStatsResponse getStats(String username) {
        ScoreStatsResponse stats = new ScoreStatsResponse();
        stats.setTotalAttempts((int) scoreRepository.countByUsername(username));
        stats.setAverageScore(scoreRepository.averageTotalScoreByUsername(username));
        stats.setHighestScore(scoreRepository.maxTotalScoreByUsername(username));
        stats.setLowestScore(scoreRepository.minTotalScoreByUsername(username));
        Long scenes = scoreRepository.countDistinctSceneByUsername(username);
        Long skills = scoreRepository.countDistinctSkillByUsername(username);
        stats.setScenesTrained(scenes == null ? 0 : scenes.intValue());
        stats.setSkillsTrained(skills == null ? 0 : skills.intValue());
        List<ScoreDto> recent = scoreRepository
                .findTop5ByUsernameOrderByCreatedAtDesc(username, PageRequest.of(0, 5))
                .getContent().stream().map(this::toDto).collect(Collectors.toList());
        stats.setRecentScores(recent);
        return stats;
    }

    // Paginated, unified response for all scores (admin view, review P0-9 / P0-12).
    @Transactional(readOnly = true)
    public Map<String, Object> getAllScores(int page, int pageSize) {
        page = clampPage(page);
        pageSize = clampPageSize(pageSize);
        Page<Score> result = scoreRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(page - 1, pageSize));
        List<ScoreDto> list = result.getContent().stream().map(this::toDto).collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", result.getTotalElements());
        map.put("page", result.getNumber() + 1);
        map.put("page_size", result.getSize());
        return map;
    }

    // Owner check: non-admins may only read their own score (review P0-11).
    @Transactional(readOnly = true)
    public ScoreDto getScoreById(Long id, String currentUsername, boolean isAdmin) {
        Score score = scoreRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "score not found"));
        if (!isAdmin && !currentUsername.equals(score.getUsername())) {
            throw new BusinessException(403, "not allowed to access this score");
        }
        return toDto(score);
    }

    @Transactional
    public void deleteScore(Long id) {
        Score score = scoreRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "score not found"));
        scoreRepository.delete(score);
    }

    private ScoreDto toDto(Score score) {
        ScoreDto dto = new ScoreDto();
        dto.setId(score.getId());
        dto.setUsername(score.getUsername());
        dto.setScene(score.getScene());
        dto.setSkill(score.getSkill());
        dto.setTotalScore(score.getTotalScore());
        dto.setCompressionDepthAvg(score.getCompressionDepthAvg());
        dto.setCompressionRateAvg(score.getCompressionRateAvg());
        dto.setErrorCount(score.getErrorCount());
        dto.setStepDetails(score.getStepDetails());
        dto.setCreatedAt(score.getCreatedAt());
        return dto;
    }

    private int clampPage(int page) {
        return page < 1 ? 1 : page;
    }

    private int clampPageSize(int pageSize) {
        if (pageSize < 1) return 10;
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private String normalize(Map<String, String> aliases, String value) {
        if (value == null) {
            return null;
        }
        return aliases.getOrDefault(value, value);
    }
}
