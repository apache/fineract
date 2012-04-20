package org.mifosng.platform.loanschedule.domain;

import org.joda.time.LocalDate;
import org.mifosng.data.CurrencyData;
import org.mifosng.data.LoanSchedule;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;

public interface LoanScheduleGenerator {

	LoanSchedule generate(LoanProductRelatedDetail loanScheduleInfo,
			LocalDate disbursementDate, LocalDate firstRepaymentDate, LocalDate interestCalculatedFrom, 
			CurrencyData currencyData);

}
