package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.entity.Score;
import com.cpr_db.cpr_db.repository.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreResponseEnvelopeTest {

    @Mock ScoreRepository scoreRepository;
    @Mock LogService logService;

    private ScoreService scoreService;

    @BeforeEach
    void setUp() {
        scoreService = new ScoreService(scoreRepository, logService);
    }

    @Test
    @DisplayName("P0-9 getAllScores returns unified {list,total,page,page_size} envelope")
    void getAllScores_envelope() {
        Score s1 = new Score(); s1.setId(1L); s1.setUsername("bob");
        Score s2 = new Score(); s2.setId(2L); s2.setUsername("bob");
        Page<Score> page = new PageImpl<>(List.of(s1, s2), PageRequest.of(0, 10), 2);
        when(scoreRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(page);

        Map<String, Object> result = scoreService.getAllScores(1, 10);

        assertTrue(result.containsKey("list"));
        assertTrue(result.containsKey("total"));
        assertTrue(result.containsKey("page"));
        assertTrue(result.containsKey("page_size"));
        assertEquals(2, ((List<?>) result.get("list")).size());
        assertEquals(2L, result.get("total"));
        assertEquals(1, result.get("page"));
        assertEquals(10, result.get("page_size"));
    }

    @Test
    @DisplayName("P0-9 getUserScores returns the same unified envelope")
    void getUserScores_envelope() {
        Score s1 = new Score(); s1.setId(1L); s1.setUsername("bob");
        Page<Score> page = new PageImpl<>(List.of(s1), PageRequest.of(0, 10), 1);
        when(scoreRepository.findByUsername(anyString(), any(Pageable.class))).thenReturn(page);

        Map<String, Object> result = scoreService.getUserScores("bob", 1, 10);

        assertTrue(result.containsKey("list"));
        assertTrue(result.containsKey("total"));
        assertTrue(result.containsKey("page"));
        assertTrue(result.containsKey("page_size"));
        assertEquals(1, ((List<?>) result.get("list")).size());
        assertEquals(1L, result.get("total"));
    }
}
