package com.quannhabaninh.service;

import com.quannhabaninh.dto.AdminSettingsDto;
import com.quannhabaninh.dto.AdminSettingsRequest;
import com.quannhabaninh.entity.AdminSettings;
import com.quannhabaninh.repository.AdminSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSettingsService {

    private final AdminSettingsRepository adminSettingsRepository;

    /**
     * Lấy cài đặt theo userId. Nếu chưa có thì tạo default.
     */
    public AdminSettingsDto getSettings(Long userId) {
        AdminSettings settings = adminSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        return toDto(settings);
    }

    /**
     * Lưu cài đặt thông báo.
     */
    public AdminSettingsDto saveSettings(Long userId, AdminSettingsRequest request) {
        AdminSettings settings = adminSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        if (request.getNewOrder() != null)    settings.setNewOrder(request.getNewOrder());
        if (request.getNewMessage() != null)  settings.setNewMessage(request.getNewMessage());
        if (request.getLowStock() != null)    settings.setLowStock(request.getLowStock());
        if (request.getDailyReport() != null) settings.setDailyReport(request.getDailyReport());
        if (request.getEmailNotif() != null)  settings.setEmailNotif(request.getEmailNotif());

        settings = adminSettingsRepository.save(settings);
        log.info("Admin settings saved for userId={}", userId);
        return toDto(settings);
    }

    /**
     * Lấy settings theo userId (dùng nội bộ bởi OrderService...).
     */
    public AdminSettings getRawSettings(Long userId) {
        return adminSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private AdminSettings createDefaultSettings(Long userId) {
        AdminSettings s = AdminSettings.builder()
                .userId(userId)
                .newOrder(true)
                .newMessage(true)
                .lowStock(false)
                .dailyReport(false)
                .emailNotif(true)
                .build();
        return adminSettingsRepository.save(s);
    }

    private AdminSettingsDto toDto(AdminSettings s) {
        return AdminSettingsDto.builder()
                .id(s.getId())
                .userId(s.getUserId())
                .newOrder(s.getNewOrder())
                .newMessage(s.getNewMessage())
                .lowStock(s.getLowStock())
                .dailyReport(s.getDailyReport())
                .emailNotif(s.getEmailNotif())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
