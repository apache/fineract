package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;

public class DefaultPaymentPeriodsInOneYearCalculator implements
		PaymentPeriodsInOneYearCalculator {

	@Override
	public Integer calculate(final PeriodFrequencyType repaymentFrequencyType) {

		Integer paymentPeriodsInOneYear = Integer.valueOf(0);
		switch (repaymentFrequencyType) {
		case DAYS:
			paymentPeriodsInOneYear = Integer.valueOf(365);
			break;
		case WEEKS:
			paymentPeriodsInOneYear = Integer.valueOf(52);
			break;
		case MONTHS:
			paymentPeriodsInOneYear = Integer.valueOf(12);
			break;
		case YEARS:
			paymentPeriodsInOneYear = Integer.valueOf(1);
			break;
		case INVALID:
			paymentPeriodsInOneYear = Integer.valueOf(0);
			break;
		}
		return paymentPeriodsInOneYear;
	}

	/**
	 * calculates the number of grace repayment periods and can be a fraction of a repayment period.
	 */
	@Override
	public double calculateRepaymentPeriodAsAFractionOfDays(
			PeriodFrequencyType repaymentPeriodFrequencyType,
			Integer every, LocalDate interestCalculatedFrom,
			List<LocalDate> scheduledDates, LocalDate disbursementDate) {
		
		Double periodFraction = Double.valueOf("0");
		
		if (interestCalculatedFrom != null && !interestCalculatedFrom.isBefore(disbursementDate)) {
			Integer repaymentPeriodIndex = 0;
			LocalDate lastRepaymentPeriod = disbursementDate;
			for (LocalDate repaymentPeriodDueDate : scheduledDates) {
				if (interestCalculatedFrom.isBefore(repaymentPeriodDueDate)) {
					// fraction is
					int numberOfDaysInterestCalculationGraceInPeriod = Days
							.daysBetween(
									lastRepaymentPeriod.toDateMidnight()
											.toDateTime(),
									interestCalculatedFrom.toDateMidnight()
											.toDateTime()).getDays();
					periodFraction = calculateRepaymentPeriodFraction(
							repaymentPeriodFrequencyType, every,
							numberOfDaysInterestCalculationGraceInPeriod);
					periodFraction = periodFraction
							+ repaymentPeriodIndex.doubleValue();
					break;
				}
				repaymentPeriodIndex++;
				lastRepaymentPeriod = repaymentPeriodDueDate;
			}
		}
		
		return periodFraction;
	}
	
	private double calculateRepaymentPeriodFraction(PeriodFrequencyType repaymentPeriodFrequencyType, Integer every, Integer numberOfDaysInterestCalculationGrace) {
		
		Double fraction = Double.valueOf("0");
		switch (repaymentPeriodFrequencyType) {
		case DAYS:
			fraction = numberOfDaysInterestCalculationGrace.doubleValue() * every.doubleValue();
			break;
		case WEEKS:
			fraction = numberOfDaysInterestCalculationGrace.doubleValue() / (Double.valueOf("7.0") * every.doubleValue());
			break;
		case MONTHS:
			fraction = numberOfDaysInterestCalculationGrace.doubleValue() / (Double.valueOf("30.0") * every.doubleValue());
			break;
		case YEARS:
			fraction = numberOfDaysInterestCalculationGrace.doubleValue() / (Double.valueOf("365.0") * every.doubleValue());
			break;
		case INVALID:
			fraction = Double.valueOf("0");
			break;
		}
		return fraction;
	}

}
