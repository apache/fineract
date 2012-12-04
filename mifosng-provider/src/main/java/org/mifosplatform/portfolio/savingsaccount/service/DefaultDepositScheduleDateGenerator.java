package org.mifosplatform.portfolio.savingsaccount.service;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsaccount.domain.DepositScheduleDateGenerator;

public class DefaultDepositScheduleDateGenerator implements	DepositScheduleDateGenerator {

	@Override
	public List<LocalDate> generate(LocalDate scheuleStartDate, Integer paymentPeriods, Integer depositFrequency, PeriodFrequencyType depositFrequencyType) {
		
		List<LocalDate> duePaymentPeriodDates = new ArrayList<LocalDate>(depositFrequency);
		LocalDate startDate = scheuleStartDate;
		
		for (int period=1; period <= paymentPeriods; period++){
			
			LocalDate duePaymentPeriodDate = startDate;
			if (period == 1) {
				duePaymentPeriodDate = startDate;
			} else {
				switch (depositFrequencyType) {
				case DAYS:
					duePaymentPeriodDate = startDate.plusDays(depositFrequency);
					break;
				case WEEKS:
					duePaymentPeriodDate = startDate.plusWeeks(depositFrequency);
					break;
				case MONTHS:
					duePaymentPeriodDate = startDate.plusMonths(depositFrequency);
					break;
				case YEARS:
					duePaymentPeriodDate = startDate.plusYears(depositFrequency);
					break;
				case INVALID:
					break;
				}
			}
			duePaymentPeriodDates.add(duePaymentPeriodDate);
			startDate = duePaymentPeriodDate;
		}
		
		return duePaymentPeriodDates;
	}
}
