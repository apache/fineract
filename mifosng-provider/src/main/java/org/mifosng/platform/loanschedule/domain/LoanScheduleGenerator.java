package org.mifosng.platform.loanschedule.domain;

import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.LoanScheduleData;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.loan.domain.LoanCharge;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;

public interface LoanScheduleGenerator {

	LoanScheduleData generate(ApplicationCurrency applicationCurrency, 
			LoanProductRelatedDetail loanScheduleInfo,
			Integer loanTermFrequency, 
			PeriodFrequencyType loanTermFrequencyType, 
			LocalDate disbursementDate, LocalDate firstRepaymentDate, LocalDate interestCalculatedFrom, Set<LoanCharge> loanCharges);

}