package com.cpr_db.cpr_db.repository;

import com.cpr_db.cpr_db.entity.Scene;
<<<<<<< HEAD
=======
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
>>>>>>> 193e2be (feat: complete all missing backend API endpoints)
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SceneRepository extends JpaRepository<Scene, Long> {
    List<Scene> findAllByOrderBySortOrderAsc();
<<<<<<< HEAD
=======
    Page<Scene> findByNameContainingIgnoreCase(String name, Pageable pageable);
>>>>>>> 193e2be (feat: complete all missing backend API endpoints)
}
