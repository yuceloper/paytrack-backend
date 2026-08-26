package com.yuceloper.paytrack.account.application;

import com.yuceloper.paytrack.account.domain.AccountTransaction;
import com.yuceloper.paytrack.account.domain.AccountTransactionType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AccountTransactionRepository {
    AccountTransaction save(AccountTransaction transaction);
    List<AccountTransaction> findBetween(Long userId, LocalDate from, LocalDate to);
    Optional<AccountTransaction> findActiveSourceTransaction(Long userId, String sourceType, Long sourceId, AccountTransactionType type);
}
