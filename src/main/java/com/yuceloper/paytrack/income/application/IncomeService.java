package com.yuceloper.paytrack.income.application;

import com.yuceloper.paytrack.account.application.AccountTransactionService;
import com.yuceloper.paytrack.income.api.dto.IncomeOccurrenceUpdateRequest;
import com.yuceloper.paytrack.income.api.dto.IncomeResponses;
import com.yuceloper.paytrack.income.api.dto.IncomeSourceRequest;
import com.yuceloper.paytrack.income.domain.*;
import com.yuceloper.paytrack.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncomeService {

    private final IncomeSourceRepository sourceRepository;
    private final IncomeOccurrenceRepository occurrenceRepository;
    private final AccountTransactionService accountTransactionService;

    public List<IncomeResponses.Source> getSources(Long userId) {
        return sourceRepository.findAllByUserId(userId).stream().map(this::toSource).toList();
    }

    public List<IncomeResponses.Occurrence> getOccurrences(Long userId, LocalDate from, LocalDate to) {
        if (to.isBefore(from)) throw new IllegalArgumentException("to must be on or after from");
        return occurrenceRepository.findBetween(userId, from, to).stream().map(this::toOccurrence).toList();
    }

    @Transactional
    public IncomeResponses.Source createSource(IncomeSourceRequest request) {
        validateRequest(request);
        IncomeSource source = sourceRepository.save(IncomeSource.builder()
                .userId(request.userId()).name(request.name()).type(request.type()).amount(request.amount())
                .currency(request.currency().trim().toUpperCase()).frequency(request.frequency())
                .recurrenceDay(resolveRecurrenceDay(request)).recurrenceInterval(resolveInterval(request))
                .recurrenceEndDate(request.recurrenceEndDate()).nextIncomeDate(request.nextIncomeDate())
                .active(true).note(request.note()).build());
        ensureOccurrence(source, source.getNextIncomeDate());
        return toSource(source);
    }

    @Transactional
    public IncomeResponses.Occurrence updateOccurrence(
            Long occurrenceId,
            IncomeOccurrenceUpdateRequest request,
            IncomeSeriesScope scope
    ) {
        IncomeOccurrence current = getOccurrence(occurrenceId);
        if (current.isReceived()) {
            throw new IllegalArgumentException("Received income occurrences cannot be edited");
        }

        IncomeSource source = getSource(current.getIncomeSourceId());
        validateOccurrenceUpdate(request);

        if (scope == IncomeSeriesScope.THIS) {
            applyOccurrence(current, request.name(), request.amount(), request.expectedDate());
            return toOccurrence(occurrenceRepository.save(current));
        }

        applySource(source, request);
        List<IncomeOccurrence> targets = occurrenceRepository.findBySourceId(source.getId()).stream()
                .filter(item -> !item.isReceived())
                .filter(item -> scope == IncomeSeriesScope.ALL || !item.getExpectedDate().isBefore(current.getExpectedDate()))
                .sorted(Comparator.comparing(IncomeOccurrence::getExpectedDate))
                .toList();

        LocalDate date = request.expectedDate();
        for (IncomeOccurrence target : targets) {
            applyOccurrence(target, request.name(), request.amount(), date);
            if (source.getFrequency() != IncomeFrequency.ONE_TIME) {
                date = nextDate(source, date);
            }
        }
        occurrenceRepository.saveAll(targets);

        source.setNextIncomeDate(request.expectedDate());
        source.setActive(true);
        if (source.getRecurrenceEndDate() != null && request.expectedDate().isAfter(source.getRecurrenceEndDate())) {
            source.setActive(false);
        }
        sourceRepository.save(source);

        return toOccurrence(current);
    }

    @Transactional
    public void deleteOccurrence(Long occurrenceId, IncomeSeriesScope scope) {
        IncomeOccurrence current = getOccurrence(occurrenceId);
        if (current.isReceived()) {
            throw new IllegalArgumentException("Received income occurrences cannot be deleted");
        }

        IncomeSource source = getSource(current.getIncomeSourceId());
        if (scope == IncomeSeriesScope.THIS) {
            occurrenceRepository.delete(current);
            if (source.getFrequency() == IncomeFrequency.ONE_TIME) {
                source.setActive(false);
            } else {
                LocalDate next = nextDate(source, current.getExpectedDate());
                if (source.getRecurrenceEndDate() != null && next.isAfter(source.getRecurrenceEndDate())) {
                    source.setActive(false);
                } else {
                    source.setNextIncomeDate(next);
                    ensureOccurrence(source, next);
                }
            }
            sourceRepository.save(source);
            return;
        }

        List<IncomeOccurrence> targets = occurrenceRepository.findBySourceId(source.getId()).stream()
                .filter(item -> !item.isReceived())
                .filter(item -> scope == IncomeSeriesScope.ALL || !item.getExpectedDate().isBefore(current.getExpectedDate()))
                .toList();
        occurrenceRepository.deleteAll(targets);
        source.setActive(false);
        sourceRepository.save(source);
    }

    @Transactional
    public IncomeResponses.Occurrence markReceived(Long occurrenceId, Long accountId) {
        IncomeOccurrence occurrence = getOccurrence(occurrenceId);
        boolean wasReceived = occurrence.isReceived();
        if (!wasReceived) occurrence.markReceived();
        occurrenceRepository.save(occurrence);

        if (!wasReceived && accountId != null) {
            accountTransactionService.recordIncome(
                    accountId, occurrence.getUserId(), occurrence.getAmount(), occurrence.getName(),
                    "INCOME_OCCURRENCE", occurrence.getId(), LocalDate.now()
            );
        }

        IncomeSource source = getSource(occurrence.getIncomeSourceId());
        if (source.isActive() && source.getFrequency() != IncomeFrequency.ONE_TIME) {
            LocalDate nextDate = nextDate(source, occurrence.getExpectedDate());
            if (source.getRecurrenceEndDate() != null && nextDate.isAfter(source.getRecurrenceEndDate())) {
                source.setActive(false);
                sourceRepository.save(source);
            } else {
                source.setNextIncomeDate(nextDate);
                sourceRepository.save(source);
                ensureOccurrence(source, nextDate);
            }
        }
        return toOccurrence(occurrence);
    }

    @Transactional
    public IncomeResponses.Occurrence markPending(Long occurrenceId) {
        IncomeOccurrence occurrence = getOccurrence(occurrenceId);
        if (occurrence.isReceived()) accountTransactionService.reverseIncome("INCOME_OCCURRENCE", occurrence.getId());
        occurrence.markPending();
        return toOccurrence(occurrenceRepository.save(occurrence));
    }

    private IncomeOccurrence getOccurrence(Long id) {
        return occurrenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Income occurrence not found: " + id));
    }

    private IncomeSource getSource(Long id) {
        return sourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Income source not found: " + id));
    }

    private void applyOccurrence(IncomeOccurrence occurrence, String name, java.math.BigDecimal amount, LocalDate expectedDate) {
        occurrence.setName(name);
        occurrence.setAmount(amount);
        occurrence.setExpectedDate(expectedDate);
    }

    private void applySource(IncomeSource source, IncomeOccurrenceUpdateRequest request) {
        source.setName(request.name());
        source.setAmount(request.amount());
        source.setFrequency(request.frequency());
        source.setRecurrenceDay(
                request.frequency() == IncomeFrequency.MONTHLY || request.frequency() == IncomeFrequency.CUSTOM_MONTHS
                        ? (request.recurrenceDay() != null ? request.recurrenceDay() : request.expectedDate().getDayOfMonth())
                        : null
        );
        source.setRecurrenceInterval(request.frequency() == IncomeFrequency.ONE_TIME
                ? null
                : (request.recurrenceInterval() != null ? request.recurrenceInterval() : 1));
        source.setRecurrenceEndDate(request.recurrenceEndDate());
    }

    private void ensureOccurrence(IncomeSource source, LocalDate date) {
        if (source.getRecurrenceEndDate() != null && date.isAfter(source.getRecurrenceEndDate())) return;
        if (occurrenceRepository.findBySourceIdAndExpectedDate(source.getId(), date).isPresent()) return;
        occurrenceRepository.save(IncomeOccurrence.builder()
                .incomeSourceId(source.getId()).userId(source.getUserId()).name(source.getName())
                .amount(source.getAmount()).currency(source.getCurrency()).expectedDate(date).received(false).build());
    }

    private LocalDate nextDate(IncomeSource source, LocalDate current) {
        int interval = source.getRecurrenceInterval() != null ? source.getRecurrenceInterval() : 1;
        return switch (source.getFrequency()) {
            case WEEKLY -> current.plusWeeks(interval);
            case YEARLY -> safeYearly(current, interval);
            case CUSTOM_DAYS -> current.plusDays(interval);
            case CUSTOM_MONTHS -> safeMonthly(source, current, interval);
            case MONTHLY -> safeMonthly(source, current, interval);
            case ONE_TIME -> current;
        };
    }

    private LocalDate safeMonthly(IncomeSource source, LocalDate current, int interval) {
        YearMonth target = YearMonth.from(current).plusMonths(interval);
        int desiredDay = source.getRecurrenceDay() != null ? source.getRecurrenceDay() : current.getDayOfMonth();
        return target.atDay(Math.min(desiredDay, target.lengthOfMonth()));
    }

    private LocalDate safeYearly(LocalDate current, int interval) {
        YearMonth target = YearMonth.of(current.getYear() + interval, current.getMonth());
        return target.atDay(Math.min(current.getDayOfMonth(), target.lengthOfMonth()));
    }

    private Integer resolveRecurrenceDay(IncomeSourceRequest request) {
        if (request.frequency() == IncomeFrequency.MONTHLY || request.frequency() == IncomeFrequency.CUSTOM_MONTHS) {
            return request.recurrenceDay() != null ? request.recurrenceDay() : request.nextIncomeDate().getDayOfMonth();
        }
        return null;
    }

    private Integer resolveInterval(IncomeSourceRequest request) {
        if (request.frequency() == IncomeFrequency.ONE_TIME) return null;
        return request.recurrenceInterval() != null ? request.recurrenceInterval() : 1;
    }

    private void validateRequest(IncomeSourceRequest request) {
        if (request.recurrenceEndDate() != null && request.recurrenceEndDate().isBefore(request.nextIncomeDate())) {
            throw new IllegalArgumentException("recurrenceEndDate must be on or after nextIncomeDate");
        }
        if (request.frequency() != IncomeFrequency.ONE_TIME &&
                request.recurrenceInterval() != null && request.recurrenceInterval() < 1) {
            throw new IllegalArgumentException("recurrenceInterval must be at least 1");
        }
    }

    private void validateOccurrenceUpdate(IncomeOccurrenceUpdateRequest request) {
        if (request.recurrenceEndDate() != null && request.recurrenceEndDate().isBefore(request.expectedDate())) {
            throw new IllegalArgumentException("recurrenceEndDate must be on or after expectedDate");
        }
        if (request.frequency() != IncomeFrequency.ONE_TIME &&
                request.recurrenceInterval() != null && request.recurrenceInterval() < 1) {
            throw new IllegalArgumentException("recurrenceInterval must be at least 1");
        }
    }

    private IncomeResponses.Source toSource(IncomeSource s) {
        return new IncomeResponses.Source(
                s.getId(), s.getUserId(), s.getName(), s.getType(), s.getAmount(), s.getCurrency(),
                s.getFrequency(), s.getRecurrenceDay(), s.getRecurrenceInterval(), s.getRecurrenceEndDate(),
                s.getNextIncomeDate(), s.isActive(), s.getNote()
        );
    }

    private IncomeResponses.Occurrence toOccurrence(IncomeOccurrence o) {
        return new IncomeResponses.Occurrence(o.getId(), o.getIncomeSourceId(), o.getUserId(), o.getName(), o.getAmount(), o.getCurrency(), o.getExpectedDate(), o.isReceived(), o.getReceivedAt());
    }
}
