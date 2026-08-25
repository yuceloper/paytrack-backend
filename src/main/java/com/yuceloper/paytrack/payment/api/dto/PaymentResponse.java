package com.yuceloper.paytrack.payment.api.dto;

import com.yuceloper.paytrack.payment.domain.PaymentRecurrenceFrequency;
import com.yuceloper.paytrack.payment.domain.PaymentSourceType;
import com.yuceloper.paytrack.payment.domain.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(
        Long id,
        Long userId,
        String name,
        PaymentType type,
        PaymentSourceType sourceType,
        Long sourceId,
        BigDecimal amount,
        LocalDate dueDate,
        boolean recurring,
        String seriesId,
        Integer recurrenceDay,
        PaymentRecurrenceFrequency recurrenceFrequency,
        Integer recurrenceInterval,
        LocalDate recurrenceEndDate,
        boolean paid,
        String institution,
        String note
) {
}
