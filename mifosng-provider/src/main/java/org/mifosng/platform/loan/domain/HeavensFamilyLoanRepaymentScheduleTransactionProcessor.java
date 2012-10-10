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
		
		final MonetaryCurrency currency = paymentInAdvance.getCurrency();
		Money transactionAmountRemaining = paymentInAdvance;
		Money principalPortion = Money.zero(transactionAmountRemaining.getCurrency());
		Money interestPortion = Money.zero(transactionAmountRemaining.getCurrency());
		Money chargesPortion = Money.zero(transactionAmountRemaining.getCurrency());
		
		if (loanTransaction.isInterestWaiver()) {
			interestPortion = currentInstallment.waiveInterestComponent(transactionAmountRemaining);
			transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);
		} else {

			if (currentInstallment.isPrincipalNotCompleted(currency)) {
				principalPortion = currentInstallment.payPrincipalComponent(transactionAmountRemaining);
				if (currentInstallment.isPrincipalCompleted(currency)) {
					// FIXME - KW - if auto waiving interest need to create another transaction to handle this.
					currentInstallment.waiveInterestComponent(currentInstallment.getInterest(currency));
				}

				loanTransaction.updateComponents(principalPortion, interestPortion, chargesPortion);

				transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);
			}
		
			interestPortion = currentInstallment.payInterestComponent(transactionAmountRemaining);
			transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);
	
			principalPortion = currentInstallment.payPrincipalComponent(transactionAmountRemaining);
			transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);
		}
		
		loanTransaction.updateComponents(principalPortion, interestPortion, chargesPortion);
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
		Money principalPortion = Money.zero(transactionAmountRemaining.getCurrency());
		Money interestPortion = Money.zero(transactionAmountRemaining.getCurrency());
		Money chargesPortion = Money.zero(transactionAmountRemaining.getCurrency());
		
		if (loanTransaction.isInterestWaiver()) {
			interestPortion = currentInstallment.waiveInterestComponent(transactionAmountRemaining);
			transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);
		} else {
		
			interestPortion = currentInstallment.payInterestComponent(transactionAmountRemaining);
			transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);
	
			principalPortion = currentInstallment.payPrincipalComponent(transactionAmountRemaining);
			transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);
		}
		
		loanTransaction.updateComponents(principalPortion, interestPortion, chargesPortion);
		return transactionAmountRemaining;
	}

	@Override
	protected void onLoanOverpayment(
			final LoanTransaction loanTransaction,
			final Money loanOverPaymentAmount) {
	}
}