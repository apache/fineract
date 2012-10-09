package org.mifosng.platform.loan.domain;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;

/**
 * Abstract implementation of {@link LoanRepaymentScheduleTransactionProcessor} which is more convenient for concrete implementations to extend.
 * 
 * @see HeavensFamilyLoanRepaymentScheduleTransactionProcessor
 * @see CreocoreLoanRepaymentScheduleTransactionProcessor
 */
public abstract class AbstractLoanRepaymentScheduleTransactionProcessor implements LoanRepaymentScheduleTransactionProcessor {

	/**
	 * Provides support for passing all {@link LoanTransaction}'s so it will
	 * completely re-process the entire loan schedule. This is required in cases
	 * where the {@link LoanTransaction} being processed is in the past and
	 * falls before existing transactions or and adjustment is made to an
	 * existing in which case the entire loan schedule needs to be re-processed.
	 */
	@Override
	public void handleTransaction(
			final List<LoanTransaction> transactionsPostDisbursement,
			final MonetaryCurrency currency,
			final List<LoanRepaymentScheduleInstallment> installments) {
		
		for (LoanRepaymentScheduleInstallment currentInstallment : installments) {
			currentInstallment.resetDerivedComponents();
		}
		
		for (LoanTransaction loanTransaction : transactionsPostDisbursement) {
			
			if (loanTransaction.isRepayment() || loanTransaction.isInterestWaiver()) {
				loanTransaction.resetDerivedComponents();
				handleTransaction(loanTransaction, currency, installments);
			} else if (loanTransaction.isWriteOff()) {
				loanTransaction.resetDerivedComponents();
				handleWriteOff(loanTransaction, currency, installments);
			}
		}
	}
	
	/**
	 * Provides support for processing the latest transaction (which should be latest transaction) against the loan schedule.
	 */
	@Override
	public void handleTransaction(
			final LoanTransaction loanTransaction, 
			final MonetaryCurrency currency, 
			final List<LoanRepaymentScheduleInstallment> installments) {
		
		// find earliest unpaid installment for which to apply this transaction to.
		int installmentIndex = 0;

		final LocalDate transactionDate = loanTransaction.getTransactionDate();
		Money transactionAmountUnprocessed = loanTransaction.getAmount(currency);
		
		for (LoanRepaymentScheduleInstallment currentInstallment : installments) {
			
			if (currentInstallment.isNotFullyCompleted()) {
				
				// is this transaction early/late/on-time with respect to the current installment?
				if (isTransactionInAdvanceOfInstallment(installmentIndex, installments, transactionDate, transactionAmountUnprocessed)) {
					transactionAmountUnprocessed = handleTransactionThatIsPaymentInAdvanceOfInstallment(currentInstallment, installments, loanTransaction, transactionDate, transactionAmountUnprocessed);
				} else if (isTransactionALateRepaymentOnInstallment(installmentIndex, installments, loanTransaction.getTransactionDate())) {
					// does this result in a late payment of existing installment?
					transactionAmountUnprocessed = handleTransactionThatIsALateRepaymentOfInstallment(currentInstallment, installments, loanTransaction, transactionAmountUnprocessed);
				} else {
					// standard transaction
					transactionAmountUnprocessed = handleTransactionThatIsOnTimePaymentOfInstallment(currentInstallment, loanTransaction, transactionAmountUnprocessed);
				}
			}
			
			installmentIndex++;
		}
		
		if (transactionAmountUnprocessed.isGreaterThanZero()) {
			onLoanOverpayment(loanTransaction, transactionAmountUnprocessed);
		}
	}
	
	@Override
	public void handleWriteOff(
			final LoanTransaction loanTransaction,
			final MonetaryCurrency currency,
			final List<LoanRepaymentScheduleInstallment> installments) {
		
		final Money interestWaivedPortion = Money.zero(currency);
		Money principalPortion = Money.zero(currency);
		Money interestPortion = Money.zero(currency);
		Money chargesPortion = Money.zero(currency);
		
		// determine how much is written off in total and breakdown for principal, interest and charges
		for (LoanRepaymentScheduleInstallment currentInstallment : installments) {
			
			if (currentInstallment.isNotFullyCompleted()) {
				principalPortion = principalPortion.plus(currentInstallment.writeOffOutstandingPrincipal(currency));
				interestPortion = interestPortion.plus(currentInstallment.writeOffOutstandingInterest(currency));
			}
		}
		
		loanTransaction.updateComponentsAndTotal(principalPortion, interestPortion, interestWaivedPortion, chargesPortion);
	}

	// abstract interface
	/**
	 * This method is responsible for checking if the current transaction is 'an
	 * advance/early payment' based on the details passed through.
	 * 
	 * Default implementation simply processes transactions as 'Late' if the
	 * transaction date is after the installment due date.
	 */
	protected boolean isTransactionALateRepaymentOnInstallment(final int installmentIndex,
			final List<LoanRepaymentScheduleInstallment> installments,
			final LocalDate transactionDate) {

		LoanRepaymentScheduleInstallment currentInstallment = installments.get(installmentIndex);
		
		return transactionDate.isAfter(currentInstallment.getDueDate());
	}
	
	/**
	 * For late repayments, how should components of installment be paid off
	 */
	protected abstract Money handleTransactionThatIsALateRepaymentOfInstallment(
			final LoanRepaymentScheduleInstallment currentInstallment, 
			final List<LoanRepaymentScheduleInstallment> installments,
			final LoanTransaction loanTransaction, 
			final Money transactionAmountUnprocessed);

	/**
	 * This method is responsible for checking if the current transaction is 'an
	 * advance/early payment' based on the details passed through.
	 */
	protected abstract boolean isTransactionInAdvanceOfInstallment(
			final int currentInstallmentIndex,
			final List<LoanRepaymentScheduleInstallment> installments,
			final LocalDate transactionDate, 
			final Money transactionAmount);
	
	/**
	 * For early/'in advance' repayments.
	 */
	protected abstract Money handleTransactionThatIsPaymentInAdvanceOfInstallment(
			final LoanRepaymentScheduleInstallment currentInstallment,
			final List<LoanRepaymentScheduleInstallment> installments,
			final LoanTransaction loanTransaction, 
			final LocalDate transactionDate, 
			final Money paymentInAdvance);

	/**
	 * For normal on-time repayments.
	 */
	protected abstract Money handleTransactionThatIsOnTimePaymentOfInstallment(
			final LoanRepaymentScheduleInstallment currentInstallment, 
			final LoanTransaction loanTransaction, 
			final Money transactionAmountUnprocessed);
	
	/**
	 * Invoked when a transaction results in an over-payment of the full loan.
	 * 
	 * transaction amount is greater than the total expected principal and interest of the loan.
	 */
	protected abstract void onLoanOverpayment(final LoanTransaction loanTransaction, final Money loanOverPaymentAmount);
}