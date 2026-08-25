package com.yuceloper.paytrack.payment.infrastructure;

import com.yuceloper.paytrack.payment.application.PaymentRepository;
import com.yuceloper.paytrack.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final SpringDataPaymentRepository repository;

    @Override
    public Payment save(Payment payment) {
        return repository.save(payment);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Payment> findDueBetween(Long userId, LocalDate start, LocalDate end) {
        return repository.findAllByUserIdAndDueDateBetweenOrderByDueDateAsc(userId, start, end);
    }

    @Override
    public List<Payment> findOverdue(Long userId, LocalDate today) {
        return repository.findAllByUserIdAndPaidFalseAndDueDateBeforeOrderByDueDateAsc(userId, today);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
