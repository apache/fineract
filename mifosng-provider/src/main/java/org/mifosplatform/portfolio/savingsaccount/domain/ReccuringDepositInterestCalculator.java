package org.mifosplatform.portfolio.savingsaccount.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.configuration.domain.Money;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingInterestCalculationMethod;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.TenureTypeEnum;
import org.springframework.stereotype.Service;

@Service
public class ReccuringDepositInterestCalculator {

    // FIXME - MADHUKAR - Are the unused field here needed?
    public Money calculateInterestOnMaturityFor(Money savingsDepositPerPeriod, Integer tenure, BigDecimal reccuringInterestRate,
            LocalDate commencementDate, TenureTypeEnum tenureTypeEnum, SavingFrequencyType savingFrequencyType,
            SavingInterestCalculationMethod savingInterestCalculationMethod) {

        /*
         * // Assuming tenure in months // see
         * http://www.onemint.com/2012/04/03/
         * how-to-calculate-interest-on-recurring-deposits/
         * 
         * A=P(1+r/n)^nt
         * 
         * A = final amount P = principal amount (initial investment) r = annual
         * nominal interest rate (as a decimal, not in percentage) n = number of
         * times the interest is compounded per year t = number of years
         */
        MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
        Integer monthsInYear = 12;

        BigDecimal interestRateAsFraction = reccuringInterestRate.divide(BigDecimal.valueOf(100), mc);
        Integer noofTimesInterestCompoundPerYear = Integer.valueOf(0);
        switch (savingFrequencyType) {
            case MONTHLY:
                noofTimesInterestCompoundPerYear = monthsInYear;
            break;
            case QUATERLY:
                noofTimesInterestCompoundPerYear = monthsInYear / 3;
            break;
            case HALFYEARLY:
                noofTimesInterestCompoundPerYear = monthsInYear / 6;
            break;
            case YEARLY:
                noofTimesInterestCompoundPerYear = monthsInYear / 12;
            break;
            default:
                throw new RuntimeException("The specified frequency not supported");

        }

        BigDecimal interestRatePerPeriod = BigDecimal.valueOf(interestRateAsFraction.doubleValue()
                / noofTimesInterestCompoundPerYear.doubleValue());
        BigDecimal onePlusInterestRatePerPeriod = BigDecimal.ONE.add(interestRatePerPeriod);
        BigDecimal monthsExpressedInYears;
        BigDecimal timeforOneCalculationPeriod;
        BigDecimal amountPerPeriod;
        BigDecimal finalAmount = BigDecimal.ZERO;
        while (tenure > 0) {
            monthsExpressedInYears = BigDecimal.valueOf(tenure.doubleValue() / monthsInYear.doubleValue());
            timeforOneCalculationPeriod = BigDecimal.valueOf(noofTimesInterestCompoundPerYear * monthsExpressedInYears.doubleValue());
            amountPerPeriod = savingsDepositPerPeriod.getAmount().multiply(
                    BigDecimal.valueOf(Math.pow(onePlusInterestRatePerPeriod.doubleValue(), timeforOneCalculationPeriod.doubleValue())));
            finalAmount = finalAmount.add(amountPerPeriod);
            tenure--;
        }

        return Money.of(savingsDepositPerPeriod.getCurrency(), finalAmount);
    }
}