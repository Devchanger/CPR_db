package com.cpr_db.cpr_db.repository;

import com.cpr_db.cpr_db.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
