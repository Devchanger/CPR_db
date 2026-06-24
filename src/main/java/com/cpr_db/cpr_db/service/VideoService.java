package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.VideoResponse;
import com.cpr_db.cpr_db.entity.Video;
import com.cpr_db.cpr_db.repository.VideoRepository;
import org.springframework.stereotype.Service;

@Service
public class VideoService {

    private final VideoRepository videoRepository;

    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public VideoResponse getVideo(String videoId) {
        Video video = videoRepository.findByVideoId(videoId)
                .orElseThrow(() -> new BusinessException(404, "video not found"));
        return new VideoResponse(video.getVideoId(), video.getUrl(), video.getDurationSeconds());
    }
}
