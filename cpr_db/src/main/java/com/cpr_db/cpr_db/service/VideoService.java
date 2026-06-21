package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.VideoResponse;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VideoService {

    private static final Map<String, VideoResponse> VIDEO_CATALOG = Map.of(
            "video1", new VideoResponse("video1", "https://example.com/videos/video1.mp4", 120),
            "video2", new VideoResponse("video2", "https://example.com/videos/video2.mp4", 180)
    );

    public VideoResponse getVideoById(String videoId) {
        return VIDEO_CATALOG.getOrDefault(videoId, null);
    }

    public VideoResponse getVideo(String videoId) {
        VideoResponse response = getVideoById(videoId);
        if (response == null) {
            throw new BusinessException(404, "video not found");
        }
        return response;
    }
}
