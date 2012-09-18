package org.mifosng.platform.loanschedule.domain;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.NewLoanScheduleData;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.LoanSchedule;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;

public interface AmortizationLoanScheduleGenerator {

	@Deprecated
	LoanSchedule generate(final LoanProductRelatedDetail loanScheduleInfo, 
			final LocalDate disbursementDate, 
			final LocalDate firstRepaymentDate,
			final LocalDate interestCalculatedFrom,
			final CurrencyData currencyData,
			final BigDecimal periodInterestRateForRepaymentPeriod, 
			final LocalDate idealDisbursementDateBasedOnFirstRepaymentDate,
			final List<LocalDate> scheduledDates);

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