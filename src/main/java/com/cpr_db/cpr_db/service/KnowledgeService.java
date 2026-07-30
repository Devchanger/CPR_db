package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.common.SecurityUtil;
import com.cpr_db.cpr_db.entity.Knowledge;
import com.cpr_db.cpr_db.repository.KnowledgeRepository;
import com.cpr_db.cpr_db.service.LogService;
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
public class KnowledgeService {

    private static final int MAX_PAGE_SIZE = 100;

    private final KnowledgeRepository knowledgeRepository;
    private final LogService logService;

    public KnowledgeService(KnowledgeRepository knowledgeRepository, LogService logService) {
        this.knowledgeRepository = knowledgeRepository;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public List<Knowledge> getAll() {
        return knowledgeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Knowledge> getByCategory(String category) {
        return knowledgeRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getKnowledgeList(String category, int page, int pageSize) {
        page = clampPage(page);
        pageSize = clampPageSize(pageSize);
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Knowledge> result;
        if (category != null && !category.isBlank()) {
            result = knowledgeRepository.findByCategory(category, pageable);
        } else {
            result = knowledgeRepository.findAll(pageable);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Knowledge k : result.getContent()) {
            list.add(toDetailMap(k));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", result.getTotalElements());
        map.put("page", result.getNumber() + 1);
        map.put("page_size", result.getSize());
        return map;
    }

    @Transactional(readOnly = true)
    public Knowledge getKnowledgeById(Long id) {
        return knowledgeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "knowledge not found"));
    }

    @Transactional
    public Knowledge createKnowledge(Knowledge body) {
        if (body.getQuestion() == null || body.getQuestion().isBlank()) {
            throw new BusinessException(400, "question is required");
        }
        Knowledge saved = knowledgeRepository.save(body);
        logChange("create_knowledge", saved.getId(),
                "created knowledge question=" + truncate(saved.getQuestion()));
        return saved;
    }

    @Transactional
    public Knowledge updateKnowledge(Long id, Knowledge body) {
        Knowledge knowledge = knowledgeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "knowledge not found"));
        if (body.getQuestion() != null) knowledge.setQuestion(body.getQuestion());
        if (body.getAnswer() != null) knowledge.setAnswer(body.getAnswer());
        if (body.getCategory() != null) knowledge.setCategory(body.getCategory());
        if (body.getTags() != null) knowledge.setTags(body.getTags());
        if (body.getStatus() != null) knowledge.setStatus(body.getStatus());
        Knowledge saved = knowledgeRepository.save(knowledge);
        logChange("update_knowledge", id, "updated knowledge id=" + id);
        return saved;
    }

    @Transactional
    public void deleteKnowledge(Long id) {
        Knowledge knowledge = knowledgeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "knowledge not found"));
        knowledgeRepository.delete(knowledge);
        logChange("delete_knowledge", id, "deleted knowledge id=" + id);
    }

    // Non-blocking audit log: never let logging failure break the main operation.
    private void logChange(String action, Long targetId, String detail) {
        try {
            logService.log(null, SecurityUtil.currentUsername(), action, "knowledge", targetId, detail, SecurityUtil.currentIp());
        } catch (Exception ignored) {
            // logging must not break the business flow
        }
    }

    private Map<String, Object> toDetailMap(Knowledge k) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", k.getId());
        map.put("question", k.getQuestion());
        map.put("answer", k.getAnswer());
        map.put("category", k.getCategory());
        map.put("tags", k.getTags());
        map.put("status", k.getStatus());
        map.put("created_at", k.getCreatedAt());
        return map;
    }

    private static String truncate(String s) {
        if (s == null) return "";
        return s.length() > 50 ? s.substring(0, 50) + "..." : s;
    }

    private int clampPage(int page) {
        return page < 1 ? 1 : page;
    }

    private int clampPageSize(int pageSize) {
        if (pageSize < 1) return 10;
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
