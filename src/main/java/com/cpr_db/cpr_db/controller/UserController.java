package com.cpr_db.cpr_db.controller;

import com.cpr_db.cpr_db.common.ApiResponse;
import com.cpr_db.cpr_db.dto.AdminCreateRequest;
import com.cpr_db.cpr_db.dto.PasswordChangeRequest;
import com.cpr_db.cpr_db.dto.UserInfoResponse;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.service.AdminService;
import com.cpr_db.cpr_db.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final AdminService adminService;

    public UserController(UserService userService, AdminService adminService) {
        this.userService = userService;
        this.adminService = adminService;
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        UserInfoResponse info = new UserInfoResponse(
                user.getId(), user.getUsername(), user.getRole(),
                user.getRealName(), user.getAvatar(), user.getCreatedAt());
        return ResponseEntity.ok(ApiResponse.success(info));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(Authentication authentication,
                                                            @Valid @RequestBody PasswordChangeRequest request) {
        User user = userService.getUserByUsername(authentication.getName());
        userService.changePassword(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "password changed"));
    }

    @GetMapping("/admins")
    @PreAuthorize("hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminList(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAdminList(keyword, page, pageSize)));
    }

    @PostMapping("/admins")
    @PreAuthorize("hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createAdmin(@Valid @RequestBody AdminCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createAdmin(request), "created"));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateRole(@PathVariable Long id,
                                                                       @RequestBody Map<String, Object> body) {
        String role = body.get("role") == null ? null : body.get("role").toString();
        return ResponseEntity.ok(ApiResponse.success(adminService.updateRole(id, role), "updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('super_admin')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id, Authentication authentication) {
        adminService.deleteUser(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "deleted"));
    }
}
