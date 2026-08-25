package com.yuceloper.paytrack.income.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "income_occurrences", uniqueConstraints = @UniqueConstraint(name = "uk_income_occurrence_source_date", columnNames = {"income_source_id", "expected_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeOccurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "income_source_id", nullable = false)
    private Long incomeSourceId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "expected_date", nullable = false)
    private LocalDate expectedDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean received = false;

    private OffsetDateTime receivedAt;

    public void markReceived() {
        this.received = true;
        this.receivedAt = OffsetDateTime.now();
    }

    public void markPending() {
        this.received = false;
        this.receivedAt = null;
    }
}
