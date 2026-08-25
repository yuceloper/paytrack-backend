package com.yuceloper.paytrack.income.application;

import com.yuceloper.paytrack.income.api.dto.IncomeResponses;
import com.yuceloper.paytrack.income.api.dto.IncomeSourceRequest;
import com.yuceloper.paytrack.income.domain.*;
import com.yuceloper.paytrack.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncomeService {

    private final IncomeSourceRepository sourceRepository;
    private final IncomeOccurrenceRepository occurrenceRepository;

    public List<IncomeResponses.Source> getSources(Long userId) {
        return sourceRepository.findAllByUserId(userId).stream().map(this::toSource).toList();
    }

    public List<IncomeResponses.Occurrence> getOccurrences(Long userId, LocalDate from, LocalDate to) {
        if (to.isBefore(from)) throw new IllegalArgumentException("to must be on or after from");
        return occurrenceRepository.findBetween(userId, from, to).stream().map(this::toOccurrence).toList();
    }

    @Transactional
    public IncomeResponses.Source createSource(IncomeSourceRequest request) {
        IncomeSource source = sourceRepository.save(IncomeSource.builder()
                .userId(request.userId())
                .name(request.name())
                .type(request.type())
                .amount(request.amount())
                .currency(request.currency().trim().toUpperCase())
                .frequency(request.frequency())
                .recurrenceDay(request.recurrenceDay())
                .nextIncomeDate(request.nextIncomeDate())
                .active(true)
                .note(request.note())
                .build());
        ensureOccurrence(source, source.getNextIncomeDate());
        return toSource(source);
    }

    @Transactional
    public IncomeResponses.Occurrence markReceived(Long occurrenceId) {
        IncomeOccurrence occurrence = occurrenceRepository.findById(occurrenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Income occurrence not found: " + occurrenceId));
        if (!occurrence.isReceived()) occurrence.markReceived();
        occurrenceRepository.save(occurrence);

        IncomeSource source = sourceRepository.findById(occurrence.getIncomeSourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Income source not found: " + occurrence.getIncomeSourceId()));
        if (source.isActive() && source.getFrequency() != IncomeFrequency.ONE_TIME) {
            LocalDate nextDate = nextDate(source, occurrence.getExpectedDate());
            source.setNextIncomeDate(nextDate);
            sourceRepository.save(source);
            ensureOccurrence(source, nextDate);
        }
        return toOccurrence(occurrence);
    }

    @Transactional
    public IncomeResponses.Occurrence markPending(Long occurrenceId) {
        IncomeOccurrence occurrence = occurrenceRepository.findById(occurrenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Income occurrence not found: " + occurrenceId));
        occurrence.markPending();
        return toOccurrence(occurrenceRepository.save(occurrence));
    }

    private void ensureOccurrence(IncomeSource source, LocalDate date) {
        if (occurrenceRepository.findBySourceIdAndExpectedDate(source.getId(), date).isPresent()) return;
        occurrenceRepository.save(IncomeOccurrence.builder()
                .incomeSourceId(source.getId())
                .userId(source.getUserId())
                .name(source.getName())
                .amount(source.getAmount())
                .currency(source.getCurrency())
                .expectedDate(date)
                .received(false)
                .build());
    }

    private LocalDate nextDate(IncomeSource source, LocalDate current) {
        return switch (source.getFrequency()) {
            case WEEKLY -> current.plusWeeks(1);
            case YEARLY -> current.plusYears(1);
            case MONTHLY -> {
                LocalDate nextMonth = current.plusMonths(1).withDayOfMonth(1);
                int desiredDay = source.getRecurrenceDay() != null ? source.getRecurrenceDay() : current.getDayOfMonth();
                yield nextMonth.withDayOfMonth(Math.min(desiredDay, nextMonth.lengthOfMonth()));
            }
            case ONE_TIME -> current;
        };
    }

    private IncomeResponses.Source toSource(IncomeSource s) {
        return new IncomeResponses.Source(s.getId(), s.getUserId(), s.getName(), s.getType(), s.getAmount(), s.getCurrency(), s.getFrequency(), s.getRecurrenceDay(), s.getNextIncomeDate(), s.isActive(), s.getNote());
    }

    private IncomeResponses.Occurrence toOccurrence(IncomeOccurrence o) {
        return new IncomeResponses.Occurrence(o.getId(), o.getIncomeSourceId(), o.getUserId(), o.getName(), o.getAmount(), o.getCurrency(), o.getExpectedDate(), o.isReceived(), o.getReceivedAt());
    }
}
