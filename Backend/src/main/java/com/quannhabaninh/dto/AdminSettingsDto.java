package com.quannhabaninh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSettingsDto {
    private Long id;
    private Long userId;
    private Boolean newOrder;
    private Boolean newMessage;
    private Boolean lowStock;
    private Boolean dailyReport;
    private Boolean emailNotif;
    private LocalDateTime updatedAt;
}
