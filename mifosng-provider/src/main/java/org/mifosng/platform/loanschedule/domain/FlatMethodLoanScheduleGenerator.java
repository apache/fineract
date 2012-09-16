package org.mifosng.platform.loanschedule.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.NewLoanScheduleData;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.LoanSchedule;
import org.mifosng.platform.api.data.MoneyData;
import org.mifosng.platform.api.data.ScheduledLoanInstallment;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;

public class FlatMethodLoanScheduleGenerator implements LoanScheduleGenerator {

	private final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
	private final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator = new DefaultPaymentPeriodsInOneYearCalculator();
	
	@Override
	public NewLoanScheduleData generate(
			LoanProductRelatedDetail loanScheduleInfo,
			LocalDate disbursementDate, LocalDate firstRepaymentDate,
			LocalDate interestCalculatedFrom) {
		return null;
	}
	
	@Override
	public LoanSchedule generate(
			final LoanProductRelatedDetail loanScheduleInfo,
			final Integer loanTermFrequency, 
			final PeriodFrequencyType loanTermFrequencyType, 
			final LocalDate disbursementDate, final LocalDate firstRepaymentDate, 
			final LocalDate interestCalculatedFrom, final CurrencyData currencyData) {

		List<ScheduledLoanInstallment> scheduledLoanInstallments = new ArrayList<ScheduledLoanInstallment>();

		List<LocalDate> scheduledDates = this.scheduledDateGenerator.generate(loanScheduleInfo, disbursementDate, firstRepaymentDate);

		MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
		
		BigDecimal loanTermPeriodsInYear = BigDecimal.valueOf(this.paymentPeriodsInOneYearCalculator.calculate(loanTermFrequencyType));
		BigDecimal interestRateForLoanTerm = loanScheduleInfo
				.getAnnualNominalInterestRate()
				.divide(loanTermPeriodsInYear, mc)
				.divide(BigDecimal.valueOf(Double.valueOf("100.0")), mc)
				.multiply(BigDecimal.valueOf(loanTermFrequency));
		
		Money totalInterestForLoanTerm = loanScheduleInfo.getPrincipal().multiplyRetainScale(interestRateForLoanTerm, RoundingMode.HALF_EVEN);

		Money interestPerInstallment = totalInterestForLoanTerm.dividedBy(Long.valueOf(loanScheduleInfo.getNumberOfRepayments()), RoundingMode.HALF_EVEN);
		
		Money principalPerInstallment = loanScheduleInfo.getPrincipal()
				.dividedBy(loanScheduleInfo.getNumberOfRepayments(),
						RoundingMode.HALF_EVEN);

		Money outstandingBalance = loanScheduleInfo.getPrincipal();
		Money totalPrincipal = Money.zero(outstandingBalance.getCurrency());
		Money totalInterest = Money.zero(outstandingBalance.getCurrency());

		LocalDate startDate = disbursementDate;
		int installmentNumber = 1;
		for (LocalDate scheduledDueDate : scheduledDates) {
			totalPrincipal = totalPrincipal.plus(principalPerInstallment);
			totalInterest = totalInterest.plus(interestPerInstallment);

			if (installmentNumber == loanScheduleInfo.getNumberOfRepayments()) {
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
			
			MoneyData principalPerInstallmentValue = MoneyData.of(currencyData, principalPerInstallment.getAmount());
			MoneyData interestPerInstallmentValue = MoneyData.of(currencyData, interestPerInstallment.getAmount());
			MoneyData totalInstallmentDueValue = MoneyData.of(currencyData, totalInstallmentDue.getAmount());
			MoneyData outstandingBalanceValue = MoneyData.of(currencyData, outstandingBalance.getAmount());

			ScheduledLoanInstallment installment = new ScheduledLoanInstallment(
					Integer.valueOf(installmentNumber), startDate,
					scheduledDueDate, principalPerInstallmentValue,
					interestPerInstallmentValue, totalInstallmentDueValue,
					outstandingBalanceValue);

			scheduledLoanInstallments.add(installment);

			startDate = scheduledDueDate;

			installmentNumber++;
		}

		return new LoanSchedule(scheduledLoanInstallments);
	}
}