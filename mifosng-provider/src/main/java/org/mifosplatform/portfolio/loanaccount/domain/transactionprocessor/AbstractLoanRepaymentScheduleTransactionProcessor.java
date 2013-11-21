/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.mifosplatform.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.impl.CreocoreLoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.impl.HeavensFamilyLoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.impl.InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor;

/**
 * Abstract implementation of {@link LoanRepaymentScheduleTransactionProcessor}
 * which is more convenient for concrete implementations to extend.
 * 
 * @see InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor
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
    public ChangedTransactionDetail handleTransaction(final LocalDate disbursementDate,
            final List<LoanTransaction> transactionsPostDisbursement, final MonetaryCurrency currency,
            final List<LoanRepaymentScheduleInstallment> installments, final Set<LoanCharge> charges) {

        if (charges != null) {
            for (final LoanCharge loanCharge : charges) {
                if (!loanCharge.isDueAtDisbursement()) {
                    loanCharge.resetPaidAmount(currency);
                }
            }
        }

        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
            currentInstallment.resetDerivedComponents();
            currentInstallment.updateDerivedFields(currency, disbursementDate);
        }

        // re-process loan charges over repayment periods (picking up on waived
        // loan charges)
        final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
        wrapper.reprocess(currency, disbursementDate, installments, charges);

        final ChangedTransactionDetail changedTransactionDetail = new ChangedTransactionDetail();
        final List<LoanTransaction> transactionstoBeProcessed = new ArrayList<LoanTransaction>();
        for (final LoanTransaction loanTransaction : transactionsPostDisbursement) {
            if (loanTransaction.isChargePayment()) {
                final List<LoanRepaymentScheduleInstallment> chargePaymentInstallments = new ArrayList<LoanRepaymentScheduleInstallment>();
                final Set<LoanChargePaidBy> chargePaidBies = loanTransaction.getLoanChargesPaid();
                final Set<LoanCharge> transferCharges = new HashSet<LoanCharge>();
                for (final LoanChargePaidBy chargePaidBy : chargePaidBies) {
                    LoanCharge loanCharge = chargePaidBy.getLoanCharge();
                    transferCharges.add(loanCharge);
                    if(loanCharge.isInstalmentFee()){
                        LoanRepaymentScheduleInstallment installment = null;
                        if(loanCharge.isFeeCharge()){
                            installment = loanCharge.fetchRepaymentInstallment(loanTransaction.getFeeChargesPortion(currency));
                        }else{
                            installment = loanCharge.fetchRepaymentInstallment(loanTransaction.getPenaltyChargesPortion(currency));
                        }
                        if(installment !=null){
                            chargePaymentInstallments.add(installment);
                        }
                    }
                }
                LocalDate startDate = disbursementDate;
                for (final LoanRepaymentScheduleInstallment installment : installments) {
                    for (final LoanCharge loanCharge : transferCharges) {
                        if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(startDate, installment.getDueDate())) {
                            chargePaymentInstallments.add(installment);
                            break;
                        }
                    }
                    startDate = installment.getDueDate();
                }
                loanTransaction.resetDerivedComponents();
                handleTransaction(loanTransaction, currency, chargePaymentInstallments, transferCharges);
            } else {
                transactionstoBeProcessed.add(loanTransaction);
            }
        }

        for (final LoanTransaction loanTransaction : transactionstoBeProcessed) {

            if (loanTransaction.isRepayment() || loanTransaction.isInterestWaiver()) {
                // pass through for new transactions
                if (loanTransaction.getId() == null) {
                    loanTransaction.resetDerivedComponents();
                    handleTransaction(loanTransaction, currency, installments, charges);
                } else {
                    /**
                     * For existing transactions, check if the re-payment
                     * breakup (principal, interest, fees, penalties) has
                     * changed.<br>
                     **/
                    final LoanTransaction newLoanTransaction = LoanTransaction.copyTransactionProperties(loanTransaction);

                    // Reset derived component of new loan transaction and
                    // re-process transaction
                    newLoanTransaction.resetDerivedComponents();
                    handleTransaction(newLoanTransaction, currency, installments, charges);

                    /**
                     * Check if the transaction amounts have changed. If so,
                     * reverse the original transaction and update
                     * changedTransactionDetail accordingly
                     **/
                    if (!LoanTransaction.transactionAmountsMatch(currency, loanTransaction, newLoanTransaction)) {
                        loanTransaction.reverse();
                        loanTransaction.updateExternalId(null);
                        changedTransactionDetail.getNewTransactionMappings().put(loanTransaction.getId(), newLoanTransaction);
                    }
                }

            } else if (loanTransaction.isWriteOff()) {
                loanTransaction.resetDerivedComponents();
                handleWriteOff(loanTransaction, currency, installments);
            }
        }
        return changedTransactionDetail;
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

        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {

            if (currentInstallment.isNotFullyPaidOff()) {

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

        final Set<LoanCharge> loanFees = extractFeeCharges(charges);
        final Set<LoanCharge> loanPenalties = extractPenaltyCharges(charges);
        Integer installmentNumber = null;
        if(loanTransaction.isChargePayment() && installments.size() == 1){
            installmentNumber = installments.get(0).getInstallmentNumber();
        }

        if (loanTransaction.isNotWaiver()) {
            final Money feeCharges = loanTransaction.getFeeChargesPortion(currency);
            if (feeCharges.isGreaterThanZero()) {
                updateChargesPaidAmountBy(loanTransaction, feeCharges, loanFees,installmentNumber);
            }

            final Money penaltyCharges = loanTransaction.getPenaltyChargesPortion(currency);
            if (penaltyCharges.isGreaterThanZero()) {
                updateChargesPaidAmountBy(loanTransaction, penaltyCharges, loanPenalties,installmentNumber);
            }
        }

        if (transactionAmountUnprocessed.isGreaterThanZero()) {
            onLoanOverpayment(loanTransaction, transactionAmountUnprocessed);
            loanTransaction.updateOverPayments(transactionAmountUnprocessed);
        }
    }

    private Set<LoanCharge> extractFeeCharges(final Set<LoanCharge> loanCharges) {
        final Set<LoanCharge> feeCharges = new HashSet<LoanCharge>();
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isFeeCharge()) {
                feeCharges.add(loanCharge);
            }
        }
        return feeCharges;
    }

    private Set<LoanCharge> extractPenaltyCharges(final Set<LoanCharge> loanCharges) {
        final Set<LoanCharge> penaltyCharges = new HashSet<LoanCharge>();
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isPenaltyCharge()) {
                penaltyCharges.add(loanCharge);
            }
        }
        return penaltyCharges;
    }

    private void updateChargesPaidAmountBy(final LoanTransaction loanTransaction, final Money feeCharges, final Set<LoanCharge> charges,final Integer installmentNumber) {

        Money amountRemaining = feeCharges;
        while(amountRemaining.isGreaterThanZero()) {
            final LoanCharge unpaidCharge = findEarliestUnpaidChargeFromUnOrderedSet(charges);
            Money  feeAmount = feeCharges.zero();
            if(loanTransaction.isChargePayment()){
                feeAmount = feeCharges;
            }
            final Money amountPaidTowardsCharge = unpaidCharge.updatePaidAmountBy(amountRemaining,installmentNumber, feeAmount);
            if (!amountPaidTowardsCharge.isZero()) {
                if (!loanTransaction.isChargePayment()) {
                    final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(loanTransaction, unpaidCharge,
                            amountPaidTowardsCharge.getAmount());
                    loanTransaction.getLoanChargesPaid().add(loanChargePaidBy);
                }
                amountRemaining = amountRemaining.minus(amountPaidTowardsCharge);
            }
        }
           
    }

    private LoanCharge findEarliestUnpaidChargeFromUnOrderedSet(final Set<LoanCharge> charges) {
        LoanCharge earliestUnpaidCharge = null;
        LoanCharge installemntCharge = null;
        LoanInstallmentCharge chargePerInstallment = null;
        for (final LoanCharge loanCharge : charges) {
            if (loanCharge.isNotFullyPaid() && !loanCharge.isDueAtDisbursement()) {
                if(loanCharge.isInstalmentFee()){
                    LoanInstallmentCharge  unpaidLoanChargePerInstallment = loanCharge.getUnpaidInstallmentLoanCharge();
                    if(chargePerInstallment == null || chargePerInstallment.getRepaymentInstallment().getDueDate().isAfter(unpaidLoanChargePerInstallment.getRepaymentInstallment().getDueDate())){
                        installemntCharge = loanCharge;
                        chargePerInstallment = unpaidLoanChargePerInstallment;
                    }
                }else if (earliestUnpaidCharge == null || loanCharge.getDueLocalDate().isBefore(earliestUnpaidCharge.getDueLocalDate())) {
                    earliestUnpaidCharge = loanCharge;
                }
            }
        }
        if(earliestUnpaidCharge == null || (chargePerInstallment!=null && earliestUnpaidCharge.getDueLocalDate().isAfter(chargePerInstallment.getRepaymentInstallment().getDueDate()))){
            earliestUnpaidCharge = installemntCharge;
        }

        return earliestUnpaidCharge;
    }

    @Override
    public void handleWriteOff(final LoanTransaction loanTransaction, final MonetaryCurrency currency,
            final List<LoanRepaymentScheduleInstallment> installments) {

        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        Money principalPortion = Money.zero(currency);
        Money interestPortion = Money.zero(currency);
        Money feeChargesPortion = Money.zero(currency);
        Money penaltychargesPortion = Money.zero(currency);

        // determine how much is written off in total and breakdown for
        // principal, interest and charges
        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {

            if (currentInstallment.isNotFullyPaidOff()) {
                principalPortion = principalPortion.plus(currentInstallment.writeOffOutstandingPrincipal(transactionDate, currency));
                interestPortion = interestPortion.plus(currentInstallment.writeOffOutstandingInterest(transactionDate, currency));
                feeChargesPortion = feeChargesPortion.plus(currentInstallment.writeOffOutstandingFeeCharges(transactionDate, currency));
                penaltychargesPortion = penaltychargesPortion.plus(currentInstallment.writeOffOutstandingPenaltyCharges(transactionDate,
                        currency));
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

        final LoanRepaymentScheduleInstallment currentInstallment = installments.get(installmentIndex);

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
     * 
     * Default implementation is check transaction date is before installment
     * due date.
     */
    protected boolean isTransactionInAdvanceOfInstallment(final int currentInstallmentIndex,
            final List<LoanRepaymentScheduleInstallment> installments, final LocalDate transactionDate,
            @SuppressWarnings("unused") final Money transactionAmount) {

        final LoanRepaymentScheduleInstallment currentInstallment = installments.get(currentInstallmentIndex);

        return transactionDate.isBefore(currentInstallment.getDueDate());
    }

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
    @SuppressWarnings("unused")
    protected void onLoanOverpayment(final LoanTransaction loanTransaction, final Money loanOverPaymentAmount) {
        // empty implementation by default.
    }
}