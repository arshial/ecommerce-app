package com.example.webdisgn.dto.request;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
