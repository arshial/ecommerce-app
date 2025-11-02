package com.example.webdisgn.controller;

import com.example.webdisgn.dto.response.AdminAnalyticsResponse;
import com.example.webdisgn.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final AdminService adminService;

    // ✅ Solo per utenti con ruolo ADMIN
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAnalyticsResponse> getAnalyticsOverview() {
        AdminAnalyticsResponse response = adminService.getAnalytics();
        return ResponseEntity.ok(response);
    }
}
