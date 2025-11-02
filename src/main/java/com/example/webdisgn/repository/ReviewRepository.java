package com.example.webdisgn.repository;

import com.example.webdisgn.model.Product;
import com.example.webdisgn.model.Review;
import com.example.webdisgn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findByProduct(Product product);
    Optional<Review> findByUserAndProduct(User user, Product product);
    boolean existsByUserAndProduct(User user, Product product);
}
