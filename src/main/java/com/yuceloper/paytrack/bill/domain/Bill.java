package com.yuceloper.paytrack.bill.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    private String provider;

    @Column(nullable = false)
    private String category;

    @Column(precision = 19, scale = 2)
    private BigDecimal expectedAmount;

    @Column(nullable = false)
    private Integer dueDay;

    private LocalDate nextDueDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
