package org.mifosng.platform.loan.domain;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;

/**
 * Heavensfamily style {@link LoanRepaymentScheduleTransactionProcessor}.
 * 
 * For standard transactions, pays off components in order of interest, then principal.
 * 
 * If a transaction results in an overpayment for a given installment, the over paid amount is pay off on the principal component of subsequent installments.
 */
public class DefaultLoanRepaymentScheduleTransactionProcessor implements LoanRepaymentScheduleTransactionProcessor {

	@Override
	public void handleTransaction(final List<LoanTransaction> repaymentsOrWaivers,
			final MonetaryCurrency currency,
			final List<LoanRepaymentScheduleInstallment> installments) {
		
		for (LoanRepaymentScheduleInstallment currentInstallment : installments) {
			currentInstallment.resetDerivedComponents();
		}
		
		for (LoanTransaction loanTransaction : repaymentsOrWaivers) {
			
			loanTransaction.resetDerivedComponents();
			handleTransaction(loanTransaction, currency, installments);
		}
	}
	
	@Override
	public void handleTransaction(final LoanTransaction loanTransaction, final MonetaryCurrency currency, final List<LoanRepaymentScheduleInstallment> installments) {
		
		// find earliest unpaid installment for which to apply this transaction to.
		boolean transactionProcessed = false;
		int installmentIndex = 0;
		for (LoanRepaymentScheduleInstallment currentInstallment : installments) {
			
			if (currentInstallment.isNotFullyCompleted() && !transactionProcessed) {
				
				if (isTransactionInAdvance(installmentIndex, installments, loanTransaction)) {
					handleTransactionInAdvance(installments, loanTransaction, currency);
					transactionProcessed = true;
				} else if (isTransactionLate(installmentIndex, installments, loanTransaction.getTransactionDate())) {
					// does this result in a late payment of existing installment?
					handleLateTransaction(currentInstallment, installments, loanTransaction, currency);
					transactionProcessed = true;
				} else {
					// standard transaction
					Money installmentOverPaymentAmount = handleOnTimeTransaction(currentInstallment, loanTransaction, currency);
					Money loanOverPaymentAmount = handleOverpaymentTransaction(installments, loanTransaction, currency, installmentOverPaymentAmount);
					if (loanOverPaymentAmount.isGreaterThanZero()) {
						onLoanOverpayment(loanTransaction, currency, loanOverPaymentAmount);
					}
					transactionProcessed = true;
				}
			}
			
			installmentIndex++;
		}
	}

	/**
	 * Dont do anything special for late payments e.g. no fees, charges or penalties are applied
	 * @param currentInstallment2 
	 */
	protected void handleLateTransaction(
			final LoanRepaymentScheduleInstallment currentInstallment, 
			final List<LoanRepaymentScheduleInstallment> installments,
			final LoanTransaction loanTransaction, 
			final MonetaryCurrency currency) {
		
		Money installmentOverPaymentAmount = handleOnTimeTransaction(currentInstallment, loanTransaction, currency);
		Money loanOverPaymentAmount = handleOverpaymentTransaction(installments, loanTransaction, currency, installmentOverPaymentAmount);
		if (loanOverPaymentAmount.isGreaterThanZero()) {
			onLoanOverpayment(loanTransaction, currency, loanOverPaymentAmount);
		}
		
	}


	/**
	 * just process transactions paid in advance the same as you would handle an overpayment.
	 */
	protected void handleTransactionInAdvance(
			List<LoanRepaymentScheduleInstallment> installments,
			LoanTransaction loanTransaction, MonetaryCurrency currency) {
		
		final Money paymentInAdvance = loanTransaction.getAmount(currency); 
		Money loanOverPaymentAmount = handleOverpaymentTransaction(installments, loanTransaction, currency, paymentInAdvance);
		if (loanOverPaymentAmount.isGreaterThanZero()) {
			onLoanOverpayment(loanTransaction, currency, loanOverPaymentAmount);
		}
	}


	/**
	 * called when a transaction results in a overpayment of the full loan i.e.
	 * transaction amount is greater than the total expected principal +
	 * interest of the loan.
	 */
	protected void onLoanOverpayment(@SuppressWarnings("unused") final LoanTransaction loanTransaction, @SuppressWarnings("unused") final MonetaryCurrency currency, 
			@SuppressWarnings("unused") final Money loanOverPaymentAmount) {
	}

	/**
	 * Handles overpayment scenario by paying of the principal of the next/subsequent installments that are not fully completed.
	 */
	protected Money handleOverpaymentTransaction(
			final List<LoanRepaymentScheduleInstallment> installments,
			final LoanTransaction loanTransaction, MonetaryCurrency currency,
			final Money overPaymentAmount) {
		
		Money transactionAmountRemaining = overPaymentAmount;
		Money principalPortion = Money.zero(currency);
		Money interestPortion = Money.zero(currency);
		Money interestWaivedPortion = Money.zero(currency);
		
		for (LoanRepaymentScheduleInstallment installment : installments) {
			if (installment.isPrincipalNotCompleted(currency) && transactionAmountRemaining.isGreaterThanZero()) {
				principalPortion = installment.payPrincipalComponent(transactionAmountRemaining);
				if (installment.isPrincipalCompleted(currency)) {
					interestWaivedPortion = installment.waiveInterestComponent(installment.getInterest(currency));
				}
				
				loanTransaction.updateComponents(principalPortion, interestPortion, interestWaivedPortion);
				
				transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);
			}
			
			// reset portions for next installment
			principalPortion = Money.zero(currency);
			interestPortion = Money.zero(currency);
			interestWaivedPortion = Money.zero(currency);
		}
		
		return transactionAmountRemaining;
	}

	/**
	 * 
	 */
	protected Money handleOnTimeTransaction(final LoanRepaymentScheduleInstallment installment, final LoanTransaction loanTransaction, MonetaryCurrency currency) {
		
		Money transactionAmountRemaining = loanTransaction.getAmount(currency);
		
		Money interestPortion = installment.payInterestComponent(transactionAmountRemaining);
		transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);
		
		Money principalPortion = installment.payPrincipalComponent(transactionAmountRemaining);
		transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);
		
		Money interestWaivedPortion = Money.zero(transactionAmountRemaining.getCurrency());
		loanTransaction.updateComponents(principalPortion, interestPortion, interestWaivedPortion);
		
		return transactionAmountRemaining;
	}

	/**
	 * Is overpayment if the amount in the transactions is greater than amount due.
	 */
	protected boolean isTransactionOverpayment(final int installmentIndex,
			final List<LoanRepaymentScheduleInstallment> installments,
			final Money transactionAmount) {
		
		LoanRepaymentScheduleInstallment currentInstallment = installments.get(installmentIndex);
		
		return transactionAmount.isGreaterThan(currentInstallment.getTotalDue(transactionAmount.getCurrency()));
	}

	/**
	 * Is late if the date of the transactions is after that of the installment due date.
	 */
	protected boolean isTransactionLate(final int installmentIndex,
			final List<LoanRepaymentScheduleInstallment> installments,
			final LocalDate transactionDate) {

		LoanRepaymentScheduleInstallment currentInstallment = installments.get(installmentIndex);
		
		return transactionDate.isAfter(currentInstallment.getDueDate());
	}

	/**
	 * is in advance if the transaction date is before or equal to last installment due date (in other words not after last installment date)
	 */
	protected boolean isTransactionInAdvance(final int installmentIndex,
			final List<LoanRepaymentScheduleInstallment> installments,
			final LoanTransaction transaction) {

		boolean isInAdvance = false;
		
		LocalDate lastInstallmentDueDate = null;
		if (installmentIndex > 0) {
			int previousInstallmentIndex = installmentIndex - 1;
			LoanRepaymentScheduleInstallment previousInstallment = installments.get(previousInstallmentIndex);
			lastInstallmentDueDate = previousInstallment.getDueDate();
			
			return !transaction.getTransactionDate().isAfter(lastInstallmentDueDate);
		} 
		
		return isInAdvance;
	}
}