package com.yuceloper.paytrack.payment.application;

import com.yuceloper.paytrack.payment.domain.Payment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    List<Payment> saveAll(List<Payment> payments);
    Optional<Payment> findById(Long id);
    List<Payment> findDueBetween(Long userId, LocalDate start, LocalDate end);
    List<Payment> findOverdue(Long userId, LocalDate today);
    List<Payment> findBySeriesId(String seriesId);
    boolean existsByUserIdAndNameAndDueDate(Long userId, String name, LocalDate dueDate);
    void deleteById(Long id);
    void deleteAll(List<Payment> payments);
}
