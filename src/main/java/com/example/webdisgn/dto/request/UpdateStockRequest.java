package com.example.webdisgn.dto.request;

import lombok.Data;

@Data
public class UpdateStockRequest {
    private int quantity;
    private String reason;
}
