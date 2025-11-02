package com.example.webdisgn.util;

import com.example.webdisgn.dto.response.CartItemResponse;
import com.example.webdisgn.model.CartItem;

public class MapperCartItem {

    public static CartItemResponse toResponse(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setProductName(item.getProduct().getName());
        response.setPrice(item.getProduct().getPrice());
        response.setQuantity(item.getQuantity());
        response.setSubtotal(item.getProduct().getPrice() * item.getQuantity());
        return response;
    }
}
