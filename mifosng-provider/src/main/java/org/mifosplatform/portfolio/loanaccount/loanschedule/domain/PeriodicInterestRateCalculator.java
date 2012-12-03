package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;

public class PeriodicInterestRateCalculator {

	private final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator = new DefaultPaymentPeriodsInOneYearCalculator();
	
	public BigDecimal calculateFrom(final PeriodFrequencyType loanTermFrequencyType, final BigDecimal annualNominalInterestRate) {
		
		MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
		
		Integer paymentPeriodsInOneYear = this.paymentPeriodsInOneYearCalculator.calculate(loanTermFrequencyType);
		
		BigDecimal divisor = BigDecimal.valueOf(paymentPeriodsInOneYear * 100);
		return annualNominalInterestRate.divide(divisor, mc);
	}
	
	public BigDecimal calculateFrom(final LoanProductRelatedDetail loanScheduleInfo) {
		
		MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
		
		Integer paymentPeriodsInOneYear = this.paymentPeriodsInOneYearCalculator.calculate(loanScheduleInfo.getRepaymentPeriodFrequencyType());
		
		BigDecimal divisor = BigDecimal.valueOf(paymentPeriodsInOneYear * 100);
		BigDecimal numberOfPeriods = BigDecimal.valueOf(loanScheduleInfo.getRepayEvery());
		
		return loanScheduleInfo.getAnnualNominalInterestRate().divide(divisor, mc).multiply(numberOfPeriods);
	}

	public Money calculateInterestOn(
			final Money outstandingBalance, 
			final BigDecimal periodInterestRateForRepaymentPeriod, 
			final int daysInPeriod, 
			final LoanProductRelatedDetail loanScheduleInfo) {

		MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
		Money interestDue = Money.zero(outstandingBalance.getCurrency());
		
		switch (loanScheduleInfo.getInterestCalculationPeriodMethod()) {
		case DAILY:
			BigDecimal dailyInterestRate = loanScheduleInfo.getAnnualNominalInterestRate()
			.divide(BigDecimal.valueOf(Long.valueOf(365)), mc)
			.divide(BigDecimal.valueOf(Double.valueOf("100.0")), mc)
			.multiply(BigDecimal.valueOf(loanScheduleInfo.getRepayEvery()));
	
			BigDecimal equivalentInterestRateForPeriod = dailyInterestRate.multiply(BigDecimal.valueOf(Long.valueOf(daysInPeriod)));
			
			interestDue = outstandingBalance.multiplyRetainScale(equivalentInterestRateForPeriod, RoundingMode.HALF_EVEN);
			break;
		default:
			interestDue = outstandingBalance.multiplyRetainScale(periodInterestRateForRepaymentPeriod, RoundingMode.HALF_EVEN);
			break;
		}
		
		return interestDue;
	}

	public Money calculatePrincipalOn(Money totalDuePerInstallment, Money interestForInstallment, LoanProductRelatedDetail loanScheduleInfo) {
		
		Money principalDue = Money.zero(totalDuePerInstallment.getCurrency());
		
		switch (loanScheduleInfo.getAmortizationMethod()) {
		case EQUAL_PRINCIPAL:
			principalDue = loanScheduleInfo.getPrincipal().dividedBy(loanScheduleInfo.getNumberOfRepayments(), RoundingMode.HALF_EVEN);
			break;
		case EQUAL_INSTALLMENTS:
			principalDue = totalDuePerInstallment.minus(interestForInstallment);
			break;
		case INVALID:
			break;
		}
		
		return principalDue;
	}
}