package com.example.webdisgn.service;

import com.example.webdisgn.dto.request.CartItemRequest;
import com.example.webdisgn.dto.request.CartRequest;
import com.example.webdisgn.dto.response.CartResponse;

public interface CartService {
    CartResponse getUserCart(String username);
    CartResponse addItem(String username, CartItemRequest request);
    CartResponse updateCart(String username, CartRequest request);
    void removeItem(String username, Long itemId);
    void clearCart(String username);
}
