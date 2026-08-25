package com.yuceloper.paytrack.creditcard.application;

import com.yuceloper.paytrack.creditcard.domain.CreditCard;

import java.util.List;
import java.util.Optional;

public interface CreditCardRepository {
    CreditCard save(CreditCard creditCard);
    Optional<CreditCard> findById(Long id);
    List<CreditCard> findAllByUserId(Long userId);
    void deleteById(Long id);
}
