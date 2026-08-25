package com.yuceloper.paytrack.payment.application;

import com.yuceloper.paytrack.payment.api.dto.PaymentResponse;
import com.yuceloper.paytrack.payment.domain.Payment;
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
        LocalDate end = start.plusDays(days);
        return repository.findDueBetween(userId, start, end).stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(), payment.getUserId(), payment.getName(), payment.getType(),
                payment.getSourceType(), payment.getSourceId(), payment.getAmount(), payment.getDueDate(),
                payment.isRecurring(), payment.isPaid(), payment.getInstitution(), payment.getNote()
        );
    }
}
