package com.yuceloper.paytrack.account.api.dto;

import com.yuceloper.paytrack.account.domain.AccountType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AccountUpsertRequest(
        @NotNull Long userId,
        @NotBlank String name,
        @NotNull AccountType type,
        String institution,
        @NotNull BigDecimal balance,
        @NotBlank @Size(min = 3, max = 3) String currency,
        Boolean active
) {
}
