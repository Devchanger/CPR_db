package com.cpr_db.cpr_db.repository;

import com.cpr_db.cpr_db.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Page<Student> findByStatus(String status, Pageable pageable);
    Page<Student> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);

    @Query("SELECT s FROM Student s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(s.phone) LIKE LOWER(CONCAT('%', :phone, '%'))")
    Page<Student> findByKeywordOrPhone(@Param("name") String name, @Param("phone") String phone, Pageable pageable);

    @Query("SELECT s FROM Student s WHERE (LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(s.phone) LIKE LOWER(CONCAT('%', :phone, '%'))) AND s.status = :status")
    Page<Student> findByKeywordOrPhoneAndStatus(@Param("name") String name, @Param("phone") String phone, @Param("status") String status, Pageable pageable);
}
