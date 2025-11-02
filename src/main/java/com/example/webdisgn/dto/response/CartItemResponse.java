package com.example.webdisgn.dto.response;

import lombok.Data;

@Data
public class CartItemResponse {
    private Long id;
    private String productName;
    private double price;
    private int quantity;
    private double subtotal;
}
