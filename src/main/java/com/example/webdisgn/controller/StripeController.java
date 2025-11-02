package com.example.webdisgn.controller;

import com.example.webdisgn.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
public class StripeController {

    private final StripeService stripeService;

    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(
            @RequestParam(required = false) String couponCode,
            @AuthenticationPrincipal(expression = "username") String username) {

        String sessionUrl = stripeService.createCheckoutSession(username, couponCode);
        return ResponseEntity.ok(sessionUrl);
    }
}
