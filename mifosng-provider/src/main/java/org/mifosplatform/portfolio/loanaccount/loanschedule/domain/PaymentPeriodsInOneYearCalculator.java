package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;

public interface PaymentPeriodsInOneYearCalculator {

    Integer calculate(PeriodFrequencyType repaymentFrequencyType);

	double calculateRepaymentPeriodAsAFractionOfDays(
			PeriodFrequencyType repaymentPeriodFrequencyType,
			Integer every, LocalDate interestCalculatedFrom,
			List<LocalDate> scheduledDates, LocalDate disbursementDate);

}
