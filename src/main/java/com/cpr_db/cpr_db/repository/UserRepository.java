package com.cpr_db.cpr_db.repository;

import com.cpr_db.cpr_db.entity.User;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
=======
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;
>>>>>>> 193e2be (feat: complete all missing backend API endpoints)

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByPhone(String phone);
    Optional<User> findByStudentId(String studentId);
<<<<<<< HEAD
=======
    Page<User> findByRoleIn(Set<String> roles, Pageable pageable);
    Page<User> findByRoleInAndUsernameContainingIgnoreCase(Set<String> roles, String username, Pageable pageable);
    long countByRole(String role);
>>>>>>> 193e2be (feat: complete all missing backend API endpoints)
}
