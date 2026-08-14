package com.cpr_db.cpr_db.validation;

import com.cpr_db.cpr_db.common.ApiResponse;
import com.cpr_db.cpr_db.common.GlobalExceptionHandler;
import com.cpr_db.cpr_db.dto.AdminCreateRequest;
import com.cpr_db.cpr_db.dto.PasswordChangeRequest;
import com.cpr_db.cpr_db.dto.RegisterRequest;
import com.cpr_db.cpr_db.dto.ScoreSubmitRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P0-7 回归（确定性回退）：DTO 强类型 + @Valid 约束，且 GlobalExceptionHandler
 * 将 MethodArgumentNotValidException 映射为 400。无需完整 Web 上下文。
 */
class DtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    @Test
    @DisplayName("P0-7 missing required fields violates @NotBlank/@NotNull constraints")
    void missingFields_invalid() {
        ScoreSubmitRequest req = new ScoreSubmitRequest();
        Set<ConstraintViolation<ScoreSubmitRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty(), "empty request must fail validation");
    }

    @Test
    @DisplayName("P0-7 out-of-range totalScore (250) violates @DecimalMax")
    void outOfRange_invalid() {
        ScoreSubmitRequest req = new ScoreSubmitRequest();
        req.setScene("s");
        req.setSkill("k");
        req.setTotalScore(250f);
        Set<ConstraintViolation<ScoreSubmitRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty(), "totalScore=250 must fail @DecimalMax(100)");
    }

    @Test
    @DisplayName("P0-7 valid request passes validation")
    void validRequest_ok() {
        ScoreSubmitRequest req = new ScoreSubmitRequest();
        req.setScene("s");
        req.setSkill("k");
        req.setTotalScore(85f);
        req.setCompressionDepthAvg(5f);
        req.setCompressionRateAvg(110f);
        req.setErrorCount(0);
        Set<ConstraintViolation<ScoreSubmitRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty(), "valid request should pass: " + violations);
    }

    @Test
    @DisplayName("P0-7 GlobalExceptionHandler maps validation failure to HTTP 400")
    void handler_returns400() {
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(new ScoreSubmitRequest(), "req");
        br.addError(new FieldError("req", "scene", "scene is required"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, br);
        ResponseEntity<ApiResponse<Void>> resp = handler.handleValidationException(ex);
        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    @DisplayName("BE-B-02 register password must satisfy D5 pattern")
    void registerPassword_policy() {
        RegisterRequest shortPwd = new RegisterRequest();
        shortPwd.setUsername("u1");
        shortPwd.setPassword("abc");
        assertFalse(validator.validate(shortPwd).isEmpty(), "short password must fail D5");

        RegisterRequest badChars = new RegisterRequest();
        badChars.setUsername("u1");
        badChars.setPassword("abcdef!");
        assertFalse(validator.validate(badChars).isEmpty(), "password with special chars must fail D5");

        RegisterRequest valid = new RegisterRequest();
        valid.setUsername("u1");
        valid.setPassword("abcdef12");
        assertTrue(validator.validate(valid).isEmpty(), "valid alphanumeric password should pass: "
                + validator.validate(valid));
    }

    @Test
    @DisplayName("BE-B-02 change-password newPassword must satisfy D5 pattern")
    void changePassword_policy() {
        PasswordChangeRequest shortPwd = new PasswordChangeRequest();
        shortPwd.setOldPassword("oldpass");
        shortPwd.setNewPassword("12345");
        assertFalse(validator.validate(shortPwd).isEmpty(), "short newPassword must fail D5");

        PasswordChangeRequest valid = new PasswordChangeRequest();
        valid.setOldPassword("oldpass");
        valid.setNewPassword("abcdef12");
        assertTrue(validator.validate(valid).isEmpty(), "valid newPassword should pass");
    }

    @Test
    @DisplayName("BE-B-02 admin-creation password must satisfy D5 pattern")
    void adminCreatePassword_policy() {
        AdminCreateRequest weak = new AdminCreateRequest();
        weak.setUsername("manager");
        weak.setPassword("a1!");
        weak.setRole("admin");
        assertFalse(validator.validate(weak).isEmpty(), "weak admin password must fail D5");
    }
}
