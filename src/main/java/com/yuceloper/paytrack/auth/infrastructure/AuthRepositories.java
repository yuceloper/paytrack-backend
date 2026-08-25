package com.yuceloper.paytrack.auth.infrastructure;

import com.yuceloper.paytrack.auth.domain.RefreshToken;
import com.yuceloper.paytrack.user.domain.AuthProvider;
import com.yuceloper.paytrack.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface AuthUserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findByAuthProviderAndProviderSubject(AuthProvider authProvider, String providerSubject);
}

interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);
}
