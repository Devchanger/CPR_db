package com.cpr_db.cpr_db.controller;

import com.cpr_db.cpr_db.common.ApiResponse;
import com.cpr_db.cpr_db.dto.PasswordChangeRequest;
import com.cpr_db.cpr_db.dto.UserInfoResponse;
import com.cpr_db.cpr_db.entity.User;
import com.cpr_db.cpr_db.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        UserInfoResponse info = new UserInfoResponse(
                user.getId(), user.getUsername(), user.getRole(),
                user.getRealName(), user.getAvatar(), user.getCreatedAt());
        return ResponseEntity.ok(ApiResponse.success(info));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(Authentication authentication,
                                                            @Valid @RequestBody PasswordChangeRequest request) {
        User user = userService.getUserByUsername(authentication.getName());
        userService.changePassword(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "password changed"));
    }

}
