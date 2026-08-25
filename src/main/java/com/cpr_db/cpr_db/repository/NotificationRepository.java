package com.cpr_db.cpr_db.repository;

import com.cpr_db.cpr_db.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStatusOrderByCreatedAtDesc(String status);
    List<Notification> findAllByOrderByCreatedAtDesc();
}
