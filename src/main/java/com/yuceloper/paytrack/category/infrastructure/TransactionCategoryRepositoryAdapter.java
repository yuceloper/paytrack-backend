package com.yuceloper.paytrack.category.infrastructure;

import com.yuceloper.paytrack.category.application.TransactionCategoryRepository;
import com.yuceloper.paytrack.category.domain.TransactionCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransactionCategoryRepositoryAdapter implements TransactionCategoryRepository {
    private final SpringDataTransactionCategoryRepository repository;

    @Override
    public TransactionCategory save(TransactionCategory category) {
        return repository.save(category);
    }

    @Override
    public Optional<TransactionCategory> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<TransactionCategory> findAllByUserId(Long userId) {
        return repository.findAllByUserIdOrderByNameAsc(userId);
    }

    @Override
    public Optional<TransactionCategory> findByUserIdAndName(Long userId, String name) {
        return repository.findByUserIdAndNameIgnoreCase(userId, name);
    }
}
