package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.dto.request.ReviewRequest;
import com.example.webdisgn.dto.response.ReviewResponse;
import com.example.webdisgn.exeption.GlobalExceptionHandler;
import com.example.webdisgn.model.*;
import com.example.webdisgn.repository.OrderRepository;
import com.example.webdisgn.repository.ProductRepository;
import com.example.webdisgn.repository.ReviewRepository;
import com.example.webdisgn.repository.UserRepository;
import com.example.webdisgn.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    // ➕ Aggiungi recensione (se ha acquistato)
    @Override
    public void addReview(String username, Long productId, ReviewRequest request) {
        User user = userRepository.findByNameAndDeletedFalse(username)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Utente non trovato"));
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Prodotto non trovato"));

        // verifica acquisto
        boolean hasOrdered = orderRepository.findByUser(user).stream()
                .flatMap(order -> order.getItems().stream())
                .anyMatch(item -> item.getProduct().getId().equals(productId));

        if (!hasOrdered) {
            throw new RuntimeException("Puoi recensire solo i prodotti che hai acquistato.");
        }

        if (reviewRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("Hai già recensito questo prodotto.");
        }

        // salva review
        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        reviewRepository.save(review);

        // aggiorna media voto
        List<Review> reviews = reviewRepository.findByProduct(product);
        double avg = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        product.setAverageRating(avg);
        productRepository.save(product);
    }

    // ❌ Elimina recensione (solo autore o admin)
    @Override
    public void deleteReview(String username, String reviewId) {
        User user = userRepository.findByNameAndDeletedFalse(username)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Utente non trovato"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Recensione non trovata"));

        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isOwner = review.getUser().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("Non sei autorizzato a eliminare questa recensione.");
        }

        reviewRepository.delete(review);

        // aggiorna media voto dopo eliminazione
        Product product = review.getProduct();
        List<Review> reviews = reviewRepository.findByProduct(product);
        double avg = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        product.setAverageRating(avg);
        productRepository.save(product);
    }

    // 📄 Lista recensioni per prodotto
    @Override
    public List<ReviewResponse> getReviewsByProduct(Long productId) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Prodotto non trovato"));

        return reviewRepository.findByProduct(product).stream().map(review -> {
            ReviewResponse r = new ReviewResponse();
            r.setId(review.getId());
            r.setUsername(review.getUser().getName());
            r.setRating(review.getRating());
            r.setComment(review.getComment());
            r.setCreatedAt(review.getCreatedAt());
            return r;
        }).toList();
    }

    // ⭐ Calcola media voto (opzionale uso separato)
    @Override
    public double getAverageRatingForProduct(Long productId) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Prodotto non trovato"));

        List<Review> reviews = reviewRepository.findByProduct(product);
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}
