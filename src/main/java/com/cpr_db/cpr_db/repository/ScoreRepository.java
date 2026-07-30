package com.cpr_db.cpr_db.repository;

import com.cpr_db.cpr_db.entity.Score;
<<<<<<< HEAD
=======
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
>>>>>>> 193e2be (feat: complete all missing backend API endpoints)
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByUsernameOrderByCreatedAtDesc(String username);
    Optional<Score> findFirstByUsernameOrderByCreatedAtDesc(String username);
<<<<<<< HEAD
=======
    Page<Score> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Optional<Score> findById(Long id);
>>>>>>> 193e2be (feat: complete all missing backend API endpoints)
}
