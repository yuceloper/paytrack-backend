package com.yuceloper.paytrack.creditcard.infrastructure;

import com.yuceloper.paytrack.creditcard.application.CreditCardRepository;
import com.yuceloper.paytrack.creditcard.domain.CreditCard;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

interface CreditCardJpaRepository extends JpaRepository<CreditCard, Long> {
    List<CreditCard> findAllByUserIdOrderByIdDesc(Long userId);
}

@Component
@RequiredArgsConstructor
public class SpringDataCreditCardRepository implements CreditCardRepository {
    private final CreditCardJpaRepository repository;

    @Override
    public CreditCard save(CreditCard creditCard) {
        return repository.save(creditCard);
    }

    @Override
    public Optional<CreditCard> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<CreditCard> findAllByUserId(Long userId) {
        return repository.findAllByUserIdOrderByIdDesc(userId);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
