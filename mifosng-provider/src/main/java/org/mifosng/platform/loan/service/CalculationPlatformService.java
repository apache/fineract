package org.mifosng.platform.loan.service;

import org.joda.time.LocalDate;
import org.mifosng.data.LoanPayoffReadModel;
import org.mifosng.data.LoanSchedule;
import org.mifosng.platform.api.commands.CalculateLoanScheduleCommand;

public interface CalculationPlatformService {

	LoanSchedule calculateLoanSchedule(CalculateLoanScheduleCommand command);

	LoanPayoffReadModel calculatePayoffOn(Long valueOf, LocalDate payoffDate);

}
