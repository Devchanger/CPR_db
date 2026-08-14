package com.cpr_db.cpr_db.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P0-6 回归：全项目零 @Transactional -> 各 Service 写方法已加 @Transactional，
 * 读方法 readOnly=true。通过反射校验已知高风险方法的注解，避免回归。
 */
class ServiceTransactionalTest {

    private record Expect(String clazz, String method, boolean readOnly) {}

    @Test
    @DisplayName("P0-6 write methods are @Transactional and read methods are readOnly=true")
    void transactionalAnnotationsPresent() throws Exception {
        Expect[] expects = {
            new Expect("ScoreService", "saveScore", false),
            new Expect("ScoreService", "deleteScore", false),
            new Expect("ScoreService", "getUserScores", true),
            new Expect("ScoreService", "getLatestScore", true),
            new Expect("ScoreService", "getScoreById", true),
            new Expect("ScoreService", "getStats", true),
            new Expect("ScoreService", "getAllScores", true),
            new Expect("AdminService", "createAdmin", false),
            new Expect("AdminService", "updateRole", false),
            new Expect("AdminService", "deleteUser", false),
            new Expect("AdminService", "getAdminList", true),
            new Expect("UserService", "changePassword", false),
            new Expect("UserService", "getUserByUsername", true),
            new Expect("StepService", "createStep", false),
            new Expect("StepService", "updateStep", false),
            new Expect("StepService", "deleteStep", false),
            new Expect("StepService", "updateStepStatus", false),
            new Expect("StepService", "reorderStep", false),
            new Expect("StepService", "getStepList", true),
            new Expect("StepService", "getStepById", true)
        };
        for (Expect e : expects) {
            Class<?> c = Class.forName("com.cpr_db.cpr_db.service." + e.clazz());
            Method m = findMethod(c, e.method());
            assertNotNull(m, e.clazz() + "." + e.method() + " not found");
            Transactional t = m.getAnnotation(Transactional.class);
            assertNotNull(t, e.clazz() + "." + e.method() + " must be @Transactional (P0-6)");
            assertEquals(e.readOnly(), t.readOnly(),
                    e.clazz() + "." + e.method() + " readOnly mismatch");
        }
    }

    private Method findMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }
}
