package com.yuceloper.paytrack.dashboard.application;

import com.yuceloper.paytrack.creditcard.application.CreditCardRepository;
import com.yuceloper.paytrack.creditcard.domain.CreditCard;
import com.yuceloper.paytrack.dashboard.api.DashboardSummaryResponse;
import com.yuceloper.paytrack.income.application.IncomeOccurrenceRepository;
import com.yuceloper.paytrack.income.domain.IncomeOccurrence;
import com.yuceloper.paytrack.payment.application.PaymentRepository;
import com.yuceloper.paytrack.payment.domain.Payment;
import com.yuceloper.paytrack.subscription.application.SubscriptionRepository;
import com.yuceloper.paytrack.subscription.domain.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final BigDecimal WEEKS_PER_YEAR = BigDecimal.valueOf(52);
    private static final BigDecimal DAYS_PER_YEAR = BigDecimal.valueOf(365);

    private final PaymentRepository paymentRepository;
    private final CreditCardRepository creditCardRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final IncomeOccurrenceRepository incomeOccurrenceRepository;

    public DashboardSummaryResponse getSummary(Long userId, LocalDate today) {
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
        LocalDate sevenDaysEnd = today.plusDays(6);

        List<Payment> monthlyPayments = paymentRepository.findDueBetween(userId, monthStart, monthEnd);
        List<Payment> nextSevenDays = paymentRepository.findDueBetween(userId, today, sevenDaysEnd);
        List<Payment> overduePayments = paymentRepository.findOverdue(userId, today);

        BigDecimal dueThisMonth = sumPending(monthlyPayments);
        BigDecimal dueNextSevenDays = sumPending(nextSevenDays);
        int upcomingPaymentCount = (int) nextSevenDays.stream().filter(payment -> !payment.isPaid()).count();
        BigDecimal overdueAmount = sumPending(overduePayments);

        BigDecimal totalCreditCardDebt = creditCardRepository.findAllByUserId(userId).stream()
                .filter(CreditCard::isActive)
                .map(CreditCard::getCurrentDebt)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlySubscriptionCost = subscriptionRepository.findAllByUserId(userId).stream()
                .filter(Subscription::isActive)
                .filter(this::isTryCurrency)
                .map(this::monthlyEquivalent)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal yearlySubscriptionCost = monthlySubscriptionCost
                .multiply(TWELVE)
                .setScale(2, RoundingMode.HALF_UP);

        List<IncomeOccurrence> monthlyIncomes = incomeOccurrenceRepository.findBetween(userId, monthStart, monthEnd)
                .stream()
                .filter(this::isTryCurrency)
                .toList();
        BigDecimal expectedIncomeThisMonth = sumIncome(monthlyIncomes);
        BigDecimal receivedIncomeThisMonth = sumIncome(monthlyIncomes.stream().filter(IncomeOccurrence::isReceived).toList());
        BigDecimal plannedNetCashFlowThisMonth = expectedIncomeThisMonth.subtract(dueThisMonth);

        Optional<IncomeOccurrence> nextIncome = incomeOccurrenceRepository.findNextPending(userId, today)
                .filter(this::isTryCurrency);
        BigDecimal requiredUntilNextIncome = nextIncome
                .map(income -> sumPending(paymentRepository.findDueBetween(userId, today, income.getExpectedDate())))
                .orElse(BigDecimal.ZERO);

        return new DashboardSummaryResponse(
                dueThisMonth,
                dueNextSevenDays,
                upcomingPaymentCount,
                overdueAmount,
                overduePayments.size(),
                totalCreditCardDebt,
                monthlySubscriptionCost,
                yearlySubscriptionCost,
                expectedIncomeThisMonth,
                receivedIncomeThisMonth,
                plannedNetCashFlowThisMonth,
                nextIncome.map(IncomeOccurrence::getName).orElse(null),
                nextIncome.map(IncomeOccurrence::getExpectedDate).orElse(null),
                nextIncome.map(IncomeOccurrence::getAmount).orElse(null),
                requiredUntilNextIncome
        );
    }

    private BigDecimal sumPending(List<Payment> payments) {
        return payments.stream()
                .filter(payment -> !payment.isPaid())
                .map(Payment::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumIncome(List<IncomeOccurrence> incomes) {
        return incomes.stream()
                .map(IncomeOccurrence::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isTryCurrency(Subscription subscription) {
        return isTryCurrency(subscription.getCurrency());
    }

    private boolean isTryCurrency(IncomeOccurrence income) {
        return isTryCurrency(income.getCurrency());
    }

    private boolean isTryCurrency(String currency) {
        if (currency == null) return false;
        String normalized = currency.trim().toUpperCase(Locale.ROOT);
        return normalized.equals("TRY") || normalized.equals("TL");
    }

    private BigDecimal monthlyEquivalent(Subscription subscription) {
        BigDecimal amount = subscription.getAmount();
        if (amount == null) {
            return BigDecimal.ZERO;
        }

        String period = subscription.getBillingPeriod() == null
                ? "MONTHLY"
                : subscription.getBillingPeriod().trim().toUpperCase(Locale.ROOT);

        return switch (period) {
            case "YEARLY", "ANNUAL" -> amount.divide(TWELVE, 6, RoundingMode.HALF_UP);
            case "WEEKLY" -> amount.multiply(WEEKS_PER_YEAR).divide(TWELVE, 6, RoundingMode.HALF_UP);
            case "DAILY" -> amount.multiply(DAYS_PER_YEAR).divide(TWELVE, 6, RoundingMode.HALF_UP);
            default -> amount;
        };
    }
}
