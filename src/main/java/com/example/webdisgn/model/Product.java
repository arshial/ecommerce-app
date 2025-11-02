package com.example.webdisgn.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "image_path")
    private String imagePath;

    @Column(nullable = false)
    private Double price;

    @Column(name = "deleted")
    private boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private int minStockThreshold = 5;

    @Column(nullable = false)
    private double averageRating = 0.0;

    @Column(length = 1000)
    private String description;

}
