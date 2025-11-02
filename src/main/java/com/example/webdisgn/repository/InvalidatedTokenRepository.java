package com.example.webdisgn.repository;

import com.example.webdisgn.model.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    boolean existsByToken(String token);
}
