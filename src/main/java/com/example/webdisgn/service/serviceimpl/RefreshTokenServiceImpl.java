package com.example.webdisgn.service.serviceimpl;

import com.example.webdisgn.model.RefreshToken;
import com.example.webdisgn.exeption.GlobalExceptionHandler;
import com.example.webdisgn.model.User;
import com.example.webdisgn.repository.RefreshTokenRepository;
import com.example.webdisgn.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public RefreshToken createRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusSeconds(60 * 60 * 24)); // 24 ore
        return refreshTokenRepository.save(token);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new GlobalExceptionHandler.ExpiredTokenException("Il refresh token è scaduto. Esegui nuovamente il login.");
        }
        return token;
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }

    @Override
    public boolean verifyToken(String token) {
        return refreshTokenRepository.findByToken(token).isPresent();
    }
}
