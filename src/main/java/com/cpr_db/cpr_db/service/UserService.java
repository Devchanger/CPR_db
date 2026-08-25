package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.common.PasswordPolicy;
import com.cpr_db.cpr_db.dto.PasswordChangeRequest;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(404, "user not found"));
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "user not found"));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(400, "old password is incorrect");
        }
        if (!PasswordPolicy.isValid(request.getNewPassword())) {
            throw new BusinessException(400, PasswordPolicy.MESSAGE);
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }
}
