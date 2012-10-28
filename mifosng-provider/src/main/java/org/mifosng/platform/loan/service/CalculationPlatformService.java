package org.mifosng.platform.loan.service;

import org.mifosng.platform.api.LoanScheduleData;
import org.mifosng.platform.api.commands.CalculateLoanScheduleCommand;

public interface CalculationPlatformService {

	LoanScheduleData calculateLoanSchedule(CalculateLoanScheduleCommand command);
}
