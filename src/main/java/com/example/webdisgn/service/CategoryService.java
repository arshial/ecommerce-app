package com.example.webdisgn.service;

import com.example.webdisgn.dto.request.CategoryRequest;
import com.example.webdisgn.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse updateCategory(String id, CategoryRequest request);
    void deleteCategory(String id);
    CategoryResponse getById(String id);

    CategoryResponse createCategory(CategoryRequest request);
    List<CategoryResponse> getAll();
}
