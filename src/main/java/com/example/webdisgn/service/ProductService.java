package com.example.webdisgn.service;

import com.example.webdisgn.dto.request.ProductRequest;
import com.example.webdisgn.dto.request.UpdateStockRequest;
import com.example.webdisgn.dto.response.ProductResponse;
import com.example.webdisgn.dto.response.StockMovementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
     Page<ProductResponse> getAllProducts(Pageable pageable);
     ProductResponse getById(long id);
     ProductResponse getByName(String name);
     ProductResponse saveProduct(ProductRequest request);
     ProductResponse updateProduct(long id, ProductRequest request);
     void deleteProduct(long id);
     void uploadImage(long productId, MultipartFile image);
     void updateStock(long productId, UpdateStockRequest request);
     List<StockMovementResponse> getStockMovements(Long productId);
     Page<ProductResponse> searchProducts(String keyword, String category, Double min, Double max, Pageable pageable);
}
