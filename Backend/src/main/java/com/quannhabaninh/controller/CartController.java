package com.quannhabaninh.controller;

import com.quannhabaninh.dto.AddToCartRequest;
import com.quannhabaninh.dto.CartResponse;
import com.quannhabaninh.dto.ErrorResponse;
import com.quannhabaninh.dto.UpdateCartItemRequest;
import com.quannhabaninh.entity.User;
import com.quannhabaninh.service.AuthService;
import com.quannhabaninh.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")

@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CartController {
    
    private final CartService cartService;
    private final AuthService authService;
    
    // POST /api/cart/items - Thêm sản phẩm vào giỏ hàng
    @PostMapping("/items")
    public ResponseEntity<?> addToCart(@Valid @RequestBody AddToCartRequest request) {
        try {
            User currentUser = authService.getCurrentUser();
            CartResponse cart = cartService.addToCart(currentUser, request);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            ErrorResponse error = new ErrorResponse(
                "Add to Cart Failed",
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                "An error occurred while adding to cart: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // GET /api/cart - Lấy giỏ hàng hiện tại
    @GetMapping
    public ResponseEntity<?> getCart() {
        try {
            User currentUser = authService.getCurrentUser();
            CartResponse cart = cartService.getCart(currentUser);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "Get Cart Failed",
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // PUT /api/cart/items/{itemId} - Cập nhật số lượng sản phẩm trong giỏ
    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        try {
            User currentUser = authService.getCurrentUser();
            CartResponse cart = cartService.updateCartItem(currentUser, itemId, request.getQuantity());
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            ErrorResponse error = new ErrorResponse(
                "Update Cart Item Failed",
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                "An error occurred while updating cart item: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // DELETE /api/cart/items/{itemId} - Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeCartItem(@PathVariable Long itemId) {
        try {
            User currentUser = authService.getCurrentUser();
            CartResponse cart = cartService.removeCartItem(currentUser, itemId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Item removed from cart successfully");
            response.put("cart", cart);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ErrorResponse error = new ErrorResponse(
                "Remove Cart Item Failed",
                e.getMessage(),
                HttpStatus.NOT_FOUND.value()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                "An error occurred while removing cart item: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // DELETE /api/cart - Xóa toàn bộ giỏ hàng
    @DeleteMapping
    public ResponseEntity<?> clearCart() {
        try {
            User currentUser = authService.getCurrentUser();
            cartService.clearCart(currentUser);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cart cleared successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                "Clear Cart Failed",
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
