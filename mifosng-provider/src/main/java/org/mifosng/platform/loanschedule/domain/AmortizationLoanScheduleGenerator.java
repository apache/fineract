package org.mifosng.platform.loanschedule.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.LoanScheduleData;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.loan.domain.LoanCharge;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;

public interface AmortizationLoanScheduleGenerator {

	LoanScheduleData generate(
			ApplicationCurrency applicationCurrency, 
			LoanProductRelatedDetail loanScheduleInfo,
			LocalDate disbursementDate, 
			LocalDate interestCalculatedFrom,
			BigDecimal periodInterestRateForRepaymentPeriod,
			LocalDate idealDisbursementDateBasedOnFirstRepaymentDate,
			List<LocalDate> scheduledDates, 
			Set<LoanCharge> loanCharges);
}