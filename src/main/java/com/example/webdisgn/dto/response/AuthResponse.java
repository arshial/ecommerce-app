package com.example.webdisgn.dto.response;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String name;
    private String role;

    public AuthResponse(String token, String refreshToken, String name, String role) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.name = name;
        this.role = role;
    }
}
