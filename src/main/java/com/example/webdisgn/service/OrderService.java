package com.example.webdisgn.service;

import com.example.webdisgn.dto.response.OrderResponse;
import com.example.webdisgn.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    OrderResponse checkout(String username, String couponCode);

    List<OrderResponse> getOrdersByUser(String username);
    void updateOrderStatus(String orderId, OrderStatus newStatus);
    List<OrderResponse> getOrdersByUserAndStatus(String username, OrderStatus status);

    List<OrderResponse> getOrdersByDateRange(LocalDateTime start, LocalDateTime end, OrderStatus status);

}
