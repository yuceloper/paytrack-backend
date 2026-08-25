package com.yuceloper.paytrack.payment.infrastructure;

import com.yuceloper.paytrack.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

interface SpringDataPaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByUserIdAndDueDateBetweenOrderByDueDateAsc(Long userId, LocalDate start, LocalDate end);
    List<Payment> findAllByUserIdAndPaidFalseAndDueDateBeforeOrderByDueDateAsc(Long userId, LocalDate today);
    boolean existsByUserIdAndNameAndDueDate(Long userId, String name, LocalDate dueDate);
}
