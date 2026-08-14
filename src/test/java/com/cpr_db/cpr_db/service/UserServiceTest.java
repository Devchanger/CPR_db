package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.dto.PasswordChangeRequest;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void changePassword_clearsMustChangePasswordFlag() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPasswordHash("oldHash");
        user.setMustChangePassword(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldpass", "oldHash")).thenReturn(true);
        when(passwordEncoder.encode("newpass1")).thenReturn("newHash");

        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setOldPassword("oldpass");
        request.setNewPassword("newpass1");

        userService.changePassword(1L, request);

        assertFalse(user.isMustChangePassword(), "changing the initial password must clear the flag (BE-B-06)");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_rejectsWeakNewPassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPasswordHash("oldHash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldpass", "oldHash")).thenReturn(true);

        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setOldPassword("oldpass");
        request.setNewPassword("abc!");

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.changePassword(1L, request));
        assertEquals(400, ex.getCode());
        verify(userRepository, never()).save(any(User.class));
    }
}
