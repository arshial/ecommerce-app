package com.example.webdisgn.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class CartResponse {
    private String cartId;
    private List<CartItemResponse> items;
    private double total;
}
