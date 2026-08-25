package com.cpr_db.cpr_db.repository;

import com.cpr_db.cpr_db.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByPhone(String phone);
    Optional<User> findByStudentId(String studentId);

    /**
     * BE-B-01: migrate any legacy super_admin rows to the converged admin role.
     * Idempotent; safe to run on every boot.
     */
    @Modifying
    @Query("update User u set u.role = 'admin' where u.role = 'super_admin'")
    int convergeSuperAdminToAdmin();
}
