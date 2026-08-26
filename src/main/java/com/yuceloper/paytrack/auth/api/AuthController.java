package com.yuceloper.paytrack.auth.api;

import com.yuceloper.paytrack.auth.infrastructure.AuthService;
import com.yuceloper.paytrack.auth.infrastructure.AuthenticatedUser;
import com.yuceloper.paytrack.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/guest")
    public ApiResponse<AuthDtos.SessionResponse> createGuest() {
        return ApiResponse.success(authService.createGuestSession());
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthDtos.SessionResponse> refresh(@RequestBody AuthDtos.RefreshRequest request) {
        return ApiResponse.success(authService.refresh(request.refreshToken()));
    }

    @GetMapping("/me")
    public ApiResponse<AuthDtos.AccountProfileResponse> me() {
        return ApiResponse.success(authService.getProfile(AuthenticatedUser.id()));
    }

    @PostMapping("/google/link")
    public ApiResponse<AuthDtos.SessionResponse> linkGoogle(@RequestBody AuthDtos.GoogleLinkRequest request) {
        return ApiResponse.success(authService.linkGoogle(AuthenticatedUser.id(), request.idToken()));
    }
}
