package com.quannhabaninh.service;

import com.quannhabaninh.dto.AddToCartRequest;
import com.quannhabaninh.dto.CartItemResponse;
import com.quannhabaninh.dto.CartResponse;
import com.quannhabaninh.entity.Cart;
import com.quannhabaninh.entity.CartItem;
import com.quannhabaninh.entity.Product;
import com.quannhabaninh.entity.User;
import com.quannhabaninh.repository.CartItemRepository;
import com.quannhabaninh.repository.CartRepository;
import com.quannhabaninh.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    
    @Transactional
    public CartResponse addToCart(User user, AddToCartRequest request) {
        // Lấy hoặc tạo giỏ hàng cho user
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
        
        // Kiểm tra sản phẩm tồn tại
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));
        
        // Kiểm tra tồn kho
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Not enough stock. Available: " + product.getStockQuantity());
        }
        
        // Make product and cart final for lambda
        final Product finalProduct = product;
        final Cart finalCart = cart;
        
        // Kiểm tra sản phẩm đã có trong giỏ chưa
        CartItem cartItem = cartItemRepository.findByCartAndProduct(finalCart, finalProduct)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(finalCart);
                    newItem.setProduct(finalProduct);
                    newItem.setPrice(finalProduct.getPrice());
                    newItem.setQuantity(0);
                    return newItem;
                });
        
        // Cập nhật số lượng
        int newQuantity = cartItem.getQuantity() + request.getQuantity();
        if (newQuantity > finalProduct.getStockQuantity()) {
            throw new RuntimeException("Total quantity exceeds available stock: " + finalProduct.getStockQuantity());
        }
        
        cartItem.setQuantity(newQuantity);
        cartItem.setPrice(finalProduct.getPrice()); // Cập nhật giá mới nhất
        cartItemRepository.save(cartItem);
        
        // Refresh cart để lấy dữ liệu mới nhất
        Cart refreshedCart = cartRepository.findById(finalCart.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found after update"));
        
        return mapToCartResponse(refreshedCart);
    }
    
    public CartResponse getCart(User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
        
        return mapToCartResponse(cart);
    }
    
    @Transactional
    public CartResponse updateCartItem(User user, Long itemId, Integer quantity) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        CartItem cartItem = cartItemRepository.findByIdAndCart(itemId, cart)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        // Kiểm tra tồn kho
        if (cartItem.getProduct().getStockQuantity() < quantity) {
            throw new RuntimeException("Not enough stock. Available: " + cartItem.getProduct().getStockQuantity());
        }
        
        cartItem.setQuantity(quantity);
        cartItem.setPrice(cartItem.getProduct().getPrice()); // Cập nhật giá mới nhất
        cartItemRepository.save(cartItem);
        
        return mapToCartResponse(cart);
    }
    
    @Transactional
    public CartResponse removeCartItem(User user, Long itemId) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        CartItem cartItem = cartItemRepository.findByIdAndCart(itemId, cart)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        cartItemRepository.delete(cartItem);
        
        // Refresh cart
        cart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found after removing item"));
        
        return mapToCartResponse(cart);
    }
    
    @Transactional
    public void clearCart(User user) {
        cartRepository.findByUser(user).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }
    
    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getImage(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getSubtotal(),
                        item.getProduct().getStockQuantity(),
                        item.getCreatedAt()
                ))
                .collect(Collectors.toList());
        
        return new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                itemResponses,
                cart.getTotalPrice(),
                cart.getTotalItems(),
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }
}
