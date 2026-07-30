package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.entity.Log;
import com.cpr_db.cpr_db.repository.LogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public Map<String, Object> getLogs(Long adminId, String action, String targetType,
                                       String startDate, String endDate, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        LocalDateTime start = parseDateTime(startDate);
        LocalDateTime end = parseDateTime(endDate);

        Page<Log> result;
        boolean hasAdminId = adminId != null;
        boolean hasAction = action != null && !action.isBlank();
        boolean hasTargetType = targetType != null && !targetType.isBlank();
        boolean hasDateRange = start != null && end != null;

        if (hasAdminId && hasDateRange) {
            result = logRepository.findByAdminIdAndCreatedAtBetween(adminId, start, end, pageable);
        } else if (hasDateRange) {
            result = logRepository.findByCreatedAtBetween(start, end, pageable);
        } else if (hasAdminId) {
            result = logRepository.findByAdminId(adminId, pageable);
        } else if (hasAction) {
            result = logRepository.findByAction(action, pageable);
        } else if (hasTargetType) {
            result = logRepository.findByTargetType(targetType, pageable);
        } else {
            result = logRepository.findAll(pageable);
        }
        return toListMap(result);
    }

    public void log(Long adminId, String adminUsername, String action, String targetType,
                    Long targetId, String detail, String ip) {
        Log log = new Log();
        log.setAdminId(adminId);
        log.setAdminUsername(adminUsername);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setIp(ip);
        logRepository.save(log);
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDateTime.parse(dateStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Map<String, Object> toDetailMap(Log log) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", log.getId());
        map.put("admin_id", log.getAdminId());
        map.put("admin_username", log.getAdminUsername());
        map.put("action", log.getAction());
        map.put("target_type", log.getTargetType());
        map.put("target_id", log.getTargetId());
        map.put("detail", log.getDetail());
        map.put("ip", log.getIp());
        map.put("created_at", log.getCreatedAt());
        return map;
    }

    private Map<String, Object> toListMap(Page<Log> result) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Log log : result.getContent()) {
            list.add(toDetailMap(log));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", result.getTotalElements());
        return map;
    }
}
