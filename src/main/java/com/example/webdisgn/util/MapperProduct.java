package com.example.webdisgn.util;

import com.example.webdisgn.dto.request.ProductRequest;
import com.example.webdisgn.dto.response.ProductResponse;
import com.example.webdisgn.model.Product;

public class MapperProduct {

    public static Product toEntity(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());
        return product;
    }

    public static ProductResponse toResponse(Product product) {
        ProductResponse dto = new ProductResponse();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setImagePath(product.getImagePath());
        dto.setStock(product.getStock());
        dto.setAverageRating(product.getAverageRating());
        dto.setDescription(product.getDescription());
        return dto;
    }

}
