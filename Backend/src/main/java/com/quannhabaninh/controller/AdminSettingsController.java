package com.quannhabaninh.controller;

import com.quannhabaninh.dto.AdminSettingsDto;
import com.quannhabaninh.dto.AdminSettingsRequest;
import com.quannhabaninh.entity.User;
import com.quannhabaninh.repository.UserRepository;
import com.quannhabaninh.service.AdminSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final AdminSettingsService adminSettingsService;
    private final UserRepository userRepository;

    /**
     * GET /api/admin/settings
     * Lấy cài đặt của admin đang đăng nhập.
     */
    @GetMapping
    public ResponseEntity<AdminSettingsDto> getSettings() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(adminSettingsService.getSettings(userId));
    }

    /**
     * PUT /api/admin/settings
     * Lưu cài đặt thông báo của admin.
     */
    @PutMapping
    public ResponseEntity<AdminSettingsDto> saveSettings(@RequestBody AdminSettingsRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(adminSettingsService.saveSettings(userId, request));
    }

    // ─── helper ─────────────────────────────────────────────────────────────

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return user.getId();
    }
}
