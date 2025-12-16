package com.hvc.brandlocus.security.filter.filterservice;

import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.entities.RefreshToken;
import com.hvc.brandlocus.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenServiceInterface{
    private final RefreshTokenRepository refreshTokenRepository;

    // Token validity duration in minutes (e.g., 7 days)
    private final long refreshTokenDurationMs = 7 * 24 * 60 * 60 * 1000L;


    @Override
    public RefreshToken createRefreshToken(BaseUser user) {

        Optional<RefreshToken> existingTokenOpt =
                refreshTokenRepository.findFirstByUserOrderByIdDesc(user);

        RefreshToken refreshToken;

        if (existingTokenOpt.isPresent()) {

            refreshToken = existingTokenOpt.get();

            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setRevoked(false);
            refreshToken.setExpiryDate(
                    LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000)
            );

        } else {
            refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(UUID.randomUUID().toString())
                    .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000))
                    .revoked(false)
                    .build();
        }

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public boolean isValid(RefreshToken token) {
        return token != null &&
                !token.isRevoked() &&
                token.getExpiryDate().isAfter(LocalDateTime.now());
    }

    @Override
    public void revokeToken(RefreshToken token) {
        if (token != null) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        }
    }

    @Override
    public void revokeAllTokensForUser(BaseUser user) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserAndRevokedFalse(user);
        for (RefreshToken token : tokens) {
            token.setRevoked(true);
        }
        refreshTokenRepository.saveAll(tokens);
    }

    @Override
    public RefreshToken getByToken(String tokenStr) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(tokenStr);
        return tokenOpt.orElse(null);
    }
}
