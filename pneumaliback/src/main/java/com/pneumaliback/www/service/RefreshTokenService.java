package com.pneumaliback.www.service;

import com.pneumaliback.www.entity.RefreshToken;
import com.pneumaliback.www.entity.User;
import com.pneumaliback.www.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration:604800000}") // 7 jours par défaut
    private long refreshTokenDurationMs;

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expiré. Veuillez vous reconnecter.");
        }
        return token;
    }

    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public void revokeByUser(User user) {
        refreshTokenRepository.revokeByUser(user);
    }

    @Scheduled(fixedRate = 86400000) // Exécution quotidienne
    public void cleanupExpiredTokens() {
        log.info("Nettoyage des refresh tokens expirés...");
        refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
        log.info("Nettoyage terminé");
    }
}
