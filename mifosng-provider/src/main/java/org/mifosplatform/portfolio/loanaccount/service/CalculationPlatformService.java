package org.mifosplatform.portfolio.loanaccount.service;

import org.mifosplatform.portfolio.loanaccount.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.command.CalculateLoanScheduleCommand;

public interface CalculationPlatformService {

	LoanScheduleData calculateLoanSchedule(CalculateLoanScheduleCommand command);
}
