package com.grocery.orderservice.service;

import com.grocery.orderservice.client.CartClient;
import com.grocery.orderservice.client.CartItemResponse;
import com.grocery.orderservice.client.CartResponse;
import com.grocery.orderservice.dto.OrderRequest;
import com.grocery.orderservice.dto.OrderResponse;
import com.grocery.orderservice.dto.OrderItemResponse;
import com.grocery.orderservice.model.Order;
import com.grocery.orderservice.model.OrderItem;
import com.grocery.orderservice.model.OrderStatus;
import com.grocery.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CartClient cartClient;
    
    public OrderResponse placeOrder(String userId, OrderRequest request, String authorization) {
        // Get user's cart
        CartResponse cart = cartClient.getCart(authorization);
        
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        // Create order
        Order order = new Order(userId, cart.getTotalAmount());
        order.setShippingAddress(request.getShippingAddress());
        
        // Convert cart items to order items
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(this::convertToOrderItem)
                .collect(Collectors.toList());
        
        order.setItems(orderItems);
        
        Order savedOrder = orderRepository.save(order);
        return convertToResponse(savedOrder);
    }
    
    public Page<OrderResponse> getUserOrders(String userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        return orders.map(this::convertToResponse);
    }
    
    public OrderResponse getOrderById(Long orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Order not found");
        }
        
        return convertToResponse(order);
    }
    
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return convertToResponse(updatedOrder);
    }
    
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    private OrderItem convertToOrderItem(CartItemResponse cartItem) {
        return new OrderItem(
                cartItem.getProductId(),
                cartItem.getProductName(),
                cartItem.getProductUnit(),
                cartItem.getQuantity(),
                cartItem.getPrice()
        );
    }
    
    private OrderResponse convertToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::convertToItemResponse)
                .collect(Collectors.toList());
        
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                itemResponses,
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
    
    private OrderItemResponse convertToItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getProductUnit(),
                item.getQuantity(),
                item.getPrice()
        );
    }
}
