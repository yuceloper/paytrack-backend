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

    public CreditCardResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public CreditCardResponse create(CreateCreditCardRequest request) {
        CreditCard card = CreditCard.builder()
                .userId(request.userId())
                .name(request.name())
                .bankName(request.bankName())
                .lastFourDigits(request.lastFourDigits())
                .statementDay(request.statementDay())
                .dueDay(request.dueDay())
                .currentDebt(defaultMoney(request.currentDebt()))
                .minimumPayment(defaultMoney(request.minimumPayment()))
                .active(true)
                .build();

        return toResponse(repository.save(card));
    }

    @Transactional
    public void delete(Long id) {
        getEntity(id);
        repository.deleteById(id);
    }

    private CreditCard getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CreditCard", id));
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private CreditCardResponse toResponse(CreditCard card) {
        return new CreditCardResponse(
                card.getId(),
                card.getUserId(),
                card.getName(),
                card.getBankName(),
                card.getLastFourDigits(),
                card.getStatementDay(),
                card.getDueDay(),
                card.getCurrentDebt(),
                card.getMinimumPayment(),
                card.isActive()
        );
    }
}
