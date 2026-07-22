package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.entity.Scene;
import com.cpr_db.cpr_db.repository.SceneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SceneService {

    private final SceneRepository sceneRepository;

    public SceneService(SceneRepository sceneRepository) {
        this.sceneRepository = sceneRepository;
    }

    public List<Scene> getAll() {
        return sceneRepository.findAllByOrderBySortOrderAsc();
    }
}
