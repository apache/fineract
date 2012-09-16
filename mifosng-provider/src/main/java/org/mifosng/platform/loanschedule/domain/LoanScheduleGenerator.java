package org.mifosng.platform.loanschedule.domain;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.NewLoanScheduleData;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.LoanSchedule;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;

public interface LoanScheduleGenerator {

	LoanSchedule generate(LoanProductRelatedDetail loanScheduleInfo,
			Integer loanTermFrequency, 
			PeriodFrequencyType loanTermFrequencyType, 
			LocalDate disbursementDate, LocalDate firstRepaymentDate, LocalDate interestCalculatedFrom, 
			CurrencyData currencyData);

	NewLoanScheduleData generate(LoanProductRelatedDetail loanScheduleInfo, LocalDate disbursementDate, LocalDate firstRepaymentDate, LocalDate interestCalculatedFrom);

}