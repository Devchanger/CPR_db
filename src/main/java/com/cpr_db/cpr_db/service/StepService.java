package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.StepCreateRequest;
import com.cpr_db.cpr_db.dto.StepUpdateRequest;
import com.cpr_db.cpr_db.entity.Skill;
import com.cpr_db.cpr_db.entity.Step;
import com.cpr_db.cpr_db.repository.SkillRepository;
import com.cpr_db.cpr_db.repository.StepRepository;
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
public class StepService {

    private static final int MAX_PAGE_SIZE = 100;

    private final StepRepository stepRepository;
    private final SkillRepository skillRepository;

    public StepService(StepRepository stepRepository, SkillRepository skillRepository) {
        this.stepRepository = stepRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStepList(Long skillId, String status, int page, int pageSize) {
        page = clampPage(page);
        pageSize = clampPageSize(pageSize);
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.ASC, "order"));
        Page<Step> result;
        boolean hasStatus = status != null && !status.isBlank();
        if (skillId != null && hasStatus) {
            result = stepRepository.findBySkillIdAndStatus(skillId, status, pageable);
        } else if (skillId != null) {
            result = stepRepository.findBySkillId(skillId, pageable);
        } else if (hasStatus) {
            result = stepRepository.findByStatus(status, pageable);
        } else {
            result = stepRepository.findAll(pageable);
        }
        return toListMap(result);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStepById(Long id) {
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "step not found"));
        return toDetailMap(step, resolveSkillNames(Set.of(step.getSkillId())));
    }

    @Transactional
    public Map<String, Object> createStep(StepCreateRequest req) {
        String title = req.getTitle() == null ? null : req.getTitle().trim();
        if (title == null || title.isBlank()) {
            throw new BusinessException(400, "title is required");
        }
        if (req.getSkillId() == null) {
            throw new BusinessException(400, "skillId is required");
        }
        if (!skillRepository.existsById(req.getSkillId())) {
            throw new BusinessException(400, "skill not found");
        }
        Step step = new Step();
        step.setTitle(title);
        step.setDescription(req.getDescription());
        step.setSkillId(req.getSkillId());
        step.setStatus(req.getStatus());
        step.setOrder(req.getOrder());
        Step saved = stepRepository.save(step);
        return toDetailMap(saved, resolveSkillNames(Set.of(saved.getSkillId())));
    }

    @Transactional
    public Map<String, Object> updateStep(Long id, StepUpdateRequest req) {
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "step not found"));
        if (req.getTitle() != null) step.setTitle(req.getTitle().trim());
        if (req.getDescription() != null) step.setDescription(req.getDescription());
        if (req.getSkillId() != null) {
            if (req.getSkillId() != 0 && !skillRepository.existsById(req.getSkillId())) {
                throw new BusinessException(400, "skill not found");
            }
            step.setSkillId(req.getSkillId());
        }
        if (req.getStatus() != null) step.setStatus(req.getStatus());
        if (req.getOrder() != null) step.setOrder(req.getOrder());
        Step saved = stepRepository.save(step);
        return toDetailMap(saved, resolveSkillNames(Set.of(saved.getSkillId())));
    }

    @Transactional
    public void deleteStep(Long id) {
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "step not found"));
        stepRepository.delete(step);
    }

    @Transactional
    public Map<String, Object> updateStepStatus(Long id, String status) {
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "step not found"));
        step.setStatus(status);
        Step saved = stepRepository.save(step);
        return toDetailMap(saved, resolveSkillNames(Set.of(saved.getSkillId())));
    }

    @Transactional
    public Map<String, Object> reorderStep(Long id, String direction) {
        if (!"up".equals(direction) && !"down".equals(direction)) {
            throw new BusinessException(400, "direction must be up or down");
        }
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "step not found"));
        if (step.getSkillId() == null) {
            throw new BusinessException(400, "step has no skill_id, cannot reorder");
        }
        List<Step> siblings = stepRepository.findBySkillIdOrderByOrderAsc(step.getSkillId());
        int idx = -1;
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i).getId().equals(step.getId())) {
                idx = i;
                break;
            }
        }
        if ("up".equals(direction)) {
            if (idx <= 0) {
                throw new BusinessException(400, "already at the top");
            }
            Step prev = siblings.get(idx - 1);
            Integer tmp = step.getOrder();
            step.setOrder(prev.getOrder());
            prev.setOrder(tmp);
            stepRepository.save(step);
            stepRepository.save(prev);
        } else {
            if (idx < 0 || idx >= siblings.size() - 1) {
                throw new BusinessException(400, "already at the bottom");
            }
            Step next = siblings.get(idx + 1);
            Integer tmp = step.getOrder();
            step.setOrder(next.getOrder());
            next.setOrder(tmp);
            stepRepository.save(step);
            stepRepository.save(next);
        }
        return toDetailMap(step, resolveSkillNames(Set.of(step.getSkillId())));
    }

    private Map<String, Object> toDetailMap(Step step, Map<Long, String> skillNameMap) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", step.getId());
        map.put("title", step.getTitle());
        map.put("description", step.getDescription());
        map.put("skill_id", step.getSkillId());
        map.put("skill_name", step.getSkillId() == null ? null : skillNameMap.get(step.getSkillId()));
        map.put("status", step.getStatus());
        map.put("order", step.getOrder());
        map.put("created_at", step.getCreatedAt());
        return map;
    }

    private Map<String, Object> toListMap(Page<Step> result) {
        List<Step> steps = result.getContent();
        Set<Long> skillIds = steps.stream().map(Step::getSkillId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        // Batch-resolve skill names in a single query (fixes N+1 from per-row lookups).
        Map<Long, String> skillNameMap = resolveSkillNames(skillIds);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Step step : steps) {
            list.add(toDetailMap(step, skillNameMap));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", result.getTotalElements());
        map.put("page", result.getNumber() + 1);
        map.put("page_size", result.getSize());
        return map;
    }

    // Batch-resolve skill names in a single query (fixes N+1 from per-row lookups).
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
