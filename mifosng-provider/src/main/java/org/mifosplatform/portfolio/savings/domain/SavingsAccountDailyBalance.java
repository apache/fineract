package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;

public class SavingsAccountDailyBalance {

    private final LocalDateInterval interestPeriodInterval;
    private final BigDecimal endOfDayBalance;
    private final BigDecimal cumulativeBalance;
    private final BigDecimal compoundedInterestToDate;

    public static SavingsAccountDailyBalance createFrom(final LocalDateInterval interestPeriodInterval, final BigDecimal endOfDayBalance,
            final BigDecimal compoundedInterestToDate) {
        return new SavingsAccountDailyBalance(interestPeriodInterval, endOfDayBalance, compoundedInterestToDate);
    }

    private SavingsAccountDailyBalance(final LocalDateInterval interestPeriodInterval, final BigDecimal endOfDayBalance,
            final BigDecimal compoundedInterestToDate) {
        this.interestPeriodInterval = interestPeriodInterval;
        this.endOfDayBalance = endOfDayBalance;
        this.compoundedInterestToDate = compoundedInterestToDate;
        final MathContext mc = new MathContext(10, RoundingMode.HALF_EVEN);
        final BigDecimal numberOfDaysBigDecimal = BigDecimal.valueOf(interestPeriodInterval.daysInPeriodInclusiveOfEndDate());
        this.cumulativeBalance = endOfDayBalance.multiply(numberOfDaysBigDecimal, mc);
    }

    public Money dailyRunningBalance(final MonetaryCurrency currency) {
        return Money.of(currency, endOfDayBalance);
    }

    public BigDecimal endOfDayBalance() {
        return this.endOfDayBalance;
    }

    public BigDecimal cumulativeBalance() {
        return this.cumulativeBalance;
    }

    public Integer numberOfDays() {
        return interestPeriodInterval.daysInPeriodInclusiveOfEndDate();
    }

    public boolean isGreaterThanZero() {
        return this.endOfDayBalance.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal interestUsingDailyBalanceMethod(final BigDecimal dailyInterestRate, final Integer numberOfDays) {
        final MathContext mc = new MathContext(10, RoundingMode.HALF_EVEN);
        final BigDecimal numberOfDaysBigDecimal = BigDecimal.valueOf(numberOfDays.longValue());

        final BigDecimal compoundedEndOfDayBalance = this.endOfDayBalance.add(this.compoundedInterestToDate, mc);
        return compoundedEndOfDayBalance.multiply(dailyInterestRate, mc).multiply(numberOfDaysBigDecimal, mc);
    }
}