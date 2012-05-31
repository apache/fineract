package org.mifosng.platform.loan.service;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.CalculateLoanScheduleCommand;
import org.mifosng.platform.api.data.LoanSchedule;

public interface CalculationPlatformService {

	LoanSchedule calculateLoanSchedule(CalculateLoanScheduleCommand command);

	LoanPayoffReadModel calculatePayoffOn(Long valueOf, LocalDate payoffDate);

}
