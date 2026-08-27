package com.yuceloper.paytrack.analytics.application;

import com.yuceloper.paytrack.account.application.AccountTransactionRepository;
import com.yuceloper.paytrack.account.domain.AccountTransaction;
import com.yuceloper.paytrack.account.domain.AccountTransactionType;
import com.yuceloper.paytrack.analytics.api.dto.MonthlyAnalyticsResponse;
import com.yuceloper.paytrack.category.application.TransactionCategoryRepository;
import com.yuceloper.paytrack.category.domain.TransactionCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyAnalyticsService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final Set<String> NON_SPENDING_SOURCE_TYPES = Set.of(
            "BALANCE_ADJUSTMENT",
            "CREDIT_CARD_PAYMENT"
    );

    private final AccountTransactionRepository transactionRepository;
    private final TransactionCategoryRepository categoryRepository;

    public MonthlyAnalyticsResponse getMonthly(Long userId, int year, int month) {
        YearMonth selected = YearMonth.of(year, month);
        YearMonth previous = selected.minusMonths(1);

        List<AccountTransaction> currentTransactions = activeTransactions(userId, selected);
        List<AccountTransaction> previousTransactions = activeTransactions(userId, previous);

        BigDecimal totalIncome = sumByType(currentTransactions, AccountTransactionType.INCOME);
        BigDecimal totalExpense = sumByType(currentTransactions, AccountTransactionType.EXPENSE);
        BigDecimal previousExpense = sumByType(previousTransactions, AccountTransactionType.EXPENSE);
        BigDecimal netCashFlow = totalIncome.subtract(totalExpense);

        Map<Long, String> categoryNames = new HashMap<>();
        for (TransactionCategory category : categoryRepository.findAllByUserId(userId)) {
            categoryNames.put(category.getId(), category.getName());
        }

        Map<Long, BigDecimal> categoryTotals = new HashMap<>();
        BigDecimal uncategorized = BigDecimal.ZERO;
        for (AccountTransaction transaction : currentTransactions) {
            if (transaction.getType() != AccountTransactionType.EXPENSE) continue;
            if (transaction.getCategoryId() == null) {
                uncategorized = uncategorized.add(transaction.getAmount());
            } else {
                categoryTotals.merge(transaction.getCategoryId(), transaction.getAmount(), BigDecimal::add);
            }
        }

        List<MonthlyAnalyticsResponse.CategoryBreakdown> breakdown = categoryTotals.entrySet().stream()
                .map(entry -> new MonthlyAnalyticsResponse.CategoryBreakdown(
                        entry.getKey(),
                        categoryNames.getOrDefault(entry.getKey(), "Kategori #" + entry.getKey()),
                        money(entry.getValue()),
                        percentage(entry.getValue(), totalExpense)
                ))
                .sorted(Comparator.comparing(MonthlyAnalyticsResponse.CategoryBreakdown::amount).reversed())
                .collect(java.util.stream.Collectors.toList());

        if (uncategorized.signum() > 0) {
            breakdown.add(new MonthlyAnalyticsResponse.CategoryBreakdown(
                    null, "Kategorisiz", money(uncategorized), percentage(uncategorized, totalExpense)
            ));
            breakdown.sort(Comparator.comparing(MonthlyAnalyticsResponse.CategoryBreakdown::amount).reversed());
        }

        String topCategory = breakdown.isEmpty() ? null : breakdown.getFirst().categoryName();
        BigDecimal changePercent = previousExpense.signum() == 0
                ? null
                : totalExpense.subtract(previousExpense)
                        .multiply(HUNDRED)
                        .divide(previousExpense, 2, RoundingMode.HALF_UP);
        BigDecimal incomeExpenseRatio = totalIncome.signum() == 0
                ? null
                : totalExpense.multiply(HUNDRED).divide(totalIncome, 2, RoundingMode.HALF_UP);

        return new MonthlyAnalyticsResponse(
                year,
                month,
                money(totalIncome),
                money(totalExpense),
                money(netCashFlow),
                money(previousExpense),
                changePercent,
                incomeExpenseRatio,
                topCategory,
                currentTransactions.size(),
                breakdown
        );
    }

    private List<AccountTransaction> activeTransactions(Long userId, YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        return transactionRepository.findBetween(userId, from, to).stream()
                .filter(transaction -> !transaction.isReversed())
                .filter(transaction -> transaction.getType() != AccountTransactionType.TRANSFER)
                .filter(transaction -> transaction.getSourceType() == null
                        || !NON_SPENDING_SOURCE_TYPES.contains(transaction.getSourceType()))
                .toList();
    }

    private BigDecimal sumByType(List<AccountTransaction> transactions, AccountTransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == type)
                .map(AccountTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal percentage(BigDecimal value, BigDecimal total) {
        if (total.signum() == 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return value.multiply(HUNDRED).divide(total, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
