package org.mifosng.platform.loanschedule.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosng.platform.api.NewLoanScheduleData;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.LoanSchedulePeriodData;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;

/**
 * Irregular payments for declining balance not supported
 */
public class EqualInstallmentsAmortizationLoanScheduleGenerator implements AmortizationLoanScheduleGenerator {

	private final PeriodicInterestRateCalculator periodicInterestRateCalculator = new PeriodicInterestRateCalculator();
	private final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator = new DefaultPaymentPeriodsInOneYearCalculator();
	private final PmtCalculator pmtCalculator = new PmtCalculator();
	
	@Override
	public NewLoanScheduleData generate(
			final ApplicationCurrency currency,
			final LoanProductRelatedDetail loanScheduleInfo, 
			final LocalDate disbursementDate, 
			final LocalDate firstRepaymentDate,
			final LocalDate interestCalculatedFrom,
			final BigDecimal periodInterestRateForRepaymentPeriod, 
			final LocalDate idealDisbursementDateBasedOnFirstRepaymentDate,
			final List<LocalDate> scheduledDates) {
		
		final Collection<LoanSchedulePeriodData> periods = new ArrayList<LoanSchedulePeriodData>();
		
		// determine 'total payment' for each repayment based on pmt function (and hence the total due overall)
		final MonetaryCurrency monetaryCurrency = loanScheduleInfo.getPrincipal().getCurrency();
		final Money totalDuePerInstallment = this.pmtCalculator.calculatePaymentForOnePeriodFrom(loanScheduleInfo, periodInterestRateForRepaymentPeriod, monetaryCurrency);
		final Money totalRepaymentDueForLoanTerm = this.pmtCalculator.calculateTotalRepaymentFrom(loanScheduleInfo, periodInterestRateForRepaymentPeriod, monetaryCurrency);
		
		Money totalInterestDue = totalRepaymentDueForLoanTerm.minus(loanScheduleInfo.getPrincipal());
		Money outstandingBalance = loanScheduleInfo.getPrincipal();
		Money totalPrincipal = Money.zero(monetaryCurrency);
		Money totalInterest = Money.zero(monetaryCurrency);
		
		double interestCalculationGraceOnRepaymentPeriodFraction = this.paymentPeriodsInOneYearCalculator.calculateRepaymentPeriodAsAFractionOfDays(
				loanScheduleInfo.getRepaymentPeriodFrequencyType(), 
				loanScheduleInfo.getRepayEvery(), 
				interestCalculatedFrom, 
				scheduledDates, 
				idealDisbursementDateBasedOnFirstRepaymentDate);
		
		// create entries of disbursement period on loan schedule
		final LoanSchedulePeriodData disbursementPeriod = LoanSchedulePeriodData.disbursement(disbursementDate, loanScheduleInfo.getPrincipal().getAmount());
		periods.add(disbursementPeriod);
		
		int loanTermInDays = Integer.valueOf(0);
		BigDecimal cumulativePrincipalDisbursed = loanScheduleInfo.getPrincipal().getAmount();
		BigDecimal cumulativePrincipalDue = BigDecimal.ZERO;
		BigDecimal cumulativeInterestExpected = BigDecimal.ZERO;
		BigDecimal cumulativeChargesToDate = BigDecimal.ZERO;
		BigDecimal totalExpectedRepayment = BigDecimal.ZERO;
		
		LocalDate startDate = disbursementDate;
		int periodNumber = 1;
		for (LocalDate scheduledDueDate : scheduledDates) {

			// number of days from startDate to this scheduledDate
			int daysInPeriod = Days.daysBetween(startDate, scheduledDueDate).getDays();
			
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
			
			if (periodNumber == loanScheduleInfo.getNumberOfRepayments()) {
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
			}

			Money totalInstallmentDue = principalForInstallment.plus(interestForInstallment);

			outstandingBalance = outstandingBalance.minus(principalForInstallment);
			
			LoanSchedulePeriodData installment = LoanSchedulePeriodData.repaymentPeriod(periodNumber, startDate, 
					scheduledDueDate, 
					principalForInstallment.getAmount(), 
					outstandingBalance.getAmount(), 
					interestForInstallment.getAmount(), totalInstallmentDue.getAmount());

			periods.add(installment);
			
			// handle cumulative fields
			loanTermInDays += daysInPeriod;
			cumulativePrincipalDue = cumulativePrincipalDue.add(principalForInstallment.getAmount());
			cumulativeInterestExpected = cumulativeInterestExpected.add(interestForInstallment.getAmount());
			totalExpectedRepayment = totalExpectedRepayment.add(totalInstallmentDue.getAmount());
			startDate = scheduledDueDate;

			periodNumber++;
		}
		
		final BigDecimal cumulativePrincipalOutstanding = cumulativePrincipalDisbursed.subtract(cumulativePrincipalDue);
		
		CurrencyData currencyData = new CurrencyData(
				currency.getCode(), 
				currency.getName(),
				monetaryCurrency.getDigitsAfterDecimal(),
				currency.getDisplaySymbol(),
				currency.getNameCode());
		
		return new NewLoanScheduleData(currencyData, periods, loanTermInDays, cumulativePrincipalDisbursed, cumulativePrincipalDue, 
				cumulativePrincipalOutstanding, cumulativeInterestExpected, cumulativeChargesToDate, totalExpectedRepayment);
	}
}