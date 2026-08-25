package com.yuceloper.paytrack.payment.application;

import com.yuceloper.paytrack.payment.api.dto.PaymentResponse;
import com.yuceloper.paytrack.payment.api.dto.PaymentUpsertRequest;
import com.yuceloper.paytrack.payment.domain.Payment;
import com.yuceloper.paytrack.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository repository;

    public List<PaymentResponse> getUpcoming(Long userId, int days) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(days - 1L);
        return repository.findDueBetween(userId, start, end).stream()
                .filter(payment -> !payment.isPaid())
                .map(this::toResponse)
                .toList();
    }

    public List<PaymentResponse> getRange(Long userId, LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("to must be on or after from");
        }
        return repository.findDueBetween(userId, from, to).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PaymentResponse create(PaymentUpsertRequest request) {
        Payment payment = Payment.builder()
                .userId(request.userId())
                .name(request.name())
                .type(request.type())
                .sourceType(request.sourceType())
                .sourceId(request.sourceId())
                .amount(request.amount())
                .dueDate(request.dueDate())
                .recurring(request.recurring())
                .recurrenceDay(request.recurrenceDay())
                .paid(false)
                .institution(request.institution())
                .note(request.note())
                .build();
        return toResponse(repository.save(payment));
    }

    @Transactional
    public PaymentResponse update(Long id, PaymentUpsertRequest request) {
        Payment payment = getEntity(id);
        payment.setUserId(request.userId());
        payment.setName(request.name());
        payment.setType(request.type());
        payment.setSourceType(request.sourceType());
        payment.setSourceId(request.sourceId());
        payment.setAmount(request.amount());
        payment.setDueDate(request.dueDate());
        payment.setRecurring(request.recurring());
        payment.setRecurrenceDay(request.recurrenceDay());
        payment.setInstitution(request.institution());
        payment.setNote(request.note());
        return toResponse(repository.save(payment));
    }

    @Transactional
    public PaymentResponse markPaid(Long id) {
        Payment payment = getEntity(id);
        payment.markPaid();
        return toResponse(repository.save(payment));
    }

    @Transactional
    public PaymentResponse markPending(Long id) {
        Payment payment = getEntity(id);
        payment.markPending();
        return toResponse(repository.save(payment));
    }

    @Transactional
    public void delete(Long id) {
        getEntity(id);
        repository.deleteById(id);
    }

    private Payment getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(), payment.getUserId(), payment.getName(), payment.getType(),
                payment.getSourceType(), payment.getSourceId(), payment.getAmount(), payment.getDueDate(),
                payment.isRecurring(), payment.isPaid(), payment.getInstitution(), payment.getNote()
        );
    }
}
