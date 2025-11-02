package com.example.webdisgn.dto.response;

import lombok.Data;

@Data
public class OrderItemResponse {
    private String productName;
    private int quantity;
    private double priceAtPurchase;
    private double subtotal;
}
