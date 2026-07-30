package com.cpr_db.cpr_db.repository;

import com.cpr_db.cpr_db.entity.Step;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StepRepository extends JpaRepository<Step, Long> {
    Page<Step> findBySkillId(Long skillId, Pageable pageable);
    Page<Step> findBySkillIdAndStatus(Long skillId, String status, Pageable pageable);
    Page<Step> findByStatus(String status, Pageable pageable);
    List<Step> findBySkillIdOrderByOrderAsc(Long skillId);
}
