package com.cpr_db.cpr_db.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CorsConfigTest {

    @Mock JwtAuthenticationFilter jwtAuthenticationFilter;
    @Mock CustomUserDetailsService customUserDetailsService;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(jwtAuthenticationFilter, customUserDetailsService);
    }

    @Test
    @DisplayName("P0-1 explicit origins: no wildcard and credentials enabled")
    void cors_explicitOrigins_noWildcard_credentialsEnabled() {
        ReflectionTestUtils.setField(securityConfig, "allowedOriginsProperty",
                "http://localhost:3000,http://localhost:5173");
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration cfg = source.getCorsConfiguration(new MockHttpServletRequest());
        assertNotNull(cfg, "CORS configuration should be present");
        List<String> origins = cfg.getAllowedOrigins();
        assertNotNull(origins, "allowedOrigins should be configured");
        assertFalse(origins.contains("*"), "CORS must not allow wildcard origin (P0-1)");
        assertTrue(cfg.getAllowCredentials(), "credentials must be enabled when no wildcard (P0-1)");
    }

    @Test
    @DisplayName("P0-1 wildcard origin disables credentials per CORS spec")
    void cors_wildcardOrigin_disablesCredentials() {
        ReflectionTestUtils.setField(securityConfig, "allowedOriginsProperty", "*");
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration cfg = source.getCorsConfiguration(new MockHttpServletRequest());
        List<String> origins = cfg.getAllowedOrigins();
        assertTrue(origins.contains("*"), "wildcard origin should be allowed");
        assertFalse(cfg.getAllowCredentials(), "credentials must be disabled with wildcard (P0-1)");
    }

    @Test
    @DisplayName("P0-1 allowed-origins externalized via @Value config key")
    void cors_externalizedViaValue() throws Exception {
        Field f = SecurityConfig.class.getDeclaredField("allowedOriginsProperty");
        Value v = f.getAnnotation(Value.class);
        assertNotNull(v, "cors allowed-origins must be externalized via @Value (P0-1)");
        assertTrue(v.value().contains("cpr.cors.allowed-origins"),
                "must read ${cpr.cors.allowed-origins}: " + v.value());
    }
}
