package org.mifosng.platform.loan.domain;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.currency.domain.Money;

/**
 * Creocore style {@link LoanRepaymentScheduleTransactionProcessor}.
 * 
 * For standard transactions, pays off components in order of interest, then
 * principal.
 * 
 * If a transaction results in an advance payment or over-payment for a given
 * installment, the over paid amount is pay off on the principal component of
 * subsequent installments.
 * 
 * If the entire principal of an installment is paid in advance then the
 * interest component is waived.
 */
public class CreocoreLoanRepaymentScheduleTransactionProcessor extends
		AbstractLoanRepaymentScheduleTransactionProcessor {

	/**
	 * For creocore, early is defined as any date before the installment due
	 * date
	 */
	@SuppressWarnings("unused")
	@Override
	protected boolean isTransactionInAdvanceOfInstallment(
			final int currentInstallmentIndex,
			final List<LoanRepaymentScheduleInstallment> installments,
			final LocalDate transactionDate, final Money transactionAmount) {

		LoanRepaymentScheduleInstallment currentInstallment = installments.get(currentInstallmentIndex);

		return transactionDate.isBefore(currentInstallment.getDueDate());
	}

	/**
	 * For early/'in advance' repayments, pay off in the same way as on-time payments,
	 * interest first then principal.
	 */
	@SuppressWarnings("unused")
	@Override
	protected Money handleTransactionThatIsPaymentInAdvanceOfInstallment(
			final LoanRepaymentScheduleInstallment currentInstallment,
			final List<LoanRepaymentScheduleInstallment> installments,
			final LoanTransaction loanTransaction,
			final LocalDate transactionDate, final Money paymentInAdvance) {
		
		return handleTransactionThatIsOnTimePaymentOfInstallment(currentInstallment, loanTransaction, paymentInAdvance);
	}

	/**
	 * For late repayments, pay off in the same way as on-time payments,
	 * interest first then principal.
	 */
	@SuppressWarnings("unused")
	@Override
	protected Money handleTransactionThatIsALateRepaymentOfInstallment(
			final LoanRepaymentScheduleInstallment currentInstallment,
			final List<LoanRepaymentScheduleInstallment> installments,
			final LoanTransaction loanTransaction,
			final Money transactionAmountUnprocessed) {

		return handleTransactionThatIsOnTimePaymentOfInstallment(
				currentInstallment, loanTransaction,
				transactionAmountUnprocessed);
	}

	/**
	 * For normal on-time repayments, pays off interest first, then principal.
	 */
	@Override
	protected Money handleTransactionThatIsOnTimePaymentOfInstallment(
			final LoanRepaymentScheduleInstallment currentInstallment,
			final LoanTransaction loanTransaction,
			final Money transactionAmountUnprocessed) {

		Money transactionAmountRemaining = transactionAmountUnprocessed;

		Money interestPortion = currentInstallment
				.payInterestComponent(transactionAmountRemaining);
		transactionAmountRemaining = transactionAmountRemaining
				.minus(interestPortion);

		Money principalPortion = currentInstallment
				.payPrincipalComponent(transactionAmountRemaining);
		transactionAmountRemaining = transactionAmountRemaining
				.minus(principalPortion);

		Money interestWaivedPortion = Money.zero(transactionAmountRemaining.getCurrency());
		loanTransaction.updateComponents(principalPortion, interestPortion, interestWaivedPortion, Money.zero(transactionAmountRemaining.getCurrency()));

		return transactionAmountRemaining;
	}

	@SuppressWarnings("unused")
	@Override
	protected void onLoanOverpayment(final LoanTransaction loanTransaction,
			final Money loanOverPaymentAmount) {
		// dont do anything for with loan over-payment
	}
}