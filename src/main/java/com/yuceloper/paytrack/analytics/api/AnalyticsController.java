package com.yuceloper.paytrack.analytics.api;

import com.yuceloper.paytrack.analytics.api.dto.MonthlyAnalyticsResponse;
import com.yuceloper.paytrack.analytics.application.MonthlyAnalyticsService;
import com.yuceloper.paytrack.auth.infrastructure.AuthenticatedUser;
import com.yuceloper.paytrack.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final MonthlyAnalyticsService service;

    @GetMapping("/monthly")
    public ApiResponse<MonthlyAnalyticsResponse> monthly(
            @RequestParam int year,
            @RequestParam int month
    ) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
        return ApiResponse.success(service.getMonthly(AuthenticatedUser.id(), year, month));
    }
}
