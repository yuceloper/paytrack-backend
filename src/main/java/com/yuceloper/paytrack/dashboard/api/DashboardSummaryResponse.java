package com.yuceloper.paytrack.dashboard.api;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        BigDecimal dueThisMonth,
        BigDecimal dueNextSevenDays,
        int upcomingPaymentCount,
        BigDecimal overdueAmount,
        int overduePaymentCount,
        BigDecimal totalCreditCardDebt,
        BigDecimal monthlySubscriptionCost,
        BigDecimal yearlySubscriptionCost
) {
}
