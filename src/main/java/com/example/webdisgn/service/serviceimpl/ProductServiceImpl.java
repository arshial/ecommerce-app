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
import com.example.webdisgn.service.ProductService;
import com.example.webdisgn.util.MapperProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final AuditService auditService;
    private final StockMovementRepository stockMovementRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, AuditService auditService, StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.auditService = auditService;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Override
    public ProductResponse saveProduct(ProductRequest request) {
        Product product = MapperProduct.toEntity(request);
        product.setDeleted(false);
        productRepository.save(product);
        log.info("Prodotto salvato: {} - €{}", product.getName(), product.getPrice());
        audit(currentUser(), "CREATE_PRODUCT", product.getName());
        return MapperProduct.toResponse(product);
    }

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> page = productRepository.findByDeletedFalse(pageable);
        return page.map(MapperProduct::toResponse);
    }


    @Override
    public ProductResponse getById(long id) {
        log.info("Ricerca prodotto per ID: {}", id);
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Prodotto con ID " + id + " non trovato o eliminato"));
        return MapperProduct.toResponse(product);
    }

    @Override
    public ProductResponse getByName(String name) {
        log.info("Ricerca prodotto per nome: {}", name);
        Product product = productRepository.findByNameAndDeletedFalse(name)
                .orElseThrow(() -> new RuntimeException("Prodotto con nome " + name + " non trovato o eliminato"));
        return MapperProduct.toResponse(product);
    }

    @Override
    public ProductResponse updateProduct(long id, ProductRequest request) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Prodotto con ID " + id + " non trovato o eliminato"));

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        productRepository.save(product);

        log.info("Prodotto aggiornato (ID: {}): {} - €{}", id, product.getName(), product.getPrice());
        audit(currentUser(), "UPDATE_PRODUCT", "ID=" + id);
        return MapperProduct.toResponse(product);
    }

    @Override
    public void deleteProduct(long id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Prodotto con ID " + id + " non trovato"));
        product.setDeleted(true);
        productRepository.save(product);
        log.warn("Soft delete eseguito sul prodotto con ID: {}", id);
        audit(currentUser(), "SOFT_DELETE_PRODUCT", "ID=" + id);
    }

    @Override
    public void uploadImage(long productId, MultipartFile image) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new RuntimeException("Prodotto con ID " + productId + " non trovato"));
        try {
            String uploadDir = "uploads";
            Files.createDirectories(Path.of(uploadDir));
            String fileName = UUID.randomUUID() + "-" + image.getOriginalFilename();
            Path filePath = Path.of(uploadDir, fileName);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            product.setImagePath(filePath.toString().replace("\\", "/"));
            productRepository.save(product);
            log.info("✅ Immagine caricata per prodotto {}: {}", product.getName(), fileName);
            audit(currentUser(), "UPLOAD_IMAGE", product.getName());
        } catch (IOException e) {
            log.error("Errore durante l'upload dell'immagine: {}", e.getMessage());
            throw new RuntimeException("Errore durante l'upload dell'immagine.");
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void updateStock(long productId, UpdateStockRequest request) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new RuntimeException("Prodotto non trovato"));

        int newStock = product.getStock() + request.getQuantity();
        if (newStock < 0) {
            throw new RuntimeException("Lo stock non può diventare negativo.");
        }

        product.setStock(newStock);
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .quantityChanged(request.getQuantity())
                .type(request.getQuantity() >= 0 ? MovementType.IN : MovementType.OUT)
                .reason(request.getReason())
                .timestamp(LocalDateTime.now())
                .build();

        stockMovementRepository.save(movement);

        log.info("🔄 Stock modificato per {}: {} (totale: {})", product.getName(), request.getQuantity(), newStock);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<StockMovementResponse> getStockMovements(Long productId) {
        List<StockMovement> movements = stockMovementRepository.findByProductIdOrderByTimestampDesc(productId);

        return movements.stream().map(m -> {
            StockMovementResponse r = new StockMovementResponse();
            r.setId(m.getId());
            r.setProductId(m.getProduct().getId());
            r.setProductName(m.getProduct().getName());
            r.setQuantityChanged(m.getQuantityChanged());
            r.setType(m.getType());
            r.setReason(m.getReason());
            r.setTimestamp(m.getTimestamp());
            return r;
        }).toList();
    }

    @Override
    public Page<ProductResponse> searchProducts(String keyword, String category, Double min, Double max, Pageable pageable) {
        Page<Product> page = productRepository.searchProducts(keyword, category, min, max, pageable);
        return page.map(MapperProduct::toResponse);
    }

    private void audit(String username, String action, String target) {
        auditService.log(username, action, target);
    }

    private String currentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
