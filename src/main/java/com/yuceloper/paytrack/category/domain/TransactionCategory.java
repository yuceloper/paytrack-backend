package com.yuceloper.paytrack.category.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transaction_categories", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionCategoryType type;

    private String iconKey;

    @Column(nullable = false)
    @Builder.Default
    private boolean builtIn = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
