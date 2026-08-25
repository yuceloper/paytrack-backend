package com.yuceloper.paytrack.income.infrastructure;

import com.yuceloper.paytrack.income.application.IncomeSourceRepository;
import com.yuceloper.paytrack.income.domain.IncomeSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IncomeRepositoryAdapter implements IncomeSourceRepository {

    private final SpringDataIncomeSourceRepository repository;

    @Override
    public IncomeSource save(IncomeSource source) {
        return repository.save(source);
    }

    @Override
    public Optional<IncomeSource> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<IncomeSource> findAllByUserId(Long userId) {
        return repository.findAllByUserIdOrderByNextIncomeDateAsc(userId);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
