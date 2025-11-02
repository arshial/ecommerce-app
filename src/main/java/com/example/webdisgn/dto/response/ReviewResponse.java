package com.example.webdisgn.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private String id;
    private String username;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
