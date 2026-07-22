package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.entity.Student;
import com.cpr_db.cpr_db.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getAll() {
        return studentRepository.findAll();
    }
}
