package com.example.webdisgn.controller;

import com.example.webdisgn.dto.request.CouponRequest;
import com.example.webdisgn.dto.response.CouponResponse;
import com.example.webdisgn.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CouponResponse create(@Valid @RequestBody CouponRequest request) {
        return couponService.createCoupon(request);
    }

    @GetMapping("/validate")
    public CouponResponse validate(@RequestParam String code) {
        return couponService.validateCoupon(code);
    }
}
