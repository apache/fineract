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
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanPaymentAllocationRule;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.apache.fineract.portfolio.loanaccount.domain.SingleLoanChargeRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.AbstractLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanproduct.domain.FutureInstallmentAllocationRule;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class AdvancedPaymentScheduleTransactionProcessor extends AbstractLoanRepaymentScheduleTransactionProcessor {

    public static final String ADVANCED_PAYMENT_ALLOCATION_STRATEGY = "advanced-payment-allocation-strategy";

    private final SingleLoanChargeRepaymentScheduleProcessingWrapper loanChargeProcessor = new SingleLoanChargeRepaymentScheduleProcessingWrapper();

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

    private void processSingleTransaction(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges,
            ChangedTransactionDetail changedTransactionDetail) {
        if (loanTransaction.getId() == null) {
            processLatestTransaction(loanTransaction, currency, installments, charges, Money.zero(currency));
            loanTransaction.adjustInterestComponent(currency);
        } else {
            /*
             * For existing transactions, check if the re-payment breakup (principal, interest, fees, penalties) has
             * changed.<br>
             */
            final LoanTransaction newLoanTransaction = LoanTransaction.copyTransactionProperties(loanTransaction);

            // Reset derived component of new loan transaction and
            // re-process transaction
            processLatestTransaction(newLoanTransaction, currency, installments, charges, Money.zero(currency));
            newLoanTransaction.adjustInterestComponent(currency);
            /*
             * Check if the transaction amounts have changed. If so, reverse the original transaction and update
             * changedTransactionDetail accordingly
             */
            if (LoanTransaction.transactionAmountsMatch(currency, loanTransaction, newLoanTransaction)) {
                loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(
                        newLoanTransaction.getLoanTransactionToRepaymentScheduleMappings());
            } else {
                createNewTransaction(loanTransaction, newLoanTransaction, changedTransactionDetail);
            }
        }
    }

    private void processSingleCharge(LoanCharge loanCharge, MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments,
            LocalDate disbursementDate) {
        loanChargeProcessor.reprocess(currency, disbursementDate, installments, loanCharge);
    }

    @Override
    public ChangedTransactionDetail reprocessLoanTransactions(LocalDate disbursementDate, List<LoanTransaction> loanTransactions,
            MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges) {

        // TODO: rewrite this whole logic step by step
        if (charges != null) {
            for (final LoanCharge loanCharge : charges) {
                if (!loanCharge.isDueAtDisbursement()) {
                    loanCharge.resetPaidAmount(currency);
                }
            }
        }

        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
            currentInstallment.resetBalances();
            currentInstallment.updateDerivedFields(currency, disbursementDate);
        }

        List<ChargeOrTransaction> chargeOrTransactions = createSortedChargesAndTransactionsList(loanTransactions, charges);

        final ChangedTransactionDetail changedTransactionDetail = new ChangedTransactionDetail();
        for (final ChargeOrTransaction chargeOrTransaction : chargeOrTransactions) {
            chargeOrTransaction.getLoanTransaction().ifPresent(loanTransaction -> processSingleTransaction(loanTransaction, currency,
                    installments, charges, changedTransactionDetail));
            chargeOrTransaction.getLoanCharge()
                    .ifPresent(loanCharge -> processSingleCharge(loanCharge, currency, installments, disbursementDate));
        }
        reprocessInstallments(installments, currency);
        return changedTransactionDetail;
    }

    @NotNull
    private List<ChargeOrTransaction> createSortedChargesAndTransactionsList(List<LoanTransaction> loanTransactions,
            Set<LoanCharge> charges) {
        List<ChargeOrTransaction> chargeOrTransactions = new ArrayList<>();
        if (charges != null) {
            chargeOrTransactions.addAll(charges.stream().map(ChargeOrTransaction::new).toList());
        }
        if (loanTransactions != null) {
            chargeOrTransactions.addAll(loanTransactions.stream().map(ChargeOrTransaction::new).toList());
        }
        Collections.sort(chargeOrTransactions);
        return chargeOrTransactions;
    }

    @Override
    public void processLatestTransaction(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges, Money overpaidAmount) {
        switch (loanTransaction.getTypeOf()) {
            case DISBURSEMENT -> handleDisbursement(loanTransaction, currency, installments);
            case WRITEOFF -> handleWriteOff(loanTransaction, currency, installments);
            case REFUND_FOR_ACTIVE_LOAN -> handleRefund(loanTransaction, currency, installments, charges);
            case CHARGEBACK -> handleChargeback(loanTransaction, currency, overpaidAmount, installments);
            case REPAYMENT, MERCHANT_ISSUED_REFUND, PAYOUT_REFUND, GOODWILL_CREDIT, CHARGE_REFUND, CHARGE_ADJUSTMENT, DOWN_PAYMENT,
                    WAIVE_INTEREST, RECOVERY_REPAYMENT ->
                handleRepayment(loanTransaction, currency, installments, charges);
            case CHARGE_OFF -> handleChargeOff(loanTransaction, currency, installments);
            case CHARGE_PAYMENT -> handleChargePayment(loanTransaction, currency, installments, charges);
            // TODO: Cover rest of the transaction types
            default -> {
                log.warn("Unhandled transaction processing for transaction type: {}", loanTransaction.getTypeOf());
            }
        }
    }

    private void handleDisbursement(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments) {
        updateLoanSchedule(loanTransaction, currency, installments);
    }

    private void updateLoanSchedule(LoanTransaction disbursementTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments) {
        final MathContext mc = MoneyHelper.getMathContext();
        List<LoanRepaymentScheduleInstallment> candidateRepaymentInstallments = installments.stream()
                .filter(i -> !i.getDueDate().isBefore(disbursementTransaction.getTransactionDate()) && !i.isDownPayment()).toList();
        int noCandidateRepaymentInstallments = candidateRepaymentInstallments.size();
        LoanProductRelatedDetail loanProductRelatedDetail = disbursementTransaction.getLoan().getLoanRepaymentScheduleDetail();
        Integer installmentAmountInMultiplesOf = disbursementTransaction.getLoan().getLoanProduct().getInstallmentAmountInMultiplesOf();
        Money downPaymentAmount = Money.zero(currency);
        if (loanProductRelatedDetail.isEnableDownPayment()) {
            LoanRepaymentScheduleInstallment downPaymentInstallment = installments.stream()
                    .filter(i -> i.isDownPayment() && i.getPrincipal(currency).isZero()).findFirst().orElseThrow();
            BigDecimal downPaymentAmt = MathUtil.percentageOf(disbursementTransaction.getAmount(),
                    loanProductRelatedDetail.getDisbursedAmountPercentageForDownPayment(), mc);
            if (installmentAmountInMultiplesOf != null) {
                downPaymentAmt = Money.roundToMultiplesOf(downPaymentAmt, installmentAmountInMultiplesOf);
            }
            downPaymentAmount = Money.of(currency, downPaymentAmt);
            downPaymentInstallment.addToPrincipal(disbursementTransaction.getTransactionDate(), downPaymentAmount);

        }
        Money amortizableAmount = disbursementTransaction.getAmount(currency).minus(downPaymentAmount);
        Money increasePrincipalBy = amortizableAmount.dividedBy(noCandidateRepaymentInstallments, mc.getRoundingMode());
        if (installmentAmountInMultiplesOf != null) {
            increasePrincipalBy = Money.roundToMultiplesOf(increasePrincipalBy, installmentAmountInMultiplesOf);
        }
        Money remainingAmount = increasePrincipalBy.multiplyRetainScale(noCandidateRepaymentInstallments, mc.getRoundingMode())
                .minus(amortizableAmount);

        Money finalIncreasePrincipalBy = increasePrincipalBy;
        candidateRepaymentInstallments
                .forEach(i -> i.addToPrincipal(disbursementTransaction.getTransactionDate(), finalIncreasePrincipalBy));
        // Hence the rounding, we might need to amend the last installment amount
        candidateRepaymentInstallments.get(noCandidateRepaymentInstallments - 1)
                .addToPrincipal(disbursementTransaction.getTransactionDate(), remainingAmount);
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
        processTransaction(loanTransaction, currency, installments, transactionAmountUnprocessed, charges);
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

    private void handleChargePayment(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges) {
        Money zero = Money.zero(currency);
        Money feeChargesPortion = zero;
        Money penaltyChargesPortion = zero;
        List<LoanTransactionToRepaymentScheduleMapping> transactionMappings = new ArrayList<>();
        LoanChargePaidBy loanChargePaidBy = loanTransaction.getLoanChargesPaid().stream().findFirst().get();
        LoanCharge loanCharge = loanChargePaidBy.getLoanCharge();
        Money amountToBePaid = Money.of(currency, loanTransaction.getAmount());
        if (loanCharge.getAmountOutstanding(currency).isLessThan(amountToBePaid)) {
            amountToBePaid = loanCharge.getAmountOutstanding(currency);
        }

        LocalDate startDate = loanTransaction.getLoan().getDisbursementDate();

        Money unprocessed = loanTransaction.getAmount(currency);
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            boolean isDue = installment.isFirstPeriod()
                    ? loanCharge.isDueForCollectionFromIncludingAndUpToAndIncluding(startDate, installment.getDueDate())
                    : loanCharge.isDueForCollectionFromAndUpToAndIncluding(startDate, installment.getDueDate());
            if (isDue) {
                Integer installmentNumber = installment.getInstallmentNumber();
                Money paidAmount = loanCharge.updatePaidAmountBy(amountToBePaid, installmentNumber, zero);

                LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                        transactionMappings, loanTransaction, installment, currency);

                if (loanTransaction.isPenaltyPayment()) {
                    penaltyChargesPortion = installment.payPenaltyChargesComponent(loanTransaction.getTransactionDate(), paidAmount);
                    loanTransaction.setLoanChargesPaid(Collections
                            .singleton(new LoanChargePaidBy(loanTransaction, loanCharge, paidAmount.getAmount(), installmentNumber)));
                    addToTransactionMapping(loanTransactionToRepaymentScheduleMapping, zero, zero, zero, penaltyChargesPortion);
                } else {
                    feeChargesPortion = installment.payFeeChargesComponent(loanTransaction.getTransactionDate(), paidAmount);
                    loanTransaction.setLoanChargesPaid(Collections
                            .singleton(new LoanChargePaidBy(loanTransaction, loanCharge, paidAmount.getAmount(), installmentNumber)));
                    addToTransactionMapping(loanTransactionToRepaymentScheduleMapping, zero, zero, feeChargesPortion, zero);
                }

                loanTransaction.updateComponents(zero, zero, feeChargesPortion, penaltyChargesPortion);
                unprocessed = loanTransaction.getAmount(currency).minus(paidAmount);
                loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(transactionMappings);
            }
        }

        if (unprocessed.isGreaterThanZero()) {
            processTransaction(loanTransaction, currency, installments, unprocessed, charges);
        }
    }

    private void processTransaction(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Money transactionAmountUnprocessed, Set<LoanCharge> charges) {
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
                                .filter(e -> loanTransaction.isAfter(e.getDueDate()))
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
                                .filter(e -> loanTransaction.isOn(e.getDueDate()))
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
                                    .filter(e -> loanTransaction.isBefore(e.getDueDate())).toList();
                        } else if (FutureInstallmentAllocationRule.NEXT_INSTALLMENT.equals(futureInstallmentAllocationRule)) {
                            currentInstallments = installments.stream().filter(LoanRepaymentScheduleInstallment::isNotFullyPaidOff)
                                    .filter(e -> loanTransaction.isBefore(e.getDueDate()))
                                    .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream().toList();
                        } else if (FutureInstallmentAllocationRule.LAST_INSTALLMENT.equals(futureInstallmentAllocationRule)) {
                            currentInstallments = installments.stream().filter(LoanRepaymentScheduleInstallment::isNotFullyPaidOff)
                                    .filter(e -> loanTransaction.isBefore(e.getDueDate()))
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

        payAdditionalCharges(loanTransaction, currency, charges);

        handleOverpayment(transactionAmountUnprocessed, loanTransaction);
    }

    private void payAdditionalCharges(LoanTransaction loanTransaction, MonetaryCurrency currency, Set<LoanCharge> charges) {
        final Set<LoanCharge> paidFeeCharges = loanTransaction.getLoanChargesPaid().stream() //
                .map(LoanChargePaidBy::getLoanCharge) //
                .filter(LoanCharge::isFeeCharge).collect(Collectors.toSet());
        final Set<LoanCharge> paidPenaltyCharges = loanTransaction.getLoanChargesPaid().stream() //
                .map(LoanChargePaidBy::getLoanCharge) //
                .filter(LoanCharge::isPenaltyCharge).collect(Collectors.toSet());
        // TODO: rewrite to provide sorted list
        final Set<LoanCharge> loanFees = extractFeeCharges(charges);
        final Set<LoanCharge> loanPenalties = extractPenaltyCharges(charges);
        loanFees.removeAll(paidFeeCharges);
        loanPenalties.removeAll(paidPenaltyCharges);

        BigDecimal sumFeePaidAmount = paidFeeCharges.stream() //
                .map(paidCharge -> paidCharge.getAmountPaid(currency)) //
                .map(Money::getAmount) //
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sumPenaltyPaidAmount = paidPenaltyCharges.stream() //
                .map(paidCharge -> paidCharge.getAmountPaid(currency)) //
                .map(Money::getAmount) //
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (loanTransaction.isNotWaiver() && !loanTransaction.isAccrual()) {
            Money feeCharges = loanTransaction.getFeeChargesPortion(currency).minus(sumFeePaidAmount);
            Money penaltyCharges = loanTransaction.getPenaltyChargesPortion(currency).minus(sumPenaltyPaidAmount);

            if (feeCharges.isGreaterThanZero()) {
                updateChargesPaidAmountBy(loanTransaction, feeCharges, loanFees, null);
            }

            if (penaltyCharges.isGreaterThanZero()) {
                updateChargesPaidAmountBy(loanTransaction, penaltyCharges, loanPenalties, null);
            }
        }
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
