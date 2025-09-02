package com.grocery.orderservice.repository;

import com.grocery.orderservice.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Page<Order> findByUserId(String userId, Pageable pageable);
    
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<Order> findByStatus(com.grocery.orderservice.model.OrderStatus status);
}
