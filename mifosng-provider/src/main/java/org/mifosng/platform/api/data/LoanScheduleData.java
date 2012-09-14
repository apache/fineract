package org.mifosng.platform.api.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.joda.time.LocalDate;

/**
 * Immutable data object representing the expected disbursement and repayment schedule for the loan.
 */
public class LoanScheduleData {

	private final LoanAccountSummaryData summary;
	@SuppressWarnings("unused")
	private final CurrencyData currency;
	@SuppressWarnings("unused")
	private final Collection<LoanRepaymentPeriodData> repaymentPeriods;

	public LoanScheduleData(final CurrencyData currency, final Collection<LoanRepaymentPeriodData> repaymentPeriods, final Collection<LoanRepaymentTransactionData> loanRepayments) {
		this.summary = derivedSummaryFrom(currency, repaymentPeriods, loanRepayments);
		this.currency = currency;
		this.repaymentPeriods = repaymentPeriods;
	}

	private LoanAccountSummaryData derivedSummaryFrom(
			final CurrencyData currency,
			final Collection<LoanRepaymentPeriodData> repaymentSchedule,
			final Collection<LoanRepaymentTransactionData> loanRepayments) {

		BigDecimal originalPrincipal = BigDecimal.ZERO;
		BigDecimal principalPaid = BigDecimal.ZERO;
		BigDecimal principalOutstanding = BigDecimal.ZERO;
		BigDecimal originalInterest = BigDecimal.ZERO;
		BigDecimal interestPaid = BigDecimal.ZERO;
		BigDecimal interestWaived = BigDecimal.ZERO;
		BigDecimal interestOutstanding = BigDecimal.ZERO;
		BigDecimal originalTotal = BigDecimal.ZERO;
		BigDecimal totalPaid = BigDecimal.ZERO;
		BigDecimal totalWaived = BigDecimal.ZERO;
		BigDecimal totalOutstanding = BigDecimal.ZERO;

		BigDecimal totalInArrears = BigDecimal.ZERO;

		for (LoanRepaymentPeriodData installment : repaymentSchedule) {
			originalPrincipal = originalPrincipal.add(installment.getPrincipal());
			principalPaid = principalPaid.add(installment.getPrincipalPaid());
			principalOutstanding = principalOutstanding.add(installment.getPrincipalOutstanding());

			originalInterest = originalInterest.add(installment.getInterest());
			interestPaid = interestPaid.add(installment.getInterestPaid());
			interestWaived = interestWaived.add(installment.getInterestWaived());
			interestOutstanding = interestOutstanding.add(installment.getInterestOutstanding());

			originalTotal = originalTotal.add(installment.getTotal());
			totalPaid = totalPaid.add(installment.getTotalPaid());
			totalWaived = totalWaived.add(installment.getTotalWaived());
			totalOutstanding = totalOutstanding.add(installment.getTotalOutstanding());

			if (installment.getDate().isBefore(new LocalDate())) {
				totalInArrears = totalInArrears.add(installment.getTotalOutstanding());
			}
		}

		// sum up all repayment transactions
		Long waiverType = (long) 4;
		MoneyData totalRepaymentAmount = MoneyData.of(currency,BigDecimal.ZERO);
		if (loanRepayments != null) {
			for (LoanRepaymentTransactionData loanRepayment : loanRepayments) {
				Long transactionType = loanRepayment.getTransactionType().getId();
				if (transactionType.equals(waiverType)) {
					// skip
				} else {
					totalRepaymentAmount = totalRepaymentAmount.plus(loanRepayment.getTotal());
				}
			}
		}

		// detect if loan is in overpaid state
		if (totalRepaymentAmount.isGreaterThan(MoneyData.of(currency, totalPaid))) {
			BigDecimal difference = totalRepaymentAmount.getAmount().subtract(totalPaid);
			totalOutstanding = totalOutstanding.subtract(difference);
		}

		return new LoanAccountSummaryData(currency, originalPrincipal,
				principalPaid, principalOutstanding, originalInterest,
				interestPaid, interestWaived, interestOutstanding,
				originalTotal, totalPaid, totalWaived, totalOutstanding,
				totalInArrears);
	}

	public BigDecimal totalOutstanding() {
		return this.summary.totalOutstanding();
	}
}