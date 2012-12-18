package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.springframework.stereotype.Component;

@Component
public class DefaultLoanScheduleGeneratorFactory implements LoanScheduleGeneratorFactory {

    @Override
    public LoanScheduleGenerator create(final InterestMethod interestMethod) {

        LoanScheduleGenerator loanScheduleGenerator = null;

        switch (interestMethod) {
            case FLAT:
                loanScheduleGenerator = new FlatMethodLoanScheduleGenerator();
            break;
            case DECLINING_BALANCE:
                loanScheduleGenerator = new DecliningBalanceMethodLoanScheduleGenerator();
            break;
            case INVALID:
            break;
        }

        return loanScheduleGenerator;
    }
}