package com.example.webdisgn.controller;

import com.example.webdisgn.dto.request.ReviewRequest;
import com.example.webdisgn.dto.response.ReviewResponse;
import com.example.webdisgn.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // ➕ Aggiungi una recensione
    @PostMapping("/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> addReview(@PathVariable Long productId,
                                            @RequestBody ReviewRequest request,
                                            @AuthenticationPrincipal(expression = "username") String username) {
        reviewService.addReview(username, productId, request);
        return ResponseEntity.ok("✅ Recensione aggiunta con successo");
    }

    // ❌ Elimina una recensione
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> deleteReview(@PathVariable String reviewId,
                                               @AuthenticationPrincipal(expression = "username") String username) {
        reviewService.deleteReview(username, reviewId);
        return ResponseEntity.ok("✅ Recensione eliminata");
    }

    // 📄 Ottieni tutte le recensioni di un prodotto
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByProduct(@PathVariable Long productId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByProduct(productId);
        return ResponseEntity.ok(reviews);
    }
    @GetMapping("/product/{productId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long productId) {
        double average = reviewService.getAverageRatingForProduct(productId);
        return ResponseEntity.ok(average);
    }

}
