package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.dto.request.CategoryRequest;
import com.example.webdisgn.dto.response.CategoryResponse;
import com.example.webdisgn.model.Category;
import com.example.webdisgn.repository.CategoryRepository;
import com.example.webdisgn.service.AuditService;
import com.example.webdisgn.service.CategoryService;
import com.example.webdisgn.util.MapperCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuditService auditService;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = MapperCategory.toEntity(request);
        Category saved = categoryRepository.save(category);
        String user = currentUser();
        auditService.log(user, "CREATE_CATEGORY", saved.getId());
        log.info("Categoria creata: {}", saved.getName());
        return MapperCategory.toResponse(saved);
    }

    @Override
    public CategoryResponse updateCategory(String id, CategoryRequest request) {
        Category category = categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Categoria non trovata"));

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updated = categoryRepository.save(category);
        auditService.log(currentUser(), "UPDATE_CATEGORY", updated.getId());
        log.info("Categoria aggiornata: {}", updated.getName());
        return MapperCategory.toResponse(updated);
    }

    public void deleteCategory(String id) {
        Category category = categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Categoria non trovata"));

        category.setDeleted(true);
        categoryRepository.save(category);
        auditService.log(currentUser(), "DELETE_CATEGORY", id);
        log.warn("Soft delete eseguito sulla categoria con ID: {}", id);
    }

    @Override
    public CategoryResponse getById(String id) {
        Category category = categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Categoria non trovata"));
        return MapperCategory.toResponse(category);
    }

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .filter(c -> !c.isDeleted())
                .map(MapperCategory::toResponse)
                .toList();
    }

    private String currentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
