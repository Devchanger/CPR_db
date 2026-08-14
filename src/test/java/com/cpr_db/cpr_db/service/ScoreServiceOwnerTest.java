package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.entity.Score;
import com.cpr_db.cpr_db.repository.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreServiceOwnerTest {

    @Mock ScoreRepository scoreRepository;
    @Mock LogService logService;

    private ScoreService scoreService;

    @BeforeEach
    void setUp() {
        scoreService = new ScoreService(scoreRepository, logService);
    }

    @Test
    @DisplayName("P0-11 non-admin querying another user's score throws 403")
    void getScoreById_nonAdminNonOwner_throws403() {
        Score score = new Score();
        score.setId(1L);
        score.setUsername("bob");
        when(scoreRepository.findById(1L)).thenReturn(Optional.of(score));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> scoreService.getScoreById(1L, "alice", false));
        assertEquals(403, ex.getCode());
    }

    @Test
    @DisplayName("P0-11 owner may read their own score")
    void getScoreById_owner_allowed() {
        Score score = new Score();
        score.setId(1L);
        score.setUsername("bob");
        when(scoreRepository.findById(1L)).thenReturn(Optional.of(score));

        assertDoesNotThrow(() -> scoreService.getScoreById(1L, "bob", false));
    }

    @Test
    @DisplayName("P0-11 admin may read any score")
    void getScoreById_admin_allowed() {
        Score score = new Score();
        score.setId(1L);
        score.setUsername("bob");
        when(scoreRepository.findById(1L)).thenReturn(Optional.of(score));

        assertDoesNotThrow(() -> scoreService.getScoreById(1L, "alice", true));
    }
}
