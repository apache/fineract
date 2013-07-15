package org.mifosplatform.portfolio.savings.domain.interest;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.LocalDateInterval;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.organisation.monetary.domain.Money;

public class EndOfDayBalance {

    private final LocalDate date;
    private final Money openingBalance;
    private final Money endOfDayBalance;
    private final int numberOfDays;

    public static EndOfDayBalance from(final LocalDate date, final Money openingBalance, final Money endOfDayBalance, final int numberOfDays) {
        return new EndOfDayBalance(date, openingBalance, endOfDayBalance, numberOfDays);
    }

    public EndOfDayBalance(final LocalDate date, final Money openingBalance, final Money endOfDayBalance, final int numberOfDays) {
        this.date = date;
        this.openingBalance = openingBalance;
        this.endOfDayBalance = endOfDayBalance;
        this.numberOfDays = numberOfDays;
    }

    public LocalDate date() {
        return this.date;
    }

    public Money closingBalance() {
        return this.endOfDayBalance;
    }

    public BigDecimal cumulativeBalance(final BigDecimal interestToCompound) {
        final BigDecimal daysAsBigDecimal = BigDecimal.valueOf(this.numberOfDays);
        final BigDecimal realBalanceForInterestCalculation = this.endOfDayBalance.getAmount().add(interestToCompound);
        return realBalanceForInterestCalculation.multiply(daysAsBigDecimal, MathContext.DECIMAL64).setScale(9, RoundingMode.HALF_EVEN);
    }

    public BigDecimal calculateInterestOnBalance(final BigDecimal interestToCompound, final BigDecimal interestRateAsFraction,
            final long daysInYear) {

        BigDecimal multiplicand = BigDecimal.ONE.divide(BigDecimal.valueOf(daysInYear), MathContext.DECIMAL64);
        BigDecimal dailyInterestRate = interestRateAsFraction.multiply(multiplicand, MathContext.DECIMAL64);
        BigDecimal periodicInterestRate = dailyInterestRate.multiply(BigDecimal.valueOf(this.numberOfDays), MathContext.DECIMAL64);

        BigDecimal realBalanceForInterestCalculation = this.endOfDayBalance.getAmount().add(interestToCompound);

        return realBalanceForInterestCalculation.multiply(periodicInterestRate, MathContext.DECIMAL64).setScale(9, RoundingMode.HALF_EVEN);
    }

    /**
     * Future Value (FV) = PV x (1+r)^n
     * 
     * Interest = FV - PV PV = Principal or the Account Balance r = rate per
     * compounding period (so for daily, r = nominalInterestRateAsFraction x
     * 1/365 n = number of periods rate is compounded
     */
    public BigDecimal calculateInterestOnBalanceAndInterest(final BigDecimal interestToCompound, final BigDecimal interestRateAsFraction,
            final long daysInYear) {
        BigDecimal multiplicand = BigDecimal.ONE.divide(BigDecimal.valueOf(daysInYear), MathContext.DECIMAL64);

        BigDecimal presentValue = endOfDayBalance.getAmount().add(interestToCompound);

        BigDecimal r = interestRateAsFraction.multiply(multiplicand);

        BigDecimal interestRateForCompoundingPeriodPlusOne = BigDecimal.ONE.add(r);

        double interestRateForCompoundingPeriodPowered = Math.pow(interestRateForCompoundingPeriodPlusOne.doubleValue(),
                Integer.valueOf(this.numberOfDays).doubleValue());

        BigDecimal futureValue = presentValue.multiply(BigDecimal.valueOf(interestRateForCompoundingPeriodPowered), MathContext.DECIMAL64)
                .setScale(9, RoundingMode.HALF_EVEN);
        return futureValue.subtract(presentValue);
    }

    public EndOfDayBalance upTo(final LocalDateInterval compoundingPeriodInterval) {

        Money startingBalance = openingBalance;
        LocalDate balanceStartDate = this.date;

        if (this.date.isBefore(compoundingPeriodInterval.startDate())) {
            balanceStartDate = compoundingPeriodInterval.startDate();
            startingBalance = endOfDayBalance;
        }

        int daysOfBalance = this.numberOfDays;
        LocalDate balanceEndDate = balanceStartDate.plusDays(this.numberOfDays);
        if (balanceEndDate.isAfter(compoundingPeriodInterval.endDate())) {
            balanceEndDate = compoundingPeriodInterval.endDate();
            LocalDateInterval balancePeriodInterval = LocalDateInterval.create(balanceStartDate, balanceEndDate);
            daysOfBalance = balancePeriodInterval.daysInPeriodInclusiveOfEndDate();
        }
        if (balanceEndDate.isAfter(DateUtils.getLocalDateOfTenant())) {
            balanceEndDate = DateUtils.getLocalDateOfTenant();
            LocalDateInterval balancePeriodInterval = LocalDateInterval.create(balanceStartDate, balanceEndDate);
            daysOfBalance = balancePeriodInterval.daysInPeriodInclusiveOfEndDate();
        }

        return new EndOfDayBalance(balanceStartDate, startingBalance, this.endOfDayBalance, daysOfBalance);
    }

    public boolean contains(final LocalDateInterval compoundingPeriodInterval) {

        final LocalDate balanceUpToDate = this.date.plusDays(this.numberOfDays);

        final LocalDateInterval balanceInterval = LocalDateInterval.create(this.date, balanceUpToDate);
        return balanceInterval.containsPortionOf(compoundingPeriodInterval);
    }

    public Integer getNumberOfDays() {
        return Integer.valueOf(this.numberOfDays);
    }
}