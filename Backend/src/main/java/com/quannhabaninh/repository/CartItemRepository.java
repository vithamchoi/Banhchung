package com.quannhabaninh.repository;

import com.quannhabaninh.entity.Cart;
import com.quannhabaninh.entity.CartItem;
import com.quannhabaninh.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    
    Optional<CartItem> findByIdAndCart(Long id, Cart cart);
}
