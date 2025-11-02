package com.example.webdisgn.service;

import com.example.webdisgn.model.RefreshToken;
import com.example.webdisgn.model.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
    RefreshToken verifyExpiration(RefreshToken token);
    void deleteByUser(User user);
    void deleteByToken(String token);
    boolean verifyToken(String token); // verifica se il token esiste (valido)
}
