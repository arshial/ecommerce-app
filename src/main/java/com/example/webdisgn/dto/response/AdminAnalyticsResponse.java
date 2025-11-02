package com.example.webdisgn.dto.response;

import lombok.Data;

import java.util.Map;

@Data
public class AdminAnalyticsResponse {
    private Map<String, Long> ordersPerDay;
    private Map<String, Long> ordersPerMonth;
    private String topProductName;
    private long topProductSold;
    private Map<String, Long> quantityPerCategory;
    private double totalRevenue;
}
