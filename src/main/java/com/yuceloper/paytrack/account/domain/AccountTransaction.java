package com.yuceloper.paytrack.account.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "account_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountTransactionType type;

    @Column(nullable = false)
    private Long accountId;

    private Long counterAccountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private LocalDate occurredOn;

    @Column(nullable = false)
    private String description;

    private String sourceType;
    private Long sourceId;

    @Column(nullable = false)
    @Builder.Default
    private boolean reversed = false;

    private OffsetDateTime reversedAt;

    public void reverse() {
        this.reversed = true;
        this.reversedAt = OffsetDateTime.now();
    }
}
