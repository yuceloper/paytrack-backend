package com.yuceloper.paytrack.income.infrastructure;

import com.yuceloper.paytrack.income.application.IncomeOccurrenceRepository;
import com.yuceloper.paytrack.income.application.IncomeSourceRepository;
import com.yuceloper.paytrack.income.domain.IncomeOccurrence;
import com.yuceloper.paytrack.income.domain.IncomeSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IncomeRepositoryAdapter implements IncomeSourceRepository, IncomeOccurrenceRepository {

    private final SpringDataIncomeSourceRepository sourceRepository;
    private final SpringDataIncomeOccurrenceRepository occurrenceRepository;

    @Override
    public IncomeSource save(IncomeSource source) {
        return sourceRepository.save(source);
    }

    @Override
    public Optional<IncomeSource> findById(Long id) {
        return sourceRepository.findById(id);
    }

    @Override
    public List<IncomeSource> findAllByUserId(Long userId) {
        return sourceRepository.findAllByUserIdOrderByNextIncomeDateAsc(userId);
    }

    @Override
    public void deleteById(Long id) {
        sourceRepository.deleteById(id);
    }

    @Override
    public IncomeOccurrence save(IncomeOccurrence occurrence) {
        return occurrenceRepository.save(occurrence);
    }

    @Override
    public Optional<IncomeOccurrence> findBySourceIdAndExpectedDate(Long sourceId, LocalDate date) {
        return occurrenceRepository.findByIncomeSourceIdAndExpectedDate(sourceId, date);
    }

    @Override
    public List<IncomeOccurrence> findBetween(Long userId, LocalDate from, LocalDate to) {
        return occurrenceRepository.findAllByUserIdAndExpectedDateBetweenOrderByExpectedDateAsc(userId, from, to);
    }
}
