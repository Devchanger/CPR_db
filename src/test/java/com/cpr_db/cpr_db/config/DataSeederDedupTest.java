package com.cpr_db.cpr_db.config;

import com.cpr_db.cpr_db.entity.Knowledge;
import com.cpr_db.cpr_db.entity.Scene;
import com.cpr_db.cpr_db.entity.Skill;
import com.cpr_db.cpr_db.entity.Student;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.entity.Video;
import com.cpr_db.cpr_db.repository.KnowledgeRepository;
import com.cpr_db.cpr_db.repository.SceneRepository;
import com.cpr_db.cpr_db.repository.SkillRepository;
import com.cpr_db.cpr_db.repository.StepRepository;
import com.cpr_db.cpr_db.repository.StudentRepository;
import com.cpr_db.cpr_db.repository.UserRepository;
import com.cpr_db.cpr_db.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederDedupTest {

    @Mock VideoRepository videoRepository;
    @Mock SceneRepository sceneRepository;
    @Mock SkillRepository skillRepository;
    @Mock StepRepository stepRepository;
    @Mock StudentRepository studentRepository;
    @Mock KnowledgeRepository knowledgeRepository;
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    private DataSeeder dataSeeder;

    @BeforeEach
    void setUp() {
        dataSeeder = new DataSeeder(videoRepository, sceneRepository, skillRepository,
                stepRepository, studentRepository, knowledgeRepository, userRepository, passwordEncoder);
    }

    private void stubEmptyDb() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(sceneRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of());
        when(knowledgeRepository.count()).thenReturn(0L);
        when(videoRepository.count()).thenReturn(0L);
        when(sceneRepository.count()).thenReturn(0L);
        when(skillRepository.count()).thenReturn(0L);
        when(stepRepository.count()).thenReturn(0L);
        when(studentRepository.count()).thenReturn(0L);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
    }

    @Test
    @DisplayName("P0-8 first run seeds each knowledge question exactly once (no duplicate rows)")
    void knowledgeSeeds_notDuplicated() {
        stubEmptyDb();
        dataSeeder.run();

        ArgumentCaptor<Knowledge> cap = ArgumentCaptor.forClass(Knowledge.class);
        verify(knowledgeRepository, atLeastOnce()).save(cap.capture());
        List<Knowledge> all = cap.getAllValues();
        long distinct = all.stream().map(Knowledge::getQuestion).distinct().count();
        assertEquals(all.size(), distinct, "Knowledge seeds contain duplicate questions (P0-8)");
        assertTrue(all.size() > 0, "Knowledge seeds should be present");
    }

    @Test
    @DisplayName("P0-8/BE-B-01 exactly one admin seed account created on first run")
    void adminSeed_unique() {
        stubEmptyDb();
        dataSeeder.run();

        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(cap.capture());
        long admins = cap.getAllValues().stream()
                .filter(u -> "admin".equals(u.getRole()))
                .count();
        assertEquals(1, admins, "should seed exactly one admin (P0-8/BE-B-01)");
        assertTrue(cap.getAllValues().stream().allMatch(User::isMustChangePassword),
                "all seed-created accounts must be flagged for forced password change (BE-B-06/D14)");
    }

    @Test
    @DisplayName("P0-8 second run (already seeded) creates no duplicates")
    void secondRun_noDuplicates() {
        when(knowledgeRepository.count()).thenReturn(999L);
        when(videoRepository.count()).thenReturn(999L);
        when(sceneRepository.count()).thenReturn(999L);
        when(skillRepository.count()).thenReturn(999L);
        when(stepRepository.count()).thenReturn(999L);
        when(studentRepository.count()).thenReturn(999L);
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        dataSeeder.run();

        verify(knowledgeRepository, never()).save(any(Knowledge.class));
        verify(videoRepository, never()).save(any(Video.class));
        verify(sceneRepository, never()).save(any(Scene.class));
        verify(skillRepository, never()).save(any(Skill.class));
        verify(studentRepository, never()).save(any(Student.class));
        verify(userRepository, never()).save(any(User.class));
    }
}
