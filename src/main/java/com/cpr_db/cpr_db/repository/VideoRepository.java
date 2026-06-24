package com.cpr_db.cpr_db.repository;

import com.cpr_db.cpr_db.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {

    Optional<Video> findByVideoId(String videoId);
}
