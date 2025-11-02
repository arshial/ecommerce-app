package com.example.webdisgn.service;

public interface AuditService {
    void log(String username, String action, String target);
}
