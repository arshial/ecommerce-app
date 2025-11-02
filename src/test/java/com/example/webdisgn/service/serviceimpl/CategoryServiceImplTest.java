package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.dto.request.CategoryRequest;
import com.example.webdisgn.dto.response.CategoryResponse;
import com.example.webdisgn.model.Category;
import com.example.webdisgn.repository.CategoryRepository;
import com.example.webdisgn.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private AuditService auditService;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        category = new Category();
        category.setId("cat123");
        category.setName("Gaming");
        category.setDescription("Accessori gaming");

        request = new CategoryRequest();
        request.setName("Gaming");
        request.setDescription("Accessori gaming");

        // Mock utente autenticato
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void createCategory_shouldSaveAndLog() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response.getName()).isEqualTo("Gaming");
        verify(auditService).log("admin", "CREATE_CATEGORY", "cat123");
    }

    @Test
    void updateCategory_shouldModifyAndLog() {
        when(categoryRepository.findByIdAndDeletedFalse("cat123")).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        request.setName("Nuovo Nome");
        request.setDescription("Nuova Descrizione");

        CategoryResponse updated = categoryService.updateCategory("cat123", request);

        assertThat(updated.getName()).isEqualTo("Nuovo Nome");
        assertThat(updated.getDescription()).isEqualTo("Nuova Descrizione");
        verify(auditService).log("admin", "UPDATE_CATEGORY", "cat123");
    }

    @Test
    void deleteCategory_shouldSoftDeleteAndLog() {
        when(categoryRepository.findByIdAndDeletedFalse("cat123")).thenReturn(Optional.of(category));

        categoryService.deleteCategory("cat123");

        assertThat(category.isDeleted()).isTrue();
        verify(categoryRepository).save(category);
        verify(auditService).log("admin", "DELETE_CATEGORY", "cat123");
    }

    @Test
    void getById_shouldReturnCategory() {
        when(categoryRepository.findByIdAndDeletedFalse("cat123")).thenReturn(Optional.of(category));

        CategoryResponse result = categoryService.getById("cat123");

        assertThat(result.getName()).isEqualTo("Gaming");
    }

    @Test
    void getAll_shouldReturnActiveOnly() {
        Category deleted = new Category();
        deleted.setId("del1");
        deleted.setName("Vecchia");
        deleted.setDeleted(true);

        when(categoryRepository.findAll()).thenReturn(List.of(category, deleted));

        List<CategoryResponse> list = categoryService.getAll();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).isEqualTo("Gaming");
    }
}
