package com.example.webdisgn.repository;

import com.example.webdisgn.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {
    Optional<Category> findByNameAndDeletedFalse(String name);
    Optional<Category> findByIdAndDeletedFalse(String id);
}

