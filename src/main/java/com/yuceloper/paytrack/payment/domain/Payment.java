package com.yuceloper.paytrack.payment.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    private PaymentSourceType sourceType;

    private Long sourceId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean recurring = false;

    private String seriesId;

    private Integer recurrenceDay;

    @Enumerated(EnumType.STRING)
    private PaymentRecurrenceFrequency recurrenceFrequency;

    private Integer recurrenceInterval;

    private LocalDate recurrenceEndDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean paid = false;

    private String institution;
    private String note;

    public void markPaid() {
        this.paid = true;
    }

    public void markPending() {
        this.paid = false;
    }
}
