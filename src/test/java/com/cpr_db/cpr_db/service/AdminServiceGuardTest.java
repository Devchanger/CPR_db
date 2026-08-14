package com.cpr_db.cpr_db.service;

import com.cpr_db.cpr_db.common.BusinessException;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceGuardTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock LogService logService;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(userRepository, passwordEncoder, logService);
    }

    @Test
    @DisplayName("P0-10 deleting the last super_admin is rejected with 409")
    void deleteLastSuperAdmin_rejected() {
        User victim = new User();
        victim.setId(1L);
        victim.setUsername("victim");
        victim.setRole("super_admin");
        when(userRepository.findById(1L)).thenReturn(Optional.of(victim));
        when(userRepository.countByRole("super_admin")).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.deleteUser(1L, "otherAdmin"));
        assertEquals(409, ex.getCode(), "last super_admin delete must be rejected");
        assertTrue(ex.getMessage().toLowerCase().contains("super admin"), ex.getMessage());
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("P0-10 deleting a super_admin is allowed when more than one remains")
    void deleteWhenMultipleSuperAdmins_allowed() {
        User victim = new User();
        victim.setId(1L);
        victim.setUsername("victim");
        victim.setRole("super_admin");
        when(userRepository.findById(1L)).thenReturn(Optional.of(victim));
        when(userRepository.countByRole("super_admin")).thenReturn(2L);

        assertDoesNotThrow(() -> adminService.deleteUser(1L, "otherAdmin"));
        verify(userRepository).delete(victim);
    }
}
