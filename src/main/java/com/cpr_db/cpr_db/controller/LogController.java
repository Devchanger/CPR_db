package com.cpr_db.cpr_db.controller;

import com.cpr_db.cpr_db.common.ApiResponse;
import com.cpr_db.cpr_db.service.LogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLogs(
            @RequestParam(name = "adminId", required = false) Long adminId,
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "targetType", required = false) String targetType,
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(
                logService.getLogs(adminId, action, targetType, startDate, endDate, page, pageSize)));
    }
}
