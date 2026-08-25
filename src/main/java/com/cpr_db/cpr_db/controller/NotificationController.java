package com.cpr_db.cpr_db.controller;

import com.cpr_db.cpr_db.common.ApiResponse;
import com.cpr_db.cpr_db.dto.NotificationCreateRequest;
import com.cpr_db.cpr_db.dto.NotificationUpdateRequest;
import com.cpr_db.cpr_db.entity.Notification;
import com.cpr_db.cpr_db.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> list(
            @RequestParam(name = "status", required = false) String status,
            Authentication authentication) {
        // S2: students only ever see published notices; admins may filter all three states.
        String effectiveStatus = isAdmin(authentication) ? status : "published";
        return ResponseEntity.ok(ApiResponse.success(notificationService.list(effectiveStatus)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Notification>> getById(@PathVariable Long id, Authentication authentication) {
        Notification notification = notificationService.getById(id);
        if (!isAdmin(authentication) && !"published".equals(notification.getStatus())) {
            return ResponseEntity.status(404).body(ApiResponse.fail(404, "notification not found"));
        }
        return ResponseEntity.ok(ApiResponse.success(notification));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<ApiResponse<Notification>> create(@Valid @RequestBody NotificationCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.create(req), "created"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<ApiResponse<Notification>> update(@PathVariable Long id,
                                                            @Valid @RequestBody NotificationUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.update(id, req), "updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "deleted"));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("admin"::equals);
    }
}
