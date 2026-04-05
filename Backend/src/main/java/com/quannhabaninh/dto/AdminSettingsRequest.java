package com.quannhabaninh.dto;

import lombok.Data;

@Data
public class AdminSettingsRequest {
    private Boolean newOrder;
    private Boolean newMessage;
    private Boolean lowStock;
    private Boolean dailyReport;
    private Boolean emailNotif;
}
