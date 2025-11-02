package com.example.webdisgn.dto.request;

import com.example.webdisgn.model.DiscountType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CouponRequest {

    @NotBlank
    private String code;

    @NotNull
    private DiscountType discountType;

    @Positive
    private double discountValue;

    private LocalDateTime expiresAt;

    private boolean singleUse = false;

    @Min(1)
    private int maxUses = 1;

    private boolean active = true;
}
