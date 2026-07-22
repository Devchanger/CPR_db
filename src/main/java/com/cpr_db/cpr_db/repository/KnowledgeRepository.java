package com.cpr_db.cpr_db.repository;

import com.cpr_db.cpr_db.entity.Knowledge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KnowledgeRepository extends JpaRepository<Knowledge, Long> {
    List<Knowledge> findByCategory(String category);
}
