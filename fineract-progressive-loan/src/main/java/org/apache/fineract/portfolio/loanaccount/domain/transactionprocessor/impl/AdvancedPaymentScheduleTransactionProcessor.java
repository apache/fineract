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

import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum.CHARGEBACK;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.FEE;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.INTEREST;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.PENALTY;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.PRINCIPAL;
import static org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType.DEFAULT;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCreditAllocationRule;
import org.apache.fineract.portfolio.loanaccount.domain.LoanPaymentAllocationRule;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.apache.fineract.portfolio.loanaccount.domain.SingleLoanChargeRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.reaging.LoanReAgeParameter;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.AbstractLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.apache.fineract.portfolio.loanproduct.domain.AllocationType;
import org.apache.fineract.portfolio.loanproduct.domain.CreditAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.DueType;
import org.apache.fineract.portfolio.loanproduct.domain.FutureInstallmentAllocationRule;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Slf4j
@RequiredArgsConstructor
public class AdvancedPaymentScheduleTransactionProcessor extends AbstractLoanRepaymentScheduleTransactionProcessor {

    public static final String ADVANCED_PAYMENT_ALLOCATION_STRATEGY = "advanced-payment-allocation-strategy";

    public final SingleLoanChargeRepaymentScheduleProcessingWrapper loanChargeProcessor = new SingleLoanChargeRepaymentScheduleProcessingWrapper();

    public final EMICalculator emiCalculator;

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
        final ChangedTransactionDetail changedTransactionDetail = new ChangedTransactionDetail();
        if (loanTransactions.isEmpty()) {
            return changedTransactionDetail;
        }
        if (charges != null) {
            for (final LoanCharge loanCharge : charges) {
                if (!loanCharge.isDueAtDisbursement()) {
                    loanCharge.resetPaidAmount(currency);
                }
            }
        }
        // Remove re-aged and additional (N+1) installments (if applicable), those will be recreated during the
        // reprocessing
        installments.removeIf(LoanRepaymentScheduleInstallment::isReAged);
        installments.removeIf(LoanRepaymentScheduleInstallment::isAdditional);

        addChargeOnlyRepaymentInstallmentIfRequired(charges, installments);

        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
            currentInstallment.resetBalances();
            currentInstallment.updateObligationsMet(currency, disbursementDate);
        }

        List<ChargeOrTransaction> chargeOrTransactions = createSortedChargesAndTransactionsList(loanTransactions, charges);

        MoneyHolder overpaymentHolder = new MoneyHolder(Money.zero(currency));
        final MathContext mc = MoneyHelper.getMathContext();
        final Loan loan = loanTransactions.get(0).getLoan();
        final Integer installmentAmountInMultiplesOf = loan.getLoanProduct().getInstallmentAmountInMultiplesOf();
        final LoanProductRelatedDetail loanProductRelatedDetail = loan.getLoanRepaymentScheduleDetail();
        ProgressiveLoanInterestScheduleModel scheduleModel = emiCalculator.generateModel(loanProductRelatedDetail,
                installmentAmountInMultiplesOf, installments, mc);

        for (final ChargeOrTransaction chargeOrTransaction : chargeOrTransactions) {
            chargeOrTransaction.getLoanTransaction().ifPresent(loanTransaction -> processSingleTransaction(loanTransaction, currency,
                    installments, charges, changedTransactionDetail, overpaymentHolder, scheduleModel));
            chargeOrTransaction.getLoanCharge()
                    .ifPresent(loanCharge -> processSingleCharge(loanCharge, currency, installments, disbursementDate));
        }
        List<LoanTransaction> txs = chargeOrTransactions.stream().map(ChargeOrTransaction::getLoanTransaction).filter(Optional::isPresent)
                .map(Optional::get).toList();
        reprocessInstallments(disbursementDate, txs, installments, currency);
        return changedTransactionDetail;
    }

    @Override
    public void processLatestTransaction(LoanTransaction loanTransaction, TransactionCtx ctx) {
        switch (loanTransaction.getTypeOf()) {
            case DISBURSEMENT -> handleDisbursement(loanTransaction, ctx);
            case WRITEOFF -> handleWriteOff(loanTransaction, ctx.getCurrency(), ctx.getInstallments());
            case REFUND_FOR_ACTIVE_LOAN -> handleRefund(loanTransaction, ctx.getCurrency(), ctx.getInstallments(), ctx.getCharges());
            case CHARGEBACK -> handleChargeback(loanTransaction, ctx);
            case CREDIT_BALANCE_REFUND ->
                handleCreditBalanceRefund(loanTransaction, ctx.getCurrency(), ctx.getInstallments(), ctx.getOverpaymentHolder());
            case REPAYMENT, MERCHANT_ISSUED_REFUND, PAYOUT_REFUND, GOODWILL_CREDIT, CHARGE_REFUND, CHARGE_ADJUSTMENT, DOWN_PAYMENT,
                    WAIVE_INTEREST, RECOVERY_REPAYMENT, INTEREST_PAYMENT_WAIVER ->
                handleRepayment(loanTransaction, ctx);
            case CHARGE_OFF -> handleChargeOff(loanTransaction, ctx);
            case CHARGE_PAYMENT -> handleChargePayment(loanTransaction, ctx);
            case WAIVE_CHARGES -> log.debug("WAIVE_CHARGES transaction will not be processed.");
            case REAMORTIZE -> handleReAmortization(loanTransaction, ctx);
            case REAGE -> handleReAge(loanTransaction, ctx);
            case ACCRUAL_ACTIVITY -> calculateAccrualActivity(loanTransaction, ctx.getCurrency(), ctx.getInstallments());
            // TODO: Cover rest of the transaction types
            default -> {
                log.warn("Unhandled transaction processing for transaction type: {}", loanTransaction.getTypeOf());
            }
        }
    }

    private void handleReAmortization(LoanTransaction loanTransaction, TransactionCtx transactionCtx) {
        LocalDate transactionDate = loanTransaction.getTransactionDate();
        List<LoanRepaymentScheduleInstallment> previousInstallments = transactionCtx.getInstallments().stream() //
                .filter(installment -> !installment.getDueDate().isAfter(transactionDate)) //
                .toList();
        List<LoanRepaymentScheduleInstallment> futureInstallments = transactionCtx.getInstallments().stream() //
                .filter(installment -> installment.getDueDate().isAfter(transactionDate)) //
                .filter(installment -> !installment.isAdditional() && !installment.isDownPayment() && !installment.isReAged()) //
                .toList();

        BigDecimal overallOverDuePrincipal = ZERO;
        for (LoanRepaymentScheduleInstallment installment : previousInstallments) {
            Money principalCompleted = installment.getPrincipalCompleted(transactionCtx.getCurrency());
            overallOverDuePrincipal = overallOverDuePrincipal
                    .add(installment.getPrincipal(transactionCtx.getCurrency()).minus(principalCompleted).getAmount());
            installment.updatePrincipal(installment.getPrincipalCompleted(transactionCtx.getCurrency()).getAmount());
            installment.updateObligationsMet(transactionCtx.getCurrency(), transactionDate);
        }

        loanTransaction.resetDerivedComponents();
        loanTransaction.updateComponentsAndTotal(Money.of(transactionCtx.getCurrency(), overallOverDuePrincipal),
                Money.zero(transactionCtx.getCurrency()), Money.zero(transactionCtx.getCurrency()),
                Money.zero(transactionCtx.getCurrency()));

        LoanRepaymentScheduleInstallment lastFutureInstallment = futureInstallments.stream()
                .max(Comparator.comparing(LoanRepaymentScheduleInstallment::getDueDate)).get();
        BigDecimal reAmortizationAmountPerInstallment = overallOverDuePrincipal.divide(BigDecimal.valueOf(futureInstallments.size()),
                MoneyHelper.getRoundingMode());
        Integer installmentAmountInMultiplesOf = loanTransaction.getLoan().getLoanProduct().getInstallmentAmountInMultiplesOf();

        for (LoanRepaymentScheduleInstallment installment : futureInstallments) {
            if (lastFutureInstallment.equals(installment)) {
                installment.addToPrincipal(transactionDate, Money.of(transactionCtx.getCurrency(), overallOverDuePrincipal));
            } else {
                if (installmentAmountInMultiplesOf != null) {
                    reAmortizationAmountPerInstallment = Money.roundToMultiplesOf(reAmortizationAmountPerInstallment,
                            installmentAmountInMultiplesOf);
                }
                installment.addToPrincipal(transactionDate, Money.of(transactionCtx.getCurrency(), reAmortizationAmountPerInstallment));
                overallOverDuePrincipal = overallOverDuePrincipal.subtract(reAmortizationAmountPerInstallment);
            }
        }
    }

    @Override
    protected void handleChargeback(LoanTransaction loanTransaction, TransactionCtx ctx) {
        processCreditTransaction(loanTransaction, ctx);
    }

    private boolean hasNoCustomCreditAllocationRule(LoanTransaction loanTransaction) {
        return (loanTransaction.getLoan().getCreditAllocationRules() == null || !loanTransaction.getLoan().getCreditAllocationRules()
                .stream().anyMatch(e -> e.getTransactionType().getLoanTransactionType().equals(loanTransaction.getTypeOf())));
    }

    protected LoanTransaction findOriginalTransaction(LoanTransaction loanTransaction, TransactionCtx ctx) {
        if (loanTransaction.getId() != null) { // this the normal case without reverse-replay
            Optional<LoanTransaction> originalTransaction = loanTransaction.getLoan().getLoanTransactions().stream()
                    .filter(tr -> tr.getLoanTransactionRelations().stream()
                            .anyMatch(this.hasMatchingToLoanTransaction(loanTransaction.getId(), CHARGEBACK)))
                    .findFirst();
            if (originalTransaction.isEmpty()) {
                throw new RuntimeException("Chargeback transaction must have an original transaction");
            }
            return originalTransaction.get();
        } else { // when there is no id, then it might be that the original transaction is changed, so we need to look
                 // it up from the Ctx.
            Long originalChargebackTransactionId = ctx.getChangedTransactionDetail().getCurrentTransactionToOldId().get(loanTransaction);
            Collection<LoanTransaction> updatedTransactions = ctx.getChangedTransactionDetail().getNewTransactionMappings().values();
            Optional<LoanTransaction> updatedTransaction = updatedTransactions.stream().filter(tr -> tr.getLoanTransactionRelations()
                    .stream().anyMatch(this.hasMatchingToLoanTransaction(originalChargebackTransactionId, CHARGEBACK))).findFirst();

            if (updatedTransaction.isPresent()) {
                return updatedTransaction.get();
            } else { // if it is not there, then it simply means that this has not changed during reverse replay
                Optional<LoanTransaction> originalTransaction = loanTransaction.getLoan().getLoanTransactions().stream()
                        .filter(tr -> tr.getLoanTransactionRelations().stream()
                                .anyMatch(this.hasMatchingToLoanTransaction(originalChargebackTransactionId, CHARGEBACK)))
                        .findFirst();
                if (originalTransaction.isEmpty()) {
                    throw new RuntimeException("Chargeback transaction must have an original transaction");
                }
                return originalTransaction.get();
            }
        }
    }

    protected void processCreditTransaction(LoanTransaction loanTransaction, TransactionCtx ctx) {
        if (hasNoCustomCreditAllocationRule(loanTransaction)) {
            super.processCreditTransaction(loanTransaction, ctx.getOverpaymentHolder(), ctx.getCurrency(), ctx.getInstallments());
        } else {
            loanTransaction.resetDerivedComponents();
            final Comparator<LoanRepaymentScheduleInstallment> byDate = Comparator.comparing(LoanRepaymentScheduleInstallment::getDueDate);
            ctx.getInstallments().sort(byDate);
            final Money zeroMoney = Money.zero(ctx.getCurrency());
            Money transactionAmount = loanTransaction.getAmount(ctx.getCurrency());
            Money totalOverpaid = ctx.getOverpaymentHolder().getMoneyObject();
            Money amountToDistribute = MathUtil.negativeToZero(loanTransaction.getAmount(ctx.getCurrency()).minus(totalOverpaid));
            Money overpaymentAmount = MathUtil.negativeToZero(transactionAmount.minus(amountToDistribute));
            loanTransaction.setOverPayments(overpaymentAmount);

            if (transactionAmount.isGreaterThanZero()) {
                if (loanTransaction.isChargeback()) {
                    LoanTransaction originalTransaction = findOriginalTransaction(loanTransaction, ctx);
                    // get the original allocation from the opriginal transaction
                    Map<AllocationType, Money> originalAllocationNotAdjusted = getOriginalAllocation(originalTransaction,
                            ctx.getCurrency());
                    LoanCreditAllocationRule chargeBackAllocationRule = getChargebackAllocationRules(loanTransaction);

                    // if there were earlier chargebacks then let's calculate the remaining amounts for each portion
                    Map<AllocationType, Money> originalAllocation = adjustOriginalAllocationWithFormerChargebacks(originalTransaction,
                            originalAllocationNotAdjusted, loanTransaction, ctx, chargeBackAllocationRule);

                    // calculate the current chargeback allocation
                    Map<AllocationType, Money> chargebackAllocation = calculateChargebackAllocationMap(originalAllocation,
                            transactionAmount.getAmount(), chargeBackAllocationRule.getAllocationTypes(), ctx.getCurrency());

                    loanTransaction.updateComponents(chargebackAllocation.get(PRINCIPAL), chargebackAllocation.get(INTEREST),
                            chargebackAllocation.get(FEE), chargebackAllocation.get(PENALTY));

                    final LocalDate transactionDate = loanTransaction.getTransactionDate();
                    boolean loanTransactionMapped = false;
                    LocalDate pastDueDate = null;
                    for (final LoanRepaymentScheduleInstallment currentInstallment : ctx.getInstallments()) {
                        pastDueDate = currentInstallment.getDueDate();
                        if (!currentInstallment.isAdditional() && DateUtils.isAfter(currentInstallment.getDueDate(), transactionDate)) {
                            recognizeAmountsAfterChargeback(ctx.getCurrency(), transactionDate, currentInstallment, chargebackAllocation);
                            loanTransactionMapped = true;
                            break;

                            // If already exists an additional installment just update the due date and
                            // principal from the Loan chargeback / CBR transaction
                        } else if (currentInstallment.isAdditional()) {
                            if (DateUtils.isAfter(transactionDate, currentInstallment.getDueDate())) {
                                currentInstallment.updateDueDate(transactionDate);
                            }
                            recognizeAmountsAfterChargeback(ctx.getCurrency(), transactionDate, currentInstallment, chargebackAllocation);
                            loanTransactionMapped = true;
                            break;
                        }
                    }

                    // New installment will be added (N+1 scenario)
                    if (!loanTransactionMapped) {
                        if (loanTransaction.getTransactionDate().equals(pastDueDate)) {
                            LoanRepaymentScheduleInstallment currentInstallment = ctx.getInstallments()
                                    .get(ctx.getInstallments().size() - 1);
                            recognizeAmountsAfterChargeback(ctx.getCurrency(), transactionDate, currentInstallment, chargebackAllocation);
                        } else {
                            Loan loan = loanTransaction.getLoan();
                            LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(loan,
                                    (ctx.getInstallments().size() + 1), pastDueDate, transactionDate, zeroMoney.getAmount(),
                                    zeroMoney.getAmount(), zeroMoney.getAmount(), zeroMoney.getAmount(), false, null);
                            recognizeAmountsAfterChargeback(ctx.getCurrency(), transactionDate, installment, chargebackAllocation);
                            installment.markAsAdditional();
                            loan.addLoanRepaymentScheduleInstallment(installment);
                        }
                    }
                    allocateOverpayment(loanTransaction, ctx);
                } else {
                    throw new RuntimeException("Unsupported transaction " + loanTransaction.getTypeOf().name());
                }
            }
        }
    }

    private Map<AllocationType, Money> adjustOriginalAllocationWithFormerChargebacks(LoanTransaction originalTransaction,
            Map<AllocationType, Money> originalAllocation, LoanTransaction chargeBackTransaction, TransactionCtx ctx,
            LoanCreditAllocationRule chargeBackAllocationRule) {
        // these are the list of existing transactions
        List<LoanTransaction> allTransactions = new ArrayList<>(chargeBackTransaction.getLoan().getLoanTransactions());

        // Remove the current chargeback from the list
        if (chargeBackTransaction.getId() != null) {
            allTransactions.remove(chargeBackTransaction);
        } else {
            Long oldId = ctx.getChangedTransactionDetail().getCurrentTransactionToOldId().get(chargeBackTransaction);
            allTransactions.remove(allTransactions.stream().filter(tr -> Objects.equals(tr.getId(), oldId)).findFirst().get());
        }

        // Add the replayed transactions and remove their old version before the replay
        if (ctx.getChangedTransactionDetail() != null && ctx.getChangedTransactionDetail().getNewTransactionMappings() != null) {
            for (Long id : ctx.getChangedTransactionDetail().getNewTransactionMappings().keySet()) {
                allTransactions.remove(allTransactions.stream().filter(tr -> Objects.equals(tr.getId(), id)).findFirst().get());
                allTransactions.add(ctx.getChangedTransactionDetail().getNewTransactionMappings().get(id));
            }
        }

        // keep only the chargeback transactions
        List<LoanTransaction> chargebacks = allTransactions.stream().filter(LoanTransaction::isChargeback).toList();

        // let's figure out the original transaction for these chargebacks, and order them by ascending order
        List<LoanTransaction> chargebacksForTheSameOriginal = chargebacks.stream()
                .filter(tr -> findOriginalTransaction(tr, ctx) == originalTransaction).sorted(loanTransactionDateComparator()).toList();

        Map<AllocationType, Money> allocation = new HashMap<>(originalAllocation);
        for (LoanTransaction loanTransaction : chargebacksForTheSameOriginal) {
            Map<AllocationType, Money> temp = calculateChargebackAllocationMap(allocation, loanTransaction.getAmount(),
                    chargeBackAllocationRule.getAllocationTypes(), ctx.getCurrency());
            allocation.keySet().forEach(k -> allocation.put(k, allocation.get(k).minus(temp.get(k))));
        }
        return allocation;
    }

    @NotNull
    private Comparator<LoanTransaction> loanTransactionDateComparator() {
        return (tr1, tr2) -> {
            if (tr1.getTransactionDate().compareTo(tr2.getTransactionDate()) != 0) {
                return tr1.getTransactionDate().compareTo(tr2.getTransactionDate());
            } else if (tr1.getSubmittedOnDate().compareTo(tr2.getSubmittedOnDate()) != 0) {
                return tr1.getSubmittedOnDate().compareTo(tr2.getSubmittedOnDate());
            } else {
                return tr1.getCreatedDateTime().compareTo(tr2.getCreatedDateTime());
            }
        };
    }

    private void recognizeAmountsAfterChargeback(MonetaryCurrency currency, LocalDate localDate,
            LoanRepaymentScheduleInstallment installment, Map<AllocationType, Money> chargebackAllocation) {
        Money principal = chargebackAllocation.get(PRINCIPAL);
        if (principal.isGreaterThanZero()) {
            installment.addToCreditedPrincipal(principal.getAmount());
            installment.addToPrincipal(localDate, principal);
        }

        Money fee = chargebackAllocation.get(FEE);
        if (fee.isGreaterThanZero()) {
            installment.addToCreditedFee(fee.getAmount());
            installment.addToChargePortion(fee, Money.zero(currency), Money.zero(currency), Money.zero(currency), Money.zero(currency),
                    Money.zero(currency));
        }

        Money penalty = chargebackAllocation.get(PENALTY);
        if (penalty.isGreaterThanZero()) {
            installment.addToCreditedPenalty(penalty.getAmount());
            installment.addToChargePortion(Money.zero(currency), Money.zero(currency), Money.zero(currency), penalty, Money.zero(currency),
                    Money.zero(currency));
        }
    }

    @NotNull
    private LoanCreditAllocationRule getChargebackAllocationRules(LoanTransaction loanTransaction) {
        LoanCreditAllocationRule chargeBackAllocationRule = loanTransaction.getLoan().getCreditAllocationRules().stream()
                .filter(tr -> tr.getTransactionType().equals(CreditAllocationTransactionType.CHARGEBACK)).findFirst().orElseThrow();
        return chargeBackAllocationRule;
    }

    @NotNull
    private Map<AllocationType, Money> getOriginalAllocation(LoanTransaction originalLoanTransaction, MonetaryCurrency currency) {
        Map<AllocationType, Money> originalAllocation = new HashMap<>();
        originalAllocation.put(PRINCIPAL, Money.of(currency, originalLoanTransaction.getPrincipalPortion()));
        originalAllocation.put(INTEREST, Money.of(currency, originalLoanTransaction.getInterestPortion()));
        originalAllocation.put(PENALTY, Money.of(currency, originalLoanTransaction.getPenaltyChargesPortion()));
        originalAllocation.put(FEE, Money.of(currency, originalLoanTransaction.getFeeChargesPortion()));
        return originalAllocation;
    }

    protected Map<AllocationType, Money> calculateChargebackAllocationMap(Map<AllocationType, Money> originalAllocation,
            BigDecimal amountToDistribute, List<AllocationType> allocationTypes, MonetaryCurrency currency) {
        BigDecimal remainingAmount = amountToDistribute;
        Map<AllocationType, Money> result = new HashMap<>();
        Arrays.stream(AllocationType.values()).forEach(allocationType -> result.put(allocationType, Money.of(currency, BigDecimal.ZERO)));
        for (AllocationType allocationType : allocationTypes) {
            if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal originalAmount = originalAllocation.get(allocationType).getAmount();
                if (originalAmount != null && remainingAmount.compareTo(originalAmount) > 0
                        && originalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    result.put(allocationType, Money.of(currency, originalAmount));
                    remainingAmount = remainingAmount.subtract(originalAmount);
                } else if (originalAmount != null && remainingAmount.compareTo(originalAmount) <= 0
                        && originalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    result.put(allocationType, Money.of(currency, remainingAmount));
                    remainingAmount = BigDecimal.ZERO;
                }
            }
        }
        return result;
    }

    private Predicate<LoanTransactionRelation> hasMatchingToLoanTransaction(Long id, LoanTransactionRelationTypeEnum typeEnum) {
        return relation -> relation.getRelationType().equals(typeEnum) && Objects.equals(relation.getToTransaction().getId(), id);
    }

    @Override
    protected void handleRefund(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges) {
        Money zero = Money.zero(currency);
        List<LoanTransactionToRepaymentScheduleMapping> transactionMappings = new ArrayList<>();
        Money transactionAmountUnprocessed = loanTransaction.getAmount(currency);

        List<LoanPaymentAllocationRule> paymentAllocationRules = loanTransaction.getLoan().getPaymentAllocationRules();
        LoanPaymentAllocationRule defaultPaymentAllocationRule = paymentAllocationRules.stream()
                .filter(e -> DEFAULT.equals(e.getTransactionType())).findFirst().orElseThrow();
        LoanPaymentAllocationRule paymentAllocationRule = paymentAllocationRules.stream()
                .filter(e -> loanTransaction.getTypeOf().equals(e.getTransactionType().getLoanTransactionType())).findFirst()
                .orElse(defaultPaymentAllocationRule);
        Balances balances = new Balances(zero, zero, zero, zero);
        List<PaymentAllocationType> paymentAllocationTypes;
        FutureInstallmentAllocationRule futureInstallmentAllocationRule;
        if (DEFAULT.equals(paymentAllocationRule.getTransactionType())) {
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
            List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges, ChangedTransactionDetail changedTransactionDetail,
            MoneyHolder overpaymentHolder, ProgressiveLoanInterestScheduleModel scheduleModel) {
        TransactionCtx ctx = new ProgressiveTransactionCtx(currency, installments, charges, overpaymentHolder, changedTransactionDetail,
                scheduleModel);
        if (loanTransaction.getId() == null) {
            processLatestTransaction(loanTransaction, ctx);
            if (loanTransaction.isInterestWaiver()) {
                loanTransaction.adjustInterestComponent(currency);
            }
        } else {
            /*
             * For existing transactions, check if the re-payment breakup (principal, interest, fees, penalties) has
             * changed.<br>
             */
            final LoanTransaction newLoanTransaction = LoanTransaction.copyTransactionProperties(loanTransaction);
            ctx.getChangedTransactionDetail().getCurrentTransactionToOldId().put(newLoanTransaction, loanTransaction.getId());

            // Reset derived component of new loan transaction and
            // re-process transaction
            processLatestTransaction(newLoanTransaction, ctx);
            if (loanTransaction.isInterestWaiver()) {
                newLoanTransaction.adjustInterestComponent(currency);
            }
            /*
             * Check if the transaction amounts have changed or was there any transaction for the same date which was
             * reverse-replayed. If so, reverse the original transaction and update changedTransactionDetail accordingly
             */
            boolean aTransactionWasAlreadyReplayedForTheSameDate = changedTransactionDetail.getNewTransactionMappings().values().stream()
                    .anyMatch(lt -> lt.getTransactionDate().equals(loanTransaction.getTransactionDate()));
            if (LoanTransaction.transactionAmountsMatch(currency, loanTransaction, newLoanTransaction)
                    && !aTransactionWasAlreadyReplayedForTheSameDate) {
                loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(
                        newLoanTransaction.getLoanTransactionToRepaymentScheduleMappings());
            } else {
                createNewTransaction(loanTransaction, newLoanTransaction, changedTransactionDetail);
                checkAndUpdateReplayedChargebackRelationWithReplayedTransaction(loanTransaction, newLoanTransaction, ctx);
            }
        }
    }

    private void checkAndUpdateReplayedChargebackRelationWithReplayedTransaction(LoanTransaction loanTransaction,
            LoanTransaction newLoanTransaction, TransactionCtx ctx) {
        // if chargeback is getting reverse-replayed
        // find replayed transaction with CHARGEBACK relation with reversed chargeback transaction
        // for replayed transaction, add relation to point to new Chargeback transaction
        if (loanTransaction.getTypeOf().isChargeback()) {
            LoanTransaction originalTransaction = findOriginalTransaction(newLoanTransaction, ctx);
            originalTransaction.getLoanTransactionRelations()
                    .add(LoanTransactionRelation.linkToTransaction(originalTransaction, newLoanTransaction, CHARGEBACK));
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

    private void handleDisbursementWithEMICalculator(LoanTransaction disbursementTransaction, TransactionCtx transactionCtx) {
        ProgressiveTransactionCtx progressiveTransactionCtx = (ProgressiveTransactionCtx) transactionCtx;
        if (progressiveTransactionCtx.getModel() == null) {
            throw new IllegalStateException("TransactionCtx has no model");
        }
        disbursementTransaction.resetDerivedComponents();
        final MathContext mc = MoneyHelper.getMathContext();
        LoanProductRelatedDetail loanProductRelatedDetail = disbursementTransaction.getLoan().getLoanRepaymentScheduleDetail();
        Integer installmentAmountInMultiplesOf = disbursementTransaction.getLoan().getLoanProduct().getInstallmentAmountInMultiplesOf();
        Money downPaymentAmount = Money.zero(progressiveTransactionCtx.getCurrency());
        if (loanProductRelatedDetail.isEnableDownPayment()) {
            LoanRepaymentScheduleInstallment downPaymentInstallment = progressiveTransactionCtx.getInstallments().stream()
                    .filter(i -> i.isDownPayment() && i.getPrincipal(progressiveTransactionCtx.getCurrency()).isZero()).findFirst()
                    .orElseThrow();
            BigDecimal downPaymentAmt = MathUtil.percentageOf(disbursementTransaction.getAmount(),
                    loanProductRelatedDetail.getDisbursedAmountPercentageForDownPayment(), mc);
            if (installmentAmountInMultiplesOf != null) {
                downPaymentAmt = Money.roundToMultiplesOf(downPaymentAmt, installmentAmountInMultiplesOf);
            }
            downPaymentAmount = Money.of(transactionCtx.getCurrency(), downPaymentAmt);
            downPaymentInstallment.addToPrincipal(disbursementTransaction.getTransactionDate(), downPaymentAmount);
        }

        Money amortizableAmount = disbursementTransaction.getAmount(transactionCtx.getCurrency()).minus(downPaymentAmount);

        emiCalculator.addDisbursement(progressiveTransactionCtx.getModel(), disbursementTransaction.getTransactionDate(),
                amortizableAmount);

        if (amortizableAmount.isGreaterThanZero()) {
            progressiveTransactionCtx.getModel().repayments().forEach(rm -> {
                LoanRepaymentScheduleInstallment installment = transactionCtx.getInstallments().stream()
                        .filter(ri -> ri.getDueDate().equals(rm.getDueDate()) && !ri.isDownPayment()
                                && !ri.getDueDate().isBefore(disbursementTransaction.getTransactionDate()))
                        .findFirst().orElse(null);
                if (installment != null) {
                    installment.updatePrincipal(rm.getPrincipalDue().getAmount());
                    installment.updateInterestCharged(rm.getInterestDue().getAmount());
                    installment.updateObligationsMet(progressiveTransactionCtx.getCurrency(), disbursementTransaction.getTransactionDate());
                }
            });
        }

        allocateOverpayment(disbursementTransaction, transactionCtx);
    }

    private void handleDisbursement(LoanTransaction disbursementTransaction, TransactionCtx transactionCtx) {
        // TODO: Fix this and enhance EMICalculator to support reamortization and reaging
        if (disbursementTransaction.getLoan().isInterestBearing()) {
            handleDisbursementWithEMICalculator(disbursementTransaction, transactionCtx);
        } else {
            handleDisbursementWithoutEMICalculator(disbursementTransaction, transactionCtx);
        }
    }

    private void handleDisbursementWithoutEMICalculator(LoanTransaction disbursementTransaction, TransactionCtx transactionCtx) {
        disbursementTransaction.resetDerivedComponents();
        final MathContext mc = MoneyHelper.getMathContext();
        List<LoanRepaymentScheduleInstallment> candidateRepaymentInstallments = transactionCtx.getInstallments().stream().filter(
                i -> i.getDueDate().isAfter(disbursementTransaction.getTransactionDate()) && !i.isDownPayment() && !i.isAdditional())
                .toList();
        int noCandidateRepaymentInstallments = candidateRepaymentInstallments.size();
        LoanProductRelatedDetail loanProductRelatedDetail = disbursementTransaction.getLoan().getLoanRepaymentScheduleDetail();
        Integer installmentAmountInMultiplesOf = disbursementTransaction.getLoan().getLoanProduct().getInstallmentAmountInMultiplesOf();
        Money downPaymentAmount = Money.zero(transactionCtx.getCurrency());
        if (loanProductRelatedDetail.isEnableDownPayment()) {
            LoanRepaymentScheduleInstallment downPaymentInstallment = transactionCtx.getInstallments().stream()
                    .filter(i -> i.isDownPayment() && i.getPrincipal(transactionCtx.getCurrency()).isZero()).findFirst().orElseThrow();
            BigDecimal downPaymentAmt = MathUtil.percentageOf(disbursementTransaction.getAmount(),
                    loanProductRelatedDetail.getDisbursedAmountPercentageForDownPayment(), mc);
            if (installmentAmountInMultiplesOf != null) {
                downPaymentAmt = Money.roundToMultiplesOf(downPaymentAmt, installmentAmountInMultiplesOf);
            }
            downPaymentAmount = Money.of(transactionCtx.getCurrency(), downPaymentAmt);
            downPaymentInstallment.addToPrincipal(disbursementTransaction.getTransactionDate(), downPaymentAmount);
        }

        Money amortizableAmount = disbursementTransaction.getAmount(transactionCtx.getCurrency()).minus(downPaymentAmount);

        if (amortizableAmount.isGreaterThanZero()) {
            Money increasePrincipalBy = amortizableAmount.dividedBy(noCandidateRepaymentInstallments, mc.getRoundingMode());
            MoneyHolder moneyHolder = new MoneyHolder(amortizableAmount);

            candidateRepaymentInstallments.forEach(i -> {
                Money previousPrincipal = i.getPrincipal(transactionCtx.getCurrency());
                Money newPrincipal = previousPrincipal.add(increasePrincipalBy);
                if (installmentAmountInMultiplesOf != null) {
                    newPrincipal = Money.roundToMultiplesOf(newPrincipal, installmentAmountInMultiplesOf);
                }
                i.updatePrincipal(newPrincipal.getAmount());
                moneyHolder.setMoneyObject(moneyHolder.getMoneyObject().minus(newPrincipal).plus(previousPrincipal));
                i.updateObligationsMet(transactionCtx.getCurrency(), disbursementTransaction.getTransactionDate());
            });
            // Hence the rounding, we might need to amend the last installment amount
            candidateRepaymentInstallments.get(noCandidateRepaymentInstallments - 1)
                    .addToPrincipal(disbursementTransaction.getTransactionDate(), moneyHolder.getMoneyObject());
        }

        allocateOverpayment(disbursementTransaction, transactionCtx);
    }

    private void allocateOverpayment(LoanTransaction loanTransaction, TransactionCtx transactionCtx) {
        if (transactionCtx.getOverpaymentHolder().getMoneyObject().isGreaterThanZero()) {
            if (transactionCtx.getOverpaymentHolder().getMoneyObject()
                    .isGreaterThan(loanTransaction.getAmount(transactionCtx.getCurrency()))) {
                loanTransaction.setOverPayments(loanTransaction.getAmount(transactionCtx.getCurrency()));
            } else {
                loanTransaction.setOverPayments(transactionCtx.getOverpaymentHolder().getMoneyObject());
            }
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings = new ArrayList<>();
            List<LoanPaymentAllocationRule> paymentAllocationRules = loanTransaction.getLoan().getPaymentAllocationRules();
            LoanPaymentAllocationRule defaultPaymentAllocationRule = paymentAllocationRules.stream()
                    .filter(e -> DEFAULT.equals(e.getTransactionType())).findFirst().orElseThrow();

            Money transactionAmountUnprocessed = null;
            Money zero = Money.zero(transactionCtx.getCurrency());
            Balances balances = new Balances(zero, zero, zero, zero);
            if (LoanScheduleProcessingType.HORIZONTAL
                    .equals(loanTransaction.getLoan().getLoanProductRelatedDetail().getLoanScheduleProcessingType())) {
                transactionAmountUnprocessed = processPeriodsHorizontally(loanTransaction, transactionCtx.getCurrency(),
                        transactionCtx.getInstallments(), transactionCtx.getOverpaymentHolder().getMoneyObject(),
                        defaultPaymentAllocationRule, transactionMappings, Set.of(), balances);
            } else if (LoanScheduleProcessingType.VERTICAL
                    .equals(loanTransaction.getLoan().getLoanProductRelatedDetail().getLoanScheduleProcessingType())) {
                transactionAmountUnprocessed = processPeriodsVertically(loanTransaction, transactionCtx.getCurrency(),
                        transactionCtx.getInstallments(), transactionCtx.getOverpaymentHolder().getMoneyObject(),
                        defaultPaymentAllocationRule, transactionMappings, Set.of(), balances);
            }
            transactionCtx.getOverpaymentHolder().setMoneyObject(transactionAmountUnprocessed);
            loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(transactionMappings);
        }
    }

    private void handleRepayment(LoanTransaction loanTransaction, TransactionCtx transactionCtx) {
        if (loanTransaction.isRepaymentLikeType() || loanTransaction.isInterestWaiver() || loanTransaction.isRecoveryRepayment()) {
            loanTransaction.resetDerivedComponents();
        }
        Money transactionAmountUnprocessed = loanTransaction.getAmount(transactionCtx.getCurrency());
        processTransaction(loanTransaction, transactionCtx, transactionAmountUnprocessed);
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

    private void handleOverpayment(Money overpaymentPortion, LoanTransaction loanTransaction, MoneyHolder overpaymentHolder) {
        if (overpaymentPortion.isGreaterThanZero()) {
            onLoanOverpayment(loanTransaction, overpaymentPortion);
            overpaymentHolder.setMoneyObject(overpaymentHolder.getMoneyObject().add(overpaymentPortion));
            loanTransaction.setOverPayments(overpaymentPortion);
        } else {
            overpaymentHolder.setMoneyObject(overpaymentPortion.zero());
        }
    }

    private void handleChargeOff(LoanTransaction loanTransaction, TransactionCtx transactionCtx) {
        loanTransaction.resetDerivedComponents();
        // determine how much is outstanding total and breakdown for principal, interest and charges
        Money principalPortion = Money.zero(transactionCtx.getCurrency());
        Money interestPortion = Money.zero(transactionCtx.getCurrency());
        Money feeChargesPortion = Money.zero(transactionCtx.getCurrency());
        Money penaltychargesPortion = Money.zero(transactionCtx.getCurrency());
        for (final LoanRepaymentScheduleInstallment currentInstallment : transactionCtx.getInstallments()) {
            if (currentInstallment.isNotFullyPaidOff()) {
                principalPortion = principalPortion.plus(currentInstallment.getPrincipalOutstanding(transactionCtx.getCurrency()));
                interestPortion = interestPortion.plus(currentInstallment.getInterestOutstanding(transactionCtx.getCurrency()));
                feeChargesPortion = feeChargesPortion.plus(currentInstallment.getFeeChargesOutstanding(transactionCtx.getCurrency()));
                penaltychargesPortion = penaltychargesPortion
                        .plus(currentInstallment.getPenaltyChargesOutstanding(transactionCtx.getCurrency()));
            }
        }

        loanTransaction.updateComponentsAndTotal(principalPortion, interestPortion, feeChargesPortion, penaltychargesPortion);
    }

    private void handleChargePayment(LoanTransaction loanTransaction, TransactionCtx transactionCtx) {
        Money zero = Money.zero(transactionCtx.getCurrency());
        Money feeChargesPortion = zero;
        Money penaltyChargesPortion = zero;
        List<LoanTransactionToRepaymentScheduleMapping> transactionMappings = new ArrayList<>();
        LoanChargePaidBy loanChargePaidBy = loanTransaction.getLoanChargesPaid().stream().findFirst().get();
        LoanCharge loanCharge = loanChargePaidBy.getLoanCharge();
        Money amountToBePaid = Money.of(transactionCtx.getCurrency(), loanTransaction.getAmount());
        if (loanCharge.getAmountOutstanding(transactionCtx.getCurrency()).isLessThan(amountToBePaid)) {
            amountToBePaid = loanCharge.getAmountOutstanding(transactionCtx.getCurrency());
        }

        LocalDate startDate = loanTransaction.getLoan().getDisbursementDate();

        Money unprocessed = loanTransaction.getAmount(transactionCtx.getCurrency());
        int firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper
                .fetchFirstNormalInstallmentNumber(transactionCtx.getInstallments());
        for (final LoanRepaymentScheduleInstallment installment : transactionCtx.getInstallments()) {
            boolean isDue = installment.getInstallmentNumber().equals(firstNormalInstallmentNumber)
                    ? loanCharge.isDueForCollectionFromIncludingAndUpToAndIncluding(startDate, installment.getDueDate())
                    : loanCharge.isDueForCollectionFromAndUpToAndIncluding(startDate, installment.getDueDate());
            if (isDue) {
                Integer installmentNumber = installment.getInstallmentNumber();
                Money paidAmount = loanCharge.updatePaidAmountBy(amountToBePaid, installmentNumber, zero);

                LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                        transactionMappings, loanTransaction, installment, transactionCtx.getCurrency());

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
                unprocessed = loanTransaction.getAmount(transactionCtx.getCurrency()).minus(paidAmount);
                loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(transactionMappings);
            }
        }

        if (unprocessed.isGreaterThanZero()) {
            processTransaction(loanTransaction, transactionCtx, unprocessed);
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

    private void processTransaction(LoanTransaction loanTransaction, TransactionCtx transactionCtx, Money transactionAmountUnprocessed) {
        Money zero = Money.zero(transactionCtx.getCurrency());
        List<LoanTransactionToRepaymentScheduleMapping> transactionMappings = new ArrayList<>();

        List<LoanPaymentAllocationRule> paymentAllocationRules = loanTransaction.getLoan().getPaymentAllocationRules();
        LoanPaymentAllocationRule defaultPaymentAllocationRule = paymentAllocationRules.stream()
                .filter(e -> DEFAULT.equals(e.getTransactionType())).findFirst().orElseThrow();
        LoanPaymentAllocationRule paymentAllocationRule = paymentAllocationRules.stream()
                .filter(e -> loanTransaction.getTypeOf().equals(e.getTransactionType().getLoanTransactionType())).findFirst()
                .orElse(defaultPaymentAllocationRule);
        Balances balances = new Balances(zero, zero, zero, zero);

        if (LoanScheduleProcessingType.HORIZONTAL
                .equals(loanTransaction.getLoan().getLoanProductRelatedDetail().getLoanScheduleProcessingType())) {
            transactionAmountUnprocessed = processPeriodsHorizontally(loanTransaction, transactionCtx.getCurrency(),
                    transactionCtx.getInstallments(), transactionAmountUnprocessed, paymentAllocationRule, transactionMappings,
                    transactionCtx.getCharges(), balances);
        } else if (LoanScheduleProcessingType.VERTICAL
                .equals(loanTransaction.getLoan().getLoanProductRelatedDetail().getLoanScheduleProcessingType())) {
            transactionAmountUnprocessed = processPeriodsVertically(loanTransaction, transactionCtx.getCurrency(),
                    transactionCtx.getInstallments(), transactionAmountUnprocessed, paymentAllocationRule, transactionMappings,
                    transactionCtx.getCharges(), balances);
        }
        loanTransaction.updateComponents(balances.getAggregatedPrincipalPortion(), balances.getAggregatedInterestPortion(),
                balances.getAggregatedFeeChargesPortion(), balances.getAggregatedPenaltyChargesPortion());
        loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(transactionMappings);

        handleOverpayment(transactionAmountUnprocessed, loanTransaction, transactionCtx.getOverpaymentHolder());
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

    private void handleReAge(LoanTransaction loanTransaction, TransactionCtx ctx) {
        loanTransaction.resetDerivedComponents();
        MonetaryCurrency currency = ctx.getCurrency();
        List<LoanRepaymentScheduleInstallment> installments = ctx.getInstallments();

        AtomicReference<Money> outstandingPrincipalBalance = new AtomicReference<>(Money.zero(currency));
        installments.forEach(i -> {
            Money principalOutstanding = i.getPrincipalOutstanding(currency);
            if (principalOutstanding.isGreaterThanZero()) {
                outstandingPrincipalBalance.set(outstandingPrincipalBalance.get().add(principalOutstanding));
                i.addToPrincipal(loanTransaction.getTransactionDate(), principalOutstanding.negated());
            }
        });

        loanTransaction.updateComponentsAndTotal(outstandingPrincipalBalance.get(), Money.zero(currency), Money.zero(currency),
                Money.zero(currency));

        Money calculatedPrincipal = Money.zero(currency);
        Money adjustCalculatedPrincipal = Money.zero(currency);
        if (outstandingPrincipalBalance.get().isGreaterThanZero()) {
            calculatedPrincipal = outstandingPrincipalBalance.get()
                    .dividedBy(loanTransaction.getLoanReAgeParameter().getNumberOfInstallments(), MoneyHelper.getRoundingMode());
            Integer installmentAmountInMultiplesOf = loanTransaction.getLoan().getLoanProduct().getInstallmentAmountInMultiplesOf();
            if (installmentAmountInMultiplesOf != null) {
                calculatedPrincipal = Money.roundToMultiplesOf(calculatedPrincipal, installmentAmountInMultiplesOf);
            }
            adjustCalculatedPrincipal = outstandingPrincipalBalance.get()
                    .minus(calculatedPrincipal.multipliedBy(loanTransaction.getLoanReAgeParameter().getNumberOfInstallments()));
        }
        LoanRepaymentScheduleInstallment lastNormalInstallment = installments.stream().filter(i -> !i.isDownPayment())
                .reduce((first, second) -> second).orElseThrow();
        LoanRepaymentScheduleInstallment reAgedInstallment = LoanRepaymentScheduleInstallment.newReAgedInstallment(
                lastNormalInstallment.getLoan(), lastNormalInstallment.getInstallmentNumber() + 1, lastNormalInstallment.getDueDate(),
                loanTransaction.getLoanReAgeParameter().getStartDate(), calculatedPrincipal.getAmount());
        installments.add(reAgedInstallment);
        reAgedInstallment.updateObligationsMet(currency, loanTransaction.getTransactionDate());

        for (int i = 1; i < loanTransaction.getLoanReAgeParameter().getNumberOfInstallments(); i++) {
            LocalDate calculatedDueDate = calculateReAgedInstallmentDueDate(loanTransaction.getLoanReAgeParameter(),
                    reAgedInstallment.getDueDate());
            reAgedInstallment = LoanRepaymentScheduleInstallment.newReAgedInstallment(reAgedInstallment.getLoan(),
                    reAgedInstallment.getInstallmentNumber() + 1, reAgedInstallment.getDueDate(), calculatedDueDate,
                    calculatedPrincipal.getAmount());
            installments.add(reAgedInstallment);
            reAgedInstallment.updateObligationsMet(currency, loanTransaction.getTransactionDate());
        }
        reAgedInstallment.addToPrincipal(loanTransaction.getTransactionDate(), adjustCalculatedPrincipal);
        reprocessInstallmentsOrder(installments);
    }

    private void reprocessInstallmentsOrder(List<LoanRepaymentScheduleInstallment> installments) {
        AtomicInteger counter = new AtomicInteger(1);
        installments.stream().sorted(LoanRepaymentScheduleInstallment::compareToByDueDate)
                .forEachOrdered(i -> i.updateInstallmentNumber(counter.getAndIncrement()));
    }

    private LocalDate calculateReAgedInstallmentDueDate(LoanReAgeParameter reAgeParameter, LocalDate dueDate) {
        return switch (reAgeParameter.getFrequencyType()) {
            case DAYS -> dueDate.plusDays(reAgeParameter.getFrequencyNumber());
            case WEEKS -> dueDate.plusWeeks(reAgeParameter.getFrequencyNumber());
            case MONTHS -> dueDate.plusMonths(reAgeParameter.getFrequencyNumber());
            case YEARS -> dueDate.plusYears(reAgeParameter.getFrequencyNumber());
            default -> throw new UnsupportedOperationException(reAgeParameter.getFrequencyType().getCode());
        };
    }
}
