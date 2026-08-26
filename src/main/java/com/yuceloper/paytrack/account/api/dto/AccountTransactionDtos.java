package com.yuceloper.paytrack.account.api.dto;

import com.yuceloper.paytrack.account.domain.AccountTransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class AccountTransactionDtos {
    private AccountTransactionDtos() {}

    public record TransferRequest(
            @NotNull Long fromAccountId,
            @NotNull Long toAccountId,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotBlank String description,
            LocalDate occurredOn
    ) {}

    public record ManualRequest(
            @NotNull Long accountId,
            @NotNull AccountTransactionType type,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            Long categoryId,
            @NotBlank String description,
            LocalDate occurredOn
    ) {}

    public record BalanceAdjustmentRequest(
            @NotNull Long accountId,
            @NotNull BigDecimal targetBalance,
            @NotBlank String description,
            LocalDate occurredOn
    ) {}

    public record Response(
            Long id,
            Long userId,
            AccountTransactionType type,
            Long accountId,
            Long counterAccountId,
            Long categoryId,
            BigDecimal amount,
            String currency,
            LocalDate occurredOn,
            String description,
            String sourceType,
            Long sourceId,
            boolean reversed
    ) {}
}
