package com.yuceloper.paytrack.bill.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateBillRequest(
        @NotNull Long userId,
        @NotBlank String name,
        String provider,
        @NotBlank String category,
        @PositiveOrZero BigDecimal expectedAmount,
        @NotNull @Min(1) @Max(31) Integer dueDay,
        LocalDate nextDueDate
) {
}
