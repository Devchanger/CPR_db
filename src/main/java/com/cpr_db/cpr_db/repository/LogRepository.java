package com.cpr_db.cpr_db.repository;

import com.cpr_db.cpr_db.entity.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface LogRepository extends JpaRepository<Log, Long> {
    Page<Log> findByAdminId(Long adminId, Pageable pageable);
    Page<Log> findByAction(String action, Pageable pageable);
    Page<Log> findByTargetType(String targetType, Pageable pageable);
    Page<Log> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<Log> findByAdminIdAndCreatedAtBetween(Long adminId, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
