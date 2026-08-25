package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.SceneCreateRequest;
import com.cpr_db.cpr_db.dto.SceneUpdateRequest;
import com.cpr_db.cpr_db.entity.Scene;
import com.cpr_db.cpr_db.repository.SceneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SceneService {

    private final SceneRepository sceneRepository;

    public SceneService(SceneRepository sceneRepository) {
        this.sceneRepository = sceneRepository;
    }

    @Transactional(readOnly = true)
    public List<Scene> getAll() {
        return sceneRepository.findAllByOrderBySortOrderAsc();
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

}
