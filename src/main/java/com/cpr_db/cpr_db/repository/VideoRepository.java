package com.cpr_db.cpr_db.repository;

import com.cpr_db.cpr_db.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {

    Optional<Video> findByVideoId(String videoId);

    Page<Video> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Video> findBySkillId(Long skillId, Pageable pageable);
    Page<Video> findByStatus(String status, Pageable pageable);
    Page<Video> findByTitleContainingIgnoreCaseAndSkillId(String title, Long skillId, Pageable pageable);
    Page<Video> findByTitleContainingIgnoreCaseAndStatus(String title, String status, Pageable pageable);
    Page<Video> findBySkillIdAndStatus(Long skillId, String status, Pageable pageable);
    Page<Video> findByTitleContainingIgnoreCaseAndSkillIdAndStatus(String title, Long skillId, String status, Pageable pageable);
}
