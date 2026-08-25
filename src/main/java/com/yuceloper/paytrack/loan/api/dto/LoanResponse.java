package com.yuceloper.paytrack.loan.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanResponse(
        Long id,
        Long userId,
        String name,
        String institutionName,
        BigDecimal installmentAmount,
        Integer paymentDay,
        Integer totalInstallments,
        Integer remainingInstallments,
        BigDecimal remainingPrincipal,
        LocalDate startDate,
        LocalDate endDate,
        boolean active
) {
}
