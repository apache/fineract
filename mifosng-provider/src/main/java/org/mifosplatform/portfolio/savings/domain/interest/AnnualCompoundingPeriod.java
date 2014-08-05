/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain.interest;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.LocalDateInterval;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;

public class AnnualCompoundingPeriod implements CompoundingPeriod {

    @SuppressWarnings("unused")
    private final LocalDateInterval periodInterval;
    private final List<EndOfDayBalance> endOfDayBalances;

    public static AnnualCompoundingPeriod create(final LocalDateInterval periodInterval, final List<EndOfDayBalance> allEndOfDayBalances,
            final LocalDate upToInterestCalculationDate) {

        final List<EndOfDayBalance> endOfDayBalancesWithinPeriod = endOfDayBalancesWithinPeriodInterval(periodInterval,
                allEndOfDayBalances, upToInterestCalculationDate);

        return new AnnualCompoundingPeriod(periodInterval, endOfDayBalancesWithinPeriod);
    }

    @Override
    public BigDecimal calculateInterest(final SavingsCompoundingInterestPeriodType compoundingInterestPeriodType,
            final SavingsInterestCalculationType interestCalculationType, final BigDecimal interestToCompound,
            final BigDecimal interestRateAsFraction, final long daysInYear, final BigDecimal minBalanceForInterestCalculation) {

        BigDecimal interestEarned = BigDecimal.ZERO;

        switch (interestCalculationType) {
            case DAILY_BALANCE:
                interestEarned = calculateUsingDailyBalanceMethod(compoundingInterestPeriodType, interestToCompound,
                        interestRateAsFraction, daysInYear, minBalanceForInterestCalculation);
            break;
            case AVERAGE_DAILY_BALANCE:
                interestEarned = calculateUsingAverageDailyBalanceMethod(interestToCompound, interestRateAsFraction, daysInYear,
                        minBalanceForInterestCalculation);
            break;
            case INVALID:
            break;
        }

        return interestEarned;
    }

    private BigDecimal calculateUsingAverageDailyBalanceMethod(final BigDecimal interestToCompound,
            final BigDecimal interestRateAsFraction, final long daysInYear, final BigDecimal minBalanceForInterestCalculation) {

        BigDecimal cumulativeBalance = BigDecimal.ZERO;
        Integer numberOfDays = Integer.valueOf(0);

        for (final EndOfDayBalance balance : this.endOfDayBalances) {
            final BigDecimal endOfDayCumulativeBalance = balance.cumulativeBalance(interestToCompound);
            cumulativeBalance = cumulativeBalance.add(endOfDayCumulativeBalance);

            final Integer balanceExistsForNumberOfDays = balance.getNumberOfDays();
            numberOfDays = numberOfDays + balanceExistsForNumberOfDays;
        }

        BigDecimal interestEarned = BigDecimal.ZERO;
        if (cumulativeBalance.compareTo(BigDecimal.ZERO) != 0 && numberOfDays > 0) {
            final BigDecimal averageDailyBalance = cumulativeBalance.divide(BigDecimal.valueOf(numberOfDays), MathContext.DECIMAL64)
                    .setScale(9, RoundingMode.HALF_EVEN);

            final BigDecimal multiplicand = BigDecimal.ONE.divide(BigDecimal.valueOf(daysInYear), MathContext.DECIMAL64);
            final BigDecimal dailyInterestRate = interestRateAsFraction.multiply(multiplicand, MathContext.DECIMAL64);
            final BigDecimal periodicInterestRate = dailyInterestRate.multiply(BigDecimal.valueOf(numberOfDays), MathContext.DECIMAL64);
            if (averageDailyBalance.compareTo(minBalanceForInterestCalculation) >= 0) {
                interestEarned = averageDailyBalance.multiply(periodicInterestRate, MathContext.DECIMAL64).setScale(9,
                        RoundingMode.HALF_EVEN);
            }
        }

        return interestEarned;
    }

    private BigDecimal calculateUsingDailyBalanceMethod(final SavingsCompoundingInterestPeriodType compoundingInterestPeriodType,
            final BigDecimal interestToCompound, final BigDecimal interestRateAsFraction, final long daysInYear,
            final BigDecimal minBalanceForInterestCalculation) {

        BigDecimal interestEarned = BigDecimal.ZERO;
        BigDecimal interestOnBalanceUnrounded = BigDecimal.ZERO;
        for (final EndOfDayBalance balance : this.endOfDayBalances) {

            switch (compoundingInterestPeriodType) {
                case DAILY:
                    interestOnBalanceUnrounded = balance.calculateInterestOnBalanceAndInterest(interestToCompound, interestRateAsFraction,
                            daysInYear, minBalanceForInterestCalculation);
                break;
                case MONTHLY:
                    interestOnBalanceUnrounded = balance.calculateInterestOnBalance(interestToCompound, interestRateAsFraction, daysInYear,
                            minBalanceForInterestCalculation);
                break;
                case QUATERLY:
                    interestOnBalanceUnrounded = balance.calculateInterestOnBalance(interestToCompound, interestRateAsFraction, daysInYear,
                            minBalanceForInterestCalculation);
                break;
                // case WEEKLY:
                // break;
                // case BIWEEKLY:
                // break;
                case BI_ANNUAL:
                    interestOnBalanceUnrounded = balance.calculateInterestOnBalance(interestToCompound, interestRateAsFraction, daysInYear,
                            minBalanceForInterestCalculation);
                break;
                case ANNUAL:
                    interestOnBalanceUnrounded = balance.calculateInterestOnBalance(interestToCompound, interestRateAsFraction, daysInYear,
                            minBalanceForInterestCalculation);
                break;
                // case NO_COMPOUNDING_SIMPLE_INTEREST:
                // break;
                case INVALID:
                break;
            }

            interestEarned = interestEarned.add(interestOnBalanceUnrounded);
        }
        return interestEarned;
    }

    private static List<EndOfDayBalance> endOfDayBalancesWithinPeriodInterval(final LocalDateInterval compoundingPeriodInterval,
            final List<EndOfDayBalance> allEndOfDayBalances, final LocalDate upToInterestCalculationDate) {

        final List<EndOfDayBalance> endOfDayBalancesForPeriodInterval = new ArrayList<>();

        for (final EndOfDayBalance endOfDayBalance : allEndOfDayBalances) {

            if (compoundingPeriodInterval.contains(endOfDayBalance.date())) {
                final EndOfDayBalance cappedToPeriodEndDate = endOfDayBalance.upTo(compoundingPeriodInterval, upToInterestCalculationDate);
                endOfDayBalancesForPeriodInterval.add(cappedToPeriodEndDate);
            } else if (endOfDayBalance.contains(compoundingPeriodInterval)) {
                final EndOfDayBalance cappedToPeriodEndDate = endOfDayBalance.upTo(compoundingPeriodInterval, upToInterestCalculationDate);
                endOfDayBalancesForPeriodInterval.add(cappedToPeriodEndDate);
            }
        }

        return endOfDayBalancesForPeriodInterval;
    }

    private AnnualCompoundingPeriod(final LocalDateInterval periodInterval, final List<EndOfDayBalance> endOfDayBalances) {
        this.periodInterval = periodInterval;
        this.endOfDayBalances = endOfDayBalances;
    }
}