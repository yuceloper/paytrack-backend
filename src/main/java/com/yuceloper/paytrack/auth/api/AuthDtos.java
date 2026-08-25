package com.yuceloper.paytrack.auth.api;

public class AuthDtos {
    private AuthDtos() {}

    public record SessionResponse(
            Long userId,
            String accessToken,
            String refreshToken,
            long expiresInSeconds,
            boolean guest
    ) {}

    public record RefreshRequest(String refreshToken) {}

    public record GoogleLinkRequest(String idToken) {}
}
