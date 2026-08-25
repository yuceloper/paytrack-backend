package com.yuceloper.paytrack.income.application;

import com.yuceloper.paytrack.income.domain.IncomeOccurrence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IncomeOccurrenceRepository {
    IncomeOccurrence save(IncomeOccurrence occurrence);
    List<IncomeOccurrence> saveAll(List<IncomeOccurrence> occurrences);
    Optional<IncomeOccurrence> findById(Long id);
    Optional<IncomeOccurrence> findBySourceIdAndExpectedDate(Long sourceId, LocalDate date);
    List<IncomeOccurrence> findBetween(Long userId, LocalDate from, LocalDate to);
    List<IncomeOccurrence> findBySourceId(Long sourceId);
    Optional<IncomeOccurrence> findNextPending(Long userId, LocalDate from);
    void delete(IncomeOccurrence occurrence);
    void deleteAll(List<IncomeOccurrence> occurrences);
}
