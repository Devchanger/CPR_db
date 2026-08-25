package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.StudentCreateRequest;
import com.cpr_db.cpr_db.entity.Student;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.repository.ScoreRepository;
import com.cpr_db.cpr_db.repository.StudentRepository;
import com.cpr_db.cpr_db.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ScoreRepository scoreRepository;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentRepository, userRepository, passwordEncoder, scoreRepository);
    }

    @Test
    void createStudent_createsLinkedUserWithPinyinCredentials() {
        StudentCreateRequest req = new StudentCreateRequest();
        req.setName("张三");
        when(userRepository.existsByUsername("zhangsan")).thenReturn(false);
        when(passwordEncoder.encode("zhangsan")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        Student student = studentService.createStudent(req);

        assertEquals("zhangsan", student.getUsername());
        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(cap.capture());
        User saved = cap.getValue();
        assertEquals("zhangsan", saved.getUsername());
        assertEquals("student", saved.getRole());
        assertTrue(saved.isMustChangePassword());
        assertEquals("hash", saved.getPasswordHash());
    }

    @Test
    void createStudent_padsShortPinyinPasswordToSixChars() {
        StudentCreateRequest req = new StudentCreateRequest();
        req.setName("李四");
        when(userRepository.existsByUsername("lisi")).thenReturn(false);
        when(passwordEncoder.encode("lisi11")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        studentService.createStudent(req);

        verify(passwordEncoder).encode("lisi11");
    }

    @Test
    void createStudent_explicitUsernameConflict_rejectsWith409() {
        StudentCreateRequest req = new StudentCreateRequest();
        req.setName("张三");
        req.setUsername("taken");
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> studentService.createStudent(req));

        assertEquals(409, ex.getCode());
        verify(userRepository, never()).save(any(User.class));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void createStudent_generatedUsernameCollision_appendsSuffix() {
        StudentCreateRequest req = new StudentCreateRequest();
        req.setName("张三");
        when(userRepository.existsByUsername("zhangsan")).thenReturn(true);
        when(userRepository.existsByUsername("zhangsan2")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        Student student = studentService.createStudent(req);

        assertEquals("zhangsan2", student.getUsername());
    }

    @Test
    void resetPassword_returnsPaddedPasswordAndFlagsUser() {
        Student student = new Student();
        student.setId(1L);
        student.setName("李四");
        student.setUsername("lisi");
        User existing = new User();
        existing.setUsername("lisi");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.findByUsername("lisi")).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("lisi11")).thenReturn("newhash");

        Map<String, Object> result = studentService.resetPassword(1L);

        assertEquals("lisi", result.get("username"));
        assertEquals("lisi11", result.get("new_password"));
        assertEquals("newhash", existing.getPasswordHash());
        assertTrue(existing.isMustChangePassword());
        assertEquals("active", existing.getStatus());
    }

    @Test
    void updateStudentStatus_disablesLinkedUser() {
        Student student = new Student();
        student.setId(1L);
        student.setUsername("zhangsan");
        student.setStatus("active");
        User linked = new User();
        linked.setUsername("zhangsan");
        linked.setStatus("active");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findByUsername("zhangsan")).thenReturn(Optional.of(linked));

        studentService.updateStudentStatus(1L, "disabled");

        assertEquals("disabled", linked.getStatus());
        verify(userRepository).save(linked);
    }

    @Test
    void updateStudentStatus_invalidStatus_rejects() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> studentService.updateStudentStatus(1L, "deleted"));

        assertEquals(400, ex.getCode());
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void getStudentById_includesUsernameAndStats() {
        Student student = new Student();
        student.setId(1L);
        student.setName("张三");
        student.setUsername("zhangsan");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(scoreRepository.countByUsername("zhangsan")).thenReturn(5L);
        when(scoreRepository.averageTotalScoreByUsername("zhangsan")).thenReturn(82.5);

        Map<String, Object> detail = studentService.getStudentById(1L);

        assertEquals("zhangsan", detail.get("username"));
        assertEquals(5L, detail.get("training_count"));
        assertEquals(82.5, detail.get("average_score"));
    }

    @Test
    void deleteStudent_softArchivesInsteadOfPhysicalDelete() {
        Student student = new Student();
        student.setId(1L);
        student.setName("张三");
        student.setStatus("active");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        studentService.deleteStudent(1L);

        assertEquals("archived", student.getStatus());
        verify(studentRepository).save(student);
        verify(studentRepository, never()).delete(any(Student.class));
    }

    @Test
    void getStudentList_excludesArchivedByDefault() {
        Page<Student> page = new PageImpl<>(java.util.List.of(), PageRequest.of(0, 10), 0);
        when(studentRepository.findByStatusNot(eq("archived"), any(Pageable.class))).thenReturn(page);

        studentService.getStudentList(null, null, 1, 10);

        verify(studentRepository).findByStatusNot(eq("archived"), any(Pageable.class));
        verify(studentRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getStudentList_keywordOnly_excludesArchived() {
        Page<Student> page = new PageImpl<>(java.util.List.of(), PageRequest.of(0, 10), 0);
        when(studentRepository.findByNameContainingIgnoreCaseAndStatusNot(eq("张"), eq("archived"), any(Pageable.class)))
                .thenReturn(page);

        studentService.getStudentList("张", null, 1, 10);

        verify(studentRepository)
                .findByNameContainingIgnoreCaseAndStatusNot(eq("张"), eq("archived"), any(Pageable.class));
    }
}
