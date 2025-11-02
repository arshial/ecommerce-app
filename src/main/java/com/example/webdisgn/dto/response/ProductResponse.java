package com.example.webdisgn.dto.response;

import lombok.Data;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private double price;
    private String imagePath;
    private int stock;
    private double averageRating;
    private String description;
}
