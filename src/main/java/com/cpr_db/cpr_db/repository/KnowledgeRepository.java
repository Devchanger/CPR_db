package com.cpr_db.cpr_db.repository;

import com.cpr_db.cpr_db.entity.Knowledge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeRepository extends JpaRepository<Knowledge, Long> {
    List<Knowledge> findByCategory(String category);
    Page<Knowledge> findByCategory(String category, Pageable pageable);
}
