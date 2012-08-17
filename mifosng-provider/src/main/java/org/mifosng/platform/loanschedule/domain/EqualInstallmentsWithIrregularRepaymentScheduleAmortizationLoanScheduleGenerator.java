package org.mifosng.platform.loanschedule.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.LoanSchedule;
import org.mifosng.platform.api.data.MoneyData;
import org.mifosng.platform.api.data.ScheduledLoanInstallment;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;

public class EqualInstallmentsWithIrregularRepaymentScheduleAmortizationLoanScheduleGenerator implements AmortizationLoanScheduleGenerator {

	private final PeriodicInterestRateCalculator periodicInterestRateCalculator = new PeriodicInterestRateCalculator();
	private final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator = new DefaultPaymentPeriodsInOneYearCalculator();
	private final PmtCalculator pmtCalculator = new PmtCalculator();
	
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
		
		// 3. determine 'total payment' for each repayment based on pmt function (and hence the total due overall)
		final MonetaryCurrency monetaryCurrency = loanScheduleInfo.getPrincipal().getCurrency();
		Money totalDuePerInstallment = this.pmtCalculator.calculatePaymentForOnePeriodFrom(loanScheduleInfo, periodInterestRateForRepaymentPeriod, monetaryCurrency);
		final Money totalRepaymentDueForLoanTerm = this.pmtCalculator.calculateTotalRepaymentFrom(loanScheduleInfo, periodInterestRateForRepaymentPeriod, monetaryCurrency);
		
		// REVIEW. calculate total interest due based on loan term settings rather than inferring loan term from regular repayment schedule
//		final BigDecimal periodInterestRateForLoanTermPeriod = this.periodicInterestRateCalculator.calculateFrom(loanTermFrequencyType, loanScheduleInfo.getAnnualNominalInterestRate());
//		final Money totalRepaymentDueForLoanTerm = this.pmtCalculator.calculateTotalRepaymentFrom(loanScheduleInfo.getPrincipal(), loanTermFrequency, periodInterestRateForLoanTermPeriod, monetaryCurrency);
		
//		totalDuePerInstallment = totalRepaymentDueForLoanTerm.dividedBy(loanScheduleInfo.getNumberOfRepayments(), RoundingMode.HALF_EVEN);
		
		Money totalInterestDue = totalRepaymentDueForLoanTerm.minus(loanScheduleInfo.getPrincipal());
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
			
			// TODO - total interest for loan is calculated using loan term settings (loanTermFrequency & loanTermFrequencyType)
			//      - its possible that the 'repyament schedule' is irregular in which case the interest for the installment might not be the periodic interest rate
			//      - but infact somethink like twice it e.g. 4 month installment followed by 2 month installment followed by 1 month installment
			//      - the below doesnt really support highly irregular loan schedules and this case needs to be looked at.
			Money interestForInstallment = this.periodicInterestRateCalculator.calculateInterestOn(outstandingBalance, periodInterestRateForRepaymentPeriod, daysInPeriod, loanScheduleInfo);
			Money principalForInstallment = this.periodicInterestRateCalculator.calculatePrincipalOn(totalDuePerInstallment, interestForInstallment, loanScheduleInfo);
			
			if (interestCalculationGraceOnRepaymentPeriodFraction >= Integer.valueOf(1).doubleValue()) {
				Money graceOnInterestForRepaymentPeriod = interestForInstallment;
				interestForInstallment = interestForInstallment.minus(graceOnInterestForRepaymentPeriod);
				totalInterestDue = totalInterestDue.minus(graceOnInterestForRepaymentPeriod);
				interestCalculationGraceOnRepaymentPeriodFraction = interestCalculationGraceOnRepaymentPeriodFraction - Integer.valueOf(1).doubleValue();
			} else if (interestCalculationGraceOnRepaymentPeriodFraction > Double.valueOf("0.25") && interestCalculationGraceOnRepaymentPeriodFraction < Integer.valueOf(1).doubleValue()) {
				Money graceOnInterestForRepaymentPeriod = interestForInstallment.multipliedBy(interestCalculationGraceOnRepaymentPeriodFraction);
				interestForInstallment = interestForInstallment.minus(graceOnInterestForRepaymentPeriod);
				totalInterestDue = totalInterestDue.minus(graceOnInterestForRepaymentPeriod);
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
				
				final Money interestDifference = totalInterest.minus(totalInterestDue);
				if (interestDifference.isLessThanZero()) {
					interestForInstallment = interestForInstallment.plus(interestDifference.abs());
				} else if (interestDifference.isGreaterThanZero()) {
					interestForInstallment = interestForInstallment.minus(interestDifference.abs());
				}
				
//				if (loanScheduleInfo.getAmortizationMethod().equals(AmortizationMethod.EQUAL_INSTALLMENTS)) {
//					Money interestDifference = totalInterest.minus(totalInterestDue);
//					if (interestDifference.isLessThanZero()) {
//						interestForInstallment = interestForInstallment.plus(interestDifference.abs());
//					} else if (interestDifference.isGreaterThanZero()) {
//						interestForInstallment = interestForInstallment.minus(interestDifference.abs());
//					}
//				}
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