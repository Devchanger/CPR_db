package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.VideoCreateRequest;
import com.cpr_db.cpr_db.dto.VideoUpdateRequest;
import com.cpr_db.cpr_db.entity.Video;
import com.cpr_db.cpr_db.repository.SkillRepository;
import com.cpr_db.cpr_db.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock private VideoRepository videoRepository;
    @Mock private SkillRepository skillRepository;

    private VideoService videoService;

    @BeforeEach
    void setUp() {
        videoService = new VideoService(videoRepository, skillRepository);
    }

    @Test
    void createVideo_defaultsToActive() {
        VideoCreateRequest req = new VideoCreateRequest();
        req.setTitle("视频");
        req.setUrl("https://example.com/v.mp4");
        when(videoRepository.save(any(Video.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = videoService.createVideo(req);

        assertEquals("active", result.get("status"));
    }

    @Test
    void createVideo_legacyPublishedStatus_rejects() {
        VideoCreateRequest req = new VideoCreateRequest();
        req.setTitle("视频");
        req.setUrl("https://example.com/v.mp4");
        req.setStatus("published");

        BusinessException ex = assertThrows(BusinessException.class, () -> videoService.createVideo(req));

        assertEquals(400, ex.getCode());
    }

    @Test
    void updateVideo_validOfflineStatus_applied() {
        Video video = new Video();
        video.setId(1L);
        video.setStatus("active");
        when(videoRepository.findById(1L)).thenReturn(Optional.of(video));
        when(videoRepository.save(any(Video.class))).thenAnswer(inv -> inv.getArgument(0));

        VideoUpdateRequest req = new VideoUpdateRequest();
        req.setStatus("offline");
        Map<String, Object> result = videoService.updateVideo(1L, req);

        assertEquals("offline", result.get("status"));
    }

    @Test
    void updateVideo_legacyPublishedStatus_rejects() {
        Video video = new Video();
        video.setId(1L);
        video.setStatus("active");
        when(videoRepository.findById(1L)).thenReturn(Optional.of(video));

        VideoUpdateRequest req = new VideoUpdateRequest();
        req.setStatus("published");

        assertThrows(BusinessException.class, () -> videoService.updateVideo(1L, req));
    }

    @Test
    void prePersist_defaultsToActive() {
        Video video = new Video("v1", "https://example.com/v.mp4", 120);

        video.prePersist();

        assertEquals("active", video.getStatus());
    }
}
