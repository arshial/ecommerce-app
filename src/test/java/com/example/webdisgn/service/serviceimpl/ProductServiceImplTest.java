package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.dto.request.ProductRequest;
import com.example.webdisgn.dto.request.UpdateStockRequest;
import com.example.webdisgn.dto.response.ProductResponse;
import com.example.webdisgn.dto.response.StockMovementResponse;
import com.example.webdisgn.model.MovementType;
import com.example.webdisgn.model.Product;
import com.example.webdisgn.model.StockMovement;
import com.example.webdisgn.repository.ProductRepository;
import com.example.webdisgn.repository.StockMovementRepository;
import com.example.webdisgn.service.AuditService;
import com.example.webdisgn.util.MapperProduct;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private AuditService auditService;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductRequest request;
    private Product product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Simula utente autenticato per SecurityContextHolder
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testUser");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);

        // Setup prodotto di esempio
        request = new ProductRequest();
        request.setName("Mouse");
        request.setPrice(29.99);
        request.setDescription("Mouse gaming");

        product = MapperProduct.toEntity(request);
        product.setId(1L);
        product.setStock(10);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();  // pulizia dopo ogni test
    }

    @Test
    void saveProduct_shouldReturnDto() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse res = productService.saveProduct(request);

        assertThat(res).isNotNull();
        assertThat(res.getName()).isEqualTo("Mouse");
        verify(auditService).log("testUser", "CREATE_PRODUCT", "Mouse");
    }

    @Test
    void getAllProducts_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product));

        when(productRepository.findByDeletedFalse(pageable)).thenReturn(page);

        Page<ProductResponse> result = productService.getAllProducts(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Mouse");
    }

    @Test
    void getById_shouldReturnProduct() {
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));

        ProductResponse result = productService.getById(1L);

        assertThat(result.getName()).isEqualTo("Mouse");
    }

    @Test
    void getByName_shouldReturnProduct() {
        when(productRepository.findByNameAndDeletedFalse("Mouse")).thenReturn(Optional.of(product));

        ProductResponse result = productService.getByName("Mouse");

        assertThat(result.getPrice()).isEqualTo(29.99);
    }

    @Test
    void updateProduct_shouldModifyAndReturnUpdatedProduct() {
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        request.setName("Mouse RGB");
        request.setPrice(49.99);

        ProductResponse updated = productService.updateProduct(1L, request);

        assertThat(updated.getName()).isEqualTo("Mouse RGB");
        assertThat(updated.getPrice()).isEqualTo(49.99);
        verify(auditService).log("testUser", "UPDATE_PRODUCT", "ID=1");
    }

    @Test
    void deleteProduct_shouldMarkAsDeleted() {
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        assertThat(product.isDeleted()).isTrue();
        verify(productRepository).save(product);
        verify(auditService).log("testUser", "SOFT_DELETE_PRODUCT", "ID=1");
    }

    @Test
    void updateStock_shouldIncreaseQuantity() {
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));

        UpdateStockRequest update = new UpdateStockRequest();
        update.setQuantity(5);
        update.setReason("Restock");

        productService.updateStock(1L, update);

        assertThat(product.getStock()).isEqualTo(15);
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    void searchProducts_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.searchProducts("mouse", null, null, null, pageable)).thenReturn(page);

        Page<ProductResponse> results = productService.searchProducts("mouse", null, null, null, pageable);

        assertThat(results.getTotalElements()).isEqualTo(1);
        assertThat(results.getContent().get(0).getName()).isEqualTo("Mouse");
    }

    @Test
    void getStockMovements_shouldMapCorrectly() {
        StockMovement movement = StockMovement.builder()
                .id("1")
                .product(product)
                .quantityChanged(5)
                .type(MovementType.IN)
                .reason("Test")
                .timestamp(LocalDateTime.now())
                .build();

        when(stockMovementRepository.findByProductIdOrderByTimestampDesc(1L)).thenReturn(List.of(movement));

        List<StockMovementResponse> list = productService.getStockMovements(1L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getProductName()).isEqualTo("Mouse");
    }
}
