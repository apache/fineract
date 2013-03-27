package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;

public class SavingsAccountDailyBalance {

    private final InterestPeriodInterval interestPeriodInterval;
    private final BigDecimal runningBalance;
    private final BigDecimal cumulativeBalance;

    public static SavingsAccountDailyBalance createFrom(final InterestPeriodInterval interestPeriodInterval, final BigDecimal runningBalance) {
        return new SavingsAccountDailyBalance(interestPeriodInterval, runningBalance);
    }

    private SavingsAccountDailyBalance(final InterestPeriodInterval interestPeriodInterval, final BigDecimal runningBalance) {
        this.interestPeriodInterval = interestPeriodInterval;
        this.runningBalance = runningBalance;
        final MathContext mc = new MathContext(10, RoundingMode.HALF_EVEN);
        final BigDecimal numberOfDaysBigDecimal = BigDecimal.valueOf(interestPeriodInterval.daysInPeriodInclusiveOfEndDate());
        this.cumulativeBalance = runningBalance.multiply(numberOfDaysBigDecimal, mc);
    }

    public Money dailyRunningBalance(final MonetaryCurrency currency) {
        return Money.of(currency, runningBalance);
    }

    public BigDecimal cumulativeBalance() {
        return this.cumulativeBalance;
    }

    public Integer numberOfDays() {
        return interestPeriodInterval.daysInPeriodInclusiveOfEndDate();
    }

    public boolean isGreaterThanZero() {
        return this.runningBalance.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal interestUsingDailyBalanceMethod(final BigDecimal dailyInterestRate, final Integer numberOfDays) {
        final MathContext mc = new MathContext(10, RoundingMode.HALF_EVEN);
        final BigDecimal numberOfDaysBigDecimal = BigDecimal.valueOf(numberOfDays.longValue());
        return this.runningBalance.multiply(dailyInterestRate, mc).multiply(numberOfDaysBigDecimal, mc);
    }
}