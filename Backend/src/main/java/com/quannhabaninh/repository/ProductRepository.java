package com.quannhabaninh.repository;

import com.quannhabaninh.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Tìm sản phẩm theo category
    List<Product> findByCategory(String category);
    
    // Tìm sản phẩm best seller
    List<Product> findByIsBestSellerTrue();
    
    // Tìm sản phẩm theo tên (tìm kiếm)
    List<Product> findByNameContainingIgnoreCase(String name);
    
    // Tìm sản phẩm có stock > 0
    List<Product> findByStockQuantityGreaterThan(Integer quantity);

    // Tính toán top sản phẩm bán chạy qua quantity (đóng hộp dynamic)
    @Query("SELECT p FROM Product p " +
           "LEFT JOIN OrderItem oi ON p = oi.product " +
           "GROUP BY p " +
           "ORDER BY COALESCE(SUM(oi.quantity), 0) DESC, p.id DESC")
    List<Product> findTopBestSellersDynamic(Pageable pageable);
}
