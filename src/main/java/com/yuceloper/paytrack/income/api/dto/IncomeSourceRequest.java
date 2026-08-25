package com.yuceloper.paytrack.income.api.dto;

import com.yuceloper.paytrack.income.domain.IncomeFrequency;
import com.yuceloper.paytrack.income.domain.IncomeType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeSourceRequest(
        @NotNull Long userId,
        @NotBlank String name,
        @NotNull IncomeType type,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency,
        @NotNull IncomeFrequency frequency,
        @Min(1) @Max(31) Integer recurrenceDay,
        @Min(1) Integer recurrenceInterval,
        LocalDate recurrenceEndDate,
        @NotNull LocalDate nextIncomeDate,
        String note
) {}
