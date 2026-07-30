package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
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

    public Knowledge getKnowledgeById(Long id) {
        return knowledgeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "knowledge not found"));
    }

    public Knowledge createKnowledge(Knowledge body) {
        if (body.getQuestion() == null || body.getQuestion().isBlank()) {
            throw new BusinessException(400, "question is required");
        }
        return knowledgeRepository.save(body);
    }

    public Knowledge updateKnowledge(Long id, Knowledge body) {
        Knowledge knowledge = knowledgeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "knowledge not found"));
        if (body.getQuestion() != null) knowledge.setQuestion(body.getQuestion());
        if (body.getAnswer() != null) knowledge.setAnswer(body.getAnswer());
        if (body.getCategory() != null) knowledge.setCategory(body.getCategory());
        if (body.getTags() != null) knowledge.setTags(body.getTags());
        if (body.getStatus() != null) knowledge.setStatus(body.getStatus());
        return knowledgeRepository.save(knowledge);
    }

    public void deleteKnowledge(Long id) {
        Knowledge knowledge = knowledgeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "knowledge not found"));
        knowledgeRepository.delete(knowledge);
    }
}
