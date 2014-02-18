package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;


public class LoanBorrowerCycleData {
    
    private final BigDecimal principal ;
    private final BigDecimal interestRatePerPeriod ;
    private final Integer numberOfRepayments;
    private final Integer termFrequency;

    
    public LoanBorrowerCycleData(final BigDecimal principal, final BigDecimal interestRatePerPeriod,final Integer numberOfRepayments,final Integer termFrequency) {
        
        this.principal = principal;
        this.interestRatePerPeriod = interestRatePerPeriod;
        this.numberOfRepayments = numberOfRepayments;
        this.termFrequency = termFrequency;
    }
}
