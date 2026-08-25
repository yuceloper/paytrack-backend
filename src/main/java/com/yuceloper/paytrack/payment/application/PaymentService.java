package com.yuceloper.paytrack.payment.application;

import com.yuceloper.paytrack.account.application.AccountTransactionService;
import com.yuceloper.paytrack.payment.api.dto.PaymentResponse;
import com.yuceloper.paytrack.payment.api.dto.PaymentUpsertRequest;
import com.yuceloper.paytrack.payment.domain.Payment;
import com.yuceloper.paytrack.payment.domain.PaymentRecurrenceFrequency;
import com.yuceloper.paytrack.payment.domain.PaymentSeriesScope;
import com.yuceloper.paytrack.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository repository;
    private final PaymentRecurrenceService recurrenceService;
    private final AccountTransactionService accountTransactionService;

    public List<PaymentResponse> getUpcoming(Long userId, int days) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(days - 1L);
        return repository.findDueBetween(userId, start, end).stream()
                .filter(payment -> !payment.isPaid())
                .map(this::toResponse)
                .toList();
    }

    public List<PaymentResponse> getRange(Long userId, LocalDate from, LocalDate to) {
        if (to.isBefore(from)) throw new IllegalArgumentException("to must be on or after from");
        return repository.findDueBetween(userId, from, to).stream().map(this::toResponse).toList();
    }

    @Transactional
    public PaymentResponse create(Long userId, PaymentUpsertRequest request) {
        Payment payment = Payment.builder()
                .userId(userId).name(request.name()).type(request.type())
                .sourceType(request.sourceType()).sourceId(request.sourceId()).amount(request.amount())
                .dueDate(request.dueDate()).recurring(request.recurring())
                .seriesId(request.recurring() ? UUID.randomUUID().toString() : null)
                .recurrenceDay(request.recurrenceDay())
                .recurrenceFrequency(resolveFrequency(request)).recurrenceInterval(resolveInterval(request))
                .recurrenceEndDate(request.recurrenceEndDate())
                .paid(false).institution(request.institution()).note(request.note()).build();
        validateRecurrence(payment);
        return toResponse(repository.save(payment));
    }

    @Transactional
    public PaymentResponse update(Long userId, Long id, PaymentUpsertRequest request, PaymentSeriesScope scope) {
        Payment current = getEntity(userId, id);
        PaymentSeriesScope safeScope = scope != null ? scope : PaymentSeriesScope.THIS;

        if (safeScope == PaymentSeriesScope.THIS) {
            if (current.isRecurring()) {
                recurrenceService.createNextOccurrenceIfNeeded(current);
            }
            applyRequest(current, request, true);
            validateRecurrence(current);
            return toResponse(repository.save(current));
        }

        List<Payment> targets = seriesTargets(userId, current, safeScope);
        for (Payment target : targets) {
            boolean isCurrent = target.getId().equals(current.getId());
            applyRequest(target, request, isCurrent);
            validateRecurrence(target);
        }
        repository.saveAll(targets);
        return toResponse(current);
    }

    @Transactional
    public PaymentResponse markPaid(Long userId, Long id, Long accountId) {
        Payment payment = getEntity(userId, id);
        boolean wasPaid = payment.isPaid();
        payment.markPaid();
        Payment saved = repository.save(payment);
        if (!wasPaid && accountId != null) {
            accountTransactionService.recordExpense(
                    accountId, userId, saved.getAmount(), saved.getName(),
                    "PAYMENT", saved.getId(), LocalDate.now()
            );
        }
        recurrenceService.createNextOccurrenceIfNeeded(saved);
        return toResponse(saved);
    }

    @Transactional
    public PaymentResponse markPending(Long userId, Long id) {
        Payment payment = getEntity(userId, id);
        if (payment.isPaid()) accountTransactionService.reverseExpense("PAYMENT", payment.getId());
        payment.markPending();
        return toResponse(repository.save(payment));
    }

    @Transactional
    public void delete(Long userId, Long id, PaymentSeriesScope scope) {
        Payment current = getEntity(userId, id);
        PaymentSeriesScope safeScope = scope != null ? scope : PaymentSeriesScope.THIS;

        if (safeScope == PaymentSeriesScope.THIS) {
            if (current.isRecurring()) {
                recurrenceService.createNextOccurrenceIfNeeded(current);
            }
            repository.deleteById(id);
            return;
        }

        List<Payment> targets = seriesTargets(userId, current, safeScope);
        repository.deleteAll(targets);
    }

    private List<Payment> seriesTargets(Long userId, Payment current, PaymentSeriesScope scope) {
        if (current.getSeriesId() == null || current.getSeriesId().isBlank()) {
            return List.of(current);
        }

        return repository.findBySeriesId(current.getSeriesId()).stream()
                .filter(item -> userId.equals(item.getUserId()))
                .filter(item -> switch (scope) {
                    case THIS -> item.getId().equals(current.getId());
                    case THIS_AND_FUTURE -> !item.getDueDate().isBefore(current.getDueDate());
                    case ALL -> true;
                })
                .filter(item -> item.getId().equals(current.getId()) || !item.isPaid())
                .toList();
    }

    private void applyRequest(Payment payment, PaymentUpsertRequest request, boolean updateDueDate) {
        payment.setName(request.name());
        payment.setType(request.type());
        payment.setSourceType(request.sourceType());
        payment.setSourceId(request.sourceId());
        payment.setAmount(request.amount());
        if (updateDueDate) payment.setDueDate(request.dueDate());
        payment.setRecurring(request.recurring());
        payment.setRecurrenceDay(request.recurrenceDay());
        payment.setRecurrenceFrequency(resolveFrequency(request));
        payment.setRecurrenceInterval(resolveInterval(request));
        payment.setRecurrenceEndDate(request.recurrenceEndDate());
        payment.setInstitution(request.institution());
        payment.setNote(request.note());

        if (request.recurring() && (payment.getSeriesId() == null || payment.getSeriesId().isBlank())) {
            payment.setSeriesId(UUID.randomUUID().toString());
        }
    }

    private Payment getEntity(Long userId, Long id) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        if (!userId.equals(payment.getUserId())) {
            throw new ResourceNotFoundException("Payment not found: " + id);
        }
        return payment;
    }

    private PaymentRecurrenceFrequency resolveFrequency(PaymentUpsertRequest request) {
        if (!request.recurring()) return null;
        return request.recurrenceFrequency() != null ? request.recurrenceFrequency() : PaymentRecurrenceFrequency.MONTHLY;
    }

    private Integer resolveInterval(PaymentUpsertRequest request) {
        if (!request.recurring()) return null;
        return request.recurrenceInterval() != null ? request.recurrenceInterval() : 1;
    }

    private void validateRecurrence(Payment payment) {
        if (!payment.isRecurring()) return;
        if (payment.getRecurrenceInterval() == null || payment.getRecurrenceInterval() < 1) {
            throw new IllegalArgumentException("recurrenceInterval must be at least 1");
        }
        if (payment.getRecurrenceEndDate() != null && payment.getRecurrenceEndDate().isBefore(payment.getDueDate())) {
            throw new IllegalArgumentException("recurrenceEndDate must be on or after dueDate");
        }
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(), payment.getUserId(), payment.getName(), payment.getType(), payment.getSourceType(), payment.getSourceId(),
                payment.getAmount(), payment.getDueDate(), payment.isRecurring(), payment.getSeriesId(), payment.getRecurrenceDay(),
                payment.getRecurrenceFrequency(), payment.getRecurrenceInterval(), payment.getRecurrenceEndDate(),
                payment.isPaid(), payment.getInstitution(), payment.getNote()
        );
    }
}
