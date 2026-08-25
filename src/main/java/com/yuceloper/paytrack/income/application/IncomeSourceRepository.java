package com.yuceloper.paytrack.income.application;

import com.yuceloper.paytrack.income.domain.IncomeSource;

import java.util.List;
import java.util.Optional;

public interface IncomeSourceRepository {
    IncomeSource save(IncomeSource source);
    Optional<IncomeSource> findById(Long id);
    List<IncomeSource> findAllByUserId(Long userId);
    void deleteById(Long id);
}
