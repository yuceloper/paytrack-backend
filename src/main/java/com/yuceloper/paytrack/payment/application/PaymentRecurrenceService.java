package com.yuceloper.paytrack.payment.application;

import com.yuceloper.paytrack.payment.domain.Payment;
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

        int preferredDay = payment.getRecurrenceDay() != null
                ? payment.getRecurrenceDay()
                : payment.getDueDate().getDayOfMonth();

        YearMonth nextMonth = YearMonth.from(payment.getDueDate()).plusMonths(1);
        int safeDay = Math.min(preferredDay, nextMonth.lengthOfMonth());
        LocalDate nextDueDate = nextMonth.atDay(safeDay);

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
                .recurrenceDay(preferredDay)
                .paid(false)
                .institution(payment.getInstitution())
                .note(payment.getNote())
                .build();

        repository.save(nextPayment);
    }
}
