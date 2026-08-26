package com.yuceloper.paytrack.account.application;

import com.yuceloper.paytrack.account.api.dto.AccountResponse;
import com.yuceloper.paytrack.account.api.dto.AccountUpsertRequest;
import com.yuceloper.paytrack.account.domain.Account;
import com.yuceloper.paytrack.account.domain.AccountNature;
import com.yuceloper.paytrack.account.domain.AccountType;
import com.yuceloper.paytrack.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    public AccountResponse create(Long userId, AccountUpsertRequest request) {
        AccountNature nature = resolveNature(request);
        validateAccountShape(request.type(), nature, request.balance(), request.creditLimit());

        Account account = Account.builder()
                .userId(userId)
                .name(request.name().trim())
                .type(request.type())
                .nature(nature)
                .institution(trimToNull(request.institution()))
                .balance(request.balance())
                .creditLimit(resolveCreditLimit(nature, request.creditLimit()))
                .currency(request.currency().trim().toUpperCase(Locale.ROOT))
                .active(request.active() == null || request.active())
                .build();
        return toResponse(repository.save(account));
    }

    @Transactional
    public AccountResponse update(Long userId, Long id, AccountUpsertRequest request) {
        Account account = getEntity(userId, id);
        if (request.balance().compareTo(account.getBalance()) != 0) {
            throw new IllegalArgumentException("Balance cannot be edited directly; use a balance adjustment transaction");
        }

        AccountNature nature = resolveNature(request);
        if (nature != account.getNature() && account.getBalance().signum() != 0) {
            throw new IllegalArgumentException("Account nature can only be changed when balance is zero");
        }
        validateAccountShape(request.type(), nature, request.balance(), request.creditLimit());

        account.setName(request.name().trim());
        account.setType(request.type());
        account.setNature(nature);
        account.setInstitution(trimToNull(request.institution()));
        account.setCreditLimit(resolveCreditLimit(nature, request.creditLimit()));
        account.setCurrency(request.currency().trim().toUpperCase(Locale.ROOT));
        if (request.active() != null) account.setActive(request.active());
        return toResponse(repository.save(account));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        getEntity(userId, id);
        repository.deleteById(id);
    }

    private Account getEntity(Long userId, Long id) {
        Account account = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
        if (!userId.equals(account.getUserId())) {
            throw new ResourceNotFoundException("Account not found: " + id);
        }
        return account;
    }

    private AccountNature resolveNature(AccountUpsertRequest request) {
        if (request.type() == AccountType.OVERDRAFT) return AccountNature.LIABILITY;
        return request.nature() != null ? request.nature() : AccountNature.ASSET;
    }

    private BigDecimal resolveCreditLimit(AccountNature nature, BigDecimal creditLimit) {
        return nature == AccountNature.LIABILITY ? creditLimit : null;
    }

    private void validateAccountShape(AccountType type, AccountNature nature, BigDecimal balance, BigDecimal creditLimit) {
        if (type == AccountType.OVERDRAFT && nature != AccountNature.LIABILITY) {
            throw new IllegalArgumentException("OVERDRAFT accounts must be liabilities");
        }
        if (nature == AccountNature.LIABILITY && balance.signum() < 0) {
            throw new IllegalArgumentException("Liability balance represents debt and cannot be negative");
        }
        if (nature == AccountNature.ASSET && creditLimit != null) {
            throw new IllegalArgumentException("creditLimit is only supported for liability accounts");
        }
        if (creditLimit != null && balance.compareTo(creditLimit) > 0) {
            throw new IllegalArgumentException("Balance cannot exceed credit limit");
        }
    }

    private AccountResponse toResponse(Account account) {
        BigDecimal availableLimit = account.getNature() == AccountNature.LIABILITY && account.getCreditLimit() != null
                ? account.getCreditLimit().subtract(account.getBalance())
                : null;
        return new AccountResponse(
                account.getId(), account.getUserId(), account.getName(), account.getType(), account.getNature(),
                account.getInstitution(), account.getBalance(), account.getCreditLimit(), availableLimit,
                account.getCurrency(), account.isActive()
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
