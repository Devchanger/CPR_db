package com.cpr_db.cpr_db.config;

import com.cpr_db.cpr_db.common.PinyinUtil;
import com.cpr_db.cpr_db.dto.StudentCreateRequest;
import com.cpr_db.cpr_db.entity.Score;
import com.cpr_db.cpr_db.entity.Student;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.repository.ScoreRepository;
import com.cpr_db.cpr_db.repository.StudentRepository;
import com.cpr_db.cpr_db.repository.UserRepository;
import com.cpr_db.cpr_db.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MockStudentDataSeederTest {

    @Mock private StudentService studentService;
    @Mock private StudentRepository studentRepository;
    @Mock private UserRepository userRepository;
    @Mock private ScoreRepository scoreRepository;

    private MockStudentDataSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new MockStudentDataSeeder(studentService, studentRepository, userRepository, scoreRepository);
    }

    @Test
    @DisplayName("BE-C-05 skipped when scores already exist (idempotent)")
    void run_skipsWhenScoresExist() {
        when(scoreRepository.count()).thenReturn(5L);

        seeder.run();

        verify(studentRepository, never()).findByUsername(anyString());
        verify(scoreRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("BE-C-05 seeds 5 students and 36 rounds incl. boundary values and canonical scenes")
    void run_seedsStudentsAndBoundaryScores() {
        when(scoreRepository.count()).thenReturn(0L);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(studentService.createStudent(any(StudentCreateRequest.class))).thenAnswer(inv -> {
            StudentCreateRequest req = inv.getArgument(0);
            Student s = new Student();
            s.setId(1L);
            s.setName(req.getName());
            s.setUsername(PinyinUtil.pinyin(req.getName()));
            s.setStatus("active");
            return s;
        });
        when(userRepository.findByUsername(anyString())).thenAnswer(inv -> {
            User u = new User();
            u.setId(1L);
            u.setUsername(inv.getArgument(0));
            return Optional.of(u);
        });
        when(scoreRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        seeder.run();

        verify(studentService, times(5)).createStudent(any(StudentCreateRequest.class));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Score>> cap = ArgumentCaptor.forClass(List.class);
        verify(scoreRepository).saveAll(cap.capture());
        List<Score> scores = cap.getValue();

        assertEquals(36, scores.size(), "30 regular + 6 boundary rounds");
        Set<Float> totals = scores.stream().map(Score::getTotalScore).collect(Collectors.toSet());
        for (int boundary : new int[]{59, 60, 79, 80, 100}) {
            assertTrue(totals.contains((float) boundary), "boundary score " + boundary + " missing");
        }
        assertTrue(scores.stream().anyMatch(s -> s.getStepDetails() == null),
                "boundary empty stepDetails round missing");
        Set<String> canonicalScenes = Set.of("地铁站", "医院走廊", "废墟", "成人 CPR 训练", "儿童 CPR 训练", "AED 使用");
        assertTrue(scores.stream().allMatch(s -> canonicalScenes.contains(s.getScene())),
                "all mock rounds must use canonical scene names (BE-C-03)");
    }
}
