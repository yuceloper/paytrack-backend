package com.yuceloper.paytrack.analytics.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyAnalyticsResponse(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netCashFlow,
        BigDecimal previousMonthExpense,
        BigDecimal expenseChangePercent,
        BigDecimal incomeExpenseRatio,
        String topExpenseCategory,
        int transactionCount,
        List<CategoryBreakdown> expenseCategories
) {
    public record CategoryBreakdown(
            Long categoryId,
            String categoryName,
            BigDecimal amount,
            BigDecimal percentage
    ) {}
}
