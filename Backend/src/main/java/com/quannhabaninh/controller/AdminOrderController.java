package com.quannhabaninh.controller;

import com.quannhabaninh.dto.OrderNotification;
import com.quannhabaninh.entity.Order;
import com.quannhabaninh.entity.OrderStatus;
import com.quannhabaninh.repository.OrderRepository;
import com.quannhabaninh.service.OrderNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
    
    private final OrderRepository orderRepository;
    private final OrderNotificationService orderNotificationService;
    
    /**
     * Get all orders (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String search) {
        try {
            List<Order> orders;
            
            if (status != null) {
                orders = orderRepository.findByStatusOrderByCreatedAtDesc(status);
            } else if (search != null && !search.isEmpty()) {
                orders = orderRepository.findByOrderNumberContainingOrShippingNameContainingOrderByCreatedAtDesc(search, search);
            } else {
                orders = orderRepository.findAllByOrderByCreatedAtDesc();
            }
            
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch orders: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Get order by ID (Admin only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
            
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(404).body(error);
        }
    }
    
    /**
     * Update order status (Admin only)
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            
            String statusStr = request.get("status");
            OrderStatus newStatus = OrderStatus.valueOf(statusStr);
            
            order.setStatus(newStatus);
            
            // Update timestamps based on status
            LocalDateTime now = LocalDateTime.now();
            switch (newStatus) {
                case CONFIRMED:
                    order.setConfirmedAt(now);
                    break;
                case SHIPPING:
                    order.setShippedAt(now);
                    break;
                case DELIVERED:
                    order.setDeliveredAt(now);
                    order.setPaymentStatus("PAID");
                    break;
                case CANCELLED:
                    order.setCancelledAt(now);
                    break;
                default:
                    break;
            }
            
            order = orderRepository.save(order);
            
            // Send real-time notification
            orderNotificationService.notifyOrderStatusChange(order);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order status updated successfully");
            response.put("order", order);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update order status: " + e.getMessage());
            return ResponseEntity.status(400).body(error);
        }
    }
    
    /**
     * Get order statistics (Admin only)
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getOrderStats() {
        try {
            long totalOrders = orderRepository.count();
            long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
            long confirmedOrders = orderRepository.countByStatus(OrderStatus.CONFIRMED);
            long shippingOrders = orderRepository.countByStatus(OrderStatus.SHIPPING);
            long deliveredOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
            long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);
            
            // Tổng doanh thu: chỉ tính đơn DELIVERED
            List<Order> deliveredOrdersList = orderRepository.findByStatus(OrderStatus.DELIVERED);
            BigDecimal totalRevenue = deliveredOrdersList.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Thống kê hôm nay: đơn tạo hôm nay
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            List<Order> todayOrders = orderRepository.findByCreatedAtAfter(startOfDay);
            long todayOrderCount = todayOrders.size();
            
            // Doanh thu hôm nay: đơn hoàn thành (deliveredAt) hôm nay
            List<Order> todayDelivered = orderRepository.findByStatusAndDeliveredAtBetween(
                    OrderStatus.DELIVERED, startOfDay, endOfDay);
            BigDecimal todayRevenue = todayDelivered.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalOrders", totalOrders);
            stats.put("pendingOrders", pendingOrders);
            stats.put("confirmedOrders", confirmedOrders);
            stats.put("shippingOrders", shippingOrders);
            stats.put("deliveredOrders", deliveredOrders);
            stats.put("cancelledOrders", cancelledOrders);
            stats.put("totalRevenue", totalRevenue);
            stats.put("todayOrderCount", todayOrderCount);
            stats.put("todayRevenue", todayRevenue);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch order stats: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Get daily revenue for a month (Admin only)
     * Returns [{day, revenue}] for each day that has DELIVERED orders
     */
    @GetMapping("/monthly-revenue")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getMonthlyRevenue(
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month) {
        try {
            // Mặc định tháng hiện tại
            YearMonth ym = (year == 0 || month == 0)
                    ? YearMonth.now()
                    : YearMonth.of(year, month);

            List<Object[]> raw = orderRepository.findDailyRevenueByMonth(ym.getYear(), ym.getMonthValue());

            // Build full days of month, defaulting to 0
            Map<Integer, BigDecimal> dayMap = new LinkedHashMap<>();
            for (int d = 1; d <= ym.lengthOfMonth(); d++) {
                dayMap.put(d, BigDecimal.ZERO);
            }
            for (Object[] row : raw) {
                int day = ((Number) row[0]).intValue();
                BigDecimal revenue = (BigDecimal) row[1];
                dayMap.put(day, revenue);
            }

            List<Map<String, Object>> result = new ArrayList<>();
            for (Map.Entry<Integer, BigDecimal> entry : dayMap.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("day", entry.getKey());
                item.put("revenue", entry.getValue());
                result.add(item);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("year", ym.getYear());
            response.put("month", ym.getMonthValue());
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch monthly revenue: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Get top selling products for a month (Admin only)
     */
    @GetMapping("/top-products")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getTopProducts(
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            YearMonth ym = (year == 0 || month == 0)
                    ? YearMonth.now()
                    : YearMonth.of(year, month);

            List<Object[]> raw = orderRepository.findTopProductsByMonth(ym.getYear(), ym.getMonthValue());

            List<Map<String, Object>> result = raw.stream()
                    .limit(limit)
                    .map(row -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("productName", row[0]);
                        item.put("productImage", row[1]);
                        item.put("totalQuantity", ((Number) row[2]).intValue());
                        item.put("totalRevenue", row[3]);
                        return item;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("year", ym.getYear());
            response.put("month", ym.getMonthValue());
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch top products: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Get recent orders (Admin only)
     */
    @GetMapping("/recent")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getRecentOrders(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Order> orders = orderRepository.findTop10ByOrderByCreatedAtDesc();
            
            if (limit > 0 && limit < orders.size()) {
                orders = orders.subList(0, limit);
            }
            
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch recent orders: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * WebSocket message handler - Subscribe to order updates
     */
    @SubscribeMapping("/orders")
    public List<OrderNotification> subscribeToOrders() {
        List<Order> pendingOrders = orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.PENDING);
        
        return pendingOrders.stream()
                .limit(5)
                .map(order -> OrderNotification.builder()
                        .orderId(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .customerName(order.getShippingName())
                        .totalAmount(order.getTotalAmount())
                        .status(order.getStatus())
                        .createdAt(order.getCreatedAt())
                        .notificationType("PENDING")
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * WebSocket message handler - Refresh stats
     */
    @MessageMapping("/orders/refresh-stats")
    @SendTo("/topic/order-stats")
    public Map<String, Object> refreshStats() {
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", totalOrders);
        stats.put("pendingOrders", pendingOrders);
        stats.put("timestamp", LocalDateTime.now());
        
        return stats;
    }
}
