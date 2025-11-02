package com.example.webdisgn.controller;

import com.example.webdisgn.dto.request.CategoryRequest;
import com.example.webdisgn.dto.response.CategoryResponse;
import com.example.webdisgn.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public CategoryResponse create(@RequestBody @Valid CategoryRequest request) {
        return categoryService.createCategory(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable String id, @RequestBody @Valid CategoryRequest request) {
        return categoryService.updateCategory(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        categoryService.deleteCategory(id);
    }

    @GetMapping("/{id}")
    public CategoryResponse getById(@PathVariable String id) {
        return categoryService.getById(id);
    }

    @GetMapping
    public List<CategoryResponse> getAll() {
        return categoryService.getAll();
    }
}
