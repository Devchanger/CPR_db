package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.NotificationCreateRequest;
import com.cpr_db.cpr_db.dto.NotificationUpdateRequest;
import com.cpr_db.cpr_db.entity.Notification;
import com.cpr_db.cpr_db.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository);
    }

    @Test
    void create_defaultsToDraft() {
        NotificationCreateRequest req = new NotificationCreateRequest();
        req.setTitle("通知一");
        req.setContentMd("# 标题\n正文");
        Notification saved = new Notification();
        saved.setTitle("通知一");
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification result = notificationService.create(req);

        assertEquals("draft", result.getStatus());
        assertNull(result.getPublishedAt());
    }

    @Test
    void create_publishedSetsPublishedAt() {
        NotificationCreateRequest req = new NotificationCreateRequest();
        req.setTitle("通知一");
        req.setContentMd("正文");
        req.setStatus("published");
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification result = notificationService.create(req);

        assertEquals("published", result.getStatus());
        assertNotNull(result.getPublishedAt());
    }

    @Test
    void create_invalidStatus_rejects() {
        NotificationCreateRequest req = new NotificationCreateRequest();
        req.setTitle("t");
        req.setContentMd("c");
        req.setStatus("deleted");

        BusinessException ex = assertThrows(BusinessException.class, () -> notificationService.create(req));

        assertEquals(400, ex.getCode());
    }

    @Test
    void update_publishTransitionSetsPublishedAt() {
        Notification draft = new Notification();
        draft.setId(1L);
        draft.setTitle("草稿");
        draft.setContentMd("正文");
        draft.setStatus("draft");
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(draft));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationUpdateRequest req = new NotificationUpdateRequest();
        req.setStatus("published");
        Notification result = notificationService.update(1L, req);

        assertEquals("published", result.getStatus());
        assertNotNull(result.getPublishedAt());
    }

    @Test
    void update_offlineKeepsPublishedAt() {
        Notification published = new Notification();
        published.setId(1L);
        published.setTitle("已发布");
        published.setContentMd("正文");
        published.setStatus("published");
        published.setPublishedAt(java.time.LocalDateTime.of(2026, 8, 1, 10, 0));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(published));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationUpdateRequest req = new NotificationUpdateRequest();
        req.setStatus("offline");
        Notification result = notificationService.update(1L, req);

        assertEquals("offline", result.getStatus());
        assertNotNull(result.getPublishedAt());
    }

    @Test
    void list_filtersByStatus() {
        when(notificationRepository.findByStatusOrderByCreatedAtDesc("published"))
                .thenReturn(List.of(new Notification()));

        List<Notification> result = notificationService.list("published");

        assertEquals(1, result.size());
        verify(notificationRepository).findByStatusOrderByCreatedAtDesc("published");
    }

    @Test
    void delete_removesExistingNotification() {
        Notification n = new Notification();
        n.setId(1L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        notificationService.delete(1L);

        verify(notificationRepository).delete(n);
    }
}
