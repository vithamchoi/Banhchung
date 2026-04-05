package com.quannhabaninh.dto;

import com.quannhabaninh.entity.OrderStatus;
import com.quannhabaninh.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String userName;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private String paymentStatus;
    
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    
    private String shippingName;
    private String shippingPhone;
    private String shippingEmail;
    private String shippingAddress;
    private String shippingCity;
    private String shippingDistrict;
    private String shippingWard;
    
    private String notes;
    
    private List<OrderItemResponse> items;
    private int totalItems;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
}
