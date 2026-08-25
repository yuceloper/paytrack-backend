package com.yuceloper.paytrack.category.infrastructure;

import com.yuceloper.paytrack.category.domain.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface SpringDataTransactionCategoryRepository extends JpaRepository<TransactionCategory, Long> {
    List<TransactionCategory> findAllByUserIdOrderByNameAsc(Long userId);
    Optional<TransactionCategory> findByUserIdAndNameIgnoreCase(Long userId, String name);
}
