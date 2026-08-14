package com.cpr_db.cpr_db.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Small helper to read the current authenticated principal and client IP from
 * the security context / request context. Used for non-blocking operation logging.
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return "unknown";
        String name = auth.getName();
        return (name == null || name.isBlank()) ? "unknown" : name;
    }

    public static String currentIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return "unknown";
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getRemoteAddr();
            return ip == null ? "unknown" : ip;
        } catch (Exception e) {
            return "unknown";
        }
    }
}
