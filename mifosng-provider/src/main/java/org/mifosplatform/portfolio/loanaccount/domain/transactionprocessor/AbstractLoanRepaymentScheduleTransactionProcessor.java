/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.data.LoanChargePaidDetail;
import org.mifosplatform.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.mifosplatform.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionType;
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
            final List<LoanRepaymentScheduleInstallment> installments, final Set<LoanCharge> charges, final LocalDate recalculateChargesFrom) {
        final boolean reprocessCharges = true;
        return handleTransaction(disbursementDate, transactionsPostDisbursement, currency, installments, charges, recalculateChargesFrom,
                reprocessCharges);
    }

    private ChangedTransactionDetail handleTransaction(final LocalDate disbursementDate,
            final List<LoanTransaction> transactionsPostDisbursement, final MonetaryCurrency currency,
            final List<LoanRepaymentScheduleInstallment> installments, final Set<LoanCharge> charges,
            final LocalDate recalculateChargesFrom, boolean reprocessCharges) {

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
        if (reprocessCharges) {
            final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
            wrapper.reprocess(currency, disbursementDate, installments, charges, recalculateChargesFrom);
        }

        final ChangedTransactionDetail changedTransactionDetail = new ChangedTransactionDetail();
        final List<LoanTransaction> transactionstoBeProcessed = new ArrayList<>();
        for (final LoanTransaction loanTransaction : transactionsPostDisbursement) {
            if (loanTransaction.isChargePayment()) {
                List<LoanChargePaidDetail> chargePaidDetails = new ArrayList<>();
                final Set<LoanChargePaidBy> chargePaidBies = loanTransaction.getLoanChargesPaid();
                final Set<LoanCharge> transferCharges = new HashSet<>();
                for (final LoanChargePaidBy chargePaidBy : chargePaidBies) {
                    LoanCharge loanCharge = chargePaidBy.getLoanCharge();
                    transferCharges.add(loanCharge);
                    if (loanCharge.isInstalmentFee()) {
                        chargePaidDetails.addAll(loanCharge.fetchRepaymentInstallment(currency));
                    }
                }
                LocalDate startDate = disbursementDate;
                for (final LoanRepaymentScheduleInstallment installment : installments) {
                    for (final LoanCharge loanCharge : transferCharges) {
                        if (loanCharge.isDueForCollectionFromAndUpToAndIncluding(startDate, installment.getDueDate())) {
                            Money amountForProcess = loanCharge.getAmount(currency);
                            if (amountForProcess.isGreaterThan(loanTransaction.getAmount(currency))) {
                                amountForProcess = loanTransaction.getAmount(currency);
                            }
                            LoanChargePaidDetail chargePaidDetail = new LoanChargePaidDetail(amountForProcess, installment,
                                    loanCharge.isFeeCharge());
                            chargePaidDetails.add(chargePaidDetail);
                            break;
                        }
                    }
                    startDate = installment.getDueDate();
                }
                loanTransaction.resetDerivedComponents();
                Money unprocessed = loanTransaction.getAmount(currency);
                for (LoanChargePaidDetail chargePaidDetail : chargePaidDetails) {
                    final List<LoanRepaymentScheduleInstallment> processInstallments = new ArrayList<>(1);
                    processInstallments.add(chargePaidDetail.getInstallment());
                    Money processAmt = chargePaidDetail.getAmount();
                    if (processAmt.isGreaterThan(unprocessed)) {
                        processAmt = unprocessed;
                    }
                    unprocessed = handleTransactionAndCharges(loanTransaction, currency, processInstallments, transferCharges, processAmt,
                            chargePaidDetail.isFeeCharge());
                    if (!unprocessed.isGreaterThanZero()) {
                        break;
                    }
                }

                if (unprocessed.isGreaterThanZero()) {
                    onLoanOverpayment(loanTransaction, unprocessed);
                    loanTransaction.updateOverPayments(unprocessed);
                }

            } else {
                transactionstoBeProcessed.add(loanTransaction);
            }
        }

        for (final LoanTransaction loanTransaction : transactionstoBeProcessed) {

            if (!loanTransaction.getTypeOf().equals(LoanTransactionType.REFUND_FOR_ACTIVE_LOAN)) {
                final Comparator<LoanRepaymentScheduleInstallment> byDate = new Comparator<LoanRepaymentScheduleInstallment>() {

                    @Override
                    public int compare(LoanRepaymentScheduleInstallment ord1, LoanRepaymentScheduleInstallment ord2) {
                        return ord1.getDueDate().compareTo(ord2.getDueDate());
                    }
                };
                Collections.sort(installments, byDate);
            }

            if (loanTransaction.isRepayment() || loanTransaction.isInterestWaiver() || loanTransaction.isRecoveryRepayment()) {
                // pass through for new transactions
                if (loanTransaction.getId() == null) {
                    handleTransaction(loanTransaction, currency, installments, charges);
                    loanTransaction.adjustInterestComponent(currency);
                } else {
                    /**
                     * For existing transactions, check if the re-payment
                     * breakup (principal, interest, fees, penalties) has
                     * changed.<br>
                     **/
                    final LoanTransaction newLoanTransaction = LoanTransaction.copyTransactionProperties(loanTransaction);

                    // Reset derived component of new loan transaction and
                    // re-process transaction
                    handleTransaction(newLoanTransaction, currency, installments, charges);
                    newLoanTransaction.adjustInterestComponent(currency);
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
            } else if (loanTransaction.isRefundForActiveLoan()) {
                loanTransaction.resetDerivedComponents();

                handleRefund(loanTransaction, currency, installments, charges);
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

        final Money amountToProcess = null;
        final boolean isChargeAmount = false;
        handleTransaction(loanTransaction, currency, installments, charges, amountToProcess, isChargeAmount);

    }

    private void handleTransaction(final LoanTransaction loanTransaction, final MonetaryCurrency currency,
            final List<LoanRepaymentScheduleInstallment> installments, final Set<LoanCharge> charges, final Money chargeAmountToProcess,
            final boolean isFeeCharge) {

        Money transactionAmountUnprocessed = handleTransactionAndCharges(loanTransaction, currency, installments, charges,
                chargeAmountToProcess, isFeeCharge);

        if (transactionAmountUnprocessed.isGreaterThanZero()) {
            if (loanTransaction.isWaiver()) {
                loanTransaction.updateComponentsAndTotal(transactionAmountUnprocessed.zero(), transactionAmountUnprocessed.zero(),
                        transactionAmountUnprocessed.zero(), transactionAmountUnprocessed.zero());
            } else {
                onLoanOverpayment(loanTransaction, transactionAmountUnprocessed);
                loanTransaction.updateOverPayments(transactionAmountUnprocessed);
            }
        }
    }

    private Money handleTransactionAndCharges(final LoanTransaction loanTransaction, final MonetaryCurrency currency,
            final List<LoanRepaymentScheduleInstallment> installments, final Set<LoanCharge> charges, final Money chargeAmountToProcess,
            final boolean isFeeCharge) {
        // to.
        if (loanTransaction.isRepayment() || loanTransaction.isInterestWaiver() || loanTransaction.isRecoveryRepayment()) {
            loanTransaction.resetDerivedComponents();
        }
        Money transactionAmountUnprocessed = processTransaction(loanTransaction, currency, installments, chargeAmountToProcess);

        final Set<LoanCharge> loanFees = extractFeeCharges(charges);
        final Set<LoanCharge> loanPenalties = extractPenaltyCharges(charges);
        Integer installmentNumber = null;
        if (loanTransaction.isChargePayment() && installments.size() == 1) {
            installmentNumber = installments.get(0).getInstallmentNumber();
        }

        if (loanTransaction.isNotWaiver()) {
            Money feeCharges = loanTransaction.getFeeChargesPortion(currency);
            Money penaltyCharges = loanTransaction.getPenaltyChargesPortion(currency);
            if (chargeAmountToProcess != null && feeCharges.isGreaterThan(chargeAmountToProcess)) {
                if (isFeeCharge) {
                    feeCharges = chargeAmountToProcess;
                } else {
                    penaltyCharges = chargeAmountToProcess;
                }
            }
            if (feeCharges.isGreaterThanZero()) {
                updateChargesPaidAmountBy(loanTransaction, feeCharges, loanFees, installmentNumber);
            }

            if (penaltyCharges.isGreaterThanZero()) {
                updateChargesPaidAmountBy(loanTransaction, penaltyCharges, loanPenalties, installmentNumber);
            }
        }
        return transactionAmountUnprocessed;
    }

    private Money processTransaction(final LoanTransaction loanTransaction, final MonetaryCurrency currency,
            final List<LoanRepaymentScheduleInstallment> installments, Money amountToProcess) {
        int installmentIndex = 0;

        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        Money transactionAmountUnprocessed = loanTransaction.getAmount(currency);
        if (amountToProcess != null) {
            transactionAmountUnprocessed = amountToProcess;
        }

        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
            if (transactionAmountUnprocessed.isGreaterThanZero()) {
                if (currentInstallment.isNotFullyPaidOff()) {

                    // is this transaction early/late/on-time with respect to
                    // the
                    // current installment?
                    if (isTransactionInAdvanceOfInstallment(installmentIndex, installments, transactionDate, transactionAmountUnprocessed)) {
                        transactionAmountUnprocessed = handleTransactionThatIsPaymentInAdvanceOfInstallment(currentInstallment,
                                installments, loanTransaction, transactionDate, transactionAmountUnprocessed);
                    } else if (isTransactionALateRepaymentOnInstallment(installmentIndex, installments,
                            loanTransaction.getTransactionDate())) {
                        // does this result in a late payment of existing
                        // installment?
                        transactionAmountUnprocessed = handleTransactionThatIsALateRepaymentOfInstallment(currentInstallment, installments,
                                loanTransaction, transactionAmountUnprocessed);
                    } else {
                        // standard transaction
                        transactionAmountUnprocessed = handleTransactionThatIsOnTimePaymentOfInstallment(currentInstallment,
                                loanTransaction, transactionAmountUnprocessed);
                    }
                }
            }

            installmentIndex++;
        }
        return transactionAmountUnprocessed;
    }

    private Set<LoanCharge> extractFeeCharges(final Set<LoanCharge> loanCharges) {
        final Set<LoanCharge> feeCharges = new HashSet<>();
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isFeeCharge()) {
                feeCharges.add(loanCharge);
            }
        }
        return feeCharges;
    }

    private Set<LoanCharge> extractPenaltyCharges(final Set<LoanCharge> loanCharges) {
        final Set<LoanCharge> penaltyCharges = new HashSet<>();
        for (final LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isPenaltyCharge()) {
                penaltyCharges.add(loanCharge);
            }
        }
        return penaltyCharges;
    }

    private void updateChargesPaidAmountBy(final LoanTransaction loanTransaction, final Money feeCharges, final Set<LoanCharge> charges,
            final Integer installmentNumber) {

        Money amountRemaining = feeCharges;
        while (amountRemaining.isGreaterThanZero()) {
            final LoanCharge unpaidCharge = findEarliestUnpaidChargeFromUnOrderedSet(charges);
            Money feeAmount = feeCharges.zero();
            if (loanTransaction.isChargePayment()) {
                feeAmount = feeCharges;
            }
            final Money amountPaidTowardsCharge = unpaidCharge.updatePaidAmountBy(amountRemaining, installmentNumber, feeAmount);
            if (!amountPaidTowardsCharge.isZero()) {
                Set<LoanChargePaidBy> chargesPaidBies = loanTransaction.getLoanChargesPaid();
                if (loanTransaction.isChargePayment()) {
                    for (final LoanChargePaidBy chargePaidBy : chargesPaidBies) {
                        LoanCharge loanCharge = chargePaidBy.getLoanCharge();
                        if (loanCharge.getId().equals(unpaidCharge.getId())) {
                            chargePaidBy.setAmount(amountPaidTowardsCharge.getAmount());
                        }
                    }
                } else {
                    final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(loanTransaction, unpaidCharge,
                            amountPaidTowardsCharge.getAmount(), installmentNumber);
                    chargesPaidBies.add(loanChargePaidBy);
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
                if (loanCharge.isInstalmentFee()) {
                    LoanInstallmentCharge unpaidLoanChargePerInstallment = loanCharge.getUnpaidInstallmentLoanCharge();
                    if (chargePerInstallment == null
                            || chargePerInstallment.getRepaymentInstallment().getDueDate()
                                    .isAfter(unpaidLoanChargePerInstallment.getRepaymentInstallment().getDueDate())) {
                        installemntCharge = loanCharge;
                        chargePerInstallment = unpaidLoanChargePerInstallment;
                    }
                } else if (earliestUnpaidCharge == null || loanCharge.getDueLocalDate().isBefore(earliestUnpaidCharge.getDueLocalDate())) {
                    earliestUnpaidCharge = loanCharge;
                }
            }
        }
        if (earliestUnpaidCharge == null
                || (chargePerInstallment != null && earliestUnpaidCharge.getDueLocalDate().isAfter(
                        chargePerInstallment.getRepaymentInstallment().getDueDate()))) {
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

    @Override
    public Money handleRepaymentSchedule(final List<LoanTransaction> transactionsPostDisbursement, final MonetaryCurrency currency,
            final List<LoanRepaymentScheduleInstallment> installments) {
        Money unProcessed = Money.zero(currency);
        for (final LoanTransaction loanTransaction : transactionsPostDisbursement) {
            Money amountToProcess = null;
            final LoanTransaction newLoanTransaction = LoanTransaction.copyTransactionProperties(loanTransaction);
            if (newLoanTransaction.isRepayment() || newLoanTransaction.isInterestWaiver() || newLoanTransaction.isRecoveryRepayment()) {
                newLoanTransaction.resetDerivedComponents();
            }
            unProcessed = processTransaction(newLoanTransaction, currency, installments, amountToProcess);
        }
        return unProcessed;
    }

    @Override
    public boolean isInterestFirstRepaymentScheduleTransactionProcessor() {
        return false;
    }

    @Override
    public void handleRefund(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, final Set<LoanCharge> charges) {
        // TODO Auto-generated method stub

        final Comparator<LoanRepaymentScheduleInstallment> byDate = new Comparator<LoanRepaymentScheduleInstallment>() {

            @Override
            public int compare(LoanRepaymentScheduleInstallment ord1, LoanRepaymentScheduleInstallment ord2) {
                return ord1.getDueDate().compareTo(ord2.getDueDate());
            }
        };
        Collections.sort(installments, Collections.reverseOrder(byDate));
        Money transactionAmountUnprocessed = loanTransaction.getAmount(currency);

        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
            Money outstanding = currentInstallment.getTotalOutstanding(currency);
            Money due = currentInstallment.getDue(currency);

            if (outstanding.isLessThan(due)) {
                transactionAmountUnprocessed = handleRefundTransactionPaymentOfInstallment(currentInstallment, loanTransaction,
                        transactionAmountUnprocessed);

            }

            if (transactionAmountUnprocessed.isZero()) break;

        }

        final Set<LoanCharge> loanFees = extractFeeCharges(charges);
        final Set<LoanCharge> loanPenalties = extractPenaltyCharges(charges);
        Integer installmentNumber = null;

        final Money feeCharges = loanTransaction.getFeeChargesPortion(currency);
        if (feeCharges.isGreaterThanZero()) {
            undoChargesPaidAmountBy(loanTransaction, feeCharges, loanFees, installmentNumber);
        }

        final Money penaltyCharges = loanTransaction.getPenaltyChargesPortion(currency);
        if (penaltyCharges.isGreaterThanZero()) {
            undoChargesPaidAmountBy(loanTransaction, penaltyCharges, loanPenalties, installmentNumber);
        }

    }

    /**
     * Invoked when a there is a refund of an active loan or undo of an active
     * loan
     * 
     * Undoes principal, interest, fees and charges of this transaction based on
     * the repayment strategy
     * 
     */
    protected abstract Money handleRefundTransactionPaymentOfInstallment(final LoanRepaymentScheduleInstallment currentInstallment,
            final LoanTransaction loanTransaction, final Money transactionAmountUnprocessed);

    private void undoChargesPaidAmountBy(final LoanTransaction loanTransaction, final Money feeCharges, final Set<LoanCharge> charges,
            final Integer installmentNumber) {

        Money amountRemaining = feeCharges;
        while (amountRemaining.isGreaterThanZero()) {
            final LoanCharge paidCharge = findLatestPaidChargeFromUnOrderedSet(charges, feeCharges.getCurrency());

            if (paidCharge != null) {
                Money feeAmount = feeCharges.zero();

                final Money amountDeductedTowardsCharge = paidCharge.undoPaidOrPartiallyAmountBy(amountRemaining, installmentNumber,
                        feeAmount);
                if (amountDeductedTowardsCharge.isGreaterThanZero()) {

                    final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(loanTransaction, paidCharge, amountDeductedTowardsCharge
                            .getAmount().multiply(new BigDecimal(-1)), null);
                    loanTransaction.getLoanChargesPaid().add(loanChargePaidBy);

                    amountRemaining = amountRemaining.minus(amountDeductedTowardsCharge);
                }
            }
        }

    }

    @Override
    public ChangedTransactionDetail populateDerivedFeildsWithoutReprocess(final LocalDate disbursementDate,
            final List<LoanTransaction> transactionsPostDisbursement, final MonetaryCurrency currency,
            final List<LoanRepaymentScheduleInstallment> installments, final Set<LoanCharge> charges, final LocalDate recalculateChargesFrom) {
        final boolean reprocessCharges = false;
        return handleTransaction(disbursementDate, transactionsPostDisbursement, currency, installments, charges, recalculateChargesFrom,
                reprocessCharges);
    }

    private LoanCharge findLatestPaidChargeFromUnOrderedSet(final Set<LoanCharge> charges, MonetaryCurrency currency) {
        LoanCharge latestPaidCharge = null;
        LoanCharge installemntCharge = null;
        LoanInstallmentCharge chargePerInstallment = null;
        for (final LoanCharge loanCharge : charges) {
            boolean isPaidOrPartiallyPaid = loanCharge.isPaidOrPartiallyPaid(currency);
            if (isPaidOrPartiallyPaid && !loanCharge.isDueAtDisbursement()) {
                if (loanCharge.isInstalmentFee()) {
                    LoanInstallmentCharge paidLoanChargePerInstallment = loanCharge
                            .getLastPaidOrPartiallyPaidInstallmentLoanCharge(currency);
                    if (chargePerInstallment == null
                            || (paidLoanChargePerInstallment != null && chargePerInstallment.getRepaymentInstallment().getDueDate()
                                    .isBefore(paidLoanChargePerInstallment.getRepaymentInstallment().getDueDate()))) {
                        installemntCharge = loanCharge;
                        chargePerInstallment = paidLoanChargePerInstallment;
                    }
                } else if (latestPaidCharge == null || (loanCharge.isPaidOrPartiallyPaid(currency))
                        && loanCharge.getDueLocalDate().isAfter(latestPaidCharge.getDueLocalDate())) {
                    latestPaidCharge = loanCharge;
                }
            }
        }
        if (latestPaidCharge == null
                || (chargePerInstallment != null && latestPaidCharge.getDueLocalDate().isAfter(
                        chargePerInstallment.getRepaymentInstallment().getDueDate()))) {
            latestPaidCharge = installemntCharge;
        }

        return latestPaidCharge;
    }

}