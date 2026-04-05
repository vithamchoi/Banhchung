package com.quannhabaninh.repository;

import com.quannhabaninh.entity.Order;
import com.quannhabaninh.entity.OrderStatus;
import com.quannhabaninh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByUserAndStatus(User user, OrderStatus status);
    
    boolean existsByOrderNumber(String orderNumber);
    
    // Admin queries
    List<Order> findAllByOrderByCreatedAtDesc();
    
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
    
    List<Order> findByOrderNumberContainingOrShippingNameContainingOrderByCreatedAtDesc(String orderNumber, String shippingName);
    
    List<Order> findTop10ByOrderByCreatedAtDesc();
    
    long countByStatus(OrderStatus status);
    
    List<Order> findByCreatedAtAfter(LocalDateTime dateTime);
    
    // Đơn DELIVERED trong khoảng thời gian (theo deliveredAt)
    List<Order> findByStatusAndDeliveredAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);

    // Doanh thu theo từng ngày trong tháng (DELIVERED)
    @Query("SELECT DAY(o.deliveredAt), SUM(o.totalAmount) FROM Order o " +
           "WHERE o.status = 'DELIVERED' " +
           "AND YEAR(o.deliveredAt) = :year AND MONTH(o.deliveredAt) = :month " +
           "GROUP BY DAY(o.deliveredAt) ORDER BY DAY(o.deliveredAt)")
    List<Object[]> findDailyRevenueByMonth(@Param("year") int year, @Param("month") int month);

    // Top sản phẩm bán chạy trong tháng
    @Query("SELECT oi.productName, oi.productImage, SUM(oi.quantity), SUM(oi.subtotal) " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' " +
           "AND YEAR(o.deliveredAt) = :year AND MONTH(o.deliveredAt) = :month " +
           "GROUP BY oi.productName, oi.productImage " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopProductsByMonth(@Param("year") int year, @Param("month") int month);
}

