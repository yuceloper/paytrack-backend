package com.yuceloper.paytrack.creditcard.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreditCardPaymentRequest(
        @NotNull Long accountId,
        @NotNull @Positive BigDecimal amount,
        LocalDate occurredOn
) {
}
