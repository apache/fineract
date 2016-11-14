/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.savings.domain.interest;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.joda.time.LocalDate;

public class DailyCompoundingPeriod implements CompoundingPeriod {

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
            final SavingsCompoundingInterestPeriodType compoundingInterestPeriodType,
            final SavingsInterestCalculationType interestCalculationType,
            final BigDecimal interestFromPreviousPostingPeriod, final BigDecimal interestRateAsFraction, final long daysInYear,
            final BigDecimal minBalanceForInterestCalculation,
            final BigDecimal overdraftInterestRateAsFraction, final BigDecimal minOverdraftForInterestCalculation) {
        BigDecimal interestEarned = BigDecimal.ZERO;

        // for daily compounding - each interest calculated from previous daily
        // calculations is 'compounded'
        BigDecimal interestToCompound = interestFromPreviousPostingPeriod;
        for (final EndOfDayBalance balance : this.endOfDayBalances) {
            final BigDecimal interestOnBalanceUnrounded = balance.calculateInterestOnBalanceAndInterest(interestToCompound,
                    interestRateAsFraction, daysInYear, minBalanceForInterestCalculation, overdraftInterestRateAsFraction,
                    minOverdraftForInterestCalculation);
            interestToCompound = interestToCompound.add(interestOnBalanceUnrounded, MathContext.DECIMAL64).setScale(9);
            interestEarned = interestEarned.add(interestOnBalanceUnrounded);
        }

        return interestEarned;
    }

	@Override
	public LocalDateInterval getPeriodInterval() {
		return this.periodInterval;
	}
}