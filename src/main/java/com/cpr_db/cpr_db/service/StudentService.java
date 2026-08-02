package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.StudentCreateRequest;
import com.cpr_db.cpr_db.dto.StudentUpdateRequest;
import com.cpr_db.cpr_db.entity.Student;
import com.cpr_db.cpr_db.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    private static final int MAX_PAGE_SIZE = 100;

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentList(String keyword, String status, int page, int pageSize) {
        page = clampPage(page);
        pageSize = clampPageSize(pageSize);
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Student> result;
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasStatus = status != null && !status.isBlank();
        if (hasKeyword && hasStatus) {
            result = studentRepository.findByKeywordOrPhoneAndStatus(keyword, keyword, status, pageable);
        } else if (hasKeyword) {
            result = studentRepository.findByKeywordOrPhone(keyword, keyword, pageable);
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
        map.put("page", result.getNumber() + 1);
        map.put("page_size", result.getSize());
        return map;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "student not found"));
        return toDetailMap(student);
    }

    @Transactional
    public Student createStudent(StudentCreateRequest req) {
        String name = req.getName() == null ? null : req.getName().trim();
        if (name == null || name.isBlank()) {
            throw new BusinessException(400, "name is required");
        }
        Student student = new Student();
        student.setName(name);
        student.setPhone(req.getPhone());
        student.setEmail(req.getEmail());
        student.setGroupName(req.getGroupName());
        student.setCertStatus(req.getCertStatus());
        student.setTrainedAt(req.getTrainedAt());
        return studentRepository.save(student);
    }

    @Transactional
    public Student updateStudent(Long id, StudentUpdateRequest req) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "student not found"));
        if (req.getName() != null) student.setName(req.getName().trim());
        if (req.getPhone() != null) student.setPhone(req.getPhone());
        if (req.getEmail() != null) student.setEmail(req.getEmail());
        if (req.getGroupName() != null) student.setGroupName(req.getGroupName());
        if (req.getCertStatus() != null) student.setCertStatus(req.getCertStatus());
        if (req.getTrainedAt() != null) student.setTrainedAt(req.getTrainedAt());
        return studentRepository.save(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "student not found"));
        studentRepository.delete(student);
    }

    @Transactional
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

    private int clampPage(int page) {
        return page < 1 ? 1 : page;
    }

    private int clampPageSize(int pageSize) {
        if (pageSize < 1) return 10;
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
