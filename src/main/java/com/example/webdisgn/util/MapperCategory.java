package com.example.webdisgn.util;

import com.example.webdisgn.dto.request.CategoryRequest;
import com.example.webdisgn.dto.response.CategoryResponse;
import com.example.webdisgn.model.Category;

public class MapperCategory {

    public static Category toEntity(CategoryRequest request) {
        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .deleted(false)
                .build();
    }

    public static CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        return response;
    }
}
