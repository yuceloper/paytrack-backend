package com.yuceloper.paytrack.loan.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateLoanRequest(
        @NotBlank String name,
        @NotBlank String institutionName,
        @NotNull @Positive BigDecimal installmentAmount,
        @NotNull @Min(1) @Max(31) Integer paymentDay,
        @NotNull @Positive Integer totalInstallments,
        @PositiveOrZero Integer paidInstallments,
        @NotNull LocalDate startDate
) {
}
