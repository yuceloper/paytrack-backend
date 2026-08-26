package com.yuceloper.paytrack.auth.infrastructure;

import com.yuceloper.paytrack.auth.api.AuthDtos;
import com.yuceloper.paytrack.auth.domain.RefreshToken;
import com.yuceloper.paytrack.user.domain.AuthProvider;
import com.yuceloper.paytrack.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final GoogleIdentityVerifier googleIdentityVerifier;
    private final JdbcTemplate jdbcTemplate;

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

    @Transactional(readOnly = true)
    public AuthDtos.AccountProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new AuthDtos.AccountProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAuthProvider().name(),
                user.getAuthProvider() == AuthProvider.GUEST
        );
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
                .filter(User::isActive)
                .orElseThrow(() -> new IllegalArgumentException("User not found or inactive"));
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return issueSession(user);
    }

    @Transactional
    public AuthDtos.SessionResponse linkGoogle(Long currentUserId, String idToken) {
        User current = userRepository.findById(currentUserId)
                .filter(User::isActive)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        GoogleIdentityVerifier.GoogleIdentity identity = googleIdentityVerifier.verify(idToken);

        var linkedBySubject = userRepository.findByAuthProviderAndProviderSubject(AuthProvider.GOOGLE, identity.subject());
        if (linkedBySubject.isPresent() && !linkedBySubject.get().getId().equals(current.getId())) {
            if (current.getAuthProvider() != AuthProvider.GUEST) {
                throw new IllegalArgumentException("This Google account is already linked to another PayTrack account");
            }

            User existingGoogleUser = linkedBySubject.get();
            if (!existingGoogleUser.isActive()) {
                throw new IllegalArgumentException("Linked Google account is inactive");
            }

            mergeGuestIntoUser(current, existingGoogleUser);
            return issueSession(existingGoogleUser);
        }

        var linkedByEmail = userRepository.findByEmailIgnoreCase(identity.email());
        if (linkedByEmail.isPresent() && !linkedByEmail.get().getId().equals(current.getId())) {
            User existing = linkedByEmail.get();
            if (current.getAuthProvider() == AuthProvider.GUEST
                    && existing.isActive()
                    && existing.getAuthProvider() == AuthProvider.GOOGLE) {
                mergeGuestIntoUser(current, existing);
                return issueSession(existing);
            }
            throw new IllegalArgumentException("This email is already used by another PayTrack account");
        }

        current.setAuthProvider(AuthProvider.GOOGLE);
        current.setProviderSubject(identity.subject());
        current.setEmail(identity.email());
        if (identity.name() != null && !identity.name().isBlank()) {
            current.setName(identity.name());
        }
        userRepository.save(current);
        return issueSession(current);
    }

    private void mergeGuestIntoUser(User guest, User target) {
        Long guestId = guest.getId();
        Long targetId = target.getId();

        jdbcTemplate.update("""
                UPDATE account_transactions tx
                   SET category_id = target_category.id
                  FROM transaction_categories guest_category
                  JOIN transaction_categories target_category
                    ON target_category.user_id = ?
                   AND target_category.name = guest_category.name
                 WHERE guest_category.user_id = ?
                   AND tx.category_id = guest_category.id
                """, targetId, guestId);

        jdbcTemplate.update("""
                DELETE FROM transaction_categories guest_category
                 USING transaction_categories target_category
                 WHERE guest_category.user_id = ?
                   AND target_category.user_id = ?
                   AND target_category.name = guest_category.name
                """, guestId, targetId);

        moveUserOwnedRows("credit_cards", guestId, targetId);
        moveUserOwnedRows("loans", guestId, targetId);
        moveUserOwnedRows("subscriptions", guestId, targetId);
        moveUserOwnedRows("bills", guestId, targetId);
        moveUserOwnedRows("accounts", guestId, targetId);
        moveUserOwnedRows("transaction_categories", guestId, targetId);
        moveUserOwnedRows("payments", guestId, targetId);
        moveUserOwnedRows("income_sources", guestId, targetId);
        moveUserOwnedRows("income_occurrences", guestId, targetId);
        moveUserOwnedRows("account_transactions", guestId, targetId);

        jdbcTemplate.update("UPDATE refresh_tokens SET revoked = TRUE WHERE user_id = ?", guestId);
        guest.setActive(false);
        userRepository.save(guest);
    }

    private void moveUserOwnedRows(String tableName, Long fromUserId, Long toUserId) {
        jdbcTemplate.update("UPDATE " + tableName + " SET user_id = ? WHERE user_id = ?", toUserId, fromUserId);
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
