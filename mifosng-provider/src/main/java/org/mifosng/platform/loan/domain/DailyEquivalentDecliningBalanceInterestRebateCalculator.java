package org.mifosng.platform.loan.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class DailyEquivalentDecliningBalanceInterestRebateCalculator implements
		InterestRebateCalculator {

	private final static Logger logger = LoggerFactory
			.getLogger(DailyEquivalentDecliningBalanceInterestRebateCalculator.class);

	@Override
	public Money calculate(
			final LocalDate actualDisbursementDate,
			final LocalDate paidInFullDate,
			final Money loanPrincipal,
			final BigDecimal interestRatePerAnnum,
			final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
			final List<LoanTransaction> loanRepayments) {

		MonetaryCurrency currency = loanPrincipal.getCurrency();
		
		Money totalOriginalInterest = getTotalInterestOnLoan(currency, repaymentScheduleInstallments);

		BigDecimal dailyEquivalentPeriodicInterestRate = interestRatePerAnnum
				.divide(BigDecimal.valueOf(36500), 9, RoundingMode.HALF_EVEN);

		Money totalRecalculatedPrincipalPaid = Money.zero(loanPrincipal.getCurrency());
		Money totalRecalculatedInterestPaid = Money.zero(loanPrincipal.getCurrency());
		LocalDate lastBalanceDate = actualDisbursementDate;
		Money outstandingBalance = loanPrincipal;
		for (LoanTransaction loanRepayment : loanRepayments) {

			int daysAtBalance = Days.daysBetween(lastBalanceDate,
					loanRepayment.getTransactionDate()).getDays();

			BigDecimal periodInterestRateForPaymentPeriod = dailyEquivalentPeriodicInterestRate.multiply(BigDecimal.valueOf(daysAtBalance));
			BigDecimal interestDueOnPaymentDateAmount = outstandingBalance.getAmount().multiply(periodInterestRateForPaymentPeriod);

			Money interestDueOnPaymentDate = Money.of(loanPrincipal.getCurrency(),interestDueOnPaymentDateAmount);
			Money principalDueOnPaymentDate = loanRepayment.getAmount(currency).minus(interestDueOnPaymentDate);

			totalRecalculatedPrincipalPaid = totalRecalculatedPrincipalPaid.plus(principalDueOnPaymentDate);
			totalRecalculatedInterestPaid = totalRecalculatedInterestPaid.plus(interestDueOnPaymentDate);

			logger.warn("Installment: " + lastBalanceDate + " - "
					+ loanRepayment.getTransactionDate() + " principal: "
					+ principalDueOnPaymentDate + "interest: "
					+ interestDueOnPaymentDate + " total: "
					+ loanRepayment.getAmount());

			lastBalanceDate = loanRepayment.getTransactionDate();
			outstandingBalance = outstandingBalance
					.minus(principalDueOnPaymentDate);
		}

		// finally
		// add up total paid to date - work out interest due on that

		int daysAtBalance = Days.daysBetween(lastBalanceDate, paidInFullDate)
				.getDays();

		BigDecimal periodInterestRateForPaymentPeriod = dailyEquivalentPeriodicInterestRate
				.multiply(BigDecimal.valueOf(daysAtBalance));
		BigDecimal interestDueOnPaymentDateAmount = outstandingBalance
				.getAmount().multiply(periodInterestRateForPaymentPeriod);

		Money interestDueOnPaymentDate = Money.of(
				loanPrincipal.getCurrency(),
				interestDueOnPaymentDateAmount);

		Money principalDueOnPaymentDate = outstandingBalance;

		Money netOutstandingOnPaidInFullDate = principalDueOnPaymentDate
				.plus(interestDueOnPaymentDate);

		totalRecalculatedPrincipalPaid = totalRecalculatedPrincipalPaid.plus(principalDueOnPaymentDate);
		totalRecalculatedInterestPaid = totalRecalculatedInterestPaid.plus(interestDueOnPaymentDate);

		logger.warn("Installment: " + lastBalanceDate + " - " + paidInFullDate
				+ " principal: " + principalDueOnPaymentDate + "interest: "
				+ interestDueOnPaymentDate + " total: "
				+ netOutstandingOnPaidInFullDate);

		lastBalanceDate = paidInFullDate;
		outstandingBalance = outstandingBalance
				.minus(principalDueOnPaymentDate);

		Money rebate = Money.zero(loanPrincipal.getCurrency());

		// Interest can be greater for following reasons:
		// 1. calculated interest may be slightly ahead based on daily
		// equivalent
		// method of calculating interest compared to original method.

		// 2. payments were under expected amount or after expected date
		// resulting in larger outstanding balance being used to calculate
		// interest
		// at each repayment point in time.
		if (totalRecalculatedInterestPaid.isLessThan(totalOriginalInterest)
				&& totalRecalculatedInterestPaid.isGreaterThanZero()) {
			rebate = totalOriginalInterest.minus(totalRecalculatedInterestPaid);
		}

		return rebate;
	}

	private Money getTotalInterestOnLoan(
			final MonetaryCurrency currency,
			final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments) {

		Money cumulativeInterest = Money.zero(currency);

		for (LoanRepaymentScheduleInstallment scheduledRepayment : repaymentScheduleInstallments) {
			cumulativeInterest = cumulativeInterest.plus(scheduledRepayment
					.getInterest(currency));
		}

		return cumulativeInterest;
	}

}
