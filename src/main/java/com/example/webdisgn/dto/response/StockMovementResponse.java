package com.example.webdisgn.dto.response;

import com.example.webdisgn.model.MovementType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockMovementResponse {
    private String id;
    private Long productId;
    private String productName;
    private int quantityChanged;
    private MovementType type;
    private String reason;
    private LocalDateTime timestamp;
}
