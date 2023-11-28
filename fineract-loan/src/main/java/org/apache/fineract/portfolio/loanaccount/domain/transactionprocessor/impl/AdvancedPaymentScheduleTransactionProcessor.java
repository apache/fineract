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

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
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
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.apache.fineract.portfolio.loanaccount.domain.SingleLoanChargeRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.AbstractLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanproduct.domain.DueType;
import org.apache.fineract.portfolio.loanproduct.domain.FutureInstallmentAllocationRule;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class AdvancedPaymentScheduleTransactionProcessor extends AbstractLoanRepaymentScheduleTransactionProcessor {

    public static final String ADVANCED_PAYMENT_ALLOCATION_STRATEGY = "advanced-payment-allocation-strategy";

    public final SingleLoanChargeRepaymentScheduleProcessingWrapper loanChargeProcessor = new SingleLoanChargeRepaymentScheduleProcessingWrapper();

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
    public Money handleRepaymentSchedule(List<LoanTransaction> transactionsPostDisbursement, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> loanCharges) {
        throw new NotImplementedException();
    }

    @Override
    public ChangedTransactionDetail reprocessLoanTransactions(LocalDate disbursementDate, List<LoanTransaction> loanTransactions,
            MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges) {
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
        List<LoanTransaction> txs = chargeOrTransactions.stream().map(ChargeOrTransaction::getLoanTransaction).filter(Optional::isPresent)
                .map(Optional::get).toList();
        reprocessInstallments(disbursementDate, txs, installments, currency);
        return changedTransactionDetail;
    }

    @Override
    public void processLatestTransaction(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges, Money overpaidAmount) {
        switch (loanTransaction.getTypeOf()) {
            case DISBURSEMENT -> handleDisbursement(loanTransaction, currency, installments);
            case WRITEOFF -> handleWriteOff(loanTransaction, currency, installments);
            case REFUND_FOR_ACTIVE_LOAN -> handleRefund(loanTransaction, currency, installments, charges);
            case CHARGEBACK -> handleChargeback(loanTransaction, currency, overpaidAmount, installments);
            case CREDIT_BALANCE_REFUND -> handleCreditBalanceRefund(loanTransaction, currency, overpaidAmount, installments);
            case REPAYMENT, MERCHANT_ISSUED_REFUND, PAYOUT_REFUND, GOODWILL_CREDIT, CHARGE_REFUND, CHARGE_ADJUSTMENT, DOWN_PAYMENT,
                    WAIVE_INTEREST, RECOVERY_REPAYMENT ->
                handleRepayment(loanTransaction, currency, installments, charges);
            case CHARGE_OFF -> handleChargeOff(loanTransaction, currency, installments);
            case CHARGE_PAYMENT -> handleChargePayment(loanTransaction, currency, installments, charges);
            case WAIVE_CHARGES -> log.debug("WAIVE_CHARGES transaction will not be processed.");
            // TODO: Cover rest of the transaction types
            default -> {
                log.warn("Unhandled transaction processing for transaction type: {}", loanTransaction.getTypeOf());
            }
        }
    }

    @Override
    protected void handleRefund(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges) {
        Money zero = Money.zero(currency);
        List<LoanTransactionToRepaymentScheduleMapping> transactionMappings = new ArrayList<>();
        Money transactionAmountUnprocessed = loanTransaction.getAmount(currency);

        List<LoanPaymentAllocationRule> paymentAllocationRules = loanTransaction.getLoan().getPaymentAllocationRules();
        LoanPaymentAllocationRule defaultPaymentAllocationRule = paymentAllocationRules.stream()
                .filter(e -> PaymentAllocationTransactionType.DEFAULT.equals(e.getTransactionType())).findFirst().orElseThrow();
        LoanPaymentAllocationRule paymentAllocationRule = paymentAllocationRules.stream()
                .filter(e -> loanTransaction.getTypeOf().equals(e.getTransactionType().getLoanTransactionType())).findFirst()
                .orElse(defaultPaymentAllocationRule);
        Balances balances = new Balances(zero, zero, zero, zero);
        List<PaymentAllocationType> paymentAllocationTypes;
        FutureInstallmentAllocationRule futureInstallmentAllocationRule;
        if (PaymentAllocationTransactionType.DEFAULT.equals(paymentAllocationRule.getTransactionType())) {
            // if the allocation rule is not defined then the reverse order of the default allocation rule will be used
            paymentAllocationTypes = new ArrayList<>(paymentAllocationRule.getAllocationTypes());
            Collections.reverse(paymentAllocationTypes);
            futureInstallmentAllocationRule = FutureInstallmentAllocationRule.LAST_INSTALLMENT;
        } else {
            paymentAllocationTypes = paymentAllocationRule.getAllocationTypes();
            futureInstallmentAllocationRule = paymentAllocationRule.getFutureInstallmentAllocationRule();
        }
        if (LoanScheduleProcessingType.HORIZONTAL
                .equals(loanTransaction.getLoan().getLoanProductRelatedDetail().getLoanScheduleProcessingType())) {
            LinkedHashMap<DueType, List<PaymentAllocationType>> paymentAllocationsMap = paymentAllocationTypes.stream().collect(
                    Collectors.groupingBy(PaymentAllocationType::getDueType, LinkedHashMap::new, mapping(Function.identity(), toList())));

            for (Map.Entry<DueType, List<PaymentAllocationType>> paymentAllocationsEntry : paymentAllocationsMap.entrySet()) {
                transactionAmountUnprocessed = refundTransactionHorizontally(loanTransaction, currency, installments,
                        transactionAmountUnprocessed, paymentAllocationsEntry.getValue(), futureInstallmentAllocationRule,
                        transactionMappings, charges, balances);
                if (!transactionAmountUnprocessed.isGreaterThanZero()) {
                    break;
                }
            }
        } else if (LoanScheduleProcessingType.VERTICAL
                .equals(loanTransaction.getLoan().getLoanProductRelatedDetail().getLoanScheduleProcessingType())) {
            for (PaymentAllocationType paymentAllocationType : paymentAllocationTypes) {
                transactionAmountUnprocessed = refundTransactionVertically(loanTransaction, currency, installments, zero,
                        transactionMappings, transactionAmountUnprocessed, futureInstallmentAllocationRule, charges, balances,
                        paymentAllocationType);
                if (!transactionAmountUnprocessed.isGreaterThanZero()) {
                    break;
                }
            }
        }

        loanTransaction.updateComponents(balances.getAggregatedPrincipalPortion(), balances.getAggregatedInterestPortion(),
                balances.getAggregatedFeeChargesPortion(), balances.getAggregatedPenaltyChargesPortion());
        loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(transactionMappings);
    }

    private void processSingleTransaction(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges,
            ChangedTransactionDetail changedTransactionDetail) {
        if (loanTransaction.getId() == null) {
            processLatestTransaction(loanTransaction, currency, installments, charges, Money.zero(currency));
            if (loanTransaction.isInterestWaiver()) {
                loanTransaction.adjustInterestComponent(currency);
            }
        } else {
            /*
             * For existing transactions, check if the re-payment breakup (principal, interest, fees, penalties) has
             * changed.<br>
             */
            final LoanTransaction newLoanTransaction = LoanTransaction.copyTransactionProperties(loanTransaction);

            // Reset derived component of new loan transaction and
            // re-process transaction
            processLatestTransaction(newLoanTransaction, currency, installments, charges, Money.zero(currency));
            if (loanTransaction.isInterestWaiver()) {
                newLoanTransaction.adjustInterestComponent(currency);
            }
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

    private void handleDisbursement(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments) {
        updateLoanSchedule(loanTransaction, currency, installments);
    }

    private void updateLoanSchedule(LoanTransaction disbursementTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments) {
        final MathContext mc = MoneyHelper.getMathContext();
        List<LoanRepaymentScheduleInstallment> candidateRepaymentInstallments = installments.stream().filter(
                i -> i.getDueDate().isAfter(disbursementTransaction.getTransactionDate()) && !i.isDownPayment() && !i.isAdditional())
                .toList();
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
        Money remainingAmount = amortizableAmount
                .minus(increasePrincipalBy.multiplyRetainScale(noCandidateRepaymentInstallments, mc.getRoundingMode()));

        Money finalIncreasePrincipalBy = increasePrincipalBy;
        candidateRepaymentInstallments
                .forEach(i -> i.addToPrincipal(disbursementTransaction.getTransactionDate(), finalIncreasePrincipalBy));
        // Hence the rounding, we might need to amend the last installment amount
        candidateRepaymentInstallments.get(noCandidateRepaymentInstallments - 1)
                .addToPrincipal(disbursementTransaction.getTransactionDate(), remainingAmount);
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

    private Money processPaymentAllocation(PaymentAllocationType paymentAllocationType, LoanRepaymentScheduleInstallment currentInstallment,
            LoanTransaction loanTransaction, Money transactionAmountUnprocessed,
            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping, Set<LoanCharge> chargesOfInstallment,
            Balances balances, LoanRepaymentScheduleInstallment.PaymentAction action) {
        LocalDate transactionDate = loanTransaction.getTransactionDate();
        Money zero = transactionAmountUnprocessed.zero();

        LoanRepaymentScheduleInstallment.PaymentFunction paymentFunction = currentInstallment
                .getPaymentFunction(paymentAllocationType.getAllocationType(), action);
        ChargesPaidByFunction chargesPaidByFunction = getChargesPaymentFunction(action);
        Money portion = paymentFunction.accept(transactionDate, transactionAmountUnprocessed);

        switch (paymentAllocationType.getAllocationType()) {
            case PENALTY -> {
                balances.setAggregatedPenaltyChargesPortion(balances.getAggregatedPenaltyChargesPortion().add(portion));
                addToTransactionMapping(loanTransactionToRepaymentScheduleMapping, zero, zero, zero, portion);
                Set<LoanCharge> penalties = chargesOfInstallment.stream().filter(LoanCharge::isPenaltyCharge).collect(Collectors.toSet());
                chargesPaidByFunction.accept(loanTransaction, portion, penalties, currentInstallment.getInstallmentNumber());
            }
            case FEE -> {
                balances.setAggregatedFeeChargesPortion(balances.getAggregatedFeeChargesPortion().add(portion));
                addToTransactionMapping(loanTransactionToRepaymentScheduleMapping, zero, zero, portion, zero);
                Set<LoanCharge> fees = chargesOfInstallment.stream().filter(LoanCharge::isFeeCharge).collect(Collectors.toSet());
                chargesPaidByFunction.accept(loanTransaction, portion, fees, currentInstallment.getInstallmentNumber());
            }
            case INTEREST -> {
                balances.setAggregatedInterestPortion(balances.getAggregatedInterestPortion().add(portion));
                addToTransactionMapping(loanTransactionToRepaymentScheduleMapping, zero, portion, zero, zero);
            }
            case PRINCIPAL -> {
                balances.setAggregatedPrincipalPortion(balances.getAggregatedPrincipalPortion().add(portion));
                addToTransactionMapping(loanTransactionToRepaymentScheduleMapping, portion, zero, zero, zero);
            }
        }
        return portion;
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
                penaltychargesPortion = penaltychargesPortion.plus(currentInstallment.getPenaltyChargesOutstanding(currency));
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
        int firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper.fetchFirstNormalInstallmentNumber(installments);
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            boolean isDue = installment.getInstallmentNumber().equals(firstNormalInstallmentNumber)
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

    private Money refundTransactionHorizontally(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Money transactionAmountUnprocessed,
            List<PaymentAllocationType> paymentAllocationTypes, FutureInstallmentAllocationRule futureInstallmentAllocationRule,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Set<LoanCharge> charges, Balances balances) {
        Money zero = Money.zero(currency);
        Money refundedPortion;
        outerLoop: do {
            LoanRepaymentScheduleInstallment latestPastDueInstallment = getLatestPastDueInstallmentForRefund(loanTransaction, currency,
                    installments, zero);
            LoanRepaymentScheduleInstallment dueInstallment = getDueInstallmentForRefund(loanTransaction, currency, installments, zero);

            List<LoanRepaymentScheduleInstallment> inAdvanceInstallments = getFutureInstallmentsForRefund(loanTransaction, currency,
                    installments, futureInstallmentAllocationRule, zero);

            int firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper.fetchFirstNormalInstallmentNumber(installments);
            for (PaymentAllocationType paymentAllocationType : paymentAllocationTypes) {
                switch (paymentAllocationType.getDueType()) {
                    case PAST_DUE -> {
                        if (latestPastDueInstallment != null) {
                            Set<LoanCharge> oldestPastDueInstallmentCharges = getLoanChargesOfInstallment(charges, latestPastDueInstallment,
                                    firstNormalInstallmentNumber);
                            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                    transactionMappings, loanTransaction, latestPastDueInstallment, currency);
                            refundedPortion = processPaymentAllocation(paymentAllocationType, latestPastDueInstallment, loanTransaction,
                                    transactionAmountUnprocessed, loanTransactionToRepaymentScheduleMapping,
                                    oldestPastDueInstallmentCharges, balances, LoanRepaymentScheduleInstallment.PaymentAction.UNPAY);
                            transactionAmountUnprocessed = transactionAmountUnprocessed.minus(refundedPortion);
                        } else {
                            break outerLoop;
                        }
                    }
                    case DUE -> {
                        if (dueInstallment != null) {
                            Set<LoanCharge> dueInstallmentCharges = getLoanChargesOfInstallment(charges, dueInstallment,
                                    firstNormalInstallmentNumber);
                            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                    transactionMappings, loanTransaction, dueInstallment, currency);
                            refundedPortion = processPaymentAllocation(paymentAllocationType, dueInstallment, loanTransaction,
                                    transactionAmountUnprocessed, loanTransactionToRepaymentScheduleMapping, dueInstallmentCharges,
                                    balances, LoanRepaymentScheduleInstallment.PaymentAction.UNPAY);
                            transactionAmountUnprocessed = transactionAmountUnprocessed.minus(refundedPortion);
                        } else {
                            break outerLoop;
                        }
                    }
                    case IN_ADVANCE -> {
                        int numberOfInstallments = inAdvanceInstallments.size();
                        if (numberOfInstallments > 0) {
                            Money evenPortion = transactionAmountUnprocessed.dividedBy(numberOfInstallments, MoneyHelper.getRoundingMode());
                            Money balanceAdjustment = transactionAmountUnprocessed.minus(evenPortion.multipliedBy(numberOfInstallments));
                            for (LoanRepaymentScheduleInstallment inAdvanceInstallment : inAdvanceInstallments) {
                                Set<LoanCharge> inAdvanceInstallmentCharges = getLoanChargesOfInstallment(charges, inAdvanceInstallment,
                                        firstNormalInstallmentNumber);
                                if (inAdvanceInstallment.equals(inAdvanceInstallments.get(numberOfInstallments - 1))) {
                                    evenPortion = evenPortion.add(balanceAdjustment);
                                }
                                LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                        transactionMappings, loanTransaction, inAdvanceInstallment, currency);
                                refundedPortion = processPaymentAllocation(paymentAllocationType, inAdvanceInstallment, loanTransaction,
                                        evenPortion, loanTransactionToRepaymentScheduleMapping, inAdvanceInstallmentCharges, balances,
                                        LoanRepaymentScheduleInstallment.PaymentAction.UNPAY);
                                transactionAmountUnprocessed = transactionAmountUnprocessed.minus(refundedPortion);
                            }
                        } else {
                            break outerLoop;
                        }
                    }
                }
            }
        } while (installments.stream().anyMatch(installment -> installment.getTotalPaid(currency).isGreaterThan(zero))
                && transactionAmountUnprocessed.isGreaterThanZero());
        return transactionAmountUnprocessed;
    }

    private Money refundTransactionVertically(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Money zero,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Money transactionAmountUnprocessed,
            FutureInstallmentAllocationRule futureInstallmentAllocationRule, Set<LoanCharge> charges, Balances balances,
            PaymentAllocationType paymentAllocationType) {
        LoanRepaymentScheduleInstallment currentInstallment = null;
        Money refundedPortion = zero;
        int firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper.fetchFirstNormalInstallmentNumber(installments);
        do {
            switch (paymentAllocationType.getDueType()) {
                case PAST_DUE -> {
                    currentInstallment = getLatestPastDueInstallmentForRefund(loanTransaction, currency, installments, zero);
                    if (currentInstallment != null) {
                        Set<LoanCharge> oldestPastDueInstallmentCharges = getLoanChargesOfInstallment(charges, currentInstallment,
                                firstNormalInstallmentNumber);
                        LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                transactionMappings, loanTransaction, currentInstallment, currency);
                        refundedPortion = processPaymentAllocation(paymentAllocationType, currentInstallment, loanTransaction,
                                transactionAmountUnprocessed, loanTransactionToRepaymentScheduleMapping, oldestPastDueInstallmentCharges,
                                balances, LoanRepaymentScheduleInstallment.PaymentAction.UNPAY);
                        transactionAmountUnprocessed = transactionAmountUnprocessed.minus(refundedPortion);
                    }
                }
                case DUE -> {
                    currentInstallment = getDueInstallmentForRefund(loanTransaction, currency, installments, zero);
                    if (currentInstallment != null) {
                        Set<LoanCharge> dueInstallmentCharges = getLoanChargesOfInstallment(charges, currentInstallment,
                                firstNormalInstallmentNumber);
                        LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                transactionMappings, loanTransaction, currentInstallment, currency);
                        refundedPortion = processPaymentAllocation(paymentAllocationType, currentInstallment, loanTransaction,
                                transactionAmountUnprocessed, loanTransactionToRepaymentScheduleMapping, dueInstallmentCharges, balances,
                                LoanRepaymentScheduleInstallment.PaymentAction.UNPAY);
                        transactionAmountUnprocessed = transactionAmountUnprocessed.minus(refundedPortion);
                    }
                }
                case IN_ADVANCE -> {
                    List<LoanRepaymentScheduleInstallment> currentInstallments = getFutureInstallmentsForRefund(loanTransaction, currency,
                            installments, futureInstallmentAllocationRule, zero);
                    int numberOfInstallments = currentInstallments.size();
                    refundedPortion = zero;
                    if (numberOfInstallments > 0) {
                        Money evenPortion = transactionAmountUnprocessed.dividedBy(numberOfInstallments, MoneyHelper.getRoundingMode());
                        Money balanceAdjustment = transactionAmountUnprocessed.minus(evenPortion.multipliedBy(numberOfInstallments));
                        for (LoanRepaymentScheduleInstallment internalCurrentInstallment : currentInstallments) {
                            currentInstallment = internalCurrentInstallment;
                            Set<LoanCharge> inAdvanceInstallmentCharges = getLoanChargesOfInstallment(charges, currentInstallment,
                                    firstNormalInstallmentNumber);
                            if (internalCurrentInstallment.equals(currentInstallments.get(numberOfInstallments - 1))) {
                                evenPortion = evenPortion.add(balanceAdjustment);
                            }
                            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                    transactionMappings, loanTransaction, currentInstallment, currency);
                            Money internalUnpaidPortion = processPaymentAllocation(paymentAllocationType, currentInstallment,
                                    loanTransaction, evenPortion, loanTransactionToRepaymentScheduleMapping, inAdvanceInstallmentCharges,
                                    balances, LoanRepaymentScheduleInstallment.PaymentAction.UNPAY);
                            if (internalUnpaidPortion.isGreaterThanZero()) {
                                refundedPortion = internalUnpaidPortion;
                            }
                            transactionAmountUnprocessed = transactionAmountUnprocessed.minus(internalUnpaidPortion);
                        }
                    } else {
                        currentInstallment = null;
                    }
                }
            }
        } while (currentInstallment != null && transactionAmountUnprocessed.isGreaterThanZero() && refundedPortion.isGreaterThanZero());
        return transactionAmountUnprocessed;
    }

    @Nullable
    private static LoanRepaymentScheduleInstallment getDueInstallmentForRefund(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Money zero) {
        return installments.stream().filter(installment -> installment.getTotalPaid(currency).isGreaterThan(zero))
                .filter(installment -> loanTransaction.isOn(installment.getDueDate()))
                .max(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).orElse(null);
    }

    @Nullable
    private static LoanRepaymentScheduleInstallment getLatestPastDueInstallmentForRefund(LoanTransaction loanTransaction,
            MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments, Money zero) {
        return installments.stream().filter(installment -> installment.getTotalPaid(currency).isGreaterThan(zero))
                .filter(e -> loanTransaction.isAfter(e.getDueDate()))
                .max(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).orElse(null);
    }

    @NotNull
    private static List<LoanRepaymentScheduleInstallment> getFutureInstallmentsForRefund(LoanTransaction loanTransaction,
            MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments,
            FutureInstallmentAllocationRule futureInstallmentAllocationRule, Money zero) {
        List<LoanRepaymentScheduleInstallment> inAdvanceInstallments = new ArrayList<>();
        if (FutureInstallmentAllocationRule.REAMORTIZATION.equals(futureInstallmentAllocationRule)) {
            inAdvanceInstallments = installments.stream().filter(installment -> installment.getTotalPaid(currency).isGreaterThan(zero))
                    .filter(e -> loanTransaction.isBefore(e.getDueDate())).toList();
        } else if (FutureInstallmentAllocationRule.NEXT_INSTALLMENT.equals(futureInstallmentAllocationRule)) {
            inAdvanceInstallments = installments.stream().filter(installment -> installment.getTotalPaid(currency).isGreaterThan(zero))
                    .filter(e -> loanTransaction.isBefore(e.getDueDate()))
                    .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream().toList();
        } else if (FutureInstallmentAllocationRule.LAST_INSTALLMENT.equals(futureInstallmentAllocationRule)) {
            inAdvanceInstallments = installments.stream().filter(installment -> installment.getTotalPaid(currency).isGreaterThan(zero))
                    .filter(e -> loanTransaction.isBefore(e.getDueDate()))
                    .max(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream().toList();
        }
        return inAdvanceInstallments;
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

        if (LoanScheduleProcessingType.HORIZONTAL
                .equals(loanTransaction.getLoan().getLoanProductRelatedDetail().getLoanScheduleProcessingType())) {
            transactionAmountUnprocessed = processPeriodsHorizontally(loanTransaction, currency, installments, transactionAmountUnprocessed,
                    paymentAllocationRule, transactionMappings, charges, balances);
        } else if (LoanScheduleProcessingType.VERTICAL
                .equals(loanTransaction.getLoan().getLoanProductRelatedDetail().getLoanScheduleProcessingType())) {
            transactionAmountUnprocessed = processPeriodsVertically(loanTransaction, currency, installments, transactionAmountUnprocessed,
                    paymentAllocationRule, transactionMappings, charges, balances);
        }
        loanTransaction.updateComponents(balances.getAggregatedPrincipalPortion(), balances.getAggregatedInterestPortion(),
                balances.getAggregatedFeeChargesPortion(), balances.getAggregatedPenaltyChargesPortion());
        loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(transactionMappings);

        handleOverpayment(transactionAmountUnprocessed, loanTransaction);
    }

    private Money processPeriodsHorizontally(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Money transactionAmountUnprocessed,
            LoanPaymentAllocationRule paymentAllocationRule, List<LoanTransactionToRepaymentScheduleMapping> transactionMappings,
            Set<LoanCharge> charges, Balances balances) {
        LinkedHashMap<DueType, List<PaymentAllocationType>> paymentAllocationsMap = paymentAllocationRule.getAllocationTypes().stream()
                .collect(Collectors.groupingBy(PaymentAllocationType::getDueType, LinkedHashMap::new,
                        mapping(Function.identity(), toList())));

        for (Map.Entry<DueType, List<PaymentAllocationType>> paymentAllocationsEntry : paymentAllocationsMap.entrySet()) {
            transactionAmountUnprocessed = processAllocationsHorizontally(loanTransaction, currency, installments,
                    transactionAmountUnprocessed, paymentAllocationsEntry.getValue(),
                    paymentAllocationRule.getFutureInstallmentAllocationRule(), transactionMappings, charges, balances);
        }
        return transactionAmountUnprocessed;
    }

    private Money processAllocationsHorizontally(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Money transactionAmountUnprocessed,
            List<PaymentAllocationType> paymentAllocationTypes, FutureInstallmentAllocationRule futureInstallmentAllocationRule,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Set<LoanCharge> charges, Balances balances) {
        Money paidPortion;
        boolean exit = false;
        do {
            LoanRepaymentScheduleInstallment oldestPastDueInstallment = installments.stream()
                    .filter(LoanRepaymentScheduleInstallment::isNotFullyPaidOff).filter(e -> loanTransaction.isAfter(e.getDueDate()))
                    .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).orElse(null);
            LoanRepaymentScheduleInstallment dueInstallment = installments.stream()
                    .filter(LoanRepaymentScheduleInstallment::isNotFullyPaidOff).filter(e -> loanTransaction.isOn(e.getDueDate()))
                    .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).orElse(null);

            // For having similar logic we are populating installment list even when the future installment
            // allocation rule is NEXT_INSTALLMENT or LAST_INSTALLMENT hence the list has only one element.
            List<LoanRepaymentScheduleInstallment> inAdvanceInstallments = new ArrayList<>();
            if (FutureInstallmentAllocationRule.REAMORTIZATION.equals(futureInstallmentAllocationRule)) {
                inAdvanceInstallments = installments.stream().filter(LoanRepaymentScheduleInstallment::isNotFullyPaidOff)
                        .filter(e -> loanTransaction.isBefore(e.getDueDate())).toList();
            } else if (FutureInstallmentAllocationRule.NEXT_INSTALLMENT.equals(futureInstallmentAllocationRule)) {
                inAdvanceInstallments = installments.stream().filter(LoanRepaymentScheduleInstallment::isNotFullyPaidOff)
                        .filter(e -> loanTransaction.isBefore(e.getDueDate()))
                        .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream().toList();
            } else if (FutureInstallmentAllocationRule.LAST_INSTALLMENT.equals(futureInstallmentAllocationRule)) {
                inAdvanceInstallments = installments.stream().filter(LoanRepaymentScheduleInstallment::isNotFullyPaidOff)
                        .filter(e -> loanTransaction.isBefore(e.getDueDate()))
                        .max(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream().toList();
            }

            int firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper.fetchFirstNormalInstallmentNumber(installments);

            for (PaymentAllocationType paymentAllocationType : paymentAllocationTypes) {
                switch (paymentAllocationType.getDueType()) {
                    case PAST_DUE -> {
                        if (oldestPastDueInstallment != null) {
                            Set<LoanCharge> oldestPastDueInstallmentCharges = getLoanChargesOfInstallment(charges, oldestPastDueInstallment,
                                    firstNormalInstallmentNumber);
                            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                    transactionMappings, loanTransaction, oldestPastDueInstallment, currency);
                            paidPortion = processPaymentAllocation(paymentAllocationType, oldestPastDueInstallment, loanTransaction,
                                    transactionAmountUnprocessed, loanTransactionToRepaymentScheduleMapping,
                                    oldestPastDueInstallmentCharges, balances, LoanRepaymentScheduleInstallment.PaymentAction.PAY);
                            transactionAmountUnprocessed = transactionAmountUnprocessed.minus(paidPortion);
                        } else {
                            exit = true;
                        }
                    }
                    case DUE -> {
                        if (dueInstallment != null) {
                            Set<LoanCharge> dueInstallmentCharges = getLoanChargesOfInstallment(charges, dueInstallment,
                                    firstNormalInstallmentNumber);
                            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                    transactionMappings, loanTransaction, dueInstallment, currency);
                            paidPortion = processPaymentAllocation(paymentAllocationType, dueInstallment, loanTransaction,
                                    transactionAmountUnprocessed, loanTransactionToRepaymentScheduleMapping, dueInstallmentCharges,
                                    balances, LoanRepaymentScheduleInstallment.PaymentAction.PAY);
                            transactionAmountUnprocessed = transactionAmountUnprocessed.minus(paidPortion);
                        } else {
                            exit = true;
                        }
                    }
                    case IN_ADVANCE -> {
                        int numberOfInstallments = inAdvanceInstallments.size();
                        if (numberOfInstallments > 0) {
                            // This will be the same amount as transactionAmountUnprocessed in case of the future
                            // installment allocation is NEXT_INSTALLMENT or LAST_INSTALLMENT
                            Money evenPortion = transactionAmountUnprocessed.dividedBy(numberOfInstallments, MoneyHelper.getRoundingMode());
                            // Adjustment might be needed due to the divide operation and the rounding mode
                            Money balanceAdjustment = transactionAmountUnprocessed.minus(evenPortion.multipliedBy(numberOfInstallments));
                            for (LoanRepaymentScheduleInstallment inAdvanceInstallment : inAdvanceInstallments) {
                                Set<LoanCharge> inAdvanceInstallmentCharges = getLoanChargesOfInstallment(charges, inAdvanceInstallment,
                                        firstNormalInstallmentNumber);
                                // Adjust the portion for the last installment
                                if (inAdvanceInstallment.equals(inAdvanceInstallments.get(numberOfInstallments - 1))) {
                                    evenPortion = evenPortion.add(balanceAdjustment);
                                }
                                LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                        transactionMappings, loanTransaction, inAdvanceInstallment, currency);
                                paidPortion = processPaymentAllocation(paymentAllocationType, inAdvanceInstallment, loanTransaction,
                                        evenPortion, loanTransactionToRepaymentScheduleMapping, inAdvanceInstallmentCharges, balances,
                                        LoanRepaymentScheduleInstallment.PaymentAction.PAY);
                                transactionAmountUnprocessed = transactionAmountUnprocessed.minus(paidPortion);
                            }
                        } else {
                            exit = true;
                        }
                    }
                }
            }
        }
        // We are allocating till there is no pending installment or there is no more unprocessed transaction amount
        // or there is no more outstanding balance of the allocation type
        while (!exit && installments.stream().anyMatch(LoanRepaymentScheduleInstallment::isNotFullyPaidOff)
                && transactionAmountUnprocessed.isGreaterThanZero());
        return transactionAmountUnprocessed;
    }

    @NotNull
    private static Set<LoanCharge> getLoanChargesOfInstallment(Set<LoanCharge> charges, LoanRepaymentScheduleInstallment currentInstallment,
            int firstNormalInstallmentNumber) {
        return charges.stream().filter(loanCharge -> currentInstallment.getInstallmentNumber().equals(firstNormalInstallmentNumber)
                ? loanCharge.isDueForCollectionFromIncludingAndUpToAndIncluding(currentInstallment.getFromDate(),
                        currentInstallment.getDueDate())
                : loanCharge.isDueForCollectionFromAndUpToAndIncluding(currentInstallment.getFromDate(), currentInstallment.getDueDate()))
                .collect(Collectors.toSet());
    }

    private Money processPeriodsVertically(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Money transactionAmountUnprocessed,
            LoanPaymentAllocationRule paymentAllocationRule, List<LoanTransactionToRepaymentScheduleMapping> transactionMappings,
            Set<LoanCharge> charges, Balances balances) {
        int firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper.fetchFirstNormalInstallmentNumber(installments);
        for (PaymentAllocationType paymentAllocationType : paymentAllocationRule.getAllocationTypes()) {
            FutureInstallmentAllocationRule futureInstallmentAllocationRule = paymentAllocationRule.getFutureInstallmentAllocationRule();
            LoanRepaymentScheduleInstallment currentInstallment = null;
            Money paidPortion = Money.zero(currency);
            do {
                Predicate<LoanRepaymentScheduleInstallment> predicate = getFilterPredicate(paymentAllocationType, currency);
                switch (paymentAllocationType.getDueType()) {
                    case PAST_DUE -> {
                        currentInstallment = installments.stream().filter(predicate).filter(e -> loanTransaction.isAfter(e.getDueDate()))
                                .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).orElse(null);
                        if (currentInstallment != null) {
                            Set<LoanCharge> oldestPastDueInstallmentCharges = getLoanChargesOfInstallment(charges, currentInstallment,
                                    firstNormalInstallmentNumber);
                            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                    transactionMappings, loanTransaction, currentInstallment, currency);
                            paidPortion = processPaymentAllocation(paymentAllocationType, currentInstallment, loanTransaction,
                                    transactionAmountUnprocessed, loanTransactionToRepaymentScheduleMapping,
                                    oldestPastDueInstallmentCharges, balances, LoanRepaymentScheduleInstallment.PaymentAction.PAY);
                            transactionAmountUnprocessed = transactionAmountUnprocessed.minus(paidPortion);
                        }
                    }
                    case DUE -> {
                        currentInstallment = installments.stream().filter(predicate).filter(e -> loanTransaction.isOn(e.getDueDate()))
                                .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).orElse(null);
                        if (currentInstallment != null) {
                            Set<LoanCharge> dueInstallmentCharges = getLoanChargesOfInstallment(charges, currentInstallment,
                                    firstNormalInstallmentNumber);
                            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                    transactionMappings, loanTransaction, currentInstallment, currency);
                            paidPortion = processPaymentAllocation(paymentAllocationType, currentInstallment, loanTransaction,
                                    transactionAmountUnprocessed, loanTransactionToRepaymentScheduleMapping, dueInstallmentCharges,
                                    balances, LoanRepaymentScheduleInstallment.PaymentAction.PAY);
                            transactionAmountUnprocessed = transactionAmountUnprocessed.minus(paidPortion);
                        }
                    }
                    case IN_ADVANCE -> {
                        // For having similar logic we are populating installment list even when the future installment
                        // allocation rule is NEXT_INSTALLMENT or LAST_INSTALLMENT hence the list has only one element.
                        List<LoanRepaymentScheduleInstallment> currentInstallments = new ArrayList<>();
                        if (FutureInstallmentAllocationRule.REAMORTIZATION.equals(futureInstallmentAllocationRule)) {
                            currentInstallments = installments.stream().filter(predicate)
                                    .filter(e -> loanTransaction.isBefore(e.getDueDate())).toList();
                        } else if (FutureInstallmentAllocationRule.NEXT_INSTALLMENT.equals(futureInstallmentAllocationRule)) {
                            currentInstallments = installments.stream().filter(predicate)
                                    .filter(e -> loanTransaction.isBefore(e.getDueDate()))
                                    .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream().toList();
                        } else if (FutureInstallmentAllocationRule.LAST_INSTALLMENT.equals(futureInstallmentAllocationRule)) {
                            currentInstallments = installments.stream().filter(predicate)
                                    .filter(e -> loanTransaction.isBefore(e.getDueDate()))
                                    .max(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream().toList();
                        }
                        int numberOfInstallments = currentInstallments.size();
                        paidPortion = Money.zero(currency);
                        if (numberOfInstallments > 0) {
                            // This will be the same amount as transactionAmountUnprocessed in case of the future
                            // installment allocation is NEXT_INSTALLMENT or LAST_INSTALLMENT
                            Money evenPortion = transactionAmountUnprocessed.dividedBy(numberOfInstallments, MoneyHelper.getRoundingMode());
                            // Adjustment might be needed due to the divide operation and the rounding mode
                            Money balanceAdjustment = transactionAmountUnprocessed.minus(evenPortion.multipliedBy(numberOfInstallments));
                            for (LoanRepaymentScheduleInstallment internalCurrentInstallment : currentInstallments) {
                                currentInstallment = internalCurrentInstallment;
                                Set<LoanCharge> inAdvanceInstallmentCharges = getLoanChargesOfInstallment(charges, currentInstallment,
                                        firstNormalInstallmentNumber);
                                // Adjust the portion for the last installment
                                if (internalCurrentInstallment.equals(currentInstallments.get(numberOfInstallments - 1))) {
                                    evenPortion = evenPortion.add(balanceAdjustment);
                                }
                                LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                        transactionMappings, loanTransaction, currentInstallment, currency);
                                Money internalPaidPortion = processPaymentAllocation(paymentAllocationType, currentInstallment,
                                        loanTransaction, evenPortion, loanTransactionToRepaymentScheduleMapping,
                                        inAdvanceInstallmentCharges, balances, LoanRepaymentScheduleInstallment.PaymentAction.PAY);
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
        return transactionAmountUnprocessed;
    }

    private Predicate<LoanRepaymentScheduleInstallment> getFilterPredicate(PaymentAllocationType paymentAllocationType,
            MonetaryCurrency currency) {
        return switch (paymentAllocationType.getAllocationType()) {
            case PENALTY -> (p) -> p.getPenaltyChargesOutstanding(currency).isGreaterThanZero();
            case FEE -> (p) -> p.getFeeChargesOutstanding(currency).isGreaterThanZero();
            case INTEREST -> (p) -> p.getInterestOutstanding(currency).isGreaterThanZero();
            case PRINCIPAL -> (p) -> p.getPrincipalOutstanding(currency).isGreaterThanZero();
        };
    }

    @AllArgsConstructor
    @Getter
    @Setter
    private static final class Balances {

        private Money aggregatedPrincipalPortion;
        private Money aggregatedFeeChargesPortion;
        private Money aggregatedInterestPortion;
        private Money aggregatedPenaltyChargesPortion;
    }
}
