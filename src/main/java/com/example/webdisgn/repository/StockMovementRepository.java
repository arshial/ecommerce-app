package com.example.webdisgn.repository;

import com.example.webdisgn.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, String> {
    List<StockMovement> findByProductIdOrderByTimestampDesc(Long productId);
}
