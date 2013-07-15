package org.mifosplatform.portfolio.savings.domain.interest;

import java.math.BigDecimal;

import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;

public interface CompoundingPeriod {

    BigDecimal calculateInterest(BigDecimal interestRateAsFraction, long daysInYear);

    BigDecimal calculateInterest(SavingsCompoundingInterestPeriodType compoundingInterestPeriodType,
            SavingsInterestCalculationType interestCalculationType, BigDecimal interestFromPreviousPostingPeriod,
            BigDecimal interestRateAsFraction, long daysInYear);
}