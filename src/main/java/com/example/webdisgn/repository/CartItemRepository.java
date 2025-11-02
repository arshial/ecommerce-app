package com.example.webdisgn.repository;

import com.example.webdisgn.model.Cart;
import com.example.webdisgn.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    void deleteAllByCart(Cart cart);

}
