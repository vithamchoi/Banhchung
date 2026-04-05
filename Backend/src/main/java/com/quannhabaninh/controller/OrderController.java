package com.quannhabaninh.controller;

import com.quannhabaninh.dto.CheckoutRequest;
import com.quannhabaninh.dto.ErrorResponse;
import com.quannhabaninh.dto.OrderResponse;
import com.quannhabaninh.entity.User;
import com.quannhabaninh.service.AuthService;
import com.quannhabaninh.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")

@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OrderController {
    
    private final OrderService orderService;
    private final AuthService authService;
    
    // POST /api/orders/checkout - Checkout và tạo đơn hàng
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@Valid @RequestBody CheckoutRequest request) {
        try {
            User currentUser = authService.getCurrentUser();
            OrderResponse order = orderService.checkout(currentUser, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order created successfully");
            response.put("order", order);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            ErrorResponse error = new ErrorResponse(
                "Checkout Failed",
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                "An error occurred during checkout: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // GET /api/orders - Lấy danh sách đơn hàng của user
    @GetMapping
    public ResponseEntity<?> getUserOrders() {
        try {
            User currentUser = authService.getCurrentUser();
            List<OrderResponse> orders = orderService.getUserOrders(currentUser);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "Get Orders Failed",
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // GET /api/orders/{id} - Lấy chi tiết đơn hàng
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            User currentUser = authService.getCurrentUser();
            OrderResponse order = orderService.getOrderById(currentUser, id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            ErrorResponse error = new ErrorResponse(
                "Order Not Found",
                e.getMessage(),
                HttpStatus.NOT_FOUND.value()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // GET /api/orders/number/{orderNumber} - Lấy đơn hàng theo số đơn
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<?> getOrderByNumber(@PathVariable String orderNumber) {
        try {
            User currentUser = authService.getCurrentUser();
            OrderResponse order = orderService.getOrderByNumber(currentUser, orderNumber);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            ErrorResponse error = new ErrorResponse(
                "Order Not Found",
                e.getMessage(),
                HttpStatus.NOT_FOUND.value()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // PUT /api/orders/{id}/cancel - Hủy đơn hàng
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            User currentUser = authService.getCurrentUser();
            OrderResponse order = orderService.cancelOrder(currentUser, id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order cancelled successfully");
            response.put("order", order);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ErrorResponse error = new ErrorResponse(
                "Cancel Order Failed",
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
