package com.yuceloper.paytrack.account.application;

import com.yuceloper.paytrack.account.domain.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(Long id);
    List<Account> findAllByUserId(Long userId);
    void deleteById(Long id);
}
