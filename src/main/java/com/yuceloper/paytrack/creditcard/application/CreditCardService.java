package com.yuceloper.paytrack.creditcard.application;

import com.yuceloper.paytrack.creditcard.api.dto.CreateCreditCardRequest;
import com.yuceloper.paytrack.creditcard.api.dto.CreditCardResponse;
import com.yuceloper.paytrack.creditcard.domain.CreditCard;
import com.yuceloper.paytrack.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditCardService {

    private final CreditCardRepository repository;

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
    public void delete(Long userId, Long id) {
        getEntity(userId, id);
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
