package org.mifosng.platform.loanschedule.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosng.platform.api.LoanScheduleData;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.LoanSchedulePeriodData;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.loan.domain.LoanCharge;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;

public class FlatMethodLoanScheduleGenerator implements LoanScheduleGenerator {

	private final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
	private final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator = new DefaultPaymentPeriodsInOneYearCalculator();
	
	@Override
	public LoanScheduleData generate(
			final ApplicationCurrency currency,
			final LoanProductRelatedDetail loanScheduleInfo,
			final Integer loanTermFrequency, 
			final PeriodFrequencyType loanTermFrequencyType, 
			final LocalDate disbursementDate, 
			final LocalDate firstRepaymentDate,
			final LocalDate interestCalculatedFrom,
			final Set<LoanCharge> loanCharges) {
		
		final Collection<LoanSchedulePeriodData> periods = new ArrayList<LoanSchedulePeriodData>();
		
		final List<LocalDate> scheduledDates = this.scheduledDateGenerator.generate(loanScheduleInfo, disbursementDate, firstRepaymentDate);

		MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
		
		BigDecimal loanTermPeriodsInYear = BigDecimal.valueOf(this.paymentPeriodsInOneYearCalculator.calculate(loanTermFrequencyType));
		BigDecimal interestRateForLoanTerm = loanScheduleInfo
				.getAnnualNominalInterestRate()
				.divide(loanTermPeriodsInYear, mc)
				.divide(BigDecimal.valueOf(Double.valueOf("100.0")), mc)
				.multiply(BigDecimal.valueOf(loanTermFrequency));
		
		final MonetaryCurrency monetaryCurrency = loanScheduleInfo.getPrincipal().getCurrency();
		Money totalInterestForLoanTerm = loanScheduleInfo.getPrincipal().multiplyRetainScale(interestRateForLoanTerm, RoundingMode.HALF_EVEN);

		Money interestPerInstallment = totalInterestForLoanTerm.dividedBy(Long.valueOf(loanScheduleInfo.getNumberOfRepayments()), RoundingMode.HALF_EVEN);
		
		Money principalPerInstallment = loanScheduleInfo.getPrincipal()
				.dividedBy(loanScheduleInfo.getNumberOfRepayments(),
						RoundingMode.HALF_EVEN);

		Money outstandingBalance = loanScheduleInfo.getPrincipal();
		Money principalDisbursed = loanScheduleInfo.getPrincipal(); 
		Money totalPrincipal = Money.zero(outstandingBalance.getCurrency());
		Money totalInterest = Money.zero(outstandingBalance.getCurrency());
		
		BigDecimal chargesDueAtTimeOfDisbursement = BigDecimal.ZERO;
		for (LoanCharge loanCharge : loanCharges) {
			// FIXME - KW - right now only charges at disbursement are supported.
			if (loanCharge.isDueAtDisbursement()) {
				chargesDueAtTimeOfDisbursement = chargesDueAtTimeOfDisbursement.add(loanCharge.amount());
			}
		}
		
		BigDecimal cumulativeChargesToDate = chargesDueAtTimeOfDisbursement;
		
		// create entries of disbursement period on loan schedule
		final LoanSchedulePeriodData disbursementPeriod = LoanSchedulePeriodData.disbursementOnlyPeriod(disbursementDate, principalDisbursed.getAmount(), chargesDueAtTimeOfDisbursement, false);
		periods.add(disbursementPeriod);
		
		int loanTermInDays = Integer.valueOf(0);
		BigDecimal cumulativePrincipalDisbursed = principalDisbursed.getAmount();
		BigDecimal cumulativePrincipalDue = BigDecimal.ZERO;
		BigDecimal cumulativeInterestExpected = BigDecimal.ZERO;
		BigDecimal totalExpectedRepayment = chargesDueAtTimeOfDisbursement;
		
		LocalDate startDate = disbursementDate;
		int periodNumber = 1;
		for (LocalDate scheduledDueDate : scheduledDates) {
			totalPrincipal = totalPrincipal.plus(principalPerInstallment);
			totalInterest = totalInterest.plus(interestPerInstallment);
			
			// number of days from startDate to this scheduledDate
			int daysInPeriod = Days.daysBetween(startDate.toDateMidnight().toDateTime(), scheduledDueDate.toDateMidnight().toDateTime()).getDays();

			if (periodNumber == loanScheduleInfo.getNumberOfRepayments()) {
				final Money difference = totalPrincipal.minus(loanScheduleInfo.getPrincipal());
				if (difference.isLessThanZero()) {
					principalPerInstallment = principalPerInstallment.plus(difference.abs());
				} else if (difference.isGreaterThanZero()) {
					principalPerInstallment = principalPerInstallment.minus(difference.abs());
				}
				
				final Money interestDifference = totalInterest.minus(totalInterestForLoanTerm);
				if (interestDifference.isLessThanZero()) {
					interestPerInstallment = interestPerInstallment.plus(interestDifference.abs());
				} else if (interestDifference.isGreaterThanZero()) {
					interestPerInstallment = interestPerInstallment.minus(interestDifference.abs());
				}
			}

			Money totalInstallmentDue = principalPerInstallment.plus(interestPerInstallment);
			outstandingBalance = outstandingBalance.minus(principalPerInstallment);
			
			LoanSchedulePeriodData installment = LoanSchedulePeriodData.repaymentOnlyPeriod(periodNumber, startDate, 
					scheduledDueDate, 
					principalPerInstallment.getAmount(), 
					outstandingBalance.getAmount(), 
					interestPerInstallment.getAmount(), totalInstallmentDue.getAmount());

			periods.add(installment);
			
			// handle cumulative fields
			loanTermInDays += daysInPeriod;
			cumulativePrincipalDue = cumulativePrincipalDue.add(principalPerInstallment.getAmount());
			cumulativeInterestExpected = cumulativeInterestExpected.add(interestPerInstallment.getAmount());
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
		
		return new LoanScheduleData(currencyData, periods, loanTermInDays, cumulativePrincipalDisbursed, cumulativePrincipalDue, 
				cumulativePrincipalOutstanding, cumulativeInterestExpected, cumulativeChargesToDate, totalExpectedRepayment);
	}
}