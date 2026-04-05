package com.quannhabaninh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /** Gửi thông báo khi có đơn hàng mới */
    @Column(name = "new_order", nullable = false)
    @Builder.Default
    private Boolean newOrder = true;

    /** Gửi thông báo khi có tin nhắn mới */
    @Column(name = "new_message", nullable = false)
    @Builder.Default
    private Boolean newMessage = true;

    /** Cảnh báo khi sản phẩm sắp hết hàng */
    @Column(name = "low_stock", nullable = false)
    @Builder.Default
    private Boolean lowStock = false;

    /** Gửi email báo cáo doanh thu mỗi ngày */
    @Column(name = "daily_report", nullable = false)
    @Builder.Default
    private Boolean dailyReport = false;

    /** Gửi thông báo qua Email */
    @Column(name = "email_notif", nullable = false)
    @Builder.Default
    private Boolean emailNotif = true;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
