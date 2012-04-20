package org.mifosng.platform.loanschedule.domain;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.loan.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;

public interface ScheduledDateGenerator {

	List<LocalDate> generate(
			LoanProductMinimumRepaymentScheduleRelatedDetail loanScheduleDateInfo,
			LocalDate disbursementDate, LocalDate firstRepaymentDate);

	LocalDate idealDisbursementDateBasedOnFirstRepaymentDate(
			LoanProductRelatedDetail loanScheduleInfo,
			List<LocalDate> scheduledDates);

}
