package com.example.webdisgn.repository;

import com.example.webdisgn.model.Cart;
import com.example.webdisgn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findByUser(User user);
}
