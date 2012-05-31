package org.mifosng.platform.loanschedule.domain;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.LoanSchedule;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;

public interface LoanScheduleGenerator {

	LoanSchedule generate(LoanProductRelatedDetail loanScheduleInfo,
			LocalDate disbursementDate, LocalDate firstRepaymentDate, LocalDate interestCalculatedFrom, 
			CurrencyData currencyData);

}
