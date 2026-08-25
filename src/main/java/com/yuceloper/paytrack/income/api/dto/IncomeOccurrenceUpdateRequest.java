package com.yuceloper.paytrack.income.api.dto;

import com.yuceloper.paytrack.income.domain.IncomeFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeOccurrenceUpdateRequest(
        @NotBlank String name,
        @NotNull @Positive BigDecimal amount,
        @NotNull LocalDate expectedDate,
        @NotNull IncomeFrequency frequency,
        Integer recurrenceDay,
        Integer recurrenceInterval,
        LocalDate recurrenceEndDate
) {}
