package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;

/**
 * Abstract implementation of {@link LoanRepaymentScheduleTransactionProcessor}
 * which is more convenient for concrete implementations to extend.
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
    public void handleTransaction(final LocalDate disbursementDate, final List<LoanTransaction> transactionsPostDisbursement,
            final MonetaryCurrency currency, final List<LoanRepaymentScheduleInstallment> installments, final Set<LoanCharge> charges) {

        if (charges != null) {
            for (LoanCharge loanCharge : charges) {
                if (!loanCharge.isDueAtDisbursement()) {
                    loanCharge.resetPaidAmount(currency);
                }
            }
        }

        for (LoanRepaymentScheduleInstallment currentInstallment : installments) {
            currentInstallment.resetDerivedComponents();
        }

        // re-process loan charges over repayment periods (picking up on waived
        // loan charges)
        LoanScheduleWrapper wrapper = new LoanScheduleWrapper();
        wrapper.reprocess(currency, disbursementDate, installments, charges);

        for (LoanTransaction loanTransaction : transactionsPostDisbursement) {

            if (loanTransaction.isRepayment() || loanTransaction.isInterestWaiver()) {
                loanTransaction.resetDerivedComponents();
                handleTransaction(loanTransaction, currency, installments, charges);
            } else if (loanTransaction.isWriteOff()) {
                loanTransaction.resetDerivedComponents();
                handleWriteOff(loanTransaction, currency, installments);
            }
        }
    }

    /**
     * Provides support for processing the latest transaction (which should be
     * latest transaction) against the loan schedule.
     */
    @Override
    public void handleTransaction(final LoanTransaction loanTransaction, final MonetaryCurrency currency,
            final List<LoanRepaymentScheduleInstallment> installments, final Set<LoanCharge> charges) {

        // find earliest unpaid installment for which to apply this transaction
        // to.
        int installmentIndex = 0;

        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        Money transactionAmountUnprocessed = loanTransaction.getAmount(currency);

        for (LoanRepaymentScheduleInstallment currentInstallment : installments) {

            if (currentInstallment.isNotFullyCompleted()) {

                // is this transaction early/late/on-time with respect to the
                // current installment?
                if (isTransactionInAdvanceOfInstallment(installmentIndex, installments, transactionDate, transactionAmountUnprocessed)) {
                    transactionAmountUnprocessed = handleTransactionThatIsPaymentInAdvanceOfInstallment(currentInstallment, installments,
                            loanTransaction, transactionDate, transactionAmountUnprocessed);
                } else if (isTransactionALateRepaymentOnInstallment(installmentIndex, installments, loanTransaction.getTransactionDate())) {
                    // does this result in a late payment of existing
                    // installment?
                    transactionAmountUnprocessed = handleTransactionThatIsALateRepaymentOfInstallment(currentInstallment, installments,
                            loanTransaction, transactionAmountUnprocessed);
                } else {
                    // standard transaction
                    transactionAmountUnprocessed = handleTransactionThatIsOnTimePaymentOfInstallment(currentInstallment, loanTransaction,
                            transactionAmountUnprocessed);
                }
            }

            installmentIndex++;
        }

        if (loanTransaction.isNotWaiver()) {
            Money feeCharges = loanTransaction.getFeeChargesPortion(currency);
            if (feeCharges.isGreaterThanZero()) {
                updateFeeChargesPaidAmountBy(feeCharges, charges);
            }

            Money penaltyCharges = loanTransaction.getPenaltyChargesPortion(currency);
            if (penaltyCharges.isGreaterThanZero()) {
                updatePenaltyChargesPaidAmountBy(penaltyCharges, charges);
            }
        }

        if (transactionAmountUnprocessed.isGreaterThanZero()) {
            onLoanOverpayment(loanTransaction, transactionAmountUnprocessed);
        }
    }

    private void updateFeeChargesPaidAmountBy(final Money feeCharges, final Set<LoanCharge> charges) {

        Money amountRemaining = feeCharges;
        for (LoanCharge loanCharge : charges) {
            if (!loanCharge.isDueAtDisbursement()) {
                if (loanCharge.isFeeCharge() && loanCharge.isNotFullyPaid() && amountRemaining.isGreaterThanZero()) {
                    final LoanCharge unpaidCharge = findEarliestUnpaidChargeFromUnOrderedSet(charges);
                    amountRemaining = unpaidCharge.updatePaidAmountBy(amountRemaining);
                }
            }
        }
    }

    private void updatePenaltyChargesPaidAmountBy(final Money feeCharges, final Set<LoanCharge> charges) {

        Money amountRemaining = feeCharges;
        for (LoanCharge loanCharge : charges) {
            if (!loanCharge.isDueAtDisbursement()) {

                if (loanCharge.isPenaltyCharge() && amountRemaining.isGreaterThanZero()) {
                    final LoanCharge unpaidCharge = findEarliestUnpaidChargeFromUnOrderedSet(charges);
                    amountRemaining = unpaidCharge.updatePaidAmountBy(amountRemaining);
                }
            }
        }
    }

    private LoanCharge findEarliestUnpaidChargeFromUnOrderedSet(Set<LoanCharge> charges) {
        LoanCharge earliestUnpaidCharge = null;

        for (LoanCharge loanCharge : charges) {
            if (loanCharge.isNotFullyPaid() && !loanCharge.isDueAtDisbursement()) {
                if (earliestUnpaidCharge == null
                        || loanCharge.getDueForCollectionAsOfLocalDate().isBefore(earliestUnpaidCharge.getDueForCollectionAsOfLocalDate())) {
                    earliestUnpaidCharge = loanCharge;
                }
            }
        }

        return earliestUnpaidCharge;
    }

    @Override
    public void handleWriteOff(final LoanTransaction loanTransaction, final MonetaryCurrency currency,
            final List<LoanRepaymentScheduleInstallment> installments) {

        Money principalPortion = Money.zero(currency);
        Money interestPortion = Money.zero(currency);
        Money feeChargesPortion = Money.zero(currency);
        Money penaltychargesPortion = Money.zero(currency);

        // determine how much is written off in total and breakdown for
        // principal, interest and charges
        for (LoanRepaymentScheduleInstallment currentInstallment : installments) {

            if (currentInstallment.isNotFullyCompleted()) {
                principalPortion = principalPortion.plus(currentInstallment.writeOffOutstandingPrincipal(currency));
                interestPortion = interestPortion.plus(currentInstallment.writeOffOutstandingInterest(currency));
            }
        }

        loanTransaction.updateComponentsAndTotal(principalPortion, interestPortion, feeChargesPortion, penaltychargesPortion);
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
            final List<LoanRepaymentScheduleInstallment> installments, final LocalDate transactionDate) {

        LoanRepaymentScheduleInstallment currentInstallment = installments.get(installmentIndex);

        return transactionDate.isAfter(currentInstallment.getDueDate());
    }

    /**
     * For late repayments, how should components of installment be paid off
     */
    protected abstract Money handleTransactionThatIsALateRepaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final List<LoanRepaymentScheduleInstallment> installments, final LoanTransaction loanTransaction,
            final Money transactionAmountUnprocessed);

    /**
     * This method is responsible for checking if the current transaction is 'an
     * advance/early payment' based on the details passed through.
     */
    protected abstract boolean isTransactionInAdvanceOfInstallment(final int currentInstallmentIndex,
            final List<LoanRepaymentScheduleInstallment> installments, final LocalDate transactionDate, final Money transactionAmount);

    /**
     * For early/'in advance' repayments.
     */
    protected abstract Money handleTransactionThatIsPaymentInAdvanceOfInstallment(
            final LoanRepaymentScheduleInstallment currentInstallment, final List<LoanRepaymentScheduleInstallment> installments,
            final LoanTransaction loanTransaction, final LocalDate transactionDate, final Money paymentInAdvance);

    /**
     * For normal on-time repayments.
     */
    protected abstract Money handleTransactionThatIsOnTimePaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final LoanTransaction loanTransaction, final Money transactionAmountUnprocessed);

    /**
     * Invoked when a transaction results in an over-payment of the full loan.
     * 
     * transaction amount is greater than the total expected principal and
     * interest of the loan.
     */
    protected abstract void onLoanOverpayment(final LoanTransaction loanTransaction, final Money loanOverPaymentAmount);
}