package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.SceneCreateRequest;
import com.cpr_db.cpr_db.dto.SceneUpdateRequest;
import com.cpr_db.cpr_db.entity.Scene;
import com.cpr_db.cpr_db.repository.SceneRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SceneService {

    private static final int MAX_PAGE_SIZE = 100;

    private final SceneRepository sceneRepository;

    public SceneService(SceneRepository sceneRepository) {
        this.sceneRepository = sceneRepository;
    }

    @Transactional(readOnly = true)
    public List<Scene> getAll() {
        return sceneRepository.findAllByOrderBySortOrderAsc();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSceneList(String keyword, int page, int pageSize) {
        page = clampPage(page);
        pageSize = clampPageSize(pageSize);
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "sortOrder"));
        Page<Scene> result;
        if (keyword != null && !keyword.isBlank()) {
            result = sceneRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            result = sceneRepository.findAll(pageable);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Scene scene : result.getContent()) {
            list.add(toDetailMap(scene));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", result.getTotalElements());
        map.put("page", result.getNumber() + 1);
        map.put("page_size", result.getSize());
        return map;
    }

    @Transactional(readOnly = true)
    public Scene getSceneById(Long id) {
        return sceneRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "scene not found"));
    }

    @Transactional
    public Scene createScene(SceneCreateRequest req) {
        String name = req.getName() == null ? null : req.getName().trim();
        if (name == null || name.isBlank()) {
            throw new BusinessException(400, "name is required");
        }
        Scene scene = new Scene();
        scene.setName(name);
        scene.setDescription(req.getDescription());
        scene.setType(req.getType());
        scene.setIcon(req.getIcon());
        scene.setSortOrder(req.getSortOrder());
        scene.setStatus(req.getStatus());
        return sceneRepository.save(scene);
    }

    @Transactional
    public Scene updateScene(Long id, SceneUpdateRequest req) {
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "scene not found"));
        if (req.getName() != null) scene.setName(req.getName().trim());
        if (req.getDescription() != null) scene.setDescription(req.getDescription());
        if (req.getType() != null) scene.setType(req.getType());
        if (req.getIcon() != null) scene.setIcon(req.getIcon());
        if (req.getSortOrder() != null) scene.setSortOrder(req.getSortOrder());
        if (req.getStatus() != null) scene.setStatus(req.getStatus());
        return sceneRepository.save(scene);
    }

    @Transactional
    public void deleteScene(Long id) {
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "scene not found"));
        sceneRepository.delete(scene);
    }

    @Transactional
    public Scene updateSceneStatus(Long id, String status) {
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "scene not found"));
        scene.setStatus(status);
        return sceneRepository.save(scene);
    }

    private Map<String, Object> toDetailMap(Scene scene) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", scene.getId());
        map.put("name", scene.getName());
        map.put("description", scene.getDescription());
        map.put("type", scene.getType());
        map.put("icon", scene.getIcon());
        map.put("sort_order", scene.getSortOrder());
        map.put("status", scene.getStatus());
        map.put("created_at", scene.getCreatedAt());
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
