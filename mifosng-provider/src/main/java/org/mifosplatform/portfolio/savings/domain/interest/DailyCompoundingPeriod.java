package org.mifosplatform.portfolio.savings.domain.interest;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.domain.LocalDateInterval;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;

public class DailyCompoundingPeriod implements CompoundingPeriod {

    @SuppressWarnings("unused")
    private final LocalDateInterval periodInterval;
    private final List<EndOfDayBalance> endOfDayBalances;

    public static DailyCompoundingPeriod create(final LocalDateInterval periodInterval, final List<EndOfDayBalance> allEndOfDayBalances) {

        final List<EndOfDayBalance> endOfDayBalancesWithinPeriod = endOfDayBalancesWithinPeriodInterval(periodInterval, allEndOfDayBalances);

        return new DailyCompoundingPeriod(periodInterval, endOfDayBalancesWithinPeriod);
    }

    private static List<EndOfDayBalance> endOfDayBalancesWithinPeriodInterval(final LocalDateInterval compoundingPeriodInterval,
            final List<EndOfDayBalance> allEndOfDayBalances) {

        List<EndOfDayBalance> endOfDayBalancesForPeriodInterval = new ArrayList<EndOfDayBalance>();

        EndOfDayBalance cappedToPeriodEndDate = null;

        for (EndOfDayBalance endOfDayBalance : allEndOfDayBalances) {

            if (compoundingPeriodInterval.contains(endOfDayBalance.date())) {
                cappedToPeriodEndDate = endOfDayBalance.upTo(compoundingPeriodInterval);
            } else if (endOfDayBalance.contains(compoundingPeriodInterval)) {
                cappedToPeriodEndDate = endOfDayBalance.upTo(compoundingPeriodInterval);
            } else {
                LocalDateInterval latestPeriod = LocalDateInterval.create(compoundingPeriodInterval.startDate(),
                        DateUtils.getLocalDateOfTenant());
                cappedToPeriodEndDate = endOfDayBalance.upTo(latestPeriod);
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
    public BigDecimal calculateInterest(final BigDecimal interestRateAsFraction, final long daysInYear) {

        BigDecimal interestEarned = BigDecimal.ZERO;

        for (EndOfDayBalance balance : this.endOfDayBalances) {
            BigDecimal interestOnBalanceUnrounded = balance.calculateInterestOnBalance(BigDecimal.ZERO, interestRateAsFraction, daysInYear);
            interestEarned = interestEarned.add(interestOnBalanceUnrounded);
        }

        return interestEarned;
    }

    @Override
    public BigDecimal calculateInterest(
            @SuppressWarnings("unused") final SavingsCompoundingInterestPeriodType compoundingInterestPeriodType,
            @SuppressWarnings("unused") final SavingsInterestCalculationType interestCalculationType,
            final BigDecimal interestFromPreviousPostingPeriod, final BigDecimal interestRateAsFraction, final long daysInYear) {
        BigDecimal interestEarned = BigDecimal.ZERO;

        // for daily compounding - each interest calculated from previous daily
        // calculations is 'compounded'
        BigDecimal interestToCompound = interestFromPreviousPostingPeriod;
        for (EndOfDayBalance balance : this.endOfDayBalances) {
            BigDecimal interestOnBalanceUnrounded = balance.calculateInterestOnBalanceAndInterest(interestToCompound,
                    interestRateAsFraction, daysInYear);
            interestToCompound = interestToCompound.add(interestOnBalanceUnrounded, MathContext.DECIMAL64).setScale(9);
            interestEarned = interestEarned.add(interestOnBalanceUnrounded);
        }

        return interestEarned;
    }
}