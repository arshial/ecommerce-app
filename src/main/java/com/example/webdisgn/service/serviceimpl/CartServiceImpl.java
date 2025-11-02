package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.dto.request.CartItemRequest;
import com.example.webdisgn.dto.request.CartRequest;
import com.example.webdisgn.dto.response.CartResponse;
import com.example.webdisgn.exeption.GlobalExceptionHandler.ResourceNotFoundException;
import com.example.webdisgn.model.*;
import com.example.webdisgn.repository.CartItemRepository;
import com.example.webdisgn.repository.CartRepository;
import com.example.webdisgn.repository.ProductRepository;
import com.example.webdisgn.repository.UserRepository;
import com.example.webdisgn.service.CartService;
import com.example.webdisgn.util.MapperCart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public CartResponse getUserCart(String username) {
        Cart cart = getOrCreateCart(username);
        return MapperCart.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(String username, CartItemRequest request) {
        Cart cart = getOrCreateCart(username);
        Product product = productRepository.findByIdAndDeletedFalse(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Prodotto non trovato"));

        if (request.getQuantity() > product.getStock()) {
            throw new RuntimeException("Stock insufficiente per il prodotto: " + product.getName());
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            cart.getItems().add(cartItemRepository.save(newItem));
        }

        return updateCartTotal(cart);
    }

    @Override
    @Transactional
    public CartResponse updateCart(String username, CartRequest request) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().clear();
        cartItemRepository.deleteAllByCart(cart);

        for (CartItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findByIdAndDeletedFalse(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Prodotto non trovato: ID " + itemReq.getProductId()));

            if (itemReq.getQuantity() > product.getStock()) {
                throw new RuntimeException("Stock insufficiente per il prodotto: " + product.getName());
            }

            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            cart.getItems().add(cartItemRepository.save(item));
        }

        return updateCartTotal(cart);
    }

    @Override
    @Transactional
    public void removeItem(String username, Long itemId) {
        Cart cart = getOrCreateCart(username);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Elemento non trovato"));
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Accesso negato al carrello");
        }
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        cartItemRepository.delete(item);
        updateCartTotal(cart);
    }

    @Override
    @Transactional
    public void clearCart(String username) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().clear();
        cartItemRepository.deleteAllByCart(cart);
        cart.setTotal(0.0);
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(String username) {
        User user = userRepository.findByNameAndDeletedFalse(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato: " + username));
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            cart.setItems(new ArrayList<>());
            cart.setTotal(0.0);
            return cartRepository.save(cart);
        });
    }

    private CartResponse updateCartTotal(Cart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();
        cart.setTotal(total);
        cartRepository.save(cart);
        return MapperCart.toResponse(cart);
    }
}

