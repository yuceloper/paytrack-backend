package com.yuceloper.paytrack.payment.api.dto;

import com.yuceloper.paytrack.payment.domain.PaymentSourceType;
import com.yuceloper.paytrack.payment.domain.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentUpsertRequest(
        @NotNull @Positive Long userId,
        @NotBlank String name,
        @NotNull PaymentType type,
        PaymentSourceType sourceType,
        @Positive Long sourceId,
        @NotNull @DecimalMin(value = "0.00") BigDecimal amount,
        @NotNull LocalDate dueDate,
        boolean recurring,
        Integer recurrenceDay,
        String institution,
        String note
) {}
