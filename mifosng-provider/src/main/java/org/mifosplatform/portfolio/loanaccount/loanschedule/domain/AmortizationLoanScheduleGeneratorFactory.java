/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
