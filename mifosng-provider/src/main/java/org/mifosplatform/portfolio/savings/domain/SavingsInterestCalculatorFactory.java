package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;

public class SavingsInterestCalculatorFactory {

    public SavingsCompoundInterestCalculator createFrom(final SavingsInterestCalculationType interestCalculationType,
            final BigDecimal periodsInOneYearAsFraction, final BigDecimal annualInterestRateAsFraction) {

        SavingsCompoundInterestCalculator savingsInterestCalculator = new SavingsCompoundInterestCalculatorForDailyBalance(periodsInOneYearAsFraction,
                annualInterestRateAsFraction);
        switch (interestCalculationType) {
            case AVERAGE_DAILY_BALANCE:
                savingsInterestCalculator = new SavingsCompoundInterestCalculatorForAverageBalance(periodsInOneYearAsFraction,
                        annualInterestRateAsFraction);
            break;
            case DAILY_BALANCE:
                savingsInterestCalculator = new SavingsCompoundInterestCalculatorForDailyBalance(periodsInOneYearAsFraction,
                        annualInterestRateAsFraction);
            break;
            case INVALID:
            break;
        }

        return savingsInterestCalculator;
    }
}