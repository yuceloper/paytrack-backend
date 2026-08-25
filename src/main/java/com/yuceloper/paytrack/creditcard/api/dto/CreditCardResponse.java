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
        BigDecimal currentDebt,
        BigDecimal minimumPayment,
        boolean active
) {
}
