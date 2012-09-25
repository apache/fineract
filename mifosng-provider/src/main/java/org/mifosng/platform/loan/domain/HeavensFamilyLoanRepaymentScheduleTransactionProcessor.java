package org.mifosng.platform.loan.domain;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;

/**
 * Heavensfamily style {@link LoanRepaymentScheduleTransactionProcessor}.
 * 
 * For standard transactions, pays off components in order of interest, then
 * principal.
 * 
 * If a transaction results in an advance payment or overpayment for a given
 * installment, the over paid amount is pay off on the principal component of
 * subsequent installments.
 * 
 * If the entire principal of an installment is paid in advance then the interest component is waived.
 */
@SuppressWarnings("unused")
public class HeavensFamilyLoanRepaymentScheduleTransactionProcessor extends AbstractLoanRepaymentScheduleTransactionProcessor {
	
	/**
	 * For late repayments, pay off in the same way as on-time payments,
	 * interest first then principal.
	 */
	@Override
	protected Money handleTransactionThatIsALateRepaymentOfInstallment(
			final LoanRepaymentScheduleInstallment currentInstallment,
			final List<LoanRepaymentScheduleInstallment> installments,
			final LoanTransaction loanTransaction,
			final Money transactionAmountUnprocessed) {

		return handleTransactionThatIsOnTimePaymentOfInstallment(currentInstallment, loanTransaction, transactionAmountUnprocessed);
	}

	@Override
	protected boolean isTransactionInAdvanceOfInstallment(
			final int currentInstallmentIndex,
			final List<LoanRepaymentScheduleInstallment> installments,
			final LocalDate transactionDate, 
			final Money transactionAmount) {
	
		boolean isInAdvance = false;
		
		LocalDate lastInstallmentDueDate = null;
		if (currentInstallmentIndex > 0) {
			int previousInstallmentIndex = currentInstallmentIndex - 1;
			LoanRepaymentScheduleInstallment previousInstallment = installments.get(previousInstallmentIndex);
			lastInstallmentDueDate = previousInstallment.getDueDate();
			
			return !transactionDate.isAfter(lastInstallmentDueDate);
		} 
		
		return isInAdvance;
	}

	/**
	 * For early/'in advance' repayments, pays off principal component only.
	 */
	@Override
	protected Money handleTransactionThatIsPaymentInAdvanceOfInstallment(
			final LoanRepaymentScheduleInstallment currentInstallment,
			final List<LoanRepaymentScheduleInstallment> installments,
			final LoanTransaction loanTransaction,
			final LocalDate transactionDate, final Money paymentInAdvance) {

		Money transactionAmountRemaining = paymentInAdvance;
		MonetaryCurrency currency = paymentInAdvance.getCurrency();

		// placeholders for tracking the breakdown of components relevant to
		// this transaction.
		Money principalPortion = Money.zero(currency);
		Money interestPortion = Money.zero(currency);
		Money interestWaivedPortion = Money.zero(currency);

		if (currentInstallment.isPrincipalNotCompleted(currency)) {
			principalPortion = currentInstallment
					.payPrincipalComponent(transactionAmountRemaining);
			if (currentInstallment.isPrincipalCompleted(currency)) {
				interestWaivedPortion = currentInstallment
						.waiveInterestComponent(currentInstallment
								.getInterest(currency));
			}

			loanTransaction.updateComponents(principalPortion, interestPortion, interestWaivedPortion, Money.zero(transactionAmountRemaining.getCurrency()));

			transactionAmountRemaining = transactionAmountRemaining
					.minus(principalPortion);
		}

		return transactionAmountRemaining;
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

		Money interestPortion = currentInstallment.payInterestComponent(transactionAmountRemaining);
		transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);

		Money principalPortion = currentInstallment.payPrincipalComponent(transactionAmountRemaining);
		transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);

		Money interestWaivedPortion = Money.zero(transactionAmountRemaining.getCurrency());
		loanTransaction.updateComponents(principalPortion, interestPortion, interestWaivedPortion, Money.zero(transactionAmountRemaining.getCurrency()));

		return transactionAmountRemaining;
	}

	@Override
	protected void onLoanOverpayment(
			final LoanTransaction loanTransaction,
			final Money loanOverPaymentAmount) {
	}
}