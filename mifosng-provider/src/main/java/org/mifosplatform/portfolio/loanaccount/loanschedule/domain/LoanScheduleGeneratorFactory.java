package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;

public interface LoanScheduleGeneratorFactory {

    LoanScheduleGenerator create(InterestMethod interestMethod);

}
