package com.example.webdisgn.controller;

import com.example.webdisgn.dto.response.ProductResponse;
import com.example.webdisgn.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    // ➕ Aggiungi prodotto ai preferiti
    @PostMapping("/{productId}")
    public ResponseEntity<String> addToWishlist(@PathVariable Long productId,
                                                @AuthenticationPrincipal(expression = "username") String username) {
        wishlistService.addToWishlist(username, productId);
        return ResponseEntity.ok("✅ Prodotto aggiunto ai preferiti");
    }

    // ➖ Rimuovi prodotto dai preferiti
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> removeFromWishlist(@PathVariable Long productId,
                                                     @AuthenticationPrincipal(expression = "username") String username) {
        wishlistService.removeFromWishlist(username, productId);
        return ResponseEntity.ok("✅ Prodotto rimosso dai preferiti");
    }

    // 📋 Ottieni tutti i prodotti nei preferiti dell'utente
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getWishlist(@AuthenticationPrincipal(expression = "username") String username) {
        List<ProductResponse> wishlist = wishlistService.getWishlist(username);
        return ResponseEntity.ok(wishlist);
    }
}
