package org.mifosng.platform.loanschedule.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.LoanSchedule;
import org.mifosng.platform.api.data.MoneyData;
import org.mifosng.platform.api.data.ScheduledLoanInstallment;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;

public class FlatLoanScheduleGenerator implements LoanScheduleGenerator {

	private final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
	private final PaymentPeriodsInOneYearCalculator paymentPeriodsInOneYearCalculator = new DefaultPaymentPeriodsInOneYearCalculator();

	@Override
	public LoanSchedule generate(
			final LoanProductRelatedDetail loanScheduleInfo,
			final LocalDate disbursementDate, final LocalDate firstRepaymentDate, 
			final LocalDate interestCalculatedFrom, final CurrencyData currencyData) {

		List<ScheduledLoanInstallment> scheduledLoanInstallments = new ArrayList<ScheduledLoanInstallment>();

		List<LocalDate> scheduledDates = this.scheduledDateGenerator.generate(loanScheduleInfo, disbursementDate, firstRepaymentDate);

		MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
		
		// FIXME - use the expected loan term to work out interest due
		// loan term = 9 months

		Money principalPerInstallment = loanScheduleInfo.getPrincipal()
				.dividedBy(loanScheduleInfo.getNumberOfRepayments(),
						RoundingMode.HALF_EVEN);

		BigDecimal paymentPeriodsInYear = BigDecimal
				.valueOf(this.paymentPeriodsInOneYearCalculator
						.calculate(loanScheduleInfo.getRepaymentPeriodFrequencyType()));

		BigDecimal periodicInterestRate = loanScheduleInfo
				.getAnnualNominalInterestRate()
				.divide(paymentPeriodsInYear, mc)
				.divide(BigDecimal.valueOf(Double.valueOf("100.0")), mc)
				.multiply(BigDecimal.valueOf(loanScheduleInfo.getRepayEvery()));

		Money interestPerInstallment = loanScheduleInfo.getPrincipal().multiplyRetainScale(periodicInterestRate, RoundingMode.HALF_EVEN);

		Money outstandingBalance = loanScheduleInfo.getPrincipal();
		Money totalPrincipal = Money.zero(outstandingBalance.getCurrency());

		LocalDate startDate = disbursementDate;
		int installmentNumber = 1;
		for (LocalDate scheduledDueDate : scheduledDates) {
			totalPrincipal = totalPrincipal.plus(principalPerInstallment);

			if (installmentNumber == loanScheduleInfo.getNumberOfRepayments()) {
				Money difference = totalPrincipal.minus(loanScheduleInfo.getPrincipal());
				if (difference.isLessThanZero()) {
					principalPerInstallment = principalPerInstallment.plus(difference.abs());
				} else if (difference.isGreaterThanZero()) {
					principalPerInstallment = principalPerInstallment.minus(difference.abs());
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