package com.yuceloper.paytrack.creditcard.api.dto;

import java.math.BigDecimal;

public record CreditCardResponse(
        Long id,
        Long userId,
        String name,
        String bankName,
        String lastFourDigits,
        Integer statementDay,
        Integer dueDay,
        BigDecimal creditLimit,
        BigDecimal currentDebt,
        BigDecimal availableLimit,
        BigDecimal minimumPayment,
        boolean active
) {
}
