package com.example.webdisgn.service;

import com.example.webdisgn.dto.request.ReviewRequest;
import com.example.webdisgn.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {

    void addReview(String username, Long productId, ReviewRequest request);

    void deleteReview(String username, String reviewId);

    List<ReviewResponse> getReviewsByProduct(Long productId);
    double getAverageRatingForProduct(Long productId);

}
