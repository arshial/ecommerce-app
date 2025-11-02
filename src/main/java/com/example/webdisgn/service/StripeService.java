package com.example.webdisgn.service;

public interface StripeService {
    String createCheckoutSession(String username, String couponCode);
}
