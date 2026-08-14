package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.entity.Log;
import com.cpr_db.cpr_db.repository.LogRepository;
import com.cpr_db.cpr_db.repository.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaginationLimitTest {

    @Mock ScoreRepository scoreRepository;
    @Mock LogRepository logRepository;
    @Mock LogService logService;

    private ScoreService scoreService;
    private LogService logQueryService;

    @BeforeEach
    void setUp() {
        scoreService = new ScoreService(scoreRepository, logService);
        logQueryService = new LogService(logRepository);
    }

    @Test
    @DisplayName("P0-12 ScoreService.getAllScores pageSize=999999 clamped to 100")
    void getAllScores_clampsPageSize() {
        when(scoreRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(Page.empty());
        scoreService.getAllScores(1, 999999);
        ArgumentCaptor<Pageable> cap = ArgumentCaptor.forClass(Pageable.class);
        verify(scoreRepository).findAllByOrderByCreatedAtDesc(cap.capture());
        assertEquals(100, cap.getValue().getPageSize());
    }

    @Test
    @DisplayName("P0-12 ScoreService.getAllScores pageSize=50 unchanged")
    void getAllScores_keepsValidPageSize() {
        when(scoreRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(Page.empty());
        scoreService.getAllScores(1, 50);
        ArgumentCaptor<Pageable> cap = ArgumentCaptor.forClass(Pageable.class);
        verify(scoreRepository).findAllByOrderByCreatedAtDesc(cap.capture());
        assertEquals(50, cap.getValue().getPageSize());
    }

    @Test
    @DisplayName("P0-12 ScoreService.getUserScores pageSize=0 falls back to 10")
    void getUserScores_invalidPageSize_defaults() {
        when(scoreRepository.findByUsername(anyString(), any(Pageable.class))).thenReturn(Page.empty());
        scoreService.getUserScores("bob", 1, 0);
        ArgumentCaptor<Pageable> cap = ArgumentCaptor.forClass(Pageable.class);
        verify(scoreRepository).findByUsername(eq("bob"), cap.capture());
        assertEquals(10, cap.getValue().getPageSize());
    }

    @Test
    @DisplayName("P0-12 LogService.getLogs pageSize=999999 clamped to 100")
    void getLogs_clampsPageSize() {
        when(logRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
        logQueryService.getLogs(null, null, null, null, null, 1, 999999);
        ArgumentCaptor<Pageable> cap = ArgumentCaptor.forClass(Pageable.class);
        verify(logRepository).findAll(cap.capture());
        assertEquals(100, cap.getValue().getPageSize());
    }
}
