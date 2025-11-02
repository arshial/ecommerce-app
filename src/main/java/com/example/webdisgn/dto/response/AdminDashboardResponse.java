package com.example.webdisgn.dto.response;

import lombok.Data;

import java.util.Map;

@Data
public class AdminDashboardResponse {
    private long totalOrders;
    private double totalRevenue;
    private Map<String, Long> ordersByStatus;
}
