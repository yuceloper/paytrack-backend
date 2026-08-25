package com.yuceloper.paytrack.category.api.dto;

import com.yuceloper.paytrack.category.domain.TransactionCategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class TransactionCategoryDtos {
    private TransactionCategoryDtos() {}

    public record CreateRequest(
            @NotBlank String name,
            @NotNull TransactionCategoryType type,
            String iconKey
    ) {}

    public record Response(
            Long id,
            Long userId,
            String name,
            TransactionCategoryType type,
            String iconKey,
            boolean builtIn,
            boolean active
    ) {}
}
