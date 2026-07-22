package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.ScoreDto;
import com.cpr_db.cpr_db.dto.ScoreStatsResponse;
import com.cpr_db.cpr_db.dto.ScoreSubmitRequest;
import com.cpr_db.cpr_db.entity.Score;
import com.cpr_db.cpr_db.repository.ScoreRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;

    public ScoreService(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    public ScoreDto saveScore(String username, Long userId, ScoreSubmitRequest request) {
        Score score = new Score();
        score.setUserId(userId);
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

    public ScoreStatsResponse getStats(String username) {
        List<Score> allScores = scoreRepository.findByUsernameOrderByCreatedAtDesc(username);

        ScoreStatsResponse stats = new ScoreStatsResponse();
        stats.setTotalAttempts(allScores.size());

        if (!allScores.isEmpty()) {
            stats.setAverageScore(allScores.stream()
                    .filter(s -> s.getTotalScore() != null)
                    .mapToDouble(Score::getTotalScore)
                    .average()
                    .orElse(0.0));
            stats.setHighestScore(allScores.stream()
                    .filter(s -> s.getTotalScore() != null)
                    .mapToDouble(Score::getTotalScore)
                    .max()
                    .orElse(0.0));
            stats.setLowestScore(allScores.stream()
                    .filter(s -> s.getTotalScore() != null)
                    .mapToDouble(Score::getTotalScore)
                    .min()
                    .orElse(0.0));
            stats.setScenesTrained((int) allScores.stream()
                    .map(Score::getScene)
                    .filter(s -> s != null)
                    .distinct()
                    .count());
            stats.setSkillsTrained((int) allScores.stream()
                    .map(Score::getSkill)
                    .filter(s -> s != null)
                    .distinct()
                    .count());
            stats.setRecentScores(allScores.stream()
                    .limit(5)
                    .map(this::toDto)
                    .collect(Collectors.toList()));
        }

        return stats;
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
