package com.cpr_db.cpr_db.controller;

import com.cpr_db.cpr_db.common.ApiResponse;
import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.ProfileResponse;
import com.cpr_db.cpr_db.dto.ProfileUpdateRequest;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.repository.UserRepository;
import com.cpr_db.cpr_db.service.UploadService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final UploadService uploadService;

    public ProfileController(UserRepository userRepository, UploadService uploadService) {
        this.userRepository = userRepository;
        this.uploadService = uploadService;
    }

    // ========== GET /api/v1/profile ==========
    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(ApiResponse.success(toResponse(user)));
    }

    // ========== PUT /api/v1/profile ==========
    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request) {

        User user = getCurrentUser(authentication);

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            userRepository.findByPhone(request.getPhone()).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new BusinessException(409, "手机号已被其他用户使用");
                }
            });
        }

        if (request.getStudentId() != null && !request.getStudentId().isBlank()) {
            userRepository.findByStudentId(request.getStudentId()).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new BusinessException(409, "学号已被其他用户使用");
                }
            });
        }

        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getPhone() != null) user.setPhone(request.getPhone().isBlank() ? null : request.getPhone());
        if (request.getStudentId() != null) user.setStudentId(request.getStudentId().isBlank() ? null : request.getStudentId());
        if (request.getClassName() != null) user.setClassName(request.getClassName());

        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(toResponse(user), "更新成功"));
    }

    // ========== POST /api/v1/profile/avatar ==========
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {

        User user = getCurrentUser(authentication);
        // File IO is delegated to UploadService, which sanitises the filename (no path traversal)
        // and enforces extension + MIME whitelists.
        String avatarUrl = uploadService.uploadAvatar(file, user.getId());
        user.setAvatar(avatarUrl);
        userRepository.save(user);

        Map<String, String> result = new HashMap<>();
        result.put("avatar_url", avatarUrl);
        return ResponseEntity.ok(ApiResponse.success(result, "上传成功"));
    }

    // ========== helpers ==========
    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    private ProfileResponse toResponse(User user) {
        ProfileResponse r = new ProfileResponse();
        r.setId(user.getId());
        r.setUsername(user.getUsername());
        r.setRealName(user.getRealName());
        r.setRole(user.getRole());
        r.setAvatar(user.getAvatar());
        r.setGender(user.getGender());
        r.setPhone(user.getPhone());
        r.setStudentId(user.getStudentId());
        r.setClassName(user.getClassName());
        r.setCreatedAt(user.getCreatedAt());
        return r;
    }
}
