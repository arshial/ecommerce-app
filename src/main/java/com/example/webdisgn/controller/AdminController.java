package com.example.webdisgn.controller;

import com.example.webdisgn.dto.response.AdminDashboardResponse;
import com.example.webdisgn.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboardResponse getStats() {
        return adminService.getDashboardStats();
    }
}

