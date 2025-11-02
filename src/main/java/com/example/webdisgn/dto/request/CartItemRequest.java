package com.example.webdisgn.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequest {

    @NotNull(message = "ID prodotto obbligatorio")
    private Long productId;

    @Min(value = 1, message = "La quantità deve essere almeno 1")
    private int quantity;
}
