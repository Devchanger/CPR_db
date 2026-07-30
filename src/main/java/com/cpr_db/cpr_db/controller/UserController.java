package com.cpr_db.cpr_db.controller;

import com.cpr_db.cpr_db.common.ApiResponse;
<<<<<<< HEAD
import com.cpr_db.cpr_db.dto.UserInfoResponse;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
=======
import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.AdminCreateRequest;
import com.cpr_db.cpr_db.dto.PasswordChangeRequest;
import com.cpr_db.cpr_db.dto.UserInfoResponse;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.repository.UserRepository;
import com.cpr_db.cpr_db.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
>>>>>>> 193e2be (feat: complete all missing backend API endpoints)

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserRepository userRepository;
<<<<<<< HEAD

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
=======
    private final PasswordEncoder passwordEncoder;
    private final AdminService adminService;

    public UserController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AdminService adminService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminService = adminService;
>>>>>>> 193e2be (feat: complete all missing backend API endpoints)
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
<<<<<<< HEAD
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        UserInfoResponse info = new UserInfoResponse(user.getId(), user.getUsername(), user.getCreatedAt());
        return ResponseEntity.ok(ApiResponse.success(info));
    }
=======
                .orElseThrow(() -> new BusinessException(404, "user not found"));
        UserInfoResponse info = new UserInfoResponse(
                user.getId(), user.getUsername(), user.getRole(),
                user.getRealName(), user.getAvatar(), user.getCreatedAt());
        return ResponseEntity.ok(ApiResponse.success(info));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(Authentication authentication,
                                                            @Valid @RequestBody PasswordChangeRequest request) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "user not found"));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(400, "old password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
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
>>>>>>> 193e2be (feat: complete all missing backend API endpoints)
}
