/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain.interest;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.LocalDateInterval;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;

public class DailyCompoundingPeriod implements CompoundingPeriod {

    @SuppressWarnings("unused")
    private final LocalDateInterval periodInterval;
    private final List<EndOfDayBalance> endOfDayBalances;

    public static DailyCompoundingPeriod create(final LocalDateInterval periodInterval, final List<EndOfDayBalance> allEndOfDayBalances,
            final LocalDate upToInterestCalculationDate) {

        final List<EndOfDayBalance> endOfDayBalancesWithinPeriod = endOfDayBalancesWithinPeriodInterval(periodInterval,
                allEndOfDayBalances, upToInterestCalculationDate);

        return new DailyCompoundingPeriod(periodInterval, endOfDayBalancesWithinPeriod);
    }

    private static List<EndOfDayBalance> endOfDayBalancesWithinPeriodInterval(final LocalDateInterval compoundingPeriodInterval,
            final List<EndOfDayBalance> allEndOfDayBalances, final LocalDate upToInterestCalculationDate) {

        final List<EndOfDayBalance> endOfDayBalancesForPeriodInterval = new ArrayList<>();

        EndOfDayBalance cappedToPeriodEndDate = null;

        for (final EndOfDayBalance endOfDayBalance : allEndOfDayBalances) {

            if (compoundingPeriodInterval.contains(endOfDayBalance.date())) {
                cappedToPeriodEndDate = endOfDayBalance.upTo(compoundingPeriodInterval, upToInterestCalculationDate);
            } else if (endOfDayBalance.contains(compoundingPeriodInterval)) {
                cappedToPeriodEndDate = endOfDayBalance.upTo(compoundingPeriodInterval, upToInterestCalculationDate);
            } else {
                final LocalDateInterval latestPeriod = LocalDateInterval.create(compoundingPeriodInterval.startDate(),
                        upToInterestCalculationDate);
                cappedToPeriodEndDate = endOfDayBalance.upTo(latestPeriod, upToInterestCalculationDate);
            }

            if (cappedToPeriodEndDate != null) {
                endOfDayBalancesForPeriodInterval.add(cappedToPeriodEndDate);
            }
        }

        return endOfDayBalancesForPeriodInterval;
    }

    private DailyCompoundingPeriod(final LocalDateInterval periodInterval, final List<EndOfDayBalance> endOfDayBalances) {
        this.periodInterval = periodInterval;
        this.endOfDayBalances = endOfDayBalances;
    }

    @Override
    public BigDecimal calculateInterest(
            @SuppressWarnings("unused") final SavingsCompoundingInterestPeriodType compoundingInterestPeriodType,
            @SuppressWarnings("unused") final SavingsInterestCalculationType interestCalculationType,
            final BigDecimal interestFromPreviousPostingPeriod, final BigDecimal interestRateAsFraction, final long daysInYear,
            final BigDecimal minBalanceForInterestCalculation) {
        BigDecimal interestEarned = BigDecimal.ZERO;

        // for daily compounding - each interest calculated from previous daily
        // calculations is 'compounded'
        BigDecimal interestToCompound = interestFromPreviousPostingPeriod;
        for (final EndOfDayBalance balance : this.endOfDayBalances) {
            final BigDecimal interestOnBalanceUnrounded = balance.calculateInterestOnBalanceAndInterest(interestToCompound,
                    interestRateAsFraction, daysInYear, minBalanceForInterestCalculation);
            interestToCompound = interestToCompound.add(interestOnBalanceUnrounded, MathContext.DECIMAL64).setScale(9);
            interestEarned = interestEarned.add(interestOnBalanceUnrounded);
        }

        return interestEarned;
    }
}