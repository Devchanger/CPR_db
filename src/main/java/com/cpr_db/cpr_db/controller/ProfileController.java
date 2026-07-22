package com.cpr_db.cpr_db.controller;

import com.cpr_db.cpr_db.common.ApiResponse;
import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.ProfileResponse;
import com.cpr_db.cpr_db.dto.ProfileUpdateRequest;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final UserRepository userRepository;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        // 手机号唯一性校验（排除自身）
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            userRepository.findByPhone(request.getPhone()).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new BusinessException(409, "手机号已被其他用户使用");
                }
            });
        }

        // 学号唯一性校验（排除自身）
        if (request.getStudentId() != null && !request.getStudentId().isBlank()) {
            userRepository.findByStudentId(request.getStudentId()).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new BusinessException(409, "学号已被其他用户使用");
                }
            });
        }

        // 部分更新：只更新非 null 字段
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
            @RequestParam("file") MultipartFile file) throws IOException {

        // 校验文件类型
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException(400, "不支持的文件格式，仅支持 jpg/png/webp");
        }

        // 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(400, "文件大小不能超过 2MB");
        }

        User user = getCurrentUser(authentication);

        // 保存到 uploads/avatars/ 目录
        Path uploadsDir = Paths.get(System.getProperty("user.dir"), "uploads", "avatars");
        Files.createDirectories(uploadsDir);

        String filename = user.getId() + "_" + System.currentTimeMillis() + "." + ext;
        Path filePath = uploadsDir.resolve(filename);
        Files.write(filePath, file.getBytes());

        // 更新用户头像字段
        String avatarUrl = "/uploads/avatars/" + filename;
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
