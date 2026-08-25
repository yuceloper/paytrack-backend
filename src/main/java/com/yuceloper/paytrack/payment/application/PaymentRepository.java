package com.yuceloper.paytrack.payment.application;

import com.yuceloper.paytrack.payment.domain.Payment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(Long id);
    List<Payment> findDueBetween(LocalDate start, LocalDate end);
}
