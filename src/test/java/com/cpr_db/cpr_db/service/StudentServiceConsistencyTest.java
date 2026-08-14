package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.entity.Student;
import com.cpr_db.cpr_db.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceConsistencyTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student sampleStudent() {
        Student s = new Student();
        s.setId(1L);
        s.setName("张三");
        s.setPhone("13800000000");
        s.setEmail("zhangsan@example.com");
        s.setGroupName("A组");
        s.setCertStatus("certified");
        s.setTrainedAt(LocalDateTime.of(2026, 7, 30, 10, 0));
        s.setStatus("active");
        s.setCreatedAt(LocalDateTime.of(2026, 7, 30, 9, 0));
        return s;
    }

    @Test
    @DisplayName("单条学员详情应与列表一致使用 snake_case（group_name/cert_status/trained_at/created_at），不再返回 camelCase")
    void getStudentById_usesSnakeCaseLikeList() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(sampleStudent()));

        Map<String, Object> detail = studentService.getStudentById(1L);

        assertTrue(detail.containsKey("name"), "应含 name");
        assertTrue(detail.containsKey("group_name"), "应含 snake_case group_name，而非 groupName");
        assertTrue(detail.containsKey("cert_status"), "应含 snake_case cert_status，而非 certStatus");
        assertTrue(detail.containsKey("trained_at"), "应含 snake_case trained_at，而非 trainedAt");
        assertTrue(detail.containsKey("created_at"), "应含 snake_case created_at，而非 createdAt");

        assertFalse(detail.containsKey("groupName"), "不应返回 camelCase groupName");
        assertFalse(detail.containsKey("certStatus"), "不应返回 camelCase certStatus");
        assertFalse(detail.containsKey("trainedAt"), "不应返回 camelCase trainedAt");
        assertFalse(detail.containsKey("createdAt"), "不应返回 camelCase createdAt");

        assertEquals("张三", detail.get("name"));
        assertEquals("A组", detail.get("group_name"));
    }

    @Test
    @DisplayName("不存在的学员详情应抛 404")
    void getStudentById_notFound_throws404() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(com.cpr_db.cpr_db.common.BusinessException.class,
                () -> studentService.getStudentById(999L));
    }
}
