package org.mifosng.platform.loanschedule.domain;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.NewLoanScheduleData;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;

public interface AmortizationLoanScheduleGenerator {

	NewLoanScheduleData generate(
			ApplicationCurrency applicationCurrency, 
			LoanProductRelatedDetail loanScheduleInfo,
			LocalDate disbursementDate, 
			LocalDate firstRepaymentDate,
			LocalDate interestCalculatedFrom,
			BigDecimal periodInterestRateForRepaymentPeriod,
			LocalDate idealDisbursementDateBasedOnFirstRepaymentDate,
			List<LocalDate> scheduledDates);
}