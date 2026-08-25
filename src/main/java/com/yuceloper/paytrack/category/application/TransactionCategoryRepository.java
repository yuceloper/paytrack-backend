package com.yuceloper.paytrack.category.application;

import com.yuceloper.paytrack.category.domain.TransactionCategory;

import java.util.List;
import java.util.Optional;

public interface TransactionCategoryRepository {
    TransactionCategory save(TransactionCategory category);
    Optional<TransactionCategory> findById(Long id);
    List<TransactionCategory> findAllByUserId(Long userId);
    Optional<TransactionCategory> findByUserIdAndName(Long userId, String name);
}
