package com.quannhabaninh.dto;

import com.quannhabaninh.entity.OrderStatus;
import com.quannhabaninh.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderNotification {
    
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private String shippingAddress;
    private LocalDateTime createdAt;
    private Integer itemCount;
    private List<OrderItemSummary> items;
    private String notificationType; // "NEW_ORDER", "STATUS_CHANGE", "CANCELLED"
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemSummary {
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }
}
