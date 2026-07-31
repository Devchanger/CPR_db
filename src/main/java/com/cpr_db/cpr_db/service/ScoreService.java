package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.ScoreDto;
import com.cpr_db.cpr_db.dto.ScoreSubmitRequest;
import com.cpr_db.cpr_db.entity.Score;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.repository.ScoreRepository;
import com.cpr_db.cpr_db.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;

    public ScoreService(ScoreRepository scoreRepository, UserRepository userRepository) {
        this.scoreRepository = scoreRepository;
        this.userRepository = userRepository;
    }

    public ScoreDto saveScore(String username, ScoreSubmitRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "current user not found"));

        Score score = new Score();
        score.setUserId(user.getId());
        score.setUsername(username);
        score.setScene(request.getScene());
        score.setSkill(request.getSkill());
        score.setTotalScore(request.getTotalScore());
        score.setCompressionDepthAvg(request.getCompressionDepthAvg());
        score.setCompressionRateAvg(request.getCompressionRateAvg());
        score.setErrorCount(request.getErrorCount());
        score.setStepDetails(request.getStepDetails());
        Score saved = scoreRepository.save(score);
        return toDto(saved);
    }

    public List<ScoreDto> getUserScores(String username) {
        return scoreRepository.findByUsernameOrderByCreatedAtDesc(username).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ScoreDto getLatestScore(String username) {
        return scoreRepository.findFirstByUsernameOrderByCreatedAtDesc(username)
                .map(this::toDto)
                .orElseThrow(() -> new BusinessException(404, "score not found"));
    }

    public Map<String, Object> getUserStats(String username) {
        List<Score> scores = scoreRepository.findByUsernameOrderByCreatedAtDesc(username);
        int totalAttempts = scores.size();

        if (totalAttempts == 0) {
            return Map.of(
                    "totalAttempts", 0,
                    "averageScore", 0.0f,
                    "highestScore", 0.0f,
                    "averageDepth", 0.0f,
                    "averageRate", 0.0f,
                    "totalErrors", 0
            );
        }

        float sumScore = 0, maxScore = Float.MIN_VALUE;
        float sumDepth = 0, sumRate = 0;
        int sumErrors = 0;
        int depthCount = 0, rateCount = 0;

        for (Score s : scores) {
            if (s.getTotalScore() != null) {
                sumScore += s.getTotalScore();
                maxScore = Math.max(maxScore, s.getTotalScore());
            }
            if (s.getCompressionDepthAvg() != null) {
                sumDepth += s.getCompressionDepthAvg();
                depthCount++;
            }
            if (s.getCompressionRateAvg() != null) {
                sumRate += s.getCompressionRateAvg();
                rateCount++;
            }
            if (s.getErrorCount() != null) {
                sumErrors += s.getErrorCount();
            }
        }

        return Map.of(
                "totalAttempts", totalAttempts,
                "averageScore", Math.round(sumScore / totalAttempts * 100) / 100.0f,
                "highestScore", maxScore,
                "averageDepth", depthCount > 0 ? Math.round(sumDepth / depthCount * 100) / 100.0f : 0.0f,
                "averageRate", rateCount > 0 ? Math.round(sumRate / rateCount * 100) / 100.0f : 0.0f,
                "totalErrors", sumErrors
        );
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
}
