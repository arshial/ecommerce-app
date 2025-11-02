package com.example.webdisgn.service;

import com.example.webdisgn.dto.response.ProductResponse;

import java.util.List;

public interface WishlistService {
    void addToWishlist(String username, Long productId);
    void removeFromWishlist(String username, Long productId);
    List<ProductResponse> getWishlist(String username);
}
