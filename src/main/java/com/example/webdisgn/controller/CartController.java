package com.example.webdisgn.controller;

import com.example.webdisgn.dto.request.CartItemRequest;
import com.example.webdisgn.dto.request.CartRequest;
import com.example.webdisgn.dto.response.CartResponse;
import com.example.webdisgn.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public CartResponse getCart() {
        return cartService.getUserCart(currentUsername());
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('USER')")
    public CartResponse addItem(@Valid @RequestBody CartItemRequest request) {
        return cartService.addItem(currentUsername(), request);
    }

    @PutMapping
    @PreAuthorize("hasRole('USER')")
    public CartResponse updateCart(@Valid @RequestBody CartRequest request) {
        return cartService.updateCart(currentUsername(), request);
    }

    @DeleteMapping("/item/{itemId}")
    @PreAuthorize("hasRole('USER')")
    public void removeItem(@PathVariable Long itemId) {
        cartService.removeItem(currentUsername(), itemId);
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('USER')")
    public void clearCart() {
        cartService.clearCart(currentUsername());
    }
}
