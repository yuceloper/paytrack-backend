package com.yuceloper.paytrack.account.infrastructure;

import com.yuceloper.paytrack.account.application.AccountTransactionRepository;
import com.yuceloper.paytrack.account.domain.AccountTransaction;
import com.yuceloper.paytrack.account.domain.AccountTransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

interface JpaAccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {
    List<AccountTransaction> findAllByUserIdAndOccurredOnBetweenOrderByOccurredOnAscIdAsc(Long userId, LocalDate from, LocalDate to);
    Optional<AccountTransaction> findFirstByUserIdAndSourceTypeAndSourceIdAndTypeAndReversedFalseOrderByIdDesc(
            Long userId,
            String sourceType,
            Long sourceId,
            AccountTransactionType type
    );
}

@Repository
@RequiredArgsConstructor
public class SpringDataAccountTransactionRepository implements AccountTransactionRepository {

    private final JpaAccountTransactionRepository repository;

    @Override
    public AccountTransaction save(AccountTransaction transaction) {
        return repository.save(transaction);
    }

    @Override
    public List<AccountTransaction> findBetween(Long userId, LocalDate from, LocalDate to) {
        return repository.findAllByUserIdAndOccurredOnBetweenOrderByOccurredOnAscIdAsc(userId, from, to);
    }

    @Override
    public Optional<AccountTransaction> findActiveSourceTransaction(Long userId, String sourceType, Long sourceId, AccountTransactionType type) {
        return repository.findFirstByUserIdAndSourceTypeAndSourceIdAndTypeAndReversedFalseOrderByIdDesc(
                userId, sourceType, sourceId, type
        );
    }
}
