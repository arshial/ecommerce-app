package com.example.webdisgn.util;

import com.example.webdisgn.dto.response.CartItemResponse;
import com.example.webdisgn.dto.response.CartResponse;
import com.example.webdisgn.model.Cart;

import java.util.List;
import java.util.stream.Collectors;

public class MapperCart {

    public static CartResponse toResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setCartId(cart.getId());

        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(MapperCartItem::toResponse)
                .collect(Collectors.toList());

        response.setItems(itemResponses);

        double total = itemResponses.stream()
                .mapToDouble(CartItemResponse::getSubtotal)
                .sum();

        response.setTotal(total);

        return response;
    }
}
