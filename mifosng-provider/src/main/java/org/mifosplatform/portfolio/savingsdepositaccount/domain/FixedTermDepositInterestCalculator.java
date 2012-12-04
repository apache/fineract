package org.mifosplatform.portfolio.savingsdepositaccount.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.springframework.stereotype.Service;

@Service
public class FixedTermDepositInterestCalculator {

    // FIXME - MADHUKAR - Are the unused field here needed?
    public Money calculateInterestOnMaturityFor(final Money deposit, final Integer tenureInMonths, final BigDecimal maturityInterestRate,
            final Integer interestCompoundedEvery, final PeriodFrequencyType interestCompoundedFrequencyPeriodType) {

        // TODO - Can change 'interest compounding' fields into something
        // simpler like interestCompoundedFrequency {None (Simple Interest),
        // Monthly, Quarterly, Half-Yearly, Annually}
        // - the 'interestCompoundedEvery' would then be inferred form the
        // selected type.
        // - the special case of 'simple interest' or 'no compounding' would use
        // simple formula rather than 'future value' function.

        // FIXME - assuming only monthly compounding period for now
        MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
        Integer monthsInYear = 12;

        BigDecimal interestRateAsFraction = maturityInterestRate.divide(BigDecimal.valueOf(100), mc);
        BigDecimal interestRateForOneMonth = interestRateAsFraction.divide(BigDecimal.valueOf(monthsInYear.doubleValue()), mc);

        BigDecimal ratePerCompoundingPeriod = interestRateForOneMonth.multiply(BigDecimal.valueOf(interestCompoundedEvery.doubleValue()),
                mc);

        Integer numberOfPeriods = tenureInMonths / interestCompoundedEvery;

        return fv(ratePerCompoundingPeriod, numberOfPeriods, deposit);
    }

    private static Money fv(final BigDecimal ratePerCompoundingPeriod, final Integer numberOfPeriods, final Money presentValue) {

        double pmtPerPeriod = Double.valueOf("0.0");
        double pv = presentValue.negated().getAmount().doubleValue();

        double futureValue = fv(ratePerCompoundingPeriod.doubleValue(), numberOfPeriods.doubleValue(), pmtPerPeriod, pv, false);

        return Money.of(presentValue.getCurrency(), BigDecimal.valueOf(futureValue));
    }

    /**
     * Future value of an amount given the number of payments, rate, amount of
     * individual payment, present value and boolean value indicating whether
     * payments are due at the beginning of period (false => payments are due at
     * end of period)
     */
    private static double fv(double ratePeriodCompoundingPeriod, double numberOfCompoundingPeriods, double pmtPerPeriod,
            double presentValue, boolean type) {
        double retval = 0;
        if (ratePeriodCompoundingPeriod == 0) {
            retval = -1 * (presentValue + (numberOfCompoundingPeriods * pmtPerPeriod));
        } else {
            double r1 = ratePeriodCompoundingPeriod + 1;
            retval = ((1 - Math.pow(r1, numberOfCompoundingPeriods)) * (type ? r1 : 1) * pmtPerPeriod) / ratePeriodCompoundingPeriod
                    - presentValue * Math.pow(r1, numberOfCompoundingPeriods);
        }
        return retval;
    }

    public Money calculateInterestOnMaturityForSimpleInterest(Money deposit, Integer tenureInMonths, BigDecimal maturityInterestRate,
            Integer interestCompoundedEvery, PeriodFrequencyType interestCompoundedFrequencyPeriodType) {

        MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
        Integer monthsInYear = 12;

        BigDecimal interestRateAsFraction = maturityInterestRate.divide(BigDecimal.valueOf(100), mc);
        BigDecimal interestRateForOneMonth = interestRateAsFraction.divide(BigDecimal.valueOf(monthsInYear.doubleValue()), mc);
        BigDecimal ratePerCompoundingPeriod = interestRateForOneMonth.multiply(BigDecimal.valueOf(interestCompoundedEvery.doubleValue()),
                mc);
        Integer numberOfPeriods = tenureInMonths / interestCompoundedEvery;

        BigDecimal interest = ratePerCompoundingPeriod.multiply(deposit.getAmount()).multiply(
                BigDecimal.valueOf(new Double(numberOfPeriods)));
        BigDecimal totalAmount = deposit.getAmount().add(interest);

        return Money.of(deposit.getCurrency(), totalAmount);
    }

    public Money calculateRemainInterest(Money deposit, Integer days, BigDecimal interestRate) {

        MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
        Integer daysInYear = 365;

        BigDecimal interestRateAsFraction = interestRate.divide(BigDecimal.valueOf(100), mc);
        BigDecimal interestRateForOneDay = interestRateAsFraction.divide(BigDecimal.valueOf(daysInYear.doubleValue()), mc);
        BigDecimal interest = BigDecimal.valueOf(deposit.getAmount().doubleValue() * days.doubleValue()
                * interestRateForOneDay.doubleValue());

        return Money.of(deposit.getCurrency(), interest);
    }
}