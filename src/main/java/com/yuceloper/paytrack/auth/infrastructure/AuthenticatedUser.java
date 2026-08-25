package com.yuceloper.paytrack.auth.infrastructure;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthenticatedUser {
    private AuthenticatedUser() {}

    public static Long id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new IllegalStateException("Authenticated user is not available");
        }
        return userId;
    }

    public static Long requireMatches(Long requestedUserId) {
        Long authenticatedUserId = id();
        if (requestedUserId == null || !authenticatedUserId.equals(requestedUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("User id does not match authenticated session");
        }
        return authenticatedUserId;
    }
}
