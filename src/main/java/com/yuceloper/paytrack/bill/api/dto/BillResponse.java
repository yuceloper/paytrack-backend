package com.yuceloper.paytrack.bill.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BillResponse(
        Long id,
        Long userId,
        String name,
        String provider,
        String category,
        BigDecimal expectedAmount,
        Integer dueDay,
        LocalDate nextDueDate,
        boolean active
) {
}
