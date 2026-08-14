package com.cpr_db.cpr_db.structural;

import com.cpr_db.cpr_db.controller.ScoreController;
import com.cpr_db.cpr_db.controller.UploadController;
import com.cpr_db.cpr_db.controller.UserController;
import com.cpr_db.cpr_db.service.ScoreService;
import com.cpr_db.cpr_db.service.UploadService;
import com.cpr_db.cpr_db.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.Repository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P0-2 / P0-3 / P0-4 回归：Controller 不得直连 Repository、不得直接持有存储/密码编码器，
 * 业务逻辑应下沉到 Service 层。通过反射校验字段结构。
 */
class ControllerLayeringTest {

    @Test
    @DisplayName("P0-2 ScoreController must not inject any Repository; delegates to Service")
    void scoreController_hasNoRepository() {
        Field[] fields = ScoreController.class.getDeclaredFields();
        for (Field f : fields) {
            assertFalse(Repository.class.isAssignableFrom(f.getType()),
                    "ScoreController must not depend on Repository: " + f.getType());
        }
        assertTrue(hasFieldOfType(ScoreController.class, ScoreService.class), "missing ScoreService field");
        assertTrue(hasFieldOfType(ScoreController.class, UserService.class), "missing UserService field");
    }

    @Test
    @DisplayName("P0-4 UploadController delegates to UploadService and holds no storage/Repository")
    void uploadController_delegatesToService() {
        Field[] fields = UploadController.class.getDeclaredFields();
        long uploadServiceFields = 0;
        for (Field f : fields) {
            Class<?> t = f.getType();
            assertFalse(Repository.class.isAssignableFrom(t), "UploadController must not depend on Repository");
            assertFalse(MultipartFile.class.isAssignableFrom(t), "UploadController must not hold MultipartFile");
            assertFalse(Path.class.isAssignableFrom(t), "UploadController must not hold Path");
            assertFalse(File.class.isAssignableFrom(t), "UploadController must not hold File");
            if (UploadService.class.equals(t)) {
                uploadServiceFields++;
            }
        }
        assertEquals(1, uploadServiceFields, "UploadController should depend on exactly one UploadService");
    }

    @Test
    @DisplayName("P0-3 UserController business logic moved to UserService (no PasswordEncoder held)")
    void userController_noPasswordEncoder() {
        Field[] fields = UserController.class.getDeclaredFields();
        for (Field f : fields) {
            assertFalse(PasswordEncoder.class.isAssignableFrom(f.getType()),
                    "UserController must not hold PasswordEncoder (move to UserService): " + f.getType());
        }
        assertTrue(hasFieldOfType(UserController.class, UserService.class), "missing UserService field");
        assertEquals(1, UserController.class.getDeclaredFields().length,
                "BE-B-05: UserController should only hold UserService after admin-management endpoints are removed");
    }

    private boolean hasFieldOfType(Class<?> c, Class<?> type) {
        for (Field f : c.getDeclaredFields()) {
            if (type.equals(f.getType())) {
                return true;
            }
        }
        return false;
    }
}
