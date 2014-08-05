/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain.interest;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.LocalDateInterval;
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
            final long daysInYear, final BigDecimal minBalanceForInterestCalculation) {

        final BigDecimal multiplicand = BigDecimal.ONE.divide(BigDecimal.valueOf(daysInYear), MathContext.DECIMAL64);
        final BigDecimal dailyInterestRate = interestRateAsFraction.multiply(multiplicand, MathContext.DECIMAL64);
        final BigDecimal periodicInterestRate = dailyInterestRate.multiply(BigDecimal.valueOf(this.numberOfDays), MathContext.DECIMAL64);

        final BigDecimal realBalanceForInterestCalculation = this.endOfDayBalance.getAmount().add(interestToCompound);
        BigDecimal interest = BigDecimal.ZERO.setScale(9, RoundingMode.HALF_EVEN);
        if (realBalanceForInterestCalculation.compareTo(minBalanceForInterestCalculation) >= 0) {
            interest = realBalanceForInterestCalculation.multiply(periodicInterestRate, MathContext.DECIMAL64).setScale(9,
                    RoundingMode.HALF_EVEN);
        }
        return interest;
    }

    /**
     * Future Value (FV) = PV x (1+r)^n
     * 
     * Interest = FV - PV PV = Principal or the Account Balance r = rate per
     * compounding period (so for daily, r = nominalInterestRateAsFraction x
     * 1/365 n = number of periods rate is compounded
     */
    public BigDecimal calculateInterestOnBalanceAndInterest(final BigDecimal interestToCompound, final BigDecimal interestRateAsFraction,
            final long daysInYear, final BigDecimal minBalanceForInterestCalculation) {
        final BigDecimal multiplicand = BigDecimal.ONE.divide(BigDecimal.valueOf(daysInYear), MathContext.DECIMAL64);

        final BigDecimal presentValue = this.endOfDayBalance.getAmount().add(interestToCompound);

        final BigDecimal r = interestRateAsFraction.multiply(multiplicand);

        final BigDecimal interestRateForCompoundingPeriodPlusOne = BigDecimal.ONE.add(r);

        final double interestRateForCompoundingPeriodPowered = Math.pow(interestRateForCompoundingPeriodPlusOne.doubleValue(), Integer
                .valueOf(this.numberOfDays).doubleValue());
        BigDecimal futureValue = presentValue.setScale(9, RoundingMode.HALF_EVEN);
        if (presentValue.compareTo(minBalanceForInterestCalculation) >= 0) {
            futureValue = presentValue.multiply(BigDecimal.valueOf(interestRateForCompoundingPeriodPowered), MathContext.DECIMAL64)
                    .setScale(9, RoundingMode.HALF_EVEN);
        }
        return futureValue.subtract(presentValue);
    }

    /**
     * @param compoundingPeriodInterval
     * @param upToInterestCalculationDate
     *            : For calculating maturity details in advance
     *            upToInterestCalculationDate will be maturity date else it will
     *            be DateUtils.getLocalDateOfTenant().
     * @return
     */
    public EndOfDayBalance upTo(final LocalDateInterval compoundingPeriodInterval, final LocalDate upToInterestCalculationDate) {

        Money startingBalance = this.openingBalance;
        LocalDate balanceStartDate = this.date;

        LocalDate oldBalanceEndDate = this.date.plusDays(this.numberOfDays - 1);

        int daysOfBalance = this.numberOfDays;

        if (this.date.isBefore(compoundingPeriodInterval.startDate())) {
            balanceStartDate = compoundingPeriodInterval.startDate();
            startingBalance = this.endOfDayBalance;
            final LocalDateInterval balancePeriodInterval = LocalDateInterval.create(balanceStartDate, oldBalanceEndDate);
            daysOfBalance = balancePeriodInterval.daysInPeriodInclusiveOfEndDate();
        }

        LocalDate balanceEndDate = balanceStartDate.plusDays(daysOfBalance - 1);
        if (balanceEndDate.isAfter(compoundingPeriodInterval.endDate())) {
            balanceEndDate = compoundingPeriodInterval.endDate();
            final LocalDateInterval balancePeriodInterval = LocalDateInterval.create(balanceStartDate, balanceEndDate);
            daysOfBalance = balancePeriodInterval.daysInPeriodInclusiveOfEndDate();
        }
        if (balanceEndDate.isAfter(upToInterestCalculationDate)) {
            balanceEndDate = upToInterestCalculationDate;
            final LocalDateInterval balancePeriodInterval = LocalDateInterval.create(balanceStartDate, balanceEndDate);
            daysOfBalance = balancePeriodInterval.daysInPeriodInclusiveOfEndDate();
        }

        return new EndOfDayBalance(balanceStartDate, startingBalance, this.endOfDayBalance, daysOfBalance);
    }

    public boolean contains(final LocalDateInterval compoundingPeriodInterval) {

        final LocalDate balanceUpToDate = this.date.plusDays(this.numberOfDays - 1);

        final LocalDateInterval balanceInterval = LocalDateInterval.create(this.date, balanceUpToDate);
        return balanceInterval.containsPortionOf(compoundingPeriodInterval);
    }

    public Integer getNumberOfDays() {
        return Integer.valueOf(this.numberOfDays);
    }
}