package com.cpr_db.cpr_db.repository;

import com.cpr_db.cpr_db.entity.Scene;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SceneRepository extends JpaRepository<Scene, Long> {
    List<Scene> findAllByOrderBySortOrderAsc();
    Page<Scene> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
