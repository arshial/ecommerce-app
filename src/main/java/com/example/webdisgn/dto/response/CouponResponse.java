package com.example.webdisgn.dto.response;

import com.example.webdisgn.model.DiscountType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CouponResponse {
    private String id;
    private String code;
    private DiscountType discountType;
    private double discountValue;
    private LocalDateTime expiresAt;
    private boolean singleUse;
    private int maxUses;
    private int usedCount;
    private boolean active;
    private boolean used;
}
