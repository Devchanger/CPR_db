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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * BE-C-05 (D10/D26/S4): demo data seeder — 5 students, 36 score rounds
 * (30 regular + 6 boundary: 59/60/79/80/100/empty stepDetails), canonical Chinese
 * scene names matching BE-C-03. Enabled by default (matchIfMissing=true); production
 * must set cpr.mock-seeder.enabled=false.
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "cpr.mock-seeder.enabled", havingValue = "true", matchIfMissing = true)
public class MockStudentDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MockStudentDataSeeder.class);

    private static final List<String> STUDENT_NAMES = List.of("张三", "李四", "王小明", "赵敏", "周婷");
    private static final List<String> SCENES =
            List.of("地铁站", "医院走廊", "废墟", "成人 CPR 训练", "儿童 CPR 训练", "AED 使用");
    private static final List<String> SKILLS =
            List.of("成人胸外按压", "成人开放气道与人工呼吸", "婴儿 CPR", "AED 操作", "CPR");

    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final ScoreRepository scoreRepository;

    public MockStudentDataSeeder(StudentService studentService,
                                 StudentRepository studentRepository,
                                 UserRepository userRepository,
                                 ScoreRepository scoreRepository) {
        this.studentService = studentService;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.scoreRepository = scoreRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (scoreRepository.count() > 0) {
            log.info("MockStudentDataSeeder skipped: scores already present (idempotent)");
            return;
        }
        List<Student> students = ensureStudents();
        List<Score> scores = buildScores(students);
        scoreRepository.saveAll(scores);
        log.info("MockStudentDataSeeder: seeded {} students, {} score rounds", students.size(), scores.size());
    }

    private List<Student> ensureStudents() {
        List<Student> students = new ArrayList<>();
        int phoneSeq = 3;
        for (String name : STUDENT_NAMES) {
            String username = PinyinUtil.pinyin(name);
            if (studentRepository.findByUsername(username).isPresent()) {
                studentRepository.findByUsername(username).ifPresent(students::add);
                continue;
            }
            if (userRepository.existsByUsername(username)) {
                log.warn("MockStudentDataSeeder: username {} already taken without a Student row; skipped", username);
                continue;
            }
            StudentCreateRequest req = new StudentCreateRequest();
            req.setName(name);
            req.setPhone("1390000000" + phoneSeq++);
            req.setGroupName("group-a");
            students.add(studentService.createStudent(req));
        }
        return students;
    }

    private List<Score> buildScores(List<Student> students) {
        List<Score> scores = new ArrayList<>();
        int round = 0;
        for (Student student : students) {
            int roundsForStudent = 6;
            for (int i = 0; i < roundsForStudent; i++) {
                int base = 72 + (round * 3) % 24;
                scores.add(score(student, base, i, null));
                round++;
            }
        }
        // Boundary rounds: 59/60/79/80/100 + empty stepDetails (index 0/1/2/3/4/5).
        int[] boundary = {59, 60, 79, 80, 100, 85};
        for (int i = 0; i < boundary.length; i++) {
            Student student = students.get(i % students.size());
            String stepDetails = i == 5 ? null : stepDetails(boundary[i]);
            scores.add(score(student, boundary[i], 30 + i, stepDetails));
        }
        return scores;
    }

    private Score score(Student student, int total, int offset, String stepDetails) {
        Score s = new Score();
        s.setUserId(userId(student));
        s.setUsername(student.getUsername());
        s.setScene(SCENES.get(offset % SCENES.size()));
        s.setSkill(SKILLS.get(offset % SKILLS.size()));
        s.setTotalScore((float) total);
        s.setCompressionDepthAvg(4.8f + (offset % 14) * 0.1f);
        s.setCompressionRateAvg(98f + (offset % 25));
        s.setErrorCount((offset * 2) % 13);
        s.setStepDetails(stepDetails);
        s.setCreatedAt(LocalDateTime.now().minusDays(35L - offset));
        return s;
    }

    private Long userId(Student student) {
        return userRepository.findByUsername(student.getUsername()).map(User::getId).orElse(null);
    }

    private String stepDetails(int total) {
        return "[{\"stepName\":\"胸外按压\",\"score\":" + total + ",\"comment\":\"深度达标\"},"
                + "{\"stepName\":\"人工呼吸\",\"score\":" + Math.max(0, total - 5) + ",\"comment\":\"见胸廓隆起\"}]";
    }
}
