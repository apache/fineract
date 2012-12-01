package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;

public class AmortizationLoanScheduleGeneratorFactory {

	public AmortizationLoanScheduleGenerator createGenerator(final AmortizationMethod amortizationMethod) {
		
		AmortizationLoanScheduleGenerator generator = new EqualInstallmentsAmortizationLoanScheduleGenerator();
		
		switch (amortizationMethod) {
		case INVALID:
			generator = new EqualInstallmentsAmortizationLoanScheduleGenerator();
			break;
		case EQUAL_INSTALLMENTS:
			generator = new EqualInstallmentsAmortizationLoanScheduleGenerator();
			break;
		case EQUAL_PRINCIPAL:
			generator = new EqualPrincipalAmortizationLoanScheduleGenerator();
			break;
		}
		
		return generator;
	}

}
