package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.dto.ScoreSubmitRequest;
import com.cpr_db.cpr_db.entity.Score;
import com.cpr_db.cpr_db.repository.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreNormalizationTest {

    @Mock ScoreRepository scoreRepository;
    @Mock LogService logService;

    private ScoreService scoreService;

    @BeforeEach
    void setUp() {
        scoreService = new ScoreService(scoreRepository, logService);
    }

    private ScoreSubmitRequest request(String scene, String skill) {
        ScoreSubmitRequest req = new ScoreSubmitRequest();
        req.setScene(scene);
        req.setSkill(skill);
        req.setTotalScore(85f);
        return req;
    }

    @Test
    @DisplayName("BE-C-03 Subway_Terminal is normalized to canonical 地铁站")
    void subwayTerminal_normalizedToMetro() {
        when(scoreRepository.save(any(Score.class))).thenAnswer(inv -> inv.getArgument(0));

        scoreService.saveScore("bob", 1L, request("Subway_Terminal", "CPR"));

        ArgumentCaptor<Score> cap = ArgumentCaptor.forClass(Score.class);
        verify(scoreRepository).save(cap.capture());
        assertEquals("地铁站", cap.getValue().getScene());
        assertEquals("成人胸外按压", cap.getValue().getSkill());
    }

    @Test
    @DisplayName("BE-C-03 Ruins/Hospital_Corridor are normalized to canonical names")
    void vrScenes_normalized() {
        when(scoreRepository.save(any(Score.class))).thenAnswer(inv -> inv.getArgument(0));

        scoreService.saveScore("bob", 1L, request("Ruins", "CPR"));
        scoreService.saveScore("bob", 1L, request("Hospital_Corridor", "CPR"));

        ArgumentCaptor<Score> cap = ArgumentCaptor.forClass(Score.class);
        verify(scoreRepository, org.mockito.Mockito.times(2)).save(cap.capture());
        assertEquals("废墟", cap.getAllValues().get(0).getScene());
        assertEquals("医院走廊", cap.getAllValues().get(1).getScene());
    }

    @Test
    @DisplayName("BE-C-03 unknown scene/skill values are stored as-is (fallback)")
    void unknownValues_storedAsIs() {
        when(scoreRepository.save(any(Score.class))).thenAnswer(inv -> inv.getArgument(0));

        scoreService.saveScore("bob", 1L, request("SomeFutureScene", "SomeFutureSkill"));

        ArgumentCaptor<Score> cap = ArgumentCaptor.forClass(Score.class);
        verify(scoreRepository).save(cap.capture());
        assertEquals("SomeFutureScene", cap.getValue().getScene());
        assertEquals("SomeFutureSkill", cap.getValue().getSkill());
    }
}
