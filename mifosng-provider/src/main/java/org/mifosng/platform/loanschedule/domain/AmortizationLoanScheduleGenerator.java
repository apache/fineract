package org.mifosng.platform.loanschedule.domain;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.LoanSchedule;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;

public interface AmortizationLoanScheduleGenerator {

	LoanSchedule generate(LoanProductRelatedDetail loanScheduleInfo, LocalDate disbursementDate, 
			LocalDate firstRepaymentDate, LocalDate interestCalculatedFrom, CurrencyData currencyData,
			BigDecimal periodInterestRateForRepaymentPeriod, 
			LocalDate idealDisbursementDateBasedOnFirstRepaymentDate, 
			List<LocalDate> scheduledDates);

}
