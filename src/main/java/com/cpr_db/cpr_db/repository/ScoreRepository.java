package com.cpr_db.cpr_db.repository;

import com.cpr_db.cpr_db.entity.Score;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByUsernameOrderByCreatedAtDesc(String username);
    Optional<Score> findFirstByUsernameOrderByCreatedAtDesc(String username);
    Page<Score> findByUsername(String username, Pageable pageable);
    Page<Score> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Score> findTop5ByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);
    Optional<Score> findById(Long id);

    long countByUsername(String username);

    @Query("select coalesce(avg(s.totalScore), 0.0) from Score s where s.username = ?1")
    Double averageTotalScoreByUsername(String username);

    @Query("select coalesce(max(s.totalScore), 0.0) from Score s where s.username = ?1")
    Double maxTotalScoreByUsername(String username);

    @Query("select coalesce(min(s.totalScore), 0.0) from Score s where s.username = ?1")
    Double minTotalScoreByUsername(String username);

    @Query("select count(distinct s.scene) from Score s where s.username = ?1")
    Long countDistinctSceneByUsername(String username);

    @Query("select count(distinct s.skill) from Score s where s.username = ?1")
    Long countDistinctSkillByUsername(String username);

    // 全校（admin all=true）聚合：PRD 修订 #14/#16 —— admin 全校统计可见
    @Query("select coalesce(avg(s.totalScore), 0.0) from Score s")
    Double averageTotalScore();

    @Query("select coalesce(max(s.totalScore), 0.0) from Score s")
    Double maxTotalScore();

    @Query("select coalesce(min(s.totalScore), 0.0) from Score s")
    Double minTotalScore();

    @Query("select count(distinct s.scene) from Score s")
    Long countDistinctScene();

    @Query("select count(distinct s.skill) from Score s")
    Long countDistinctSkill();
}
