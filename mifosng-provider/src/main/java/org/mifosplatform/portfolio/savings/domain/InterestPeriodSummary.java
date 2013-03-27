package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;

public class InterestPeriodSummary {

    @SuppressWarnings("unused")
    private final Date startDate;
    @SuppressWarnings("unused")
    private final Date endDate;
    @SuppressWarnings("unused")
    private final Integer numberOfDays;
    @SuppressWarnings("unused")
    private final BigDecimal startingBalance;
    private final BigDecimal closingBalance;
    private final BigDecimal interestEarnedUnrounded;
    private final BigDecimal interestEarned;
    @SuppressWarnings("unused")
    private final List<SavingsAccountDailyBalance> dailyBalances;

    public static InterestPeriodSummary from(final InterestPeriodInterval interestPeriodInterval, final Money startingBalance,
            final List<SavingsAccountDailyBalance> dailyBalances, final BigDecimal totalInterestEarnedUnrounded) {

        return new InterestPeriodSummary(interestPeriodInterval, startingBalance, dailyBalances, totalInterestEarnedUnrounded);
    }

    private InterestPeriodSummary(final InterestPeriodInterval interestPeriodInterval, final Money startingBalance,
            final List<SavingsAccountDailyBalance> dailyBalances, final BigDecimal totalInterestEarnedUnrounded) {
        this.startDate = interestPeriodInterval.startDate().toDate();
        this.endDate = interestPeriodInterval.endDate().toDate();
        this.numberOfDays = interestPeriodInterval.daysInPeriodInclusiveOfEndDate();
        this.startingBalance = startingBalance.getAmount();

        final MonetaryCurrency currency = startingBalance.getCurrency();

        this.interestEarnedUnrounded = totalInterestEarnedUnrounded;
        this.interestEarned = Money.of(currency, this.interestEarnedUnrounded).getAmount();

        Money closingBalanceForPeriod = startingBalance.copy();

        for (SavingsAccountDailyBalance balance : dailyBalances) {
            closingBalanceForPeriod = balance.dailyRunningBalance(currency);
        }

        this.closingBalance = closingBalanceForPeriod.getAmount();
        this.dailyBalances = dailyBalances;
    }

    public Money closingBalance(final MonetaryCurrency currency) {
        return Money.of(currency, this.closingBalance);
    }

    public BigDecimal interest() {
        return this.interestEarned;
    }
}