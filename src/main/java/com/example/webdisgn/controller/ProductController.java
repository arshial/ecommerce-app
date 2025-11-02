package com.example.webdisgn.controller;

import com.example.webdisgn.dto.request.ProductRequest;
import com.example.webdisgn.dto.request.UpdateStockRequest;
import com.example.webdisgn.dto.response.ProductResponse;
import com.example.webdisgn.dto.response.StockMovementResponse;
import com.example.webdisgn.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name,asc") String[] sort) {

        Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<ProductResponse> result = productService.getAllProducts(pageable);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable long id) {
        log.info("Chiamata GET /api/products/{}", id);
        return productService.getById(id);
    }

    @GetMapping("/name/{name}")
    public ProductResponse getByName(@PathVariable String name) {
        log.info("Chiamata GET /api/products/name/{}", name);
        return productService.getByName(name);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double min,
            @RequestParam(required = false) Double max,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name,asc") String[] sort) {

        Sort sorting = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<ProductResponse> result = productService.searchProducts(keyword, category, min, max, pageable);

        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        log.info("Chiamata POST /api/products - creazione prodotto: {} - €{}", request.getName(), request.getPrice());
        return productService.saveProduct(request);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable long id,
                                         @Valid @RequestBody ProductRequest request) {
        log.info("Chiamata PUT /api/products/{} - aggiornamento prodotto: {} - €{}", id, request.getName(), request.getPrice());
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable long id) {
        log.warn("Chiamata DELETE /api/products/{} - eliminazione prodotto", id);
        productService.deleteProduct(id);
    }

    @PostMapping("/{id}/upload-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadImage(@PathVariable long id, @RequestParam("image") MultipartFile image) {
        productService.uploadImage(id, image);
        return ResponseEntity.ok("Immagine caricata con successo.");
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateStock(@PathVariable Long id,
                                              @RequestBody UpdateStockRequest request) {
        productService.updateStock(id, request);
        return ResponseEntity.ok("✅ Stock aggiornato con successo");
    }

    @GetMapping("/{id}/stock-movements")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StockMovementResponse>> getStockMovements(@PathVariable Long id) {
        List<StockMovementResponse> list = productService.getStockMovements(id);
        return ResponseEntity.ok(list);
    }
}
