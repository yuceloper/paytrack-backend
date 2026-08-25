package com.yuceloper.paytrack.account.application;

import com.yuceloper.paytrack.account.api.dto.AccountTransactionDtos;
import com.yuceloper.paytrack.account.domain.Account;
import com.yuceloper.paytrack.account.domain.AccountTransaction;
import com.yuceloper.paytrack.account.domain.AccountTransactionType;
import com.yuceloper.paytrack.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountTransactionService {

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository transactionRepository;

    public List<AccountTransactionDtos.Response> getRange(Long userId, LocalDate from, LocalDate to) {
        if (to.isBefore(from)) throw new IllegalArgumentException("to must be on or after from");
        return transactionRepository.findBetween(userId, from, to).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AccountTransactionDtos.Response transfer(AccountTransactionDtos.TransferRequest request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new IllegalArgumentException("fromAccountId and toAccountId must be different");
        }
        Account from = getAccount(request.fromAccountId());
        Account to = getAccount(request.toAccountId());
        validateOwnershipAndCurrency(request.userId(), from, to);

        from.setBalance(from.getBalance().subtract(request.amount()));
        to.setBalance(to.getBalance().add(request.amount()));
        accountRepository.save(from);
        accountRepository.save(to);

        return toResponse(transactionRepository.save(AccountTransaction.builder()
                .userId(request.userId())
                .type(AccountTransactionType.TRANSFER)
                .accountId(from.getId())
                .counterAccountId(to.getId())
                .amount(request.amount())
                .currency(from.getCurrency())
                .occurredOn(request.occurredOn() != null ? request.occurredOn() : LocalDate.now())
                .description(request.description().trim())
                .build()));
    }

    @Transactional
    public void recordExpense(Long accountId, Long userId, BigDecimal amount, String description, String sourceType, Long sourceId, LocalDate date) {
        if (transactionRepository.findActiveSourceTransaction(sourceType, sourceId, AccountTransactionType.EXPENSE).isPresent()) return;
        Account account = getAccount(accountId);
        validateOwner(userId, account);
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        transactionRepository.save(AccountTransaction.builder()
                .userId(userId).type(AccountTransactionType.EXPENSE).accountId(accountId)
                .amount(amount).currency(account.getCurrency()).occurredOn(date)
                .description(description).sourceType(sourceType).sourceId(sourceId).build());
    }

    @Transactional
    public void recordIncome(Long accountId, Long userId, BigDecimal amount, String description, String sourceType, Long sourceId, LocalDate date) {
        if (transactionRepository.findActiveSourceTransaction(sourceType, sourceId, AccountTransactionType.INCOME).isPresent()) return;
        Account account = getAccount(accountId);
        validateOwner(userId, account);
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        transactionRepository.save(AccountTransaction.builder()
                .userId(userId).type(AccountTransactionType.INCOME).accountId(accountId)
                .amount(amount).currency(account.getCurrency()).occurredOn(date)
                .description(description).sourceType(sourceType).sourceId(sourceId).build());
    }

    @Transactional
    public void reverseExpense(String sourceType, Long sourceId) {
        transactionRepository.findActiveSourceTransaction(sourceType, sourceId, AccountTransactionType.EXPENSE).ifPresent(tx -> {
            Account account = getAccount(tx.getAccountId());
            account.setBalance(account.getBalance().add(tx.getAmount()));
            accountRepository.save(account);
            tx.reverse();
            transactionRepository.save(tx);
        });
    }

    @Transactional
    public void reverseIncome(String sourceType, Long sourceId) {
        transactionRepository.findActiveSourceTransaction(sourceType, sourceId, AccountTransactionType.INCOME).ifPresent(tx -> {
            Account account = getAccount(tx.getAccountId());
            account.setBalance(account.getBalance().subtract(tx.getAmount()));
            accountRepository.save(account);
            tx.reverse();
            transactionRepository.save(tx);
        });
    }

    private Account getAccount(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
    }

    private void validateOwner(Long userId, Account account) {
        if (!account.getUserId().equals(userId)) throw new IllegalArgumentException("Account does not belong to user");
    }

    private void validateOwnershipAndCurrency(Long userId, Account from, Account to) {
        validateOwner(userId, from);
        validateOwner(userId, to);
        if (!from.getCurrency().equalsIgnoreCase(to.getCurrency())) {
            throw new IllegalArgumentException("Transfer accounts must use the same currency");
        }
    }

    private AccountTransactionDtos.Response toResponse(AccountTransaction tx) {
        return new AccountTransactionDtos.Response(
                tx.getId(), tx.getUserId(), tx.getType(), tx.getAccountId(), tx.getCounterAccountId(),
                tx.getAmount(), tx.getCurrency(), tx.getOccurredOn(), tx.getDescription(),
                tx.getSourceType(), tx.getSourceId(), tx.isReversed()
        );
    }
}
