package com.yuceloper.paytrack.account.api.dto;

import com.yuceloper.paytrack.account.domain.AccountType;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        Long userId,
        String name,
        AccountType type,
        String institution,
        BigDecimal balance,
        String currency,
        boolean active
) {
}
