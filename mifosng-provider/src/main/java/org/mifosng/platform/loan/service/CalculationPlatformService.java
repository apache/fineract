package org.mifosng.platform.loan.service;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.NewLoanScheduleData;
import org.mifosng.platform.api.commands.CalculateLoanScheduleCommand;
import org.mifosng.platform.api.data.LoanSchedule;

public interface CalculationPlatformService {

	LoanSchedule calculateLoanSchedule(CalculateLoanScheduleCommand command);

	NewLoanScheduleData calculateLoanScheduleNew(CalculateLoanScheduleCommand command);
	
	/**
	 * Not used at present from application. 
	 * 
	 * was a requirement from creocore that got into early prototye.
	 */
	LoanPayoffReadModel calculatePayoffOn(Long valueOf, LocalDate payoffDate);
}
