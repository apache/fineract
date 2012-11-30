package org.mifosng.platform.loanschedule.domain;

import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;

public class DefaultLoanScheduleGeneratorFactory implements
		LoanScheduleGeneratorFactory {

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