package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.entity.Knowledge;
import com.cpr_db.cpr_db.repository.KnowledgeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeService {

    private final KnowledgeRepository knowledgeRepository;

    public KnowledgeService(KnowledgeRepository knowledgeRepository) {
        this.knowledgeRepository = knowledgeRepository;
    }

    public List<Knowledge> getAll() {
        return knowledgeRepository.findAll();
    }

    public List<Knowledge> getByCategory(String category) {
        return knowledgeRepository.findByCategory(category);
    }
}
