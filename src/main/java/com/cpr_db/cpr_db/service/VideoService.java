package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.VideoResponse;
import com.cpr_db.cpr_db.entity.Skill;
import com.cpr_db.cpr_db.entity.Video;
import com.cpr_db.cpr_db.repository.SkillRepository;
import com.cpr_db.cpr_db.repository.VideoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final SkillRepository skillRepository;

    public VideoService(VideoRepository videoRepository, SkillRepository skillRepository) {
        this.videoRepository = videoRepository;
        this.skillRepository = skillRepository;
    }

    public VideoResponse getVideo(String videoId) {
        Video video = videoRepository.findByVideoId(videoId)
                .orElseThrow(() -> new BusinessException(404, "video not found"));
        return new VideoResponse(video.getVideoId(), video.getUrl(), video.getDurationSeconds());
    }

    public Map<String, Object> getVideoList(String keyword, Long skillId, String status, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Video> result;
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasStatus = status != null && !status.isBlank();
        if (hasKeyword && skillId != null && hasStatus) {
            result = videoRepository.findByTitleContainingIgnoreCaseAndSkillIdAndStatus(keyword, skillId, status, pageable);
        } else if (hasKeyword && skillId != null) {
            result = videoRepository.findByTitleContainingIgnoreCaseAndSkillId(keyword, skillId, pageable);
        } else if (hasKeyword && hasStatus) {
            result = videoRepository.findByTitleContainingIgnoreCaseAndStatus(keyword, status, pageable);
        } else if (skillId != null && hasStatus) {
            result = videoRepository.findBySkillIdAndStatus(skillId, status, pageable);
        } else if (hasKeyword) {
            result = videoRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else if (skillId != null) {
            result = videoRepository.findBySkillId(skillId, pageable);
        } else if (hasStatus) {
            result = videoRepository.findByStatus(status, pageable);
        } else {
            result = videoRepository.findAll(pageable);
        }
        return toListMap(result);
    }

    public Map<String, Object> createVideo(Map<String, Object> body) {
        String title = body.get("title") == null ? null : body.get("title").toString().trim();
        String url = body.get("url") == null ? null : body.get("url").toString().trim();
        if (title == null || title.isBlank()) {
            throw new BusinessException(400, "title is required");
        }
        if (url == null || url.isBlank()) {
            throw new BusinessException(400, "url is required");
        }
        Video video = new Video();
        video.setTitle(title);
        video.setUrl(url);
        video.setVideoId("v" + System.currentTimeMillis());
        if (body.containsKey("skill_id") && body.get("skill_id") != null) {
            video.setSkillId(toLong(body.get("skill_id")));
        }
        if (body.containsKey("duration_seconds") && body.get("duration_seconds") != null) {
            video.setDurationSeconds(toInt(body.get("duration_seconds")));
        } else {
            video.setDurationSeconds(0);
        }
        if (body.containsKey("status") && body.get("status") != null) {
            video.setStatus(body.get("status").toString());
        } else {
            video.setStatus("published");
        }
        Video saved = videoRepository.save(video);
        return toDetailMap(saved);
    }

    public Map<String, Object> updateVideo(Long id, Map<String, Object> body) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "video not found"));
        if (body.containsKey("title") && body.get("title") != null) {
            video.setTitle(body.get("title").toString().trim());
        }
        if (body.containsKey("url") && body.get("url") != null) {
            video.setUrl(body.get("url").toString().trim());
        }
        if (body.containsKey("skill_id")) {
            video.setSkillId(body.get("skill_id") == null ? null : toLong(body.get("skill_id")));
        }
        if (body.containsKey("duration_seconds")) {
            video.setDurationSeconds(body.get("duration_seconds") == null ? null : toInt(body.get("duration_seconds")));
        }
        if (body.containsKey("status")) {
            video.setStatus(body.get("status") == null ? null : body.get("status").toString());
        }
        Video saved = videoRepository.save(video);
        return toDetailMap(saved);
    }

    public void deleteVideo(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "video not found"));
        videoRepository.delete(video);
    }

    public Map<String, Object> updateVideoStatus(Long id, String status) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "video not found"));
        video.setStatus(status);
        Video saved = videoRepository.save(video);
        return toDetailMap(saved);
    }

    private Map<String, Object> toDetailMap(Video video) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", video.getId());
        map.put("video_id", video.getVideoId());
        map.put("title", video.getTitle());
        map.put("url", video.getUrl());
        map.put("skill_id", video.getSkillId());
        map.put("skill_name", resolveSkillName(video.getSkillId()));
        map.put("duration_seconds", video.getDurationSeconds());
        map.put("status", video.getStatus());
        map.put("created_at", video.getCreatedAt());
        return map;
    }

    private Map<String, Object> toListMap(Page<Video> result) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Video video : result.getContent()) {
            list.add(toDetailMap(video));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", result.getTotalElements());
        return map;
    }

    private String resolveSkillName(Long skillId) {
        if (skillId == null) return null;
        return skillRepository.findById(skillId)
                .map(Skill::getName)
                .orElse(null);
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        return Long.parseLong(o.toString());
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        return Integer.parseInt(o.toString());
    }
}
