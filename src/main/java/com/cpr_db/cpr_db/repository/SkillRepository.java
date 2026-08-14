package com.cpr_db.cpr_db.repository;

import com.cpr_db.cpr_db.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByName(String name);
    Page<Skill> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Skill> findByStatus(String status, Pageable pageable);
    Page<Skill> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);
    boolean existsByName(String name);
}
