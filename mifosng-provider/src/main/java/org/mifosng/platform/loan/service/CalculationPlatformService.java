package org.mifosng.platform.loan.service;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.LoanScheduleData;
import org.mifosng.platform.api.commands.CalculateLoanScheduleCommand;

public interface CalculationPlatformService {

	LoanScheduleData calculateLoanScheduleNew(CalculateLoanScheduleCommand command);
	
	/**
	 * Not used at present from application. 
	 * 
	 * was a requirement from creocore that got into early prototye.
	 */
	LoanPayoffReadModel calculatePayoffOn(Long valueOf, LocalDate payoffDate);
}
