package com.yuceloper.paytrack.subscription.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionResponse(
        Long id,
        Long userId,
        String name,
        String provider,
        BigDecimal amount,
        String currency,
        String billingPeriod,
        Integer billingDay,
        LocalDate nextBillingDate,
        boolean active
) {
}
