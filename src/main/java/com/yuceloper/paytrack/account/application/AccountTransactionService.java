package com.yuceloper.paytrack.account.application;

import com.yuceloper.paytrack.account.api.dto.AccountTransactionDtos;
import com.yuceloper.paytrack.account.domain.Account;
import com.yuceloper.paytrack.account.domain.AccountNature;
import com.yuceloper.paytrack.account.domain.AccountTransaction;
import com.yuceloper.paytrack.account.domain.AccountTransactionType;
import com.yuceloper.paytrack.category.application.TransactionCategoryRepository;
import com.yuceloper.paytrack.category.domain.TransactionCategory;
import com.yuceloper.paytrack.category.domain.TransactionCategoryType;
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
    private final TransactionCategoryRepository categoryRepository;

    public List<AccountTransactionDtos.Response> getRange(Long userId, LocalDate from, LocalDate to) {
        if (to.isBefore(from)) throw new IllegalArgumentException("to must be on or after from");
        return transactionRepository.findBetween(userId, from, to).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AccountTransactionDtos.Response transfer(Long userId, AccountTransactionDtos.TransferRequest request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new IllegalArgumentException("fromAccountId and toAccountId must be different");
        }
        Account from = getAccount(request.fromAccountId());
        Account to = getAccount(request.toAccountId());
        validateOwnershipAndCurrency(userId, from, to);

        applyExpense(from, request.amount());
        applyIncome(to, request.amount());
        accountRepository.save(from);
        accountRepository.save(to);

        return toResponse(transactionRepository.save(AccountTransaction.builder()
                .userId(userId)
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
    public AccountTransactionDtos.Response manual(Long userId, AccountTransactionDtos.ManualRequest request) {
        if (request.type() == AccountTransactionType.TRANSFER) {
            throw new IllegalArgumentException("Use transfer endpoint for TRANSFER transactions");
        }
        Account account = getAccount(request.accountId());
        validateOwner(userId, account);
        validateCategory(userId, request.categoryId(), request.type());

        if (request.type() == AccountTransactionType.EXPENSE) {
            applyExpense(account, request.amount());
        } else if (request.type() == AccountTransactionType.INCOME) {
            applyIncome(account, request.amount());
        } else {
            throw new IllegalArgumentException("Manual transaction type must be INCOME or EXPENSE");
        }
        accountRepository.save(account);

        AccountTransaction saved = transactionRepository.save(AccountTransaction.builder()
                .userId(userId)
                .type(request.type())
                .accountId(account.getId())
                .categoryId(request.categoryId())
                .amount(request.amount())
                .currency(account.getCurrency())
                .occurredOn(request.occurredOn() != null ? request.occurredOn() : LocalDate.now())
                .description(request.description().trim())
                .sourceType("MANUAL")
                .build());
        return toResponse(saved);
    }

    @Transactional
    public AccountTransactionDtos.Response adjustBalance(Long userId, AccountTransactionDtos.BalanceAdjustmentRequest request) {
        Account account = getAccount(request.accountId());
        validateOwner(userId, account);
        validateTargetBalance(account, request.targetBalance());

        BigDecimal delta = request.targetBalance().subtract(account.getBalance());
        if (delta.signum() == 0) {
            throw new IllegalArgumentException("Target balance is already the current balance");
        }

        AccountTransactionType type;
        if (account.getNature() == AccountNature.LIABILITY) {
            type = delta.signum() > 0 ? AccountTransactionType.EXPENSE : AccountTransactionType.INCOME;
        } else {
            type = delta.signum() > 0 ? AccountTransactionType.INCOME : AccountTransactionType.EXPENSE;
        }

        BigDecimal amount = delta.abs();
        account.setBalance(request.targetBalance());
        accountRepository.save(account);

        AccountTransaction saved = transactionRepository.save(AccountTransaction.builder()
                .userId(userId)
                .type(type)
                .accountId(account.getId())
                .amount(amount)
                .currency(account.getCurrency())
                .occurredOn(request.occurredOn() != null ? request.occurredOn() : LocalDate.now())
                .description(request.description().trim())
                .sourceType("BALANCE_ADJUSTMENT")
                .build());
        return toResponse(saved);
    }

    @Transactional
    public void recordExpense(Long accountId, Long userId, BigDecimal amount, String description, String sourceType, Long sourceId, LocalDate date) {
        if (transactionRepository.findActiveSourceTransaction(userId, sourceType, sourceId, AccountTransactionType.EXPENSE).isPresent()) return;
        Account account = getAccount(accountId);
        validateOwner(userId, account);
        applyExpense(account, amount);
        accountRepository.save(account);
        transactionRepository.save(AccountTransaction.builder()
                .userId(userId).type(AccountTransactionType.EXPENSE).accountId(accountId)
                .amount(amount).currency(account.getCurrency()).occurredOn(date)
                .description(description).sourceType(sourceType).sourceId(sourceId).build());
    }

    @Transactional
    public void recordIncome(Long accountId, Long userId, BigDecimal amount, String description, String sourceType, Long sourceId, LocalDate date) {
        if (transactionRepository.findActiveSourceTransaction(userId, sourceType, sourceId, AccountTransactionType.INCOME).isPresent()) return;
        Account account = getAccount(accountId);
        validateOwner(userId, account);
        applyIncome(account, amount);
        accountRepository.save(account);
        transactionRepository.save(AccountTransaction.builder()
                .userId(userId).type(AccountTransactionType.INCOME).accountId(accountId)
                .amount(amount).currency(account.getCurrency()).occurredOn(date)
                .description(description).sourceType(sourceType).sourceId(sourceId).build());
    }

    @Transactional
    public void reverseExpense(Long userId, String sourceType, Long sourceId) {
        transactionRepository.findActiveSourceTransaction(userId, sourceType, sourceId, AccountTransactionType.EXPENSE).ifPresent(tx -> {
            Account account = getAccount(tx.getAccountId());
            validateOwner(userId, account);
            applyIncome(account, tx.getAmount());
            accountRepository.save(account);
            tx.reverse();
            transactionRepository.save(tx);
        });
    }

    @Transactional
    public void reverseIncome(Long userId, String sourceType, Long sourceId) {
        transactionRepository.findActiveSourceTransaction(userId, sourceType, sourceId, AccountTransactionType.INCOME).ifPresent(tx -> {
            Account account = getAccount(tx.getAccountId());
            validateOwner(userId, account);
            applyExpense(account, tx.getAmount());
            accountRepository.save(account);
            tx.reverse();
            transactionRepository.save(tx);
        });
    }

    private void applyExpense(Account account, BigDecimal amount) {
        BigDecimal next = account.getNature() == AccountNature.LIABILITY
                ? account.getBalance().add(amount)
                : account.getBalance().subtract(amount);
        validateTargetBalance(account, next);
        account.setBalance(next);
    }

    private void applyIncome(Account account, BigDecimal amount) {
        BigDecimal next = account.getNature() == AccountNature.LIABILITY
                ? account.getBalance().subtract(amount)
                : account.getBalance().add(amount);
        validateTargetBalance(account, next);
        account.setBalance(next);
    }

    private void validateTargetBalance(Account account, BigDecimal targetBalance) {
        if (account.getNature() == AccountNature.LIABILITY) {
            if (targetBalance.signum() < 0) {
                throw new IllegalArgumentException("Liability payment cannot exceed outstanding debt");
            }
            if (account.getCreditLimit() != null && targetBalance.compareTo(account.getCreditLimit()) > 0) {
                throw new IllegalArgumentException("Transaction would exceed the account credit limit");
            }
        }
    }

    private Account getAccount(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
    }

    private void validateOwner(Long userId, Account account) {
        if (!account.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Account not found: " + account.getId());
        }
    }

    private void validateOwnershipAndCurrency(Long userId, Account from, Account to) {
        validateOwner(userId, from);
        validateOwner(userId, to);
        if (!from.getCurrency().equalsIgnoreCase(to.getCurrency())) {
            throw new IllegalArgumentException("Transfer accounts must use the same currency");
        }
    }

    private void validateCategory(Long userId, Long categoryId, AccountTransactionType transactionType) {
        if (categoryId == null) return;
        TransactionCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
        if (!userId.equals(category.getUserId()) || !category.isActive()) {
            throw new ResourceNotFoundException("Category not found: " + categoryId);
        }
        TransactionCategoryType expected = transactionType == AccountTransactionType.INCOME
                ? TransactionCategoryType.INCOME
                : TransactionCategoryType.EXPENSE;
        if (category.getType() != TransactionCategoryType.BOTH && category.getType() != expected) {
            throw new IllegalArgumentException("Category is not compatible with transaction type");
        }
    }

    private AccountTransactionDtos.Response toResponse(AccountTransaction tx) {
        return new AccountTransactionDtos.Response(
                tx.getId(), tx.getUserId(), tx.getType(), tx.getAccountId(), tx.getCounterAccountId(),
                tx.getCategoryId(), tx.getAmount(), tx.getCurrency(), tx.getOccurredOn(), tx.getDescription(),
                tx.getSourceType(), tx.getSourceId(), tx.isReversed()
        );
    }
}
