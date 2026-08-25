package com.yuceloper.paytrack.income.infrastructure;

import com.yuceloper.paytrack.income.application.IncomeOccurrenceRepository;
import com.yuceloper.paytrack.income.domain.IncomeOccurrence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IncomeOccurrenceRepositoryAdapter implements IncomeOccurrenceRepository {

    private final SpringDataIncomeOccurrenceRepository repository;

    @Override
    public IncomeOccurrence save(IncomeOccurrence occurrence) {
        return repository.save(occurrence);
    }

    @Override
    public Optional<IncomeOccurrence> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<IncomeOccurrence> findBySourceIdAndExpectedDate(Long sourceId, LocalDate date) {
        return repository.findByIncomeSourceIdAndExpectedDate(sourceId, date);
    }

    @Override
    public List<IncomeOccurrence> findBetween(Long userId, LocalDate from, LocalDate to) {
        return repository.findAllByUserIdAndExpectedDateBetweenOrderByExpectedDateAsc(userId, from, to);
    }

    @Override
    public Optional<IncomeOccurrence> findNextPending(Long userId, LocalDate from) {
        return repository.findFirstByUserIdAndReceivedFalseAndExpectedDateGreaterThanEqualOrderByExpectedDateAsc(userId, from);
    }
}
