package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.NotificationCreateRequest;
import com.cpr_db.cpr_db.dto.NotificationUpdateRequest;
import com.cpr_db.cpr_db.entity.Notification;
import com.cpr_db.cpr_db.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class NotificationService {

    private static final Set<String> VALID_STATUSES = Set.of("draft", "published", "offline");

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification create(NotificationCreateRequest req) {
        Notification notification = new Notification();
        notification.setTitle(req.getTitle().trim());
        notification.setContentMd(req.getContentMd());
        String status = req.getStatus() == null || req.getStatus().isBlank() ? "draft" : req.getStatus();
        validateStatus(status);
        notification.setStatus(status);
        if ("published".equals(status)) {
            notification.setPublishedAt(LocalDateTime.now());
        }
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification update(Long id, NotificationUpdateRequest req) {
        Notification notification = getById(id);
        if (req.getTitle() != null) {
            notification.setTitle(req.getTitle().trim());
        }
        if (req.getContentMd() != null) {
            notification.setContentMd(req.getContentMd());
        }
        if (req.getStatus() != null) {
            validateStatus(req.getStatus());
            if ("published".equals(req.getStatus()) && !"published".equals(notification.getStatus())) {
                notification.setPublishedAt(LocalDateTime.now());
            }
            notification.setStatus(req.getStatus());
        }
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> list(String status) {
        if (status != null && !status.isBlank()) {
            validateStatus(status);
            return notificationRepository.findByStatusOrderByCreatedAtDesc(status);
        }
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Notification getById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "notification not found"));
    }

    @Transactional
    public void delete(Long id) {
        notificationRepository.delete(getById(id));
    }

    private void validateStatus(String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw new BusinessException(400, "status must be draft, published or offline");
        }
    }
}
