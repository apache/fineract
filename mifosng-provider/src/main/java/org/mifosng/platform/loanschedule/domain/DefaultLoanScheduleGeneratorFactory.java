package org.mifosng.platform.loanschedule.domain;

import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.InterestMethod;

public class DefaultLoanScheduleGeneratorFactory implements
		LoanScheduleGeneratorFactory {

	@Override
	public LoanScheduleGenerator create(final InterestMethod interestMethod,
			AmortizationMethod amortizationMethod) {

		LoanScheduleGenerator loanScheduleGenerator = null;

		switch (amortizationMethod) {
		case EQUAL_PRINCIPAL:
			switch (interestMethod) {
			case FLAT:
				loanScheduleGenerator = new FlatLoanScheduleGenerator();
				break;
			case DECLINING_BALANCE:
				loanScheduleGenerator = new DecliningBalanceLoanScheduleGenerator();
				break;
			case INVALID:
				break;
			}
			break;
		case EQUAL_INSTALLMENTS:
			switch (interestMethod) {
			case FLAT:
				loanScheduleGenerator = new FlatLoanScheduleGenerator();
				break;
			case DECLINING_BALANCE:
				loanScheduleGenerator = new DecliningBalanceEqualInstallmentsLoanScheduleGenerator();
				break;
			case INVALID:
				break;
			}
			break;
		case INVALID:
			break;
		}

		return loanScheduleGenerator;
	}
}