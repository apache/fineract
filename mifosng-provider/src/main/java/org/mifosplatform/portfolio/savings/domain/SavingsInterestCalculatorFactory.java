package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;

public class SavingsInterestCalculatorFactory {

    public SavingsInterestCalculator createFrom(final SavingsInterestCalculationType interestCalculationType,
            final BigDecimal periodsInOneYearAsFraction, final BigDecimal annualInterestRateAsFraction) {

        SavingsInterestCalculator savingsInterestCalculator = new SavingsInterestCalculatorForDailyBalance(periodsInOneYearAsFraction,
                annualInterestRateAsFraction);
        switch (interestCalculationType) {
            case AVERAGE_DAILY_BALANCE:
                savingsInterestCalculator = new SavingsInterestCalculatorForAverageBalance(periodsInOneYearAsFraction,
                        annualInterestRateAsFraction);
            break;
            case DAILY_BALANCE:
                savingsInterestCalculator = new SavingsInterestCalculatorForDailyBalance(periodsInOneYearAsFraction,
                        annualInterestRateAsFraction);
            break;
            case INVALID:
            break;
        }

        return savingsInterestCalculator;
    }
}