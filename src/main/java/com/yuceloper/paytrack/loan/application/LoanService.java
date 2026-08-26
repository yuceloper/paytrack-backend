package com.yuceloper.paytrack.loan.application;

import com.yuceloper.paytrack.loan.api.dto.CreateLoanRequest;
import com.yuceloper.paytrack.loan.api.dto.LoanResponse;
import com.yuceloper.paytrack.loan.domain.Loan;
import com.yuceloper.paytrack.payment.application.PaymentRepository;
import com.yuceloper.paytrack.payment.domain.Payment;
import com.yuceloper.paytrack.payment.domain.PaymentSourceType;
import com.yuceloper.paytrack.payment.domain.PaymentType;
import com.yuceloper.paytrack.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanService {

    private final LoanRepository repository;
    private final PaymentRepository paymentRepository;

    public List<LoanResponse> getByUserId(Long userId) {
        return repository.findAllByUserId(userId).stream().map(this::toResponse).toList();
    }

    public LoanResponse getById(Long userId, Long id) {
        return toResponse(getEntity(userId, id));
    }

    @Transactional
    public LoanResponse create(Long userId, CreateLoanRequest request) {
        int paidInstallments = request.paidInstallments() == null ? 0 : request.paidInstallments();
        if (paidInstallments > request.totalInstallments()) {
            throw new IllegalArgumentException("paidInstallments cannot exceed totalInstallments");
        }

        int remainingInstallments = request.totalInstallments() - paidInstallments;
        LocalDate firstDueDate = firstPaymentDate(request.startDate(), request.paymentDay());
        LocalDate endDate = paymentDate(
                firstDueDate.plusMonths(request.totalInstallments() - 1L),
                request.paymentDay()
        );

        Loan loan = Loan.builder()
                .userId(userId)
                .name(request.name().trim())
                .institutionName(request.institutionName().trim())
                .installmentAmount(request.installmentAmount())
                .paymentDay(request.paymentDay())
                .totalInstallments(request.totalInstallments())
                .remainingInstallments(remainingInstallments)
                .remainingPrincipal(null)
                .startDate(request.startDate())
                .endDate(endDate)
                .active(remainingInstallments > 0)
                .build();

        Loan saved = repository.save(loan);
        createRemainingInstallmentPayments(saved, firstDueDate, paidInstallments);
        return toResponse(saved);
    }

    @Transactional
    public void installmentPaid(Long userId, Payment payment) {
        Loan loan = linkedLoan(userId, payment);
        if (loan == null) return;

        int remaining = Math.max(loan.getRemainingInstallments() - 1, 0);
        loan.setRemainingInstallments(remaining);
        loan.setActive(remaining > 0);
        repository.save(loan);
    }

    @Transactional
    public void installmentReverted(Long userId, Payment payment) {
        Loan loan = linkedLoan(userId, payment);
        if (loan == null) return;

        int remaining = Math.min(loan.getRemainingInstallments() + 1, loan.getTotalInstallments());
        loan.setRemainingInstallments(remaining);
        loan.setActive(remaining > 0);
        repository.save(loan);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Loan loan = getEntity(userId, id);
        List<Payment> installments = paymentRepository.findBySeriesId(seriesId(loan.getId()));
        if (installments.stream().anyMatch(Payment::isPaid)) {
            throw new IllegalArgumentException("A loan with paid installments cannot be deleted");
        }
        paymentRepository.deleteAll(installments);
        repository.deleteById(id);
    }

    private Loan linkedLoan(Long userId, Payment payment) {
        if (payment.getSourceType() != PaymentSourceType.LOAN || payment.getSourceId() == null) {
            return null;
        }
        return getEntity(userId, payment.getSourceId());
    }

    private void createRemainingInstallmentPayments(
            Loan loan,
            LocalDate firstDueDate,
            int paidInstallments
    ) {
        int remaining = loan.getRemainingInstallments();
        if (remaining <= 0) return;

        List<Payment> payments = new ArrayList<>(remaining);
        for (int i = 0; i < remaining; i++) {
            int installmentNo = paidInstallments + i + 1;
            LocalDate dueDate = paymentDate(
                    firstDueDate.plusMonths(paidInstallments + i),
                    loan.getPaymentDay()
            );
            payments.add(Payment.builder()
                    .userId(loan.getUserId())
                    .name(loan.getName() + " taksit " + installmentNo + "/" + loan.getTotalInstallments())
                    .type(PaymentType.LOAN)
                    .sourceType(PaymentSourceType.LOAN)
                    .sourceId(loan.getId())
                    .amount(loan.getInstallmentAmount())
                    .dueDate(dueDate)
                    .recurring(false)
                    .seriesId(seriesId(loan.getId()))
                    .paid(false)
                    .institution(loan.getInstitutionName())
                    .note("Kredi taksiti")
                    .build());
        }

        paymentRepository.saveAll(payments);
    }

    private LocalDate firstPaymentDate(LocalDate startDate, int paymentDay) {
        LocalDate candidate = paymentDate(startDate, paymentDay);
        if (candidate.isBefore(startDate)) {
            candidate = paymentDate(startDate.plusMonths(1), paymentDay);
        }
        return candidate;
    }

    private LocalDate paymentDate(LocalDate date, int paymentDay) {
        YearMonth month = YearMonth.from(date);
        return month.atDay(Math.min(paymentDay, month.lengthOfMonth()));
    }

    private String seriesId(Long loanId) {
        return "loan:" + loanId;
    }

    private Loan getEntity(Long userId, Long id) {
        Loan loan = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", id));
        if (!loan.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Loan", id);
        }
        return loan;
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
