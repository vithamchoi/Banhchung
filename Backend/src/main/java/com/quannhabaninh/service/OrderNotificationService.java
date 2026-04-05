package com.quannhabaninh.service;

import com.quannhabaninh.dto.OrderNotification;
import com.quannhabaninh.entity.Order;
import com.quannhabaninh.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Send new order notification to all connected admin clients
     */
    public void notifyNewOrder(Order order) {
        OrderNotification notification = buildOrderNotification(order, "NEW_ORDER");
        
        // Send to /topic/orders - all admin subscribers will receive this
        messagingTemplate.convertAndSend("/topic/orders", notification);
        
        log.info("Sent new order notification: Order #{} - {}", order.getId(), order.getOrderNumber());
    }
    
    /**
     * Send order status change notification
     */
    public void notifyOrderStatusChange(Order order) {
        OrderNotification notification = buildOrderNotification(order, "STATUS_CHANGE");
        
        // Send to /topic/orders for admin
        messagingTemplate.convertAndSend("/topic/orders", notification);
        
        // Send to specific user queue
        messagingTemplate.convertAndSendToUser(
            order.getUser().getUsername(),
            "/queue/orders",
            notification
        );
        
        log.info("Sent order status change notification: Order #{} - Status: {}", 
                order.getId(), order.getStatus());
    }
    
    /**
     * Send order cancellation notification
     */
    public void notifyOrderCancelled(Order order) {
        OrderNotification notification = buildOrderNotification(order, "CANCELLED");
        
        messagingTemplate.convertAndSend("/topic/orders", notification);
        
        log.info("Sent order cancellation notification: Order #{}", order.getId());
    }
    
    /**
     * Send order statistics update
     */
    public void notifyOrderStats(Object stats) {
        messagingTemplate.convertAndSend("/topic/order-stats", stats);
    }
    
    /**
     * Build OrderNotification from Order entity
     */
    private OrderNotification buildOrderNotification(Order order, String notificationType) {
        return OrderNotification.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .customerName(order.getUser().getFullName() != null ? 
                        order.getUser().getFullName() : order.getUser().getUsername())
                .customerEmail(order.getUser().getEmail())
                .customerPhone(order.getUser().getPhoneNumber())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .itemCount(order.getItems().size())
                .items(order.getItems().stream()
                        .map(this::mapToItemSummary)
                        .collect(Collectors.toList()))
                .notificationType(notificationType)
                .build();
    }
    
    private OrderNotification.OrderItemSummary mapToItemSummary(OrderItem item) {
        return OrderNotification.OrderItemSummary.builder()
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }
}
