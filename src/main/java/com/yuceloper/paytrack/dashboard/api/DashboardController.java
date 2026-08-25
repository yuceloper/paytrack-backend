package com.yuceloper.paytrack.dashboard.api;

import com.yuceloper.paytrack.auth.infrastructure.AuthenticatedUser;
import com.yuceloper.paytrack.dashboard.application.DashboardService;
import com.yuceloper.paytrack.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> getSummary() {
        return ApiResponse.success(dashboardService.getSummary(AuthenticatedUser.id(), LocalDate.now()));
    }
}
