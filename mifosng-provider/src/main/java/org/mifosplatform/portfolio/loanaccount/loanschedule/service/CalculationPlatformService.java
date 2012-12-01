package org.mifosplatform.portfolio.loanaccount.loanschedule.service;

import org.mifosplatform.portfolio.loanaccount.loanschedule.command.CalculateLoanScheduleCommand;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;

public interface CalculationPlatformService {

	LoanScheduleData calculateLoanSchedule(CalculateLoanScheduleCommand command);
}
