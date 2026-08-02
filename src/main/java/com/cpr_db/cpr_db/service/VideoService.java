package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.VideoCreateRequest;
import com.cpr_db.cpr_db.dto.VideoUpdateRequest;
import com.cpr_db.cpr_db.entity.Skill;
import com.cpr_db.cpr_db.entity.Video;
import com.cpr_db.cpr_db.repository.SkillRepository;
import com.cpr_db.cpr_db.repository.VideoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VideoService {

    private static final int MAX_PAGE_SIZE = 100;

    private final VideoRepository videoRepository;
    private final SkillRepository skillRepository;

    public VideoService(VideoRepository videoRepository, SkillRepository skillRepository) {
        this.videoRepository = videoRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional(readOnly = true)
    public Video getVideoEntity(String videoId) {
        return videoRepository.findByVideoId(videoId)
                .orElseThrow(() -> new BusinessException(404, "video not found"));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getVideoDetail(String videoId) {
        Video video = getVideoEntity(videoId);
        return toDetailMap(video, resolveSkillNames(java.util.Set.of(video.getSkillId())));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getVideoList(String keyword, Long skillId, String status, int page, int pageSize) {
        page = clampPage(page);
        pageSize = clampPageSize(pageSize);
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

    @Transactional
    public Map<String, Object> createVideo(VideoCreateRequest req) {
        String title = req.getTitle() == null ? null : req.getTitle().trim();
        String url = req.getUrl() == null ? null : req.getUrl().trim();
        if (title == null || title.isBlank()) {
            throw new BusinessException(400, "title is required");
        }
        if (url == null || url.isBlank()) {
            throw new BusinessException(400, "url is required");
        }
        if (req.getSkillId() != null && !skillRepository.existsById(req.getSkillId())) {
            throw new BusinessException(400, "skill not found");
        }
        Video video = new Video();
        video.setTitle(title);
        video.setUrl(url);
        video.setSkillId(req.getSkillId());
        video.setDurationSeconds(req.getDurationSeconds());
        video.setStatus(req.getStatus() == null || req.getStatus().isBlank() ? "published" : req.getStatus());
        Video saved = videoRepository.save(video);
        return toDetailMap(saved, resolveSkillNames(java.util.Set.of(saved.getSkillId())));
    }

    @Transactional
    public Map<String, Object> updateVideo(Long id, VideoUpdateRequest req) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "video not found"));
        if (req.getTitle() != null) video.setTitle(req.getTitle().trim());
        if (req.getUrl() != null) video.setUrl(req.getUrl().trim());
        if (req.getSkillId() != null) {
            if (req.getSkillId() != 0 && !skillRepository.existsById(req.getSkillId())) {
                throw new BusinessException(400, "skill not found");
            }
            video.setSkillId(req.getSkillId());
        }
        if (req.getDurationSeconds() != null) {
            video.setDurationSeconds(req.getDurationSeconds());
        }
        if (req.getStatus() != null) video.setStatus(req.getStatus());
        Video saved = videoRepository.save(video);
        return toDetailMap(saved, resolveSkillNames(java.util.Set.of(saved.getSkillId())));
    }

    @Transactional
    public void deleteVideo(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "video not found"));
        videoRepository.delete(video);
    }

    @Transactional
    public Map<String, Object> updateVideoStatus(Long id, String status) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "video not found"));
        video.setStatus(status);
        Video saved = videoRepository.save(video);
        return toDetailMap(saved, resolveSkillNames(java.util.Set.of(saved.getSkillId())));
    }

    private Map<String, Object> toDetailMap(Video video, Map<Long, String> skillNameMap) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", video.getId());
        map.put("video_id", video.getVideoId());
        map.put("title", video.getTitle());
        map.put("url", video.getUrl());
        map.put("skill_id", video.getSkillId());
        map.put("skill_name", video.getSkillId() == null ? null : skillNameMap.get(video.getSkillId()));
        map.put("duration_seconds", video.getDurationSeconds());
        map.put("status", video.getStatus());
        map.put("created_at", video.getCreatedAt());
        return map;
    }

    private Map<String, Object> toListMap(Page<Video> result) {
        List<Video> videos = result.getContent();
        Set<Long> skillIds = videos.stream().map(Video::getSkillId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> skillNameMap = resolveSkillNames(skillIds);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Video video : videos) {
            list.add(toDetailMap(video, skillNameMap));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", result.getTotalElements());
        map.put("page", result.getNumber() + 1);
        map.put("page_size", result.getSize());
        return map;
    }

    private Map<Long, String> resolveSkillNames(Set<Long> skillIds) {
        Map<Long, String> map = new HashMap<>();
        if (skillIds.isEmpty()) return map;
        List<Skill> skills = skillRepository.findAllById(skillIds);
        for (Skill s : skills) {
            map.put(s.getId(), s.getName());
        }
        return map;
    }

    private int clampPage(int page) {
        return page < 1 ? 1 : page;
    }

    private int clampPageSize(int pageSize) {
        if (pageSize < 1) return 10;
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
