package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.dto.AuthRequest;
import com.cpr_db.cpr_db.dto.AuthResponse;
import com.cpr_db.cpr_db.dto.RegisterRequest;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.repository.UserRepository;
import com.cpr_db.cpr_db.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        when(jwtTokenUtil.getExpirationMs()).thenReturn(3600000L);
    }

    @Test
    void shouldRegisterNewUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(jwtTokenUtil.generateToken("newuser")).thenReturn("jwt.token.here");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldLoginSuccessfully() {
        AuthRequest request = new AuthRequest();
        request.setUsername("existinguser");
        request.setPassword("correctpassword");

        User user = new User();
        user.setUsername("existinguser");
        user.setPasswordHash("hashedPassword");

        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctpassword", "hashedPassword")).thenReturn(true);
        when(jwtTokenUtil.generateToken("existinguser")).thenReturn("jwt.token.here");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
    }
}
