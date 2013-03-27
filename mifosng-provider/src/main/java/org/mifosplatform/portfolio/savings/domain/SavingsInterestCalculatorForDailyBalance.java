package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calculates interest on a savings account using the <b>Daily Balance</b>
 * method.
 */
public class SavingsInterestCalculatorForDailyBalance implements SavingsInterestCalculator {

    private final BigDecimal periodsInOneYearAsFraction;
    private final BigDecimal annualInterestRateAsFraction;

    public SavingsInterestCalculatorForDailyBalance(final BigDecimal periodsInOneYearAsFraction,
            final BigDecimal annualInterestRateAsFraction) {
        this.periodsInOneYearAsFraction = periodsInOneYearAsFraction;
        this.annualInterestRateAsFraction = annualInterestRateAsFraction;
    }

    @Override
    public BigDecimal calculate(final List<SavingsAccountDailyBalance> dailyBalances, final Integer numberOfDays) {
        final MathContext mc = new MathContext(10, RoundingMode.HALF_EVEN);
        final BigDecimal dailyInterestRate = annualInterestRateAsFraction.multiply(periodsInOneYearAsFraction, mc);

        BigDecimal interestCalculated = BigDecimal.ZERO;

        for (SavingsAccountDailyBalance balance : dailyBalances) {
            if (balance.isGreaterThanZero()) {
                final BigDecimal interestForBalance = balance.interestUsingDailyBalanceMethod(dailyInterestRate, numberOfDays);
                interestCalculated = interestCalculated.add(interestForBalance, mc);
            }
        }

        return interestCalculated;
    }
}