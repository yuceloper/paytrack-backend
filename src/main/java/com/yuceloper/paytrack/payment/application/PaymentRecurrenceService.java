package com.yuceloper.paytrack.payment.application;

import com.yuceloper.paytrack.payment.domain.Payment;
import com.yuceloper.paytrack.payment.domain.PaymentRecurrenceFrequency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class PaymentRecurrenceService {

    private final PaymentRepository repository;

    public void createNextOccurrenceIfNeeded(Payment payment) {
        if (!payment.isRecurring()) {
            return;
        }

        PaymentRecurrenceFrequency frequency = payment.getRecurrenceFrequency() != null
                ? payment.getRecurrenceFrequency()
                : PaymentRecurrenceFrequency.MONTHLY;
        int interval = payment.getRecurrenceInterval() != null ? payment.getRecurrenceInterval() : 1;

        LocalDate nextDueDate = calculateNextDate(payment, frequency, interval);
        if (payment.getRecurrenceEndDate() != null && nextDueDate.isAfter(payment.getRecurrenceEndDate())) {
            return;
        }

        boolean alreadyExists = repository.existsByUserIdAndNameAndDueDate(
                payment.getUserId(),
                payment.getName(),
                nextDueDate
        );

        if (alreadyExists) {
            return;
        }

        Payment nextPayment = Payment.builder()
                .userId(payment.getUserId())
                .name(payment.getName())
                .type(payment.getType())
                .sourceType(payment.getSourceType())
                .sourceId(payment.getSourceId())
                .amount(payment.getAmount())
                .dueDate(nextDueDate)
                .recurring(true)
                .recurrenceDay(payment.getRecurrenceDay())
                .recurrenceFrequency(frequency)
                .recurrenceInterval(interval)
                .recurrenceEndDate(payment.getRecurrenceEndDate())
                .paid(false)
                .institution(payment.getInstitution())
                .note(payment.getNote())
                .build();

        repository.save(nextPayment);
    }

    private LocalDate calculateNextDate(Payment payment, PaymentRecurrenceFrequency frequency, int interval) {
        LocalDate current = payment.getDueDate();
        return switch (frequency) {
            case WEEKLY -> current.plusWeeks(interval);
            case YEARLY -> safeYearly(current, interval);
            case CUSTOM_DAYS -> current.plusDays(interval);
            case CUSTOM_MONTHS -> safeMonthly(payment, interval);
            case MONTHLY -> safeMonthly(payment, interval);
        };
    }

    private LocalDate safeMonthly(Payment payment, int interval) {
        int preferredDay = payment.getRecurrenceDay() != null
                ? payment.getRecurrenceDay()
                : payment.getDueDate().getDayOfMonth();
        YearMonth target = YearMonth.from(payment.getDueDate()).plusMonths(interval);
        return target.atDay(Math.min(preferredDay, target.lengthOfMonth()));
    }

    private LocalDate safeYearly(LocalDate current, int interval) {
        int targetYear = current.getYear() + interval;
        YearMonth targetMonth = YearMonth.of(targetYear, current.getMonth());
        return targetMonth.atDay(Math.min(current.getDayOfMonth(), targetMonth.lengthOfMonth()));
    }
}
