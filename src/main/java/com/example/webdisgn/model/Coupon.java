package com.example.webdisgn.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private double discountValue;

    private LocalDateTime expiresAt;

    private boolean singleUse = false;

    private boolean used = false;

    private int maxUses = 1;

    private int usedCount = 0;

    private boolean active = true;
}
