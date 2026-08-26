package com.yuceloper.paytrack.account.api.dto;

import com.yuceloper.paytrack.account.domain.AccountNature;
import com.yuceloper.paytrack.account.domain.AccountType;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        Long userId,
        String name,
        AccountType type,
        AccountNature nature,
        String institution,
        BigDecimal balance,
        BigDecimal creditLimit,
        BigDecimal availableLimit,
        String currency,
        boolean active
) {
}
