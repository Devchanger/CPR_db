package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.common.PasswordPolicy;
import com.cpr_db.cpr_db.common.SecurityUtil;
import com.cpr_db.cpr_db.dto.AuthRequest;
import com.cpr_db.cpr_db.dto.AuthResponse;
import com.cpr_db.cpr_db.dto.RegisterRequest;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.repository.UserRepository;
import com.cpr_db.cpr_db.security.JwtTokenUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final LogService logService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenUtil jwtTokenUtil,
                       LogService logService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.logService = logService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!PasswordPolicy.isValid(request.getPassword())) {
            throw new BusinessException(400, PasswordPolicy.MESSAGE);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(400, "username already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        User saved = userRepository.save(user);
        String token = jwtTokenUtil.generateToken(saved.getUsername());
        logAuth("register", saved.getId(), saved.getUsername(), "registered");
        return new AuthResponse(token, jwtTokenUtil.getExpirationMs() + System.currentTimeMillis());
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "invalid username or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "invalid username or password");
        }
        String token = jwtTokenUtil.generateToken(user.getUsername());
        logAuth("login", user.getId(), user.getUsername(), "logged in");
        return new AuthResponse(token, jwtTokenUtil.getExpirationMs() + System.currentTimeMillis());
    }

    // Non-blocking audit log: never let logging failure break the auth flow.
    private void logAuth(String action, Long targetId, String username, String detail) {
        try {
            logService.log(targetId, username, action, "auth", targetId, detail, SecurityUtil.currentIp());
        } catch (Exception ignored) {
            // logging must not break the business flow
        }
    }
}
