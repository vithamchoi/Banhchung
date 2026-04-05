package com.quannhabaninh.service;

import com.quannhabaninh.dto.CheckoutRequest;
import com.quannhabaninh.dto.OrderItemResponse;
import com.quannhabaninh.dto.OrderResponse;
import com.quannhabaninh.entity.*;
import com.quannhabaninh.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderNotificationService orderNotificationService;
    private final AdminSettingsService adminSettingsService;
    private final EmailService emailService;

    // Email address of the owner / admin to receive new order notifications
    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String adminEmail;
    
    @Transactional
    public OrderResponse checkout(User user, CheckoutRequest request) {
        // Lấy giỏ hàng
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart is empty"));
        
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        // Kiểm tra tồn kho và tính tổng
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            
            // Kiểm tra tồn kho
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Product '" + product.getName() + 
                    "' only has " + product.getStockQuantity() + " items in stock");
            }
            
            subtotal = subtotal.add(cartItem.getSubtotal());
        }
        
        // Tạo đơn hàng
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus("UNPAID");
        
        // Thông tin giao hàng
        order.setShippingName(request.getShippingName());
        order.setShippingPhone(request.getShippingPhone());
        order.setShippingEmail(request.getShippingEmail());
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingCity(request.getShippingCity());
        order.setShippingDistrict(request.getShippingDistrict());
        order.setShippingWard(request.getShippingWard());
        order.setNotes(request.getNotes());
        
        // Tính toán
        order.setSubtotal(subtotal);
        order.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);
        
        // Kiểm tra discountAmount từ request
        BigDecimal discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        order.setDiscountAmount(discount);
        
        BigDecimal total = subtotal.add(order.getShippingFee()).subtract(order.getDiscountAmount());
        // Đảm bảo total không âm
        order.setTotalAmount(total.compareTo(BigDecimal.ZERO) >= 0 ? total : BigDecimal.ZERO);
        
        // Lưu đơn hàng
        order = orderRepository.save(order);
        
        // Tạo order items và cập nhật tồn kho
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setProductName(cartItem.getProduct().getName());
            orderItem.setProductImage(cartItem.getProduct().getImage());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setSubtotal(cartItem.getSubtotal());
            
            orderItemRepository.save(orderItem);
            
            // Giảm tồn kho
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }
        
        // Xóa giỏ hàng
        cart.getItems().clear();
        cartRepository.save(cart);
        
        // Refresh order để lấy items
        order = orderRepository.findById(order.getId()).orElseThrow();
        
        // Send real-time WebSocket notification to admin
        orderNotificationService.notifyNewOrder(order);

        // Send email notification to admin if settings allow it
        try {
            // Find first admin user to get their settings – fallback to a fixed admin userId=1
            java.util.List<com.quannhabaninh.entity.User> admins = userRepository.findAll().stream()
                    .filter(u -> u.getRoles().contains(com.quannhabaninh.entity.Role.ROLE_ADMIN))
                    .toList();
            if (!admins.isEmpty()) {
                com.quannhabaninh.entity.AdminSettings adminSettings =
                        adminSettingsService.getRawSettings(admins.get(0).getId());
                if (Boolean.TRUE.equals(adminSettings.getEmailNotif())
                        && Boolean.TRUE.equals(adminSettings.getNewOrder())) {
                    String customerName = order.getUser().getFullName() != null
                            ? order.getUser().getFullName() : order.getUser().getUsername();
                    String totalFormatted = String.format("%,.0f VND", order.getTotalAmount());
                    emailService.sendNewOrderNotification(adminEmail, order.getOrderNumber(),
                            customerName, totalFormatted);
                }
            }
        } catch (Exception ex) {
            // Do not fail the order if email notification fails
        }

        return mapToOrderResponse(order);
    }
    
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(User user) {
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        return mapToOrderResponse(order);
    }
    
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(User user, String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        return mapToOrderResponse(order);
    }
    
    @Transactional
    public OrderResponse cancelOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("Cannot cancel order with status: " + order.getStatus());
        }
        
        // Hoàn trả tồn kho
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        // Send cancellation notification
        orderNotificationService.notifyOrderCancelled(order);
        
        order.setCancelledAt(LocalDateTime.now());
        order = orderRepository.save(order);
        
        return mapToOrderResponse(order);
    }
    
    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        String orderNumber = "ORD-" + datePart + "-" + random;
        
        // Đảm bảo unique
        while (orderRepository.existsByOrderNumber(orderNumber)) {
            random = String.format("%04d", (int) (Math.random() * 10000));
            orderNumber = "ORD-" + datePart + "-" + random;
        }
        
        return orderNumber;
    }
    
    private BigDecimal calculateShippingFee(String city) {
        // Logic tính phí ship theo thành phố
        if (city == null) {
            return new BigDecimal("30000");
        }
        
        if (city.toLowerCase().contains("hà nội") || city.toLowerCase().contains("hồ chí minh")) {
            return new BigDecimal("20000");
        }
        
        return new BigDecimal("30000");
    }
    
    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getProductImage(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getSubtotal()
                ))
                .collect(Collectors.toList());
        
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUser().getId(),
                order.getUser().getFullName(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getSubtotal(),
                order.getShippingFee(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getShippingName(),
                order.getShippingPhone(),
                order.getShippingEmail(),
                order.getShippingAddress(),
                order.getShippingCity(),
                order.getShippingDistrict(),
                order.getShippingWard(),
                order.getNotes(),
                itemResponses,
                order.getTotalItems(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getConfirmedAt(),
                order.getShippedAt(),
                order.getDeliveredAt(),
                order.getCancelledAt()
        );
    }
}
