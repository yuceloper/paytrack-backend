package com.yuceloper.paytrack.creditcard.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateCreditCardRequest(
        @NotNull Long userId,
        @NotBlank String name,
        @NotBlank String bankName,
        @Pattern(regexp = "\\d{4}", message = "lastFourDigits must contain exactly 4 digits") String lastFourDigits,
        @NotNull @Min(1) @Max(31) Integer statementDay,
        @NotNull @Min(1) @Max(31) Integer dueDay,
        @PositiveOrZero BigDecimal currentDebt,
        @PositiveOrZero BigDecimal minimumPayment
) {
}
