package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.entity.Scene;
import com.cpr_db.cpr_db.entity.Skill;
import com.cpr_db.cpr_db.repository.SceneRepository;
import com.cpr_db.cpr_db.repository.SkillRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final SceneRepository sceneRepository;

    public SkillService(SkillRepository skillRepository, SceneRepository sceneRepository) {
        this.skillRepository = skillRepository;
        this.sceneRepository = sceneRepository;
    }

    public Map<String, Object> getSkillList(String keyword, String status, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
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

    public Map<String, Object> getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "skill not found"));
        return toDetailMap(skill);
    }

    public Map<String, Object> createSkill(Map<String, Object> body) {
        String name = body.get("name") == null ? null : body.get("name").toString().trim();
        if (name == null || name.isBlank()) {
            throw new BusinessException(400, "name is required");
        }
        if (skillRepository.existsByName(name)) {
            throw new BusinessException(409, "skill name already exists");
        }
        Skill skill = new Skill();
        skill.setName(name);
        if (body.containsKey("description") && body.get("description") != null) {
            skill.setDescription(body.get("description").toString());
        }
        if (body.containsKey("icon") && body.get("icon") != null) {
            skill.setIcon(body.get("icon").toString());
        }
        if (body.containsKey("scene_id") && body.get("scene_id") != null) {
            skill.setSceneId(toLong(body.get("scene_id")));
        }
        if (body.containsKey("status") && body.get("status") != null) {
            skill.setStatus(body.get("status").toString());
        }
        if (body.containsKey("sort_order") && body.get("sort_order") != null) {
            skill.setSortOrder(toInt(body.get("sort_order")));
        }
        Skill saved = skillRepository.save(skill);
        return toDetailMap(saved);
    }

    public Map<String, Object> updateSkill(Long id, Map<String, Object> body) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "skill not found"));
        if (body.containsKey("name") && body.get("name") != null) {
            String name = body.get("name").toString().trim();
            if (!name.equals(skill.getName()) && skillRepository.existsByName(name)) {
                throw new BusinessException(409, "skill name already exists");
            }
            skill.setName(name);
        }
        if (body.containsKey("description")) {
            skill.setDescription(body.get("description") == null ? null : body.get("description").toString());
        }
        if (body.containsKey("icon")) {
            skill.setIcon(body.get("icon") == null ? null : body.get("icon").toString());
        }
        if (body.containsKey("scene_id")) {
            skill.setSceneId(body.get("scene_id") == null ? null : toLong(body.get("scene_id")));
        }
        if (body.containsKey("status")) {
            skill.setStatus(body.get("status") == null ? null : body.get("status").toString());
        }
        if (body.containsKey("sort_order")) {
            skill.setSortOrder(body.get("sort_order") == null ? null : toInt(body.get("sort_order")));
        }
        Skill saved = skillRepository.save(skill);
        return toDetailMap(saved);
    }

    public void deleteSkill(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "skill not found"));
        skillRepository.delete(skill);
    }

    public Map<String, Object> updateSkillStatus(Long id, String status) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "skill not found"));
        skill.setStatus(status);
        Skill saved = skillRepository.save(skill);
        return toDetailMap(saved);
    }

    private Map<String, Object> toDetailMap(Skill skill) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", skill.getId());
        map.put("name", skill.getName());
        map.put("description", skill.getDescription());
        map.put("icon", skill.getIcon());
        map.put("scene_id", skill.getSceneId());
        map.put("scene_name", resolveSceneName(skill.getSceneId()));
        map.put("status", skill.getStatus());
        map.put("sort_order", skill.getSortOrder());
        map.put("created_at", skill.getCreatedAt());
        return map;
    }

    private Map<String, Object> toListMap(Page<Skill> result) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Skill skill : result.getContent()) {
            list.add(toDetailMap(skill));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", result.getTotalElements());
        return map;
    }

    private String resolveSceneName(Long sceneId) {
        if (sceneId == null) return null;
        return sceneRepository.findById(sceneId)
                .map(Scene::getName)
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
