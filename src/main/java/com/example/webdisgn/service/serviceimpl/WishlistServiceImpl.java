package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.dto.response.ProductResponse;
import com.example.webdisgn.exeption.*;
import com.example.webdisgn.model.Product;
import com.example.webdisgn.model.User;
import com.example.webdisgn.model.Wishlist;
import com.example.webdisgn.repository.ProductRepository;
import com.example.webdisgn.repository.UserRepository;
import com.example.webdisgn.repository.WishlistRepository;
import com.example.webdisgn.service.WishlistService;
import com.example.webdisgn.util.MapperProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public void addToWishlist(String username, Long productId) {
        User user = userRepository.findByNameAndDeletedFalse(username)
                .orElseThrow(() -> new ExceptionConfig.ResourceNotFoundException("Utente non trovato"));
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new ExceptionConfig.ResourceNotFoundException("Prodotto non trovato"));

        if (wishlistRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("Prodotto già nei preferiti.");
        }

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        wishlistRepository.save(wishlist);
    }

    @Override
    public void removeFromWishlist(String username, Long productId) {
        User user = userRepository.findByNameAndDeletedFalse(username)
                .orElseThrow(() -> new ExceptionConfig.ResourceNotFoundException("Utente non trovato"));
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new ExceptionConfig.ResourceNotFoundException("Prodotto non trovato"));

        wishlistRepository.deleteByUserAndProduct(user, product);
    }

    @Override
    public List<ProductResponse> getWishlist(String username) {
        User user = userRepository.findByNameAndDeletedFalse(username)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Utente non trovato"));

        return wishlistRepository.findByUser(user).stream()
                .map(w -> MapperProduct.toResponse(w.getProduct()))
                .toList();
    }
}
