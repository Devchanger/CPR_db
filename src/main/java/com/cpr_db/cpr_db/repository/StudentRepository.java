package com.cpr_db.cpr_db.repository;

import com.cpr_db.cpr_db.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Page<Student> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Student> findByStatus(String status, Pageable pageable);
    Page<Student> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);
}
