package com.yuceloper.paytrack.loan.application;

import com.yuceloper.paytrack.loan.api.dto.CreateLoanRequest;
import com.yuceloper.paytrack.loan.api.dto.LoanResponse;
import com.yuceloper.paytrack.loan.domain.Loan;
import com.yuceloper.paytrack.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanService {

    private final LoanRepository repository;

    public List<LoanResponse> getByUserId(Long userId) {
        return repository.findAllByUserId(userId).stream().map(this::toResponse).toList();
    }

    public LoanResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public LoanResponse create(CreateLoanRequest request) {
        if (request.remainingInstallments() > request.totalInstallments()) {
            throw new IllegalArgumentException("remainingInstallments cannot exceed totalInstallments");
        }

        Loan loan = Loan.builder()
                .userId(request.userId())
                .name(request.name())
                .institutionName(request.institutionName())
                .installmentAmount(request.installmentAmount())
                .paymentDay(request.paymentDay())
                .totalInstallments(request.totalInstallments())
                .remainingInstallments(request.remainingInstallments())
                .remainingPrincipal(request.remainingPrincipal())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .active(true)
                .build();

        return toResponse(repository.save(loan));
    }

    @Transactional
    public void delete(Long id) {
        getEntity(id);
        repository.deleteById(id);
    }

    private Loan getEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Loan", id));
    }

    private LoanResponse toResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(), loan.getUserId(), loan.getName(), loan.getInstitutionName(),
                loan.getInstallmentAmount(), loan.getPaymentDay(), loan.getTotalInstallments(),
                loan.getRemainingInstallments(), loan.getRemainingPrincipal(), loan.getStartDate(),
                loan.getEndDate(), loan.isActive()
        );
    }
}
