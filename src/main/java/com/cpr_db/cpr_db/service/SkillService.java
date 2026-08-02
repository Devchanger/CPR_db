package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.SkillCreateRequest;
import com.cpr_db.cpr_db.dto.SkillUpdateRequest;
import com.cpr_db.cpr_db.entity.Scene;
import com.cpr_db.cpr_db.entity.Skill;
import com.cpr_db.cpr_db.repository.SceneRepository;
import com.cpr_db.cpr_db.repository.SkillRepository;
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
public class SkillService {

    private static final int MAX_PAGE_SIZE = 100;

    private final SkillRepository skillRepository;
    private final SceneRepository sceneRepository;

    public SkillService(SkillRepository skillRepository, SceneRepository sceneRepository) {
        this.skillRepository = skillRepository;
        this.sceneRepository = sceneRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSkillList(String keyword, String status, int page, int pageSize) {
        page = clampPage(page);
        pageSize = clampPageSize(pageSize);
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "sortOrder"));
        Page<Skill> result;
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasStatus = status != null && !status.isBlank();
        if (hasKeyword && hasStatus) {
            result = skillRepository.findByNameContainingIgnoreCaseAndStatus(keyword, status, pageable);
        } else if (hasKeyword) {
            result = skillRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else if (hasStatus) {
            result = skillRepository.findByStatus(status, pageable);
        } else {
            result = skillRepository.findAll(pageable);
        }
        return toListMap(result);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "skill not found"));
        return toDetailMap(skill, resolveSceneNames(Set.of(skill.getSceneId())));
    }

    @Transactional
    public Map<String, Object> createSkill(SkillCreateRequest req) {
        String name = req.getName() == null ? null : req.getName().trim();
        if (name == null || name.isBlank()) {
            throw new BusinessException(400, "name is required");
        }
        if (skillRepository.existsByName(name)) {
            throw new BusinessException(409, "skill name already exists");
        }
        if (req.getSceneId() != null && !sceneRepository.existsById(req.getSceneId())) {
            throw new BusinessException(400, "scene not found");
        }
        Skill skill = new Skill();
        skill.setName(name);
        skill.setDescription(req.getDescription());
        skill.setIcon(req.getIcon());
        skill.setSceneId(req.getSceneId());
        skill.setStatus(req.getStatus());
        skill.setSortOrder(req.getSortOrder());
        Skill saved = skillRepository.save(skill);
        return toDetailMap(saved, resolveSceneNames(Set.of(saved.getSceneId())));
    }

    @Transactional
    public Map<String, Object> updateSkill(Long id, SkillUpdateRequest req) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "skill not found"));
        if (req.getName() != null) {
            String name = req.getName().trim();
            if (!name.equals(skill.getName()) && skillRepository.existsByName(name)) {
                throw new BusinessException(409, "skill name already exists");
            }
            skill.setName(name);
        }
        if (req.getDescription() != null) skill.setDescription(req.getDescription());
        if (req.getIcon() != null) skill.setIcon(req.getIcon());
        if (req.getSceneId() != null) {
            if (req.getSceneId() != 0 && !sceneRepository.existsById(req.getSceneId())) {
                throw new BusinessException(400, "scene not found");
            }
            skill.setSceneId(req.getSceneId());
        }
        if (req.getStatus() != null) skill.setStatus(req.getStatus());
        if (req.getSortOrder() != null) skill.setSortOrder(req.getSortOrder());
        Skill saved = skillRepository.save(skill);
        return toDetailMap(saved, resolveSceneNames(Set.of(saved.getSceneId())));
    }

    @Transactional
    public void deleteSkill(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "skill not found"));
        skillRepository.delete(skill);
    }

    @Transactional
    public Map<String, Object> updateSkillStatus(Long id, String status) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "skill not found"));
        skill.setStatus(status);
        Skill saved = skillRepository.save(skill);
        return toDetailMap(saved, resolveSceneNames(Set.of(saved.getSceneId())));
    }

    private Map<String, Object> toDetailMap(Skill skill, Map<Long, String> sceneNameMap) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", skill.getId());
        map.put("name", skill.getName());
        map.put("description", skill.getDescription());
        map.put("icon", skill.getIcon());
        map.put("scene_id", skill.getSceneId());
        map.put("scene_name", skill.getSceneId() == null ? null : sceneNameMap.get(skill.getSceneId()));
        map.put("status", skill.getStatus());
        map.put("sort_order", skill.getSortOrder());
        map.put("created_at", skill.getCreatedAt());
        return map;
    }

    private Map<String, Object> toListMap(Page<Skill> result) {
        List<Skill> skills = result.getContent();
        Set<Long> sceneIds = skills.stream().map(Skill::getSceneId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> sceneNameMap = resolveSceneNames(sceneIds);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Skill skill : skills) {
            list.add(toDetailMap(skill, sceneNameMap));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", result.getTotalElements());
        map.put("page", result.getNumber() + 1);
        map.put("page_size", result.getSize());
        return map;
    }

    // Batch-resolve scene names in a single query (fixes N+1 from per-row lookups).
    private Map<Long, String> resolveSceneNames(Set<Long> sceneIds) {
        Map<Long, String> map = new HashMap<>();
        if (sceneIds.isEmpty()) return map;
        List<Scene> scenes = sceneRepository.findAllById(sceneIds);
        for (Scene s : scenes) {
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
