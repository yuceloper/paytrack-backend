package com.yuceloper.paytrack.dashboard.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardSummaryResponse(
        BigDecimal dueThisMonth,
        BigDecimal dueNextSevenDays,
        int upcomingPaymentCount,
        BigDecimal overdueAmount,
        int overduePaymentCount,
        BigDecimal totalCreditCardDebt,
        BigDecimal monthlySubscriptionCost,
        BigDecimal yearlySubscriptionCost,
        BigDecimal expectedIncomeThisMonth,
        BigDecimal receivedIncomeThisMonth,
        BigDecimal plannedNetCashFlowThisMonth,
        String nextIncomeName,
        LocalDate nextIncomeDate,
        BigDecimal nextIncomeAmount,
        BigDecimal requiredUntilNextIncome
) {
}
