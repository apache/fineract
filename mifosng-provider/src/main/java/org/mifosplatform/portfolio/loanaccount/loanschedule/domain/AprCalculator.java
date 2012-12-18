package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;

import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.springframework.stereotype.Component;

@Component
public class AprCalculator {

    public BigDecimal calculateFrom(PeriodFrequencyType interestPeriodFrequencyType, BigDecimal interestRatePerPeriod) {
        BigDecimal defaultAnnualNominalInterestRate = BigDecimal.ZERO;
        switch (interestPeriodFrequencyType) {
            case DAYS:
            break;
            case WEEKS:
                defaultAnnualNominalInterestRate = interestRatePerPeriod.multiply(BigDecimal.valueOf(52));
            break;
            case MONTHS:
                defaultAnnualNominalInterestRate = interestRatePerPeriod.multiply(BigDecimal.valueOf(12));
            break;
            case YEARS:
                defaultAnnualNominalInterestRate = interestRatePerPeriod.multiply(BigDecimal.valueOf(1));
            break;
            case INVALID:
            break;
        }

        return defaultAnnualNominalInterestRate;
    }

}
