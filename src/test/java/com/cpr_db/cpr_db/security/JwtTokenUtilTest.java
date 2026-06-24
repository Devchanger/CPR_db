package com.cpr_db.cpr_db.security;

import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;

    @BeforeEach
    void setUp() {
        String testSecret = "TestSecretKeyForJwtTokenAtLeast32Characters!!";
        jwtTokenUtil = new JwtTokenUtil();
        setField(jwtTokenUtil, "secret", testSecret);
        setField(jwtTokenUtil, "expirationMs", 3600000L);
        setField(jwtTokenUtil, "key", Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void shouldGenerateAndParseToken() {
        String token = jwtTokenUtil.generateToken("testuser");
        assertNotNull(token);
        assertEquals("testuser", jwtTokenUtil.getUsernameFromToken(token));
    }

    @Test
    void shouldValidateValidToken() {
        String token = jwtTokenUtil.generateToken("testuser");
        assertTrue(jwtTokenUtil.validateToken(token));
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtTokenUtil expiredUtil = new JwtTokenUtil();
        String testSecret = "TestSecretKeyForJwtTokenAtLeast32Characters!!";
        setField(expiredUtil, "secret", testSecret);
        setField(expiredUtil, "expirationMs", -1000L);
        setField(expiredUtil, "key", Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8)));
        String token = expiredUtil.generateToken("testuser");
        assertFalse(expiredUtil.validateToken(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(jwtTokenUtil.validateToken("invalid.token.here"));
    }

    @Test
    void shouldRejectEmptyToken() {
        assertFalse(jwtTokenUtil.validateToken(""));
    }

    @Test
    void shouldRejectNullToken() {
        assertFalse(jwtTokenUtil.validateToken(null));
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Class<?> clazz = target.getClass();
            while (clazz != null) {
                try {
                    java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(target, value);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            throw new NoSuchFieldException(fieldName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
