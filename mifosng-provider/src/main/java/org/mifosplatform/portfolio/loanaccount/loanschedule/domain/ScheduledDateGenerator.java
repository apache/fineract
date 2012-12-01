package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;

public interface ScheduledDateGenerator {

	List<LocalDate> generate(
			LoanProductMinimumRepaymentScheduleRelatedDetail loanScheduleDateInfo,
			LocalDate disbursementDate, LocalDate firstRepaymentDate);

	LocalDate idealDisbursementDateBasedOnFirstRepaymentDate(
			LoanProductRelatedDetail loanScheduleInfo,
			List<LocalDate> scheduledDates);

}
