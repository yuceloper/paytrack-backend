package com.yuceloper.paytrack.auth.infrastructure;

import com.yuceloper.paytrack.auth.api.AuthDtos;
import com.yuceloper.paytrack.auth.domain.RefreshToken;
import com.yuceloper.paytrack.user.domain.AuthProvider;
import com.yuceloper.paytrack.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthUserJpaRepository userRepository;
    private final RefreshTokenJpaRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${paytrack.auth.refresh-token-days:180}")
    private long refreshTokenDays;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AuthDtos.SessionResponse createGuestSession() {
        String subject = UUID.randomUUID().toString();
        User user = userRepository.save(User.builder()
                .name("Misafir")
                .email("guest-" + subject + "@guest.paytrack.local")
                .authProvider(AuthProvider.GUEST)
                .providerSubject(subject)
                .active(true)
                .build());
        return issueSession(user);
    }

    @Transactional
    public AuthDtos.SessionResponse refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new IllegalArgumentException("refreshToken is required");
        }
        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash(rawRefreshToken))
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (stored.getExpiresAt().isBefore(OffsetDateTime.now())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new IllegalArgumentException("Refresh token expired");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return issueSession(user);
    }

    private AuthDtos.SessionResponse issueSession(User user) {
        String rawRefreshToken = newRefreshToken();
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hash(rawRefreshToken))
                .expiresAt(OffsetDateTime.now().plusDays(refreshTokenDays))
                .build());

        return new AuthDtos.SessionResponse(
                user.getId(),
                jwtService.createAccessToken(user),
                rawRefreshToken,
                jwtService.accessTokenExpiresInSeconds(),
                user.getAuthProvider() == AuthProvider.GUEST
        );
    }

    private String newRefreshToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash refresh token", e);
        }
    }
}
