package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.entity.Skill;
import com.cpr_db.cpr_db.entity.Step;
import com.cpr_db.cpr_db.repository.SkillRepository;
import com.cpr_db.cpr_db.repository.StepRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StepService {

    private final StepRepository stepRepository;
    private final SkillRepository skillRepository;

    public StepService(StepRepository stepRepository, SkillRepository skillRepository) {
        this.stepRepository = stepRepository;
        this.skillRepository = skillRepository;
    }

    public Map<String, Object> getStepList(Long skillId, String status, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
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

    public Map<String, Object> getStepById(Long id) {
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "step not found"));
        return toDetailMap(step);
    }

    public Map<String, Object> createStep(Map<String, Object> body) {
        String title = body.get("title") == null ? null : body.get("title").toString().trim();
        if (title == null || title.isBlank()) {
            throw new BusinessException(400, "title is required");
        }
        Step step = new Step();
        step.setTitle(title);
        if (body.containsKey("description") && body.get("description") != null) {
            step.setDescription(body.get("description").toString());
        }
        if (body.containsKey("skill_id") && body.get("skill_id") != null) {
            step.setSkillId(toLong(body.get("skill_id")));
        }
        if (body.containsKey("status") && body.get("status") != null) {
            step.setStatus(body.get("status").toString());
        }
        if (body.containsKey("step_order") && body.get("step_order") != null) {
            step.setOrder(toInt(body.get("step_order")));
        }
        Step saved = stepRepository.save(step);
        return toDetailMap(saved);
    }

    public Map<String, Object> updateStep(Long id, Map<String, Object> body) {
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "step not found"));
        if (body.containsKey("title") && body.get("title") != null) {
            step.setTitle(body.get("title").toString().trim());
        }
        if (body.containsKey("description")) {
            step.setDescription(body.get("description") == null ? null : body.get("description").toString());
        }
        if (body.containsKey("skill_id")) {
            step.setSkillId(body.get("skill_id") == null ? null : toLong(body.get("skill_id")));
        }
        if (body.containsKey("status")) {
            step.setStatus(body.get("status") == null ? null : body.get("status").toString());
        }
        if (body.containsKey("step_order")) {
            step.setOrder(body.get("step_order") == null ? null : toInt(body.get("step_order")));
        }
        Step saved = stepRepository.save(step);
        return toDetailMap(saved);
    }

    public void deleteStep(Long id) {
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "step not found"));
        stepRepository.delete(step);
    }

    public Map<String, Object> updateStepStatus(Long id, String status) {
        Step step = stepRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "step not found"));
        step.setStatus(status);
        Step saved = stepRepository.save(step);
        return toDetailMap(saved);
    }

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
        return toDetailMap(step);
    }

    private Map<String, Object> toDetailMap(Step step) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", step.getId());
        map.put("skill_id", step.getSkillId());
        map.put("skill_name", resolveSkillName(step.getSkillId()));
        map.put("title", step.getTitle());
        map.put("description", step.getDescription());
        map.put("step_order", step.getOrder());
        map.put("status", step.getStatus());
        map.put("created_at", step.getCreatedAt());
        return map;
    }

    private Map<String, Object> toListMap(Page<Step> result) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Step step : result.getContent()) {
            list.add(toDetailMap(step));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", result.getTotalElements());
        return map;
    }

    private String resolveSkillName(Long skillId) {
        if (skillId == null) return null;
        return skillRepository.findById(skillId)
                .map(Skill::getName)
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
