package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.entity.Scene;
import com.cpr_db.cpr_db.repository.SceneRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SceneService {

    private final SceneRepository sceneRepository;

    public SceneService(SceneRepository sceneRepository) {
        this.sceneRepository = sceneRepository;
    }

    public List<Scene> getAll() {
        return sceneRepository.findAllByOrderBySortOrderAsc();
    }

    public Map<String, Object> getSceneList(String keyword, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
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
        return map;
    }

    public Scene getSceneById(Long id) {
        return sceneRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "scene not found"));
    }

    public Scene createScene(Scene body) {
        if (body.getName() == null || body.getName().isBlank()) {
            throw new BusinessException(400, "name is required");
        }
        return sceneRepository.save(body);
    }

    public Scene updateScene(Long id, Scene body) {
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "scene not found"));
        if (body.getName() != null) scene.setName(body.getName());
        if (body.getDescription() != null) scene.setDescription(body.getDescription());
        if (body.getType() != null) scene.setType(body.getType());
        if (body.getIcon() != null) scene.setIcon(body.getIcon());
        if (body.getSortOrder() != null) scene.setSortOrder(body.getSortOrder());
        if (body.getStatus() != null) scene.setStatus(body.getStatus());
        return sceneRepository.save(scene);
    }

    public void deleteScene(Long id) {
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "scene not found"));
        sceneRepository.delete(scene);
    }

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
}
