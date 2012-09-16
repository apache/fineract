package org.mifosng.platform.loanschedule.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosng.platform.api.NewLoanScheduleData;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.LoanSchedule;
import org.mifosng.platform.api.data.MoneyData;
import org.mifosng.platform.api.data.ScheduledLoanInstallment;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;

public class EqualPrincipalAmortizationLoanScheduleGenerator implements AmortizationLoanScheduleGenerator {

	private final PeriodicInterestRateCalculator periodicInterestRateCalculator = new PeriodicInterestRateCalculator();
	private final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator = new DefaultPaymentPeriodsInOneYearCalculator();
	
	@Override
	public NewLoanScheduleData generate(
			LoanProductRelatedDetail loanScheduleInfo,
			LocalDate disbursementDate, LocalDate firstRepaymentDate,
			LocalDate interestCalculatedFrom,
			BigDecimal periodInterestRateForRepaymentPeriod,
			LocalDate idealDisbursementDateBasedOnFirstRepaymentDate,
			List<LocalDate> scheduledDates) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public LoanSchedule generate(final LoanProductRelatedDetail loanScheduleInfo, 
			final LocalDate disbursementDate, 
			final LocalDate firstRepaymentDate,
			final LocalDate interestCalculatedFrom,
			final CurrencyData currencyData,
			final BigDecimal periodInterestRateForRepaymentPeriod, 
			final LocalDate idealDisbursementDateBasedOnFirstRepaymentDate,
			final List<LocalDate> scheduledDates) {
		
		List<ScheduledLoanInstallment> scheduledLoanInstallments = new ArrayList<ScheduledLoanInstallment>();
		
		// 1. we know 'principal' must be equal over repayments so use this to calculate interest and thus total repayment due.
		final MonetaryCurrency monetaryCurrency = loanScheduleInfo.getPrincipal().getCurrency();
		Money totalDuePerInstallment = Money.zero(monetaryCurrency);

		Money outstandingBalance = loanScheduleInfo.getPrincipal();
		Money totalPrincipal = Money.zero(monetaryCurrency);
		Money totalInterest = Money.zero(monetaryCurrency);
		
		double interestCalculationGraceOnRepaymentPeriodFraction = this.paymentPeriodsInOneYearCalculator.calculateRepaymentPeriodAsAFractionOfDays(loanScheduleInfo.getRepaymentPeriodFrequencyType(), 
												loanScheduleInfo.getRepayEvery(), interestCalculatedFrom, scheduledDates, idealDisbursementDateBasedOnFirstRepaymentDate);
		
		LocalDate startDate = disbursementDate;
		int installmentNumber = 1;
		for (LocalDate scheduledDueDate : scheduledDates) {

			// number of days from startDate to this scheduledDate
			int daysInPeriod = Days.daysBetween(startDate.toDateMidnight().toDateTime(), scheduledDueDate.toDateMidnight().toDateTime()).getDays();
			
			Money interestForInstallment = this.periodicInterestRateCalculator.calculateInterestOn(outstandingBalance, periodInterestRateForRepaymentPeriod, daysInPeriod, loanScheduleInfo);
			Money principalForInstallment = this.periodicInterestRateCalculator.calculatePrincipalOn(totalDuePerInstallment, interestForInstallment, loanScheduleInfo);
			
			if (interestCalculationGraceOnRepaymentPeriodFraction >= Integer.valueOf(1).doubleValue()) {
				Money graceOnInterestForRepaymentPeriod = interestForInstallment;
				interestForInstallment = interestForInstallment.minus(graceOnInterestForRepaymentPeriod);
				interestCalculationGraceOnRepaymentPeriodFraction = interestCalculationGraceOnRepaymentPeriodFraction - Integer.valueOf(1).doubleValue();
			} else if (interestCalculationGraceOnRepaymentPeriodFraction > Double.valueOf("0.25") && interestCalculationGraceOnRepaymentPeriodFraction < Integer.valueOf(1).doubleValue()) {
				Money graceOnInterestForRepaymentPeriod = interestForInstallment.multipliedBy(interestCalculationGraceOnRepaymentPeriodFraction);
				interestForInstallment = interestForInstallment.minus(graceOnInterestForRepaymentPeriod);
				interestCalculationGraceOnRepaymentPeriodFraction = Double.valueOf("0");
			}
			
			totalPrincipal = totalPrincipal.plus(principalForInstallment);
			totalInterest = totalInterest.plus(interestForInstallment);
			
			if (installmentNumber == loanScheduleInfo.getNumberOfRepayments()) {
				Money principalDifference = totalPrincipal.minus(loanScheduleInfo.getPrincipal());
				if (principalDifference.isLessThanZero()) {
					principalForInstallment = principalForInstallment.plus(principalDifference.abs());
				} else if (principalDifference.isGreaterThanZero()) {
					principalForInstallment = principalForInstallment.minus(principalDifference.abs());
				}
			}

			Money totalInstallmentDue = principalForInstallment.plus(interestForInstallment);

			outstandingBalance = outstandingBalance.minus(principalForInstallment);
			
			MoneyData principalPerInstallmentValue = MoneyData.of(currencyData, principalForInstallment.getAmount());
			MoneyData interestPerInstallmentValue = MoneyData.of(currencyData, interestForInstallment.getAmount());
			MoneyData totalInstallmentDueValue = MoneyData.of(currencyData, totalInstallmentDue.getAmount());
			MoneyData outstandingBalanceValue = MoneyData.of(currencyData, outstandingBalance.getAmount());
					
			ScheduledLoanInstallment installment = new ScheduledLoanInstallment(
					Integer.valueOf(installmentNumber), startDate,
					scheduledDueDate, principalPerInstallmentValue,
					interestPerInstallmentValue, 
					totalInstallmentDueValue,
					outstandingBalanceValue);

			scheduledLoanInstallments.add(installment);

			startDate = scheduledDueDate;

			installmentNumber++;
		}
		
		return new LoanSchedule(scheduledLoanInstallments);
	}
}