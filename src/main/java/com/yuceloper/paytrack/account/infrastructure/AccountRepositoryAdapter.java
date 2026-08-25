package com.yuceloper.paytrack.account.infrastructure;

import com.yuceloper.paytrack.account.application.AccountRepository;
import com.yuceloper.paytrack.account.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

    private final SpringDataAccountRepository repository;

    @Override
    public Account save(Account account) {
        return repository.save(account);
    }

    @Override
    public Optional<Account> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Account> findAllByUserId(Long userId) {
        return repository.findAllByUserIdOrderByNameAsc(userId);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
