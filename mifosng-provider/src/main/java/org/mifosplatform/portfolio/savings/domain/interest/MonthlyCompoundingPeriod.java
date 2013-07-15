package org.mifosplatform.portfolio.savings.domain.interest;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.domain.LocalDateInterval;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;

public class MonthlyCompoundingPeriod implements CompoundingPeriod {

    @SuppressWarnings("unused")
    private final LocalDateInterval periodInterval;
    private final List<EndOfDayBalance> endOfDayBalances;

    public static MonthlyCompoundingPeriod create(final LocalDateInterval periodInterval, final List<EndOfDayBalance> allEndOfDayBalances) {

        final List<EndOfDayBalance> endOfDayBalancesWithinPeriod = endOfDayBalancesWithinPeriodInterval(periodInterval, allEndOfDayBalances);

        return new MonthlyCompoundingPeriod(periodInterval, endOfDayBalancesWithinPeriod);
    }

    @Override
    public BigDecimal calculateInterest(final BigDecimal interestRateAsFraction, final long daysInYear) {

        BigDecimal interestEarned = BigDecimal.ZERO;

        for (EndOfDayBalance balance : endOfDayBalances) {
            BigDecimal interestOnBalanceUnrounded = balance.calculateInterestOnBalance(BigDecimal.ZERO, interestRateAsFraction, daysInYear);
            interestEarned = interestEarned.add(interestOnBalanceUnrounded);
        }

        return interestEarned;
    }

    @Override
    public BigDecimal calculateInterest(final SavingsCompoundingInterestPeriodType compoundingInterestPeriodType,
            final SavingsInterestCalculationType interestCalculationType, final BigDecimal interestToCompound,
            final BigDecimal interestRateAsFraction, final long daysInYear) {

        BigDecimal interestEarned = BigDecimal.ZERO;

        switch (interestCalculationType) {
            case DAILY_BALANCE:
                interestEarned = calculateUsingDailyBalanceMethod(compoundingInterestPeriodType, interestToCompound,
                        interestRateAsFraction, daysInYear);
            break;
            case AVERAGE_DAILY_BALANCE:
                interestEarned = calculateUsingAverageDailyBalanceMethod(interestToCompound, interestRateAsFraction, daysInYear);
            break;
            case INVALID:
            break;
        }

        return interestEarned;
    }

    private BigDecimal calculateUsingAverageDailyBalanceMethod(final BigDecimal interestToCompound,
            final BigDecimal interestRateAsFraction, final long daysInYear) {

        BigDecimal cumulativeBalance = BigDecimal.ZERO;
        Integer numberOfDays = Integer.valueOf(0);

        for (EndOfDayBalance balance : endOfDayBalances) {
            BigDecimal endOfDayCumulativeBalance = balance.cumulativeBalance(interestToCompound);
            cumulativeBalance = cumulativeBalance.add(endOfDayCumulativeBalance);

            Integer balanceExistsForNumberOfDays = balance.getNumberOfDays();
            numberOfDays = numberOfDays + balanceExistsForNumberOfDays;
        }

        BigDecimal interestEarned = BigDecimal.ZERO;
        if (cumulativeBalance.compareTo(BigDecimal.ZERO) != 0 && numberOfDays > 0) {
            BigDecimal averageDailyBalance = cumulativeBalance.divide(BigDecimal.valueOf(numberOfDays), MathContext.DECIMAL64).setScale(9,
                    RoundingMode.HALF_EVEN);

            BigDecimal multiplicand = BigDecimal.ONE.divide(BigDecimal.valueOf(daysInYear), MathContext.DECIMAL64);
            BigDecimal dailyInterestRate = interestRateAsFraction.multiply(multiplicand, MathContext.DECIMAL64);
            BigDecimal periodicInterestRate = dailyInterestRate.multiply(BigDecimal.valueOf(numberOfDays), MathContext.DECIMAL64);

            interestEarned = averageDailyBalance.multiply(periodicInterestRate, MathContext.DECIMAL64).setScale(9, RoundingMode.HALF_EVEN);
        }

        return interestEarned;
    }

    private BigDecimal calculateUsingDailyBalanceMethod(final SavingsCompoundingInterestPeriodType compoundingInterestPeriodType,
            final BigDecimal interestToCompound, final BigDecimal interestRateAsFraction, final long daysInYear) {

        BigDecimal interestEarned = BigDecimal.ZERO;
        BigDecimal interestOnBalanceUnrounded = BigDecimal.ZERO;
        for (EndOfDayBalance balance : endOfDayBalances) {

            switch (compoundingInterestPeriodType) {
                case DAILY:
                    interestOnBalanceUnrounded = balance.calculateInterestOnBalanceAndInterest(interestToCompound, interestRateAsFraction,
                            daysInYear);
                break;
                case MONTHLY:
                    interestOnBalanceUnrounded = balance.calculateInterestOnBalance(interestToCompound, interestRateAsFraction, daysInYear);
                break;
                // case QUATERLY:
                // break;
                // case WEEKLY:
                // break;
                // case BIWEEKLY:
                // break;
                // case BI_ANNUAL:
                // break;
                // case ANNUAL:
                // break;
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
            final List<EndOfDayBalance> allEndOfDayBalances) {

        List<EndOfDayBalance> endOfDayBalancesForPeriodInterval = new ArrayList<EndOfDayBalance>();

        for (EndOfDayBalance endOfDayBalance : allEndOfDayBalances) {

            if (compoundingPeriodInterval.contains(endOfDayBalance.date())) {
                final EndOfDayBalance cappedToPeriodEndDate = endOfDayBalance.upTo(compoundingPeriodInterval);
                endOfDayBalancesForPeriodInterval.add(cappedToPeriodEndDate);
            } else if (endOfDayBalance.contains(compoundingPeriodInterval)) {
                final EndOfDayBalance cappedToPeriodEndDate = endOfDayBalance.upTo(compoundingPeriodInterval);
                endOfDayBalancesForPeriodInterval.add(cappedToPeriodEndDate);
            }
        }

        return endOfDayBalancesForPeriodInterval;
    }

    private MonthlyCompoundingPeriod(final LocalDateInterval periodInterval, final List<EndOfDayBalance> endOfDayBalances) {
        this.periodInterval = periodInterval;
        this.endOfDayBalances = endOfDayBalances;
    }
}