package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.AdminCreateRequest;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private static final Set<String> ADMIN_ROLES = Set.of("admin", "super_admin");

    public Map<String, Object> getAdminList(String keyword, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> result;
        if (keyword != null && !keyword.isBlank()) {
            result = userRepository.findByRoleInAndUsernameContainingIgnoreCase(ADMIN_ROLES, keyword, pageable);
        } else {
            result = userRepository.findByRoleIn(ADMIN_ROLES, pageable);
        }
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (User user : result.getContent()) {
            list.add(toDetailMap(user));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", result.getTotalElements());
        return map;
    }

    public Map<String, Object> createAdmin(AdminCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(409, "username already exists");
        }
        String role = request.getRole();
        if (!"admin".equals(role) && !"super_admin".equals(role)) {
            throw new BusinessException(400, "role must be admin or super_admin");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        User saved = userRepository.save(user);
        return toDetailMap(saved);
    }

    public Map<String, Object> updateRole(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "user not found"));
        if (!"admin".equals(role) && !"super_admin".equals(role) && !"student".equals(role)) {
            throw new BusinessException(400, "role must be admin, super_admin or student");
        }
        user.setRole(role);
        User saved = userRepository.save(user);
        return toDetailMap(saved);
    }

    public void deleteUser(Long id, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "user not found"));
        if (user.getUsername().equals(currentUsername)) {
            throw new BusinessException(400, "cannot delete current user");
        }
        userRepository.delete(user);
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
}
