package com.yuceloper.paytrack.account.api.dto;

import com.yuceloper.paytrack.account.domain.AccountNature;
import com.yuceloper.paytrack.account.domain.AccountType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AccountUpsertRequest(
        @NotBlank String name,
        @NotNull AccountType type,
        AccountNature nature,
        String institution,
        @NotNull BigDecimal balance,
        @DecimalMin("0.00") BigDecimal creditLimit,
        @NotBlank @Size(min = 3, max = 3) String currency,
        Boolean active
) {
}
