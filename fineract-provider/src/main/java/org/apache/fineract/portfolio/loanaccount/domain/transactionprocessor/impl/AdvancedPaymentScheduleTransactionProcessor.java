/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanPaymentAllocationRule;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.AbstractLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanproduct.domain.FutureInstallmentAllocationRule;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.springframework.context.annotation.Profile;

//TODO: remove `test` profile when it is finished
@Profile("test")
@Slf4j
public class AdvancedPaymentScheduleTransactionProcessor extends AbstractLoanRepaymentScheduleTransactionProcessor {

    public static final String ADVANCED_PAYMENT_ALLOCATION_STRATEGY = "advanced-payment-allocation-strategy";

    @Override
    public String getCode() {
        return ADVANCED_PAYMENT_ALLOCATION_STRATEGY;
    }

    @Override
    public String getName() {
        return "Advanced payment allocation strategy";
    }

    @Override
    protected Money handleTransactionThatIsALateRepaymentOfInstallment(LoanRepaymentScheduleInstallment currentInstallment,
            List<LoanRepaymentScheduleInstallment> installments, LoanTransaction loanTransaction, Money transactionAmountUnprocessed,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Set<LoanCharge> charges) {
        throw new NotImplementedException();
    }

    @Override
    protected Money handleTransactionThatIsPaymentInAdvanceOfInstallment(LoanRepaymentScheduleInstallment currentInstallment,
            List<LoanRepaymentScheduleInstallment> installments, LoanTransaction loanTransaction, Money paymentInAdvance,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Set<LoanCharge> charges) {
        throw new NotImplementedException();
    }

    @Override
    protected Money handleTransactionThatIsOnTimePaymentOfInstallment(LoanRepaymentScheduleInstallment currentInstallment,
            LoanTransaction loanTransaction, Money transactionAmountUnprocessed,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Set<LoanCharge> charges) {
        throw new NotImplementedException();
    }

    @Override
    protected Money handleRefundTransactionPaymentOfInstallment(LoanRepaymentScheduleInstallment currentInstallment,
            LoanTransaction loanTransaction, Money transactionAmountUnprocessed,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings) {
        throw new NotImplementedException();
    }

    @Override
    public ChangedTransactionDetail reprocessLoanTransactions(LocalDate disbursementDate,
            List<LoanTransaction> transactionsPostDisbursement, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges) {

        // TODO: rewrite this whole logic step by step
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

        // TODO: Remove this reprocess and add the charges to the installment in chronological order
        final LoanRepaymentScheduleProcessingWrapper wrapper = new LoanRepaymentScheduleProcessingWrapper();
        wrapper.reprocess(currency, disbursementDate, installments, charges);
        final ChangedTransactionDetail changedTransactionDetail = new ChangedTransactionDetail();
        for (final LoanTransaction loanTransaction : transactionsPostDisbursement) {
            if (loanTransaction.getId() == null) {
                processLatestTransaction(loanTransaction, currency, installments, charges, Money.zero(currency));
                loanTransaction.adjustInterestComponent(currency);
            } else {
                /**
                 * For existing transactions, check if the re-payment breakup (principal, interest, fees, penalties) has
                 * changed.<br>
                 **/
                final LoanTransaction newLoanTransaction = LoanTransaction.copyTransactionProperties(loanTransaction);

                // Reset derived component of new loan transaction and
                // re-process transaction
                processLatestTransaction(newLoanTransaction, currency, installments, charges, Money.zero(currency));
                newLoanTransaction.adjustInterestComponent(currency);
                /**
                 * Check if the transaction amounts have changed. If so, reverse the original transaction and update
                 * changedTransactionDetail accordingly
                 **/
                if (LoanTransaction.transactionAmountsMatch(currency, loanTransaction, newLoanTransaction)) {
                    loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(
                            newLoanTransaction.getLoanTransactionToRepaymentScheduleMappings());
                } else {
                    createNewTransaction(loanTransaction, newLoanTransaction, changedTransactionDetail);
                }
            }
        }
        reprocessInstallments(installments, currency);
        return changedTransactionDetail;
    }

    @Override
    public void processLatestTransaction(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges, Money overpaidAmount) {
        switch (loanTransaction.getTypeOf()) {
            case WRITEOFF -> handleWriteOff(loanTransaction, currency, installments);
            case REFUND_FOR_ACTIVE_LOAN -> handleRefund(loanTransaction, currency, installments, charges);
            case CHARGEBACK -> handleChargeback(loanTransaction, currency, overpaidAmount, installments);

            case REPAYMENT, MERCHANT_ISSUED_REFUND, PAYOUT_REFUND, GOODWILL_CREDIT, CHARGE_REFUND, CHARGE_ADJUSTMENT, DOWN_PAYMENT,
                    WAIVE_INTEREST, RECOVERY_REPAYMENT ->
                handleRepayment(loanTransaction, currency, installments, charges);
            case CHARGE_OFF -> handleChargeOff(loanTransaction, currency, installments);
            // TODO: Cover rest of the transaction types
            default -> {
                log.warn("Unhandled transaction processing for transaction type: {}", loanTransaction.getTypeOf());
            }
        }
    }

    @Override
    public Money handleRepaymentSchedule(List<LoanTransaction> transactionsPostDisbursement, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> loanCharges) {
        throw new NotImplementedException();
    }

    private void handleRepayment(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges) {
        if (loanTransaction.isRepaymentLikeType() || loanTransaction.isInterestWaiver() || loanTransaction.isRecoveryRepayment()) {
            loanTransaction.resetDerivedComponents();
        }
        Money transactionAmountUnprocessed = loanTransaction.getAmount(currency);
        Money zero = Money.zero(currency);
        List<LoanTransactionToRepaymentScheduleMapping> transactionMappings = new ArrayList<>();

        List<LoanPaymentAllocationRule> paymentAllocationRules = loanTransaction.getLoan().getPaymentAllocationRules();
        LoanPaymentAllocationRule defaultPaymentAllocationRule = paymentAllocationRules.stream()
                .filter(e -> PaymentAllocationTransactionType.DEFAULT.equals(e.getTransactionType())).findFirst().orElseThrow();
        LoanPaymentAllocationRule paymentAllocationRule = paymentAllocationRules.stream()
                .filter(e -> loanTransaction.getTypeOf().equals(e.getTransactionType().getLoanTransactionType())).findFirst()
                .orElse(defaultPaymentAllocationRule);
        Balances balances = new Balances(zero, zero, zero, zero);
        for (PaymentAllocationType paymentAllocationType : paymentAllocationRule.getAllocationTypes()) {
            FutureInstallmentAllocationRule futureInstallmentAllocationRule = paymentAllocationRule.getFutureInstallmentAllocationRule();
            LoanRepaymentScheduleInstallment currentInstallment = null;
            Money paidPortion = zero;
            do {
                switch (paymentAllocationType.getDueType()) {
                    case PAST_DUE -> {
                        currentInstallment = installments.stream().filter(LoanRepaymentScheduleInstallment::isNotFullyPaidOff)
                                .filter(e -> loanTransaction.getTransactionDate().isAfter(e.getDueDate()))
                                .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).orElse(null);
                        if (currentInstallment != null) {
                            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                    transactionMappings, loanTransaction, currentInstallment, currency);
                            paidPortion = payAllocation(paymentAllocationType, currentInstallment, loanTransaction,
                                    transactionAmountUnprocessed, loanTransactionToRepaymentScheduleMapping, balances);
                            transactionAmountUnprocessed = transactionAmountUnprocessed.minus(paidPortion);
                        }
                    }
                    case DUE -> {
                        currentInstallment = installments.stream().filter(LoanRepaymentScheduleInstallment::isNotFullyPaidOff)
                                .filter(e -> loanTransaction.getTransactionDate().isEqual(e.getDueDate()))
                                .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).orElse(null);
                        if (currentInstallment != null) {
                            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                    transactionMappings, loanTransaction, currentInstallment, currency);
                            paidPortion = payAllocation(paymentAllocationType, currentInstallment, loanTransaction,
                                    transactionAmountUnprocessed, loanTransactionToRepaymentScheduleMapping, balances);
                            transactionAmountUnprocessed = transactionAmountUnprocessed.minus(paidPortion);
                        }
                    }
                    case IN_ADVANCE -> {
                        // For having similar logic we are populating installment list even when the future installment
                        // allocation rule is NEXT_INSTALLMENT or LAST_INSTALLMENT hence the list has only one element.
                        List<LoanRepaymentScheduleInstallment> currentInstallments = new ArrayList<>();
                        if (FutureInstallmentAllocationRule.REAMORTIZATION.equals(futureInstallmentAllocationRule)) {
                            currentInstallments = installments.stream().filter(LoanRepaymentScheduleInstallment::isNotFullyPaidOff)
                                    .filter(e -> loanTransaction.getTransactionDate().isBefore(e.getDueDate())).toList();
                        } else if (FutureInstallmentAllocationRule.NEXT_INSTALLMENT.equals(futureInstallmentAllocationRule)) {
                            currentInstallments = installments.stream().filter(LoanRepaymentScheduleInstallment::isNotFullyPaidOff)
                                    .filter(e -> loanTransaction.getTransactionDate().isBefore(e.getDueDate()))
                                    .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream().toList();
                        } else if (FutureInstallmentAllocationRule.LAST_INSTALLMENT.equals(futureInstallmentAllocationRule)) {
                            currentInstallments = installments.stream().filter(LoanRepaymentScheduleInstallment::isNotFullyPaidOff)
                                    .filter(e -> loanTransaction.getTransactionDate().isBefore(e.getDueDate()))
                                    .max(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream().toList();
                        }
                        int numberOfInstallments = currentInstallments.size();
                        paidPortion = zero;
                        if (numberOfInstallments > 0) {
                            // This will be the same amount as transactionAmountUnprocessed in case of the future
                            // installment allocation is NEXT_INSTALLMENT or LAST_INSTALLMENT
                            Money evenPortion = transactionAmountUnprocessed.dividedBy(numberOfInstallments, MoneyHelper.getRoundingMode());
                            // Adjustment might be needed due to the divide operation and the rounding mode
                            Money balanceAdjustment = transactionAmountUnprocessed.minus(evenPortion.multipliedBy(numberOfInstallments));
                            for (LoanRepaymentScheduleInstallment internalCurrentInstallment : currentInstallments) {
                                currentInstallment = internalCurrentInstallment;
                                // Adjust the portion for the last installment
                                if (internalCurrentInstallment.equals(currentInstallments.get(numberOfInstallments - 1))) {
                                    evenPortion = evenPortion.add(balanceAdjustment);
                                }
                                LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                        transactionMappings, loanTransaction, currentInstallment, currency);
                                Money internalPaidPortion = payAllocation(paymentAllocationType, currentInstallment, loanTransaction,
                                        evenPortion, loanTransactionToRepaymentScheduleMapping, balances);
                                // Some extra logic to allocate as much as possible across the installments if the
                                // outstanding balances are different
                                if (internalPaidPortion.isGreaterThanZero()) {
                                    paidPortion = internalPaidPortion;
                                }
                                transactionAmountUnprocessed = transactionAmountUnprocessed.minus(internalPaidPortion);
                            }
                        } else {
                            currentInstallment = null;
                        }
                    }
                }
            }
            // We are allocating till there is no pending installment or there is no more unprocessed transaction amount
            // or there is no more outstanding balance of the allocation type
            while (currentInstallment != null && transactionAmountUnprocessed.isGreaterThanZero() && paidPortion.isGreaterThanZero());

        }
        loanTransaction.updateComponents(balances.getAggregatedPrincipalPortion(), balances.getAggregatedInterestPortion(),
                balances.getAggregatedFeeChargesPortion(), balances.getAggregatedPenaltyChargesPortion());
        loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(transactionMappings);

        // TODO: rewrite to provide sorted list
        final Set<LoanCharge> loanFees = extractFeeCharges(charges);
        final Set<LoanCharge> loanPenalties = extractPenaltyCharges(charges);

        if (loanTransaction.isNotWaiver() && !loanTransaction.isAccrual()) {
            Money feeCharges = loanTransaction.getFeeChargesPortion(currency);
            Money penaltyCharges = loanTransaction.getPenaltyChargesPortion(currency);
            if (feeCharges.isGreaterThanZero()) {
                // TODO: Rewrite to exclude charge payment
                updateChargesPaidAmountBy(loanTransaction, feeCharges, loanFees, null);
            }

            if (penaltyCharges.isGreaterThanZero()) {
                // TODO: Rewrite to exclude charge payment
                updateChargesPaidAmountBy(loanTransaction, penaltyCharges, loanPenalties, null);
            }
        }
        handleOverpayment(transactionAmountUnprocessed, loanTransaction);
    }

    private LoanTransactionToRepaymentScheduleMapping getTransactionMapping(
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, LoanTransaction loanTransaction,
            LoanRepaymentScheduleInstallment currentInstallment, MonetaryCurrency currency) {
        Money zero = Money.zero(currency);
        LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = transactionMappings.stream()
                .filter(e -> loanTransaction.equals(e.getLoanTransaction()))
                .filter(e -> currentInstallment.equals(e.getLoanRepaymentScheduleInstallment())).findFirst().orElse(null);
        if (loanTransactionToRepaymentScheduleMapping == null) {
            loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction,
                    currentInstallment, zero, zero, zero, zero);
            transactionMappings.add(loanTransactionToRepaymentScheduleMapping);
        }
        return loanTransactionToRepaymentScheduleMapping;
    }

    private Money payAllocation(PaymentAllocationType paymentAllocationType, LoanRepaymentScheduleInstallment currentInstallment,
            LoanTransaction loanTransaction, Money transactionAmountUnprocessed,
            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping, Balances balances) {
        LocalDate transactionDate = loanTransaction.getTransactionDate();
        Money zero = transactionAmountUnprocessed.zero();
        return switch (paymentAllocationType.getAllocationType()) {
            case PENALTY -> {
                Money subPenaltyPortion = currentInstallment.payPenaltyChargesComponent(transactionDate, transactionAmountUnprocessed);
                balances.setAggregatedPenaltyChargesPortion(balances.getAggregatedPenaltyChargesPortion().add(subPenaltyPortion));
                addToTransactionMapping(loanTransactionToRepaymentScheduleMapping, zero, zero, zero, subPenaltyPortion);
                yield subPenaltyPortion;
            }
            case FEE -> {
                Money subFeePortion = currentInstallment.payFeeChargesComponent(transactionDate, transactionAmountUnprocessed);
                balances.setAggregatedFeeChargesPortion(balances.getAggregatedFeeChargesPortion().add(subFeePortion));
                addToTransactionMapping(loanTransactionToRepaymentScheduleMapping, zero, zero, subFeePortion, zero);
                yield subFeePortion;
            }
            case INTEREST -> {
                Money subInterestPortion = currentInstallment.payInterestComponent(transactionDate, transactionAmountUnprocessed);
                balances.setAggregatedInterestPortion(balances.getAggregatedInterestPortion().add(subInterestPortion));
                addToTransactionMapping(loanTransactionToRepaymentScheduleMapping, zero, subInterestPortion, zero, zero);
                yield subInterestPortion;
            }
            case PRINCIPAL -> {
                Money subPrincipalPortion = currentInstallment.payPrincipalComponent(transactionDate, transactionAmountUnprocessed);
                balances.setAggregatedPrincipalPortion(balances.getAggregatedPrincipalPortion().add(subPrincipalPortion));
                addToTransactionMapping(loanTransactionToRepaymentScheduleMapping, subPrincipalPortion, zero, zero, zero);
                yield subPrincipalPortion;
            }
        };
    }

    private void addToTransactionMapping(LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping,
            Money principalPortion, Money interestPortion, Money feePortion, Money penaltyPortion) {
        BigDecimal aggregatedPenalty = ObjectUtils
                .defaultIfNull(loanTransactionToRepaymentScheduleMapping.getPenaltyChargesPortion(), BigDecimal.ZERO)
                .add(penaltyPortion.getAmount());
        BigDecimal aggregatedFee = ObjectUtils
                .defaultIfNull(loanTransactionToRepaymentScheduleMapping.getFeeChargesPortion(), BigDecimal.ZERO)
                .add(feePortion.getAmount());
        BigDecimal aggregatedInterest = ObjectUtils
                .defaultIfNull(loanTransactionToRepaymentScheduleMapping.getInterestPortion(), BigDecimal.ZERO)
                .add(interestPortion.getAmount());
        BigDecimal aggregatedPrincipal = ObjectUtils
                .defaultIfNull(loanTransactionToRepaymentScheduleMapping.getPrincipalPortion(), BigDecimal.ZERO)
                .add(principalPortion.getAmount());
        loanTransactionToRepaymentScheduleMapping.setComponents(aggregatedPrincipal, aggregatedInterest, aggregatedFee, aggregatedPenalty);
    }

    private void handleOverpayment(Money overpaymentPortion, LoanTransaction loanTransaction) {
        if (overpaymentPortion.isGreaterThanZero()) {
            onLoanOverpayment(loanTransaction, overpaymentPortion);
            loanTransaction.updateOverPayments(overpaymentPortion);
        }
    }

    private void handleChargeOff(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments) {
        loanTransaction.resetDerivedComponents();
        // determine how much is outstanding total and breakdown for principal, interest and charges
        Money principalPortion = Money.zero(currency);
        Money interestPortion = Money.zero(currency);
        Money feeChargesPortion = Money.zero(currency);
        Money penaltychargesPortion = Money.zero(currency);
        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
            if (currentInstallment.isNotFullyPaidOff()) {
                principalPortion = principalPortion.plus(currentInstallment.getPrincipalOutstanding(currency));
                interestPortion = interestPortion.plus(currentInstallment.getInterestOutstanding(currency));
                feeChargesPortion = feeChargesPortion.plus(currentInstallment.getFeeChargesOutstanding(currency));
                penaltychargesPortion = penaltychargesPortion.plus(currentInstallment.getPenaltyChargesCharged(currency));
            }
        }

        loanTransaction.updateComponentsAndTotal(principalPortion, interestPortion, feeChargesPortion, penaltychargesPortion);
    }

    @AllArgsConstructor
    @Getter
    @Setter
    private static class Balances {

        private Money aggregatedPrincipalPortion;
        private Money aggregatedFeeChargesPortion;
        private Money aggregatedInterestPortion;
        private Money aggregatedPenaltyChargesPortion;
    }
}
