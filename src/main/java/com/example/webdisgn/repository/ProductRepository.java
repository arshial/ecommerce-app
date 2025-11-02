package com.example.webdisgn.repository;

import com.example.webdisgn.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndDeletedFalse(Long id);

    Optional<Product> findByNameAndDeletedFalse(String name);
    Page<Product> findByDeletedFalse(Pageable pageable);

    @Query("""
        SELECT p FROM Product p 
        WHERE p.deleted = false
          AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                             OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:category IS NULL OR p.category.name = :category)
          AND (:min IS NULL OR p.price >= :min)
          AND (:max IS NULL OR p.price <= :max)
    """)
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("min") Double min,
            @Param("max") Double max,
            Pageable pageable
    );
}
