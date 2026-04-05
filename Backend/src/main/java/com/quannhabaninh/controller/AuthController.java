package com.quannhabaninh.controller;

import com.quannhabaninh.dto.AssignCardRequest;
import com.quannhabaninh.dto.AuthResponse;
import com.quannhabaninh.dto.GoogleLoginRequest;
import com.quannhabaninh.dto.LoginRequest;
import com.quannhabaninh.dto.RefreshTokenRequest;
import com.quannhabaninh.dto.RegisterRequest;
import com.quannhabaninh.dto.UpdateRoleRequest;
import com.quannhabaninh.dto.UserResponse;
import com.quannhabaninh.entity.RefreshToken;
import com.quannhabaninh.entity.User;
import com.quannhabaninh.security.JwtTokenProvider;
import com.quannhabaninh.service.AuthService;
import com.quannhabaninh.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")

@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            logger.info("Registration attempt for username: {}, email: {}", request.getUsername(), request.getEmail());

            // Validate required fields
            if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Full name is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Phone number is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            AuthResponse response = authService.register(request);
            logger.info("Registration successful for username: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Registration failed: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Unexpected error during registration: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            logger.info("Login attempt for: {}", request.getEmailOrPhone());
            AuthResponse response = authService.login(request);
            logger.info("Login successful for: {}", request.getEmailOrPhone());
            return ResponseEntity.ok(response);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            logger.warn("Login failed - bad credentials for: {}", request.getEmailOrPhone());
            Map<String, String> error = new HashMap<>();
            error.put("message", "Sai mật khẩu. Vui lòng kiểm tra lại.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            logger.warn("Login failed - user not found: {}", request.getEmailOrPhone());
            Map<String, String> error = new HashMap<>();
            error.put("message", "Tài khoản không tồn tại. Vui lòng đăng ký trước.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (org.springframework.security.authentication.DisabledException e) {
            logger.warn("Login failed - account disabled for: {}", request.getEmailOrPhone());
            Map<String, String> error = new HashMap<>();
            error.put("message", "Tài khoản đã bị vô hiệu hóa.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            logger.error("Login failed - unexpected error for {}: {}", request.getEmailOrPhone(), e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Đăng nhập thất bại: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        try {
            logger.info("Google login attempt for email: {}", request.getEmail());
            AuthResponse response = authService.googleLogin(request);
            logger.info("Google login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Google login failed for {}: {}", request.getEmail(), e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Google login failed - unexpected error for {}: {}", request.getEmail(), e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Đăng nhập Google thất bại: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser() {
        try {
            User user = authService.getCurrentUser();
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
            response.put("phoneNumber", user.getPhoneNumber());
            response.put("roles", user.getRoles());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // JWT is stateless, logout is handled on client side by removing token
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String requestRefreshToken = request.getRefreshToken();

            return refreshTokenService.findByToken(requestRefreshToken)
                    .map(refreshTokenService::verifyExpiration)
                    .map(RefreshToken::getUser)
                    .map(user -> {
                        String newAccessToken = tokenProvider.generateTokenFromUsername(user.getUsername());

                        Map<String, String> response = new HashMap<>();
                        response.put("accessToken", newAccessToken);
                        response.put("refreshToken", requestRefreshToken);
                        response.put("tokenType", "Bearer");

                        return ResponseEntity.ok(response);
                    })
                    .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    /**
     * Admin endpoint: Get all users
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserResponse> users = authService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch users");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Admin endpoint: Get user by ID
     */
    @GetMapping("/admin/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = authService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Admin endpoint: Update user role
     */
    @PutMapping("/admin/users/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateUserRole(@Valid @RequestBody UpdateRoleRequest request) {
        try {
            User updatedUser = authService.updateUserRole(request.getUserId(), request.getRoles());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User role updated successfully");
            response.put("userId", updatedUser.getId());
            response.put("username", updatedUser.getUsername());
            response.put("roles", updatedUser.getRoles());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Admin endpoint: Assign membership card to user
     */
    @PutMapping("/admin/users/{userId}/assign-card")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> assignCardToUser(
            @PathVariable Long userId,
            @Valid @RequestBody AssignCardRequest request) {
        try {
            UserResponse user = authService.assignCardToUser(userId, request.getMembershipCardId());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Admin endpoint: Toggle user enabled/disabled (ban/unban)
     */
    @PatchMapping("/admin/users/{userId}/toggle-status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId) {
        try {
            UserResponse user = authService.toggleUserStatus(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", Boolean.TRUE.equals(user.getEnabled()) ? "Đã kích hoạt tài khoản" : "Đã vô hiệu hóa tài khoản");
            response.put("user", user);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }


    // ===================== FORGOT PASSWORD / OTP =====================

    /**
     * Bước 1: Nhận email, gửi OTP về Gmail
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody com.quannhabaninh.dto.ForgotPasswordRequest request) {
        try {
            authService.sendForgotPasswordOtp(request.getEmail());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Mã OTP đã được gửi tới email của bạn.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("=== FORGOT PASSWORD ERROR === {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("=== FORGOT PASSWORD UNEXPECTED ERROR === {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Không thể gửi email. Vui lòng thử lại sau.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Bước 2: Xác thực OTP
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody com.quannhabaninh.dto.VerifyOtpRequest request) {
        try {
            authService.verifyOtp(request.getEmail(), request.getOtp());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mã OTP hợp lệ.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Bước 3: Đặt lại mật khẩu mới
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody com.quannhabaninh.dto.ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Mật khẩu đã được đặt lại thành công.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("Error resetting password: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Đặt lại mật khẩu thất bại. Vui lòng thử lại.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

