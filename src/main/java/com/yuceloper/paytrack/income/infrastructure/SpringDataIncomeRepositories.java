package com.yuceloper.paytrack.income.infrastructure;

import com.yuceloper.paytrack.income.domain.IncomeOccurrence;
import com.yuceloper.paytrack.income.domain.IncomeSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

interface SpringDataIncomeSourceRepository extends JpaRepository<IncomeSource, Long> {
    List<IncomeSource> findAllByUserIdOrderByNextIncomeDateAsc(Long userId);
}

interface SpringDataIncomeOccurrenceRepository extends JpaRepository<IncomeOccurrence, Long> {
    Optional<IncomeOccurrence> findByIncomeSourceIdAndExpectedDate(Long incomeSourceId, LocalDate expectedDate);
    List<IncomeOccurrence> findAllByUserIdAndExpectedDateBetweenOrderByExpectedDateAsc(Long userId, LocalDate from, LocalDate to);
}
