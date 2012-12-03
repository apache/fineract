package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;

/**
 * Old Mifos style {@link LoanRepaymentScheduleTransactionProcessor}.
 * 
 * For ALL types of transactions, pays off components in order of interest, then
 * principal.
 * 
 * Other formulas exist on mifos where you can choose 'Declining-Balance Interest Recalculation' which simply
 * means, recalculate the interest component based on the how much principal is outstanding at a point in time;
 * but this isnt trying to model that option only the basic one for now.
 */
@SuppressWarnings("unused")
public class MifosStyleLoanRepaymentScheduleTransactionProcessor extends
		AbstractLoanRepaymentScheduleTransactionProcessor {

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
		
		final MonetaryCurrency currency = transactionAmountUnprocessed.getCurrency();
		Money transactionAmountRemaining = transactionAmountUnprocessed;
		Money principalPortion = Money.zero(transactionAmountRemaining.getCurrency());
		Money interestPortion = Money.zero(transactionAmountRemaining.getCurrency());
		Money feeChargesPortion = Money.zero(transactionAmountRemaining.getCurrency());
		Money penaltyChargesPortion = Money.zero(transactionAmountRemaining.getCurrency());
		
		if (loanTransaction.isChargesWaiver()) {
//			penaltyChargesPortion = currentInstallment.waivePenaltyChargesComponent(loanTransaction.getPenaltyChargesPortion(currency));
//			transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
//			
//			feeChargesPortion = currentInstallment.waiveFeeChargesComponent(loanTransaction.getFeeChargesPortion(currency));
//			transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);
//			
			// zero this type of transaction and ignore it for now.
			transactionAmountRemaining = Money.zero(currency);
		} else if (loanTransaction.isInterestWaiver()) {
			interestPortion = currentInstallment.waiveInterestComponent(transactionAmountRemaining);
			transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);
			
			loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
		} else {
			penaltyChargesPortion = currentInstallment.payPenaltyChargesComponent(transactionAmountRemaining);
			transactionAmountRemaining = transactionAmountRemaining.minus(penaltyChargesPortion);
			
			feeChargesPortion = currentInstallment.payFeeChargesComponent(transactionAmountRemaining);
			transactionAmountRemaining = transactionAmountRemaining.minus(feeChargesPortion);
			
			interestPortion = currentInstallment.payInterestComponent(transactionAmountRemaining);
			transactionAmountRemaining = transactionAmountRemaining.minus(interestPortion);
	
			principalPortion = currentInstallment.payPrincipalComponent(transactionAmountRemaining);
			transactionAmountRemaining = transactionAmountRemaining.minus(principalPortion);
			
			loanTransaction.updateComponents(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
		}
		
		return transactionAmountRemaining;
	}

	@Override
	protected void onLoanOverpayment(final LoanTransaction loanTransaction,
			final Money loanOverPaymentAmount) {
		// TODO - KW - dont do anything with loan over-payment for now
	}
}