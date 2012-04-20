package org.mifosng.platform.loanschedule.domain;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;

public interface PaymentPeriodsInOneYearCalculator {

    Integer calculate(PeriodFrequencyType repaymentFrequencyType);

	double calculateRepaymentPeriodAsAFractionOfDays(
			PeriodFrequencyType repaymentPeriodFrequencyType,
			Integer every, LocalDate interestCalculatedFrom,
			List<LocalDate> scheduledDates, LocalDate disbursementDate);

}
