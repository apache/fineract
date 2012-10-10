package org.mifosng.platform.loan.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;

@SuppressWarnings("unused")
@Deprecated
public class DailyEquivalentFlatInterestRebateCalculator implements
		InterestRebateCalculator {

	@Override
	public Money calculate(
			final LocalDate actualDisbursementDate,
			final LocalDate paidInFullDate,
			final Money loanPrincipal,
			final BigDecimal interestRatePerAnnum,
			final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments,
			final List<LoanTransaction> loanRepayments) {

		MonetaryCurrency currency = loanPrincipal.getCurrency();
		
		LocalDate maturityDate = repaymentScheduleInstallments.get(
				repaymentScheduleInstallments.size() - 1).getDueDate();

		int daysEquivalentOfLoanTerm = Days.daysBetween(actualDisbursementDate,
				maturityDate).getDays();
		Money totalOriginalInterest = getTotalInterestOnLoan(
				currency,
				repaymentScheduleInstallments);

		int actualLoanTermInDays = Days.daysBetween(actualDisbursementDate,
				paidInFullDate).getDays();

		BigDecimal divisor = loanPrincipal.getAmount().multiply(
				BigDecimal.valueOf(daysEquivalentOfLoanTerm));

		BigDecimal periodicInterestRate = totalOriginalInterest.getAmount().divide(
				divisor, 9, RoundingMode.HALF_EVEN);

		Money totalRecalculatedInterest = loanPrincipal.multipliedBy(
				periodicInterestRate).multipliedBy(actualLoanTermInDays);

		Money rebate = Money.zero(loanPrincipal.getCurrency());

		if (totalRecalculatedInterest.isLessThan(totalOriginalInterest)
				&& totalRecalculatedInterest.isGreaterThanZero()) {
			rebate = totalOriginalInterest.minus(totalRecalculatedInterest);
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