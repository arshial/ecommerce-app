package com.example.webdisgn.service;

import com.example.webdisgn.dto.response.AdminAnalyticsResponse;
import com.example.webdisgn.dto.response.AdminDashboardResponse;

public interface AdminService {
    AdminDashboardResponse getDashboardStats();

    AdminAnalyticsResponse getAnalytics();

}
