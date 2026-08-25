package com.yuceloper.paytrack.account.application;

import com.yuceloper.paytrack.account.api.dto.AccountResponse;
import com.yuceloper.paytrack.account.api.dto.AccountUpsertRequest;
import com.yuceloper.paytrack.account.domain.Account;
import com.yuceloper.paytrack.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository repository;

    public List<AccountResponse> getAll(Long userId) {
        return repository.findAllByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AccountResponse create(AccountUpsertRequest request) {
        Account account = Account.builder()
                .userId(request.userId())
                .name(request.name().trim())
                .type(request.type())
                .institution(trimToNull(request.institution()))
                .balance(request.balance())
                .currency(request.currency().trim().toUpperCase(Locale.ROOT))
                .active(request.active() == null || request.active())
                .build();
        return toResponse(repository.save(account));
    }

    @Transactional
    public AccountResponse update(Long id, AccountUpsertRequest request) {
        Account account = getEntity(id);
        account.setUserId(request.userId());
        account.setName(request.name().trim());
        account.setType(request.type());
        account.setInstitution(trimToNull(request.institution()));
        account.setBalance(request.balance());
        account.setCurrency(request.currency().trim().toUpperCase(Locale.ROOT));
        if (request.active() != null) account.setActive(request.active());
        return toResponse(repository.save(account));
    }

    @Transactional
    public void delete(Long id) {
        getEntity(id);
        repository.deleteById(id);
    }

    private Account getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(), account.getUserId(), account.getName(), account.getType(),
                account.getInstitution(), account.getBalance(), account.getCurrency(), account.isActive()
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
