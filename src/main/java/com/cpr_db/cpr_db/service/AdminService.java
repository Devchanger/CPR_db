package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.common.SecurityUtil;
import com.cpr_db.cpr_db.dto.AdminCreateRequest;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.repository.UserRepository;
import com.cpr_db.cpr_db.service.LogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class AdminService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ADMIN_ROLES = Set.of("admin");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogService logService;

    public AdminService(UserRepository userRepository, PasswordEncoder passwordEncoder, LogService logService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminList(String keyword, int page, int pageSize) {
        page = clampPage(page);
        pageSize = clampPageSize(pageSize);
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> result;
        if (keyword != null && !keyword.isBlank()) {
            result = userRepository.findByRoleInAndUsernameContainingIgnoreCase(ADMIN_ROLES, keyword, pageable);
        } else {
            result = userRepository.findByRoleIn(ADMIN_ROLES, pageable);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (User user : result.getContent()) {
            list.add(toDetailMap(user));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", result.getTotalElements());
        map.put("page", result.getNumber() + 1);
        map.put("page_size", result.getSize());
        return map;
    }

    @Transactional
    public Map<String, Object> createAdmin(AdminCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(409, "username already exists");
        }
        String role = request.getRole();
        if (!"admin".equals(role)) {
            throw new BusinessException(400, "role must be admin");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        User saved = userRepository.save(user);
        logChange("create_user", saved.getId(),
                "created user username=" + saved.getUsername() + " role=" + saved.getRole());
        return toDetailMap(saved);
    }

    @Transactional
    public Map<String, Object> updateRole(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "user not found"));
        if (!"admin".equals(role) && !"student".equals(role)) {
            throw new BusinessException(400, "role must be admin or student");
        }
        user.setRole(role);
        User saved = userRepository.save(user);
        logChange("update_user_role", id, "updated role to " + role + " for user id=" + id);
        return toDetailMap(saved);
    }

    @Transactional
    public void deleteUser(Long id, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "user not found"));
        if (user.getUsername().equals(currentUsername)) {
            throw new BusinessException(400, "cannot delete current user");
        }
        // Guard: never delete the last remaining admin (BE-B-01, former P0-10 guard).
        if ("admin".equals(user.getRole())
                && userRepository.countByRole("admin") <= 1) {
            throw new BusinessException(409, "cannot delete the last admin");
        }
        userRepository.delete(user);
        logChange("delete_user", id, "deleted user id=" + id + " username=" + user.getUsername());
    }

    // Non-blocking audit log: never let logging failure break the main operation.
    private void logChange(String action, Long targetId, String detail) {
        try {
            String actor = SecurityUtil.currentUsername();
            Optional<User> actorUser = userRepository.findByUsername(actor);
            Long adminId = actorUser.map(User::getId).orElse(null);
            logService.log(adminId, actor, action, "user", targetId, detail, SecurityUtil.currentIp());
        } catch (Exception ignored) {
            // logging must not break the business flow
        }
    }

    private Map<String, Object> toDetailMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("real_name", user.getRealName());
        map.put("role", user.getRole());
        map.put("avatar", user.getAvatar());
        map.put("created_at", user.getCreatedAt());
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
