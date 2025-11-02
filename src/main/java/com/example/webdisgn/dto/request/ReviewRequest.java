package com.example.webdisgn.dto.request;

import lombok.Data;

@Data
public class ReviewRequest {
    private int rating;
    private String comment;
}
