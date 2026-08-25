package com.yuceloper.paytrack.subscription.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateSubscriptionRequest(
        @NotNull Long userId,
        @NotBlank String name,
        String provider,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String billingPeriod,
        @Min(1) @Max(31) Integer billingDay,
        LocalDate nextBillingDate
) {
}
