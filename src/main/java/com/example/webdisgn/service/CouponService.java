package com.example.webdisgn.service;

import com.example.webdisgn.dto.request.CouponRequest;
import com.example.webdisgn.dto.response.CouponResponse;

public interface CouponService {
    CouponResponse createCoupon(CouponRequest request);
    CouponResponse validateCoupon(String code);
    String syncCouponToStripe(String code);

}
