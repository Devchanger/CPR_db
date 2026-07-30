package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.entity.Student;
import com.cpr_db.cpr_db.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    public Map<String, Object> getStudentList(String keyword, String status, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Student> result;
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasStatus = status != null && !status.isBlank();
        if (hasKeyword && hasStatus) {
            result = studentRepository.findByNameContainingIgnoreCaseAndStatus(keyword, status, pageable);
        } else if (hasKeyword) {
            result = studentRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else if (hasStatus) {
            result = studentRepository.findByStatus(status, pageable);
        } else {
            result = studentRepository.findAll(pageable);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Student student : result.getContent()) {
            list.add(toDetailMap(student));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", result.getTotalElements());
        return map;
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "student not found"));
    }

    public Student createStudent(Student body) {
        if (body.getName() == null || body.getName().isBlank()) {
            throw new BusinessException(400, "name is required");
        }
        return studentRepository.save(body);
    }

    public Student updateStudent(Long id, Student body) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "student not found"));
        if (body.getName() != null) student.setName(body.getName());
        if (body.getPhone() != null) student.setPhone(body.getPhone());
        if (body.getEmail() != null) student.setEmail(body.getEmail());
        if (body.getGroupName() != null) student.setGroupName(body.getGroupName());
        if (body.getCertStatus() != null) student.setCertStatus(body.getCertStatus());
        if (body.getTrainedAt() != null) student.setTrainedAt(body.getTrainedAt());
        if (body.getStatus() != null) student.setStatus(body.getStatus());
        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "student not found"));
        studentRepository.delete(student);
    }

    public Student updateStudentStatus(Long id, String status) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "student not found"));
        student.setStatus(status);
        return studentRepository.save(student);
    }

    private Map<String, Object> toDetailMap(Student student) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", student.getId());
        map.put("name", student.getName());
        map.put("phone", student.getPhone());
        map.put("email", student.getEmail());
        map.put("group_name", student.getGroupName());
        map.put("cert_status", student.getCertStatus());
        map.put("trained_at", student.getTrainedAt());
        map.put("status", student.getStatus());
        map.put("created_at", student.getCreatedAt());
        return map;
    }
}
