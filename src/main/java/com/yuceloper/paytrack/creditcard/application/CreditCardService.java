package com.yuceloper.paytrack.creditcard.application;

import com.yuceloper.paytrack.account.application.AccountTransactionService;
import com.yuceloper.paytrack.creditcard.api.dto.CreateCreditCardRequest;
import com.yuceloper.paytrack.creditcard.api.dto.CreditCardPaymentRequest;
import com.yuceloper.paytrack.creditcard.api.dto.CreditCardResponse;
import com.yuceloper.paytrack.creditcard.domain.CreditCard;
import com.yuceloper.paytrack.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditCardService {

    private final CreditCardRepository repository;
    private final AccountTransactionService accountTransactionService;

    public List<CreditCardResponse> getByUserId(Long userId) {
        return repository.findAllByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public CreditCardResponse getById(Long userId, Long id) {
        return toResponse(getEntity(userId, id));
    }

    @Transactional
    public CreditCardResponse create(Long userId, CreateCreditCardRequest request) {
        BigDecimal creditLimit = request.creditLimit();
        BigDecimal currentDebt = defaultMoney(request.currentDebt());
        if (creditLimit != null && currentDebt.compareTo(creditLimit) > 0) {
            throw new IllegalArgumentException("Current debt cannot exceed credit limit");
        }

        CreditCard card = CreditCard.builder()
                .userId(userId)
                .name(request.name().trim())
                .bankName(request.bankName().trim())
                .lastFourDigits(request.lastFourDigits())
                .statementDay(request.statementDay())
                .dueDay(request.dueDay())
                .creditLimit(creditLimit)
                .currentDebt(currentDebt)
                .minimumPayment(defaultMoney(request.minimumPayment()))
                .active(true)
                .build();

        return toResponse(repository.save(card));
    }

    @Transactional
    public CreditCardResponse pay(Long userId, Long id, CreditCardPaymentRequest request) {
        CreditCard card = getEntity(userId, id);
        BigDecimal debt = defaultMoney(card.getCurrentDebt());
        if (debt.signum() <= 0) {
            throw new IllegalArgumentException("Credit card has no outstanding debt");
        }
        if (request.amount().compareTo(debt) > 0) {
            throw new IllegalArgumentException("Payment amount cannot exceed current debt");
        }

        accountTransactionService.recordNonAnalyticOutflow(
                request.accountId(),
                userId,
                request.amount(),
                card.getName() + " kredi kartı ödemesi",
                "CREDIT_CARD_PAYMENT",
                card.getId(),
                request.occurredOn() != null ? request.occurredOn() : LocalDate.now()
        );

        BigDecimal remainingDebt = debt.subtract(request.amount());
        card.setCurrentDebt(remainingDebt);
        if (remainingDebt.signum() == 0) {
            card.setMinimumPayment(BigDecimal.ZERO);
        } else if (card.getMinimumPayment() != null && card.getMinimumPayment().compareTo(remainingDebt) > 0) {
            card.setMinimumPayment(remainingDebt);
        }
        return toResponse(repository.save(card));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        CreditCard card = getEntity(userId, id);
        if (defaultMoney(card.getCurrentDebt()).signum() > 0) {
            throw new IllegalArgumentException("A credit card with outstanding debt cannot be deleted");
        }
        repository.deleteById(id);
    }

    private CreditCard getEntity(Long userId, Long id) {
        CreditCard card = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CreditCard", id));
        if (!userId.equals(card.getUserId())) {
            throw new ResourceNotFoundException("CreditCard", id);
        }
        return card;
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private CreditCardResponse toResponse(CreditCard card) {
        BigDecimal availableLimit = card.getCreditLimit() == null
                ? null
                : card.getCreditLimit().subtract(card.getCurrentDebt());
        return new CreditCardResponse(
                card.getId(),
                card.getUserId(),
                card.getName(),
                card.getBankName(),
                card.getLastFourDigits(),
                card.getStatementDay(),
                card.getDueDay(),
                card.getCreditLimit(),
                card.getCurrentDebt(),
                availableLimit,
                card.getMinimumPayment(),
                card.isActive()
        );
    }
}
