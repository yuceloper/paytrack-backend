package com.yuceloper.paytrack.loan.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String institutionName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal installmentAmount;

    @Column(nullable = false)
    private Integer paymentDay;

    @Column(nullable = false)
    private Integer totalInstallments;

    @Column(nullable = false)
    private Integer remainingInstallments;

    @Column(precision = 19, scale = 2)
    private BigDecimal remainingPrincipal;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
