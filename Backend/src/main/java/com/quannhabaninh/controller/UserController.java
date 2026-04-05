package com.quannhabaninh.controller;

import com.quannhabaninh.dto.ChangePasswordRequest;
import com.quannhabaninh.dto.UpdateProfileRequest;
import com.quannhabaninh.dto.UserResponse;
import com.quannhabaninh.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")

@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users/profile - Get current user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile() {
        return ResponseEntity.ok(userService.getProfile());
    }

    /**
     * PUT /api/users/profile - Update current user profile info
     */
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    /**
     * PUT /api/users/change-password - Change current user password
     */
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(request);
            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
