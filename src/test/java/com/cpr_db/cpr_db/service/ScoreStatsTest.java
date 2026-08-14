package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.dto.ScoreStatsResponse;
import com.cpr_db.cpr_db.repository.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreStatsTest {

    @Mock ScoreRepository scoreRepository;
    @Mock LogService logService;

    private ScoreService scoreService;

    @BeforeEach
    void setUp() {
        scoreService = new ScoreService(scoreRepository, logService);
        // recent scores page must be stubbed to avoid NPE inside getStats
        when(scoreRepository.findTop5ByUsernameOrderByCreatedAtDesc(anyString(), any(Pageable.class)))
                .thenReturn(Page.empty());
    }

    @Test
    @DisplayName("P1-6 getStats delegates to DB aggregation (count/average/max/min/distinct)")
    void getStats_dbAggregation() {
        when(scoreRepository.countByUsername("u1")).thenReturn(3L);
        when(scoreRepository.averageTotalScoreByUsername("u1")).thenReturn(80.0);
        when(scoreRepository.maxTotalScoreByUsername("u1")).thenReturn(100.0);
        when(scoreRepository.minTotalScoreByUsername("u1")).thenReturn(60.0);
        when(scoreRepository.countDistinctSceneByUsername("u1")).thenReturn(1L);
        when(scoreRepository.countDistinctSkillByUsername("u1")).thenReturn(1L);

        ScoreStatsResponse stats = scoreService.getStats("u1");

        assertEquals(3, stats.getTotalAttempts());
        assertEquals(80.0, stats.getAverageScore(), 0.001, "average should be (80+100+60)/3 = 80");
        assertEquals(100.0, stats.getHighestScore(), 0.001);
        assertEquals(60.0, stats.getLowestScore(), 0.001);
        assertEquals(1, stats.getScenesTrained());
        assertEquals(1, stats.getSkillsTrained());

        // Prove aggregation is done at the DB layer, not by loading all rows in memory.
        verify(scoreRepository).countByUsername("u1");
        verify(scoreRepository).averageTotalScoreByUsername("u1");
        verify(scoreRepository).maxTotalScoreByUsername("u1");
        verify(scoreRepository).minTotalScoreByUsername("u1");
        verify(scoreRepository).countDistinctSceneByUsername("u1");
        verify(scoreRepository).countDistinctSkillByUsername("u1");
        verify(scoreRepository, never()).findAll();
    }

    @Test
    @DisplayName("P1-6 getStats for user with no scores returns zeroed stats")
    void getStats_emptyUser_zeroed() {
        when(scoreRepository.countByUsername("nobody")).thenReturn(0L);
        when(scoreRepository.averageTotalScoreByUsername("nobody")).thenReturn(0.0);
        when(scoreRepository.maxTotalScoreByUsername("nobody")).thenReturn(0.0);
        when(scoreRepository.minTotalScoreByUsername("nobody")).thenReturn(0.0);
        when(scoreRepository.countDistinctSceneByUsername("nobody")).thenReturn(0L);
        when(scoreRepository.countDistinctSkillByUsername("nobody")).thenReturn(0L);

        ScoreStatsResponse stats = scoreService.getStats("nobody");

        assertEquals(0, stats.getTotalAttempts());
        assertEquals(0.0, stats.getAverageScore(), 0.001);
        assertEquals(0.0, stats.getHighestScore(), 0.001);
        assertEquals(0.0, stats.getLowestScore(), 0.001);
    }
}
