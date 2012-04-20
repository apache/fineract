package org.mifosng.platform;

import org.joda.time.LocalDate;
import org.mifosng.data.LoanPayoffReadModel;
import org.mifosng.data.LoanSchedule;
import org.mifosng.data.command.CalculateLoanScheduleCommand;

public interface CalculationPlatformService {

	LoanSchedule calculateLoanSchedule(CalculateLoanScheduleCommand command);

	LoanPayoffReadModel calculatePayoffOn(Long valueOf, LocalDate payoffDate);

}
