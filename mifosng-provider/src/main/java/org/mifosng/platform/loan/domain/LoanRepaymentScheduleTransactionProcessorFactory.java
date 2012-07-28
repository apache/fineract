package org.mifosng.platform.loan.domain;

import org.mifosng.platform.infrastructure.MifosPlatformTenant;
import org.mifosng.platform.infrastructure.ThreadLocalContextUtil;

/**
 *
 */
public class LoanRepaymentScheduleTransactionProcessorFactory {

	public LoanRepaymentScheduleTransactionProcessor determineProcessor() {

		LoanRepaymentScheduleTransactionProcessor processor = new HeavensFamilyLoanRepaymentScheduleTransactionProcessor();
		
		// FIXME - Just temp way of deciding what 'repayment strategy' to use for now.
		MifosPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
		if ("mifostenant-creocore".equalsIgnoreCase(tenant.getSchemaName())) {
			processor = new CreocoreLoanRepaymentScheduleTransactionProcessor();
		}
		
		return processor;
	}

}
