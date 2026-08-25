package com.yuceloper.paytrack.payment.infrastructure;

import com.yuceloper.paytrack.payment.application.PaymentRepository;
import com.yuceloper.paytrack.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JpaPaymentRepository extends JpaRepository<Payment, Long>, PaymentRepository {

    @Override
    default List<Payment> findDueBetween(LocalDate start, LocalDate end) {
        return findAllByDueDateBetweenOrderByDueDateAsc(start, end);
    }

    List<Payment> findAllByDueDateBetweenOrderByDueDateAsc(LocalDate start, LocalDate end);
}
