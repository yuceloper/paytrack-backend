package com.yuceloper.paytrack.income.api.dto;

import com.yuceloper.paytrack.income.domain.IncomeFrequency;
import com.yuceloper.paytrack.income.domain.IncomeType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class IncomeResponses {
    private IncomeResponses() {}

    public record Source(
            Long id,
            Long userId,
            String name,
            IncomeType type,
            BigDecimal amount,
            String currency,
            IncomeFrequency frequency,
            Integer recurrenceDay,
            Integer recurrenceInterval,
            LocalDate recurrenceEndDate,
            LocalDate nextIncomeDate,
            boolean active,
            String note
    ) {}

    public record Occurrence(
            Long id,
            Long incomeSourceId,
            Long userId,
            String name,
            BigDecimal amount,
            String currency,
            LocalDate expectedDate,
            boolean received,
            OffsetDateTime receivedAt
    ) {}
}
