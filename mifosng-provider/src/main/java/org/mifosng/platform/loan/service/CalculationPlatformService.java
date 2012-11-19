package org.mifosng.platform.loan.service;

import org.mifosng.platform.api.commands.CalculateLoanScheduleCommand;
import org.mifosng.platform.api.data.LoanScheduleData;

public interface CalculationPlatformService {

	LoanScheduleData calculateLoanSchedule(CalculateLoanScheduleCommand command);
}
