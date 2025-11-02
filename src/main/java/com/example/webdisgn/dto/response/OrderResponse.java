package com.example.webdisgn.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private String id;
    private List<OrderItemResponse> items;
    private double total;
    private String status;
    private LocalDateTime createdAt;
    private String couponCode;
}
