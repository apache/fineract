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

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeOffBehaviour;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCreditAllocationRule;
import org.apache.fineract.portfolio.loanaccount.domain.LoanPaymentAllocationRule;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariations;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionComparator;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.apache.fineract.portfolio.loanaccount.domain.reaging.LoanReAgeParameter;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.AbstractLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.PeriodDueDetails;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.service.InterestRefundService;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.apache.fineract.portfolio.loanproduct.domain.AllocationType;
import org.apache.fineract.portfolio.loanproduct.domain.CreditAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.DueType;
import org.apache.fineract.portfolio.loanproduct.domain.FutureInstallmentAllocationRule;
import org.apache.fineract.portfolio.loanproduct.domain.LoanPreCloseInterestCalculationStrategy;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class AdvancedPaymentScheduleTransactionProcessor extends AbstractLoanRepaymentScheduleTransactionProcessor {

    public static final String ADVANCED_PAYMENT_ALLOCATION_STRATEGY = "advanced-payment-allocation-strategy";
    public static final String ADVANCED_PAYMENT_ALLOCATION_STRATEGY_NAME = "Advanced payment allocation strategy";

    public final EMICalculator emiCalculator;
    public final LoanRepositoryWrapper loanRepositoryWrapper;
    public final InterestRefundService interestRefundService;

    @Override
    public String getCode() {
        return ADVANCED_PAYMENT_ALLOCATION_STRATEGY;
    }

    @Override
    public String getName() {
        return ADVANCED_PAYMENT_ALLOCATION_STRATEGY_NAME;
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

    // only for progressive loans
    public Pair<ChangedTransactionDetail, ProgressiveLoanInterestScheduleModel> reprocessProgressiveLoanTransactions(
            LocalDate disbursementDate, LocalDate targetDate, List<LoanTransaction> loanTransactions, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges) {
        final ChangedTransactionDetail changedTransactionDetail = new ChangedTransactionDetail();
        if (loanTransactions.isEmpty()) {
            return Pair.of(changedTransactionDetail, null);
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

        for (final LoanRepaymentScheduleInstallment currentInstallment : installments) {
            currentInstallment.resetBalances();
            currentInstallment.updateObligationsMet(currency, disbursementDate);
        }

        MoneyHolder overpaymentHolder = new MoneyHolder(Money.zero(currency));
        final Loan loan = loanTransactions.get(0).getLoan();
        List<LoanTermVariationsData> loanTermVariations = loan.getActiveLoanTermVariations().stream().map(LoanTermVariations::toData)
                .collect(Collectors.toCollection(ArrayList::new));
        final Integer installmentAmountInMultiplesOf = loan.getLoanProduct().getInstallmentAmountInMultiplesOf();
        final LoanProductRelatedDetail loanProductRelatedDetail = loan.getLoanRepaymentScheduleDetail();
        ProgressiveLoanInterestScheduleModel scheduleModel = emiCalculator.generateInstallmentInterestScheduleModel(installments,
                loanProductRelatedDetail, loanTermVariations, installmentAmountInMultiplesOf, overpaymentHolder.getMoneyObject().getMc());
        ProgressiveTransactionCtx ctx = new ProgressiveTransactionCtx(currency, installments, charges, overpaymentHolder,
                changedTransactionDetail, scheduleModel);

        List<ChangeOperation> changeOperations = createSortedChangeList(loanTermVariations, loanTransactions, charges);

        List<LoanTransaction> overpaidTransactions = new ArrayList<>();
        for (final ChangeOperation changeOperation : changeOperations) {
            if (changeOperation.isInterestRateChange()) {
                final LoanTermVariationsData interestRateChange = changeOperation.getInterestRateChange().get();
                processInterestRateChange(installments, interestRateChange, scheduleModel);
            } else if (changeOperation.isTransaction()) {
                LoanTransaction transaction = changeOperation.getLoanTransaction().get();
                processSingleTransaction(transaction, ctx);
                transaction = getProcessedTransaction(changedTransactionDetail, transaction);
                ctx.getAlreadyProcessedTransactions().add(transaction);
                if (transaction.isOverPaid() && transaction.isRepaymentLikeType()) { // TODO CREDIT, DEBIT
                    overpaidTransactions.add(transaction);
                }
            } else {
                LoanCharge loanCharge = changeOperation.getLoanCharge().get();
                processSingleCharge(loanCharge, currency, installments, disbursementDate);
                if (!loanCharge.isFullyPaid() && !overpaidTransactions.isEmpty()) {
                    overpaidTransactions = processOverpaidTransactions(overpaidTransactions, ctx);
                }
            }
        }
        Map<Long, LoanTransaction> newTransactionMappings = changedTransactionDetail.getNewTransactionMappings();
        for (Long oldTransactionId : newTransactionMappings.keySet()) {
            LoanTransaction oldTransaction = loanTransactions.stream().filter(e -> oldTransactionId.equals(e.getId())).findFirst().get();
            LoanTransaction newTransaction = newTransactionMappings.get(oldTransactionId);
            createNewTransaction(oldTransaction, newTransaction, ctx);
        }
        recalculateInterestForDate(targetDate, ctx);
        List<LoanTransaction> txs = changeOperations.stream() //
                .filter(ChangeOperation::isTransaction) //
                .map(e -> e.getLoanTransaction().get()).toList();
        reprocessInstallments(disbursementDate, txs, installments, currency);
        return Pair.of(changedTransactionDetail, scheduleModel);
    }

    @Override
    public ChangedTransactionDetail reprocessLoanTransactions(LocalDate disbursementDate, List<LoanTransaction> loanTransactions,
            MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments, Set<LoanCharge> charges) {
        LocalDate currentDate = DateUtils.getBusinessLocalDate();
        Pair<ChangedTransactionDetail, ProgressiveLoanInterestScheduleModel> result = reprocessProgressiveLoanTransactions(disbursementDate,
                currentDate, loanTransactions, currency, installments, charges);
        return result.getLeft();
    }

    @NotNull
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public ProgressiveLoanInterestScheduleModel calculateInterestScheduleModel(@NotNull Long loanId, LocalDate targetDate) {
        Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        List<LoanTransaction> transactions = loan.retrieveListOfTransactionsForReprocessing();
        MonetaryCurrency currency = loan.getLoanRepaymentScheduleDetail().getCurrency();
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        Set<LoanCharge> charges = loan.getActiveCharges();
        return reprocessProgressiveLoanTransactions(loan.getDisbursementDate(), targetDate, transactions, currency, installments, charges)
                .getRight();
    }

    @NotNull
    private static LoanTransaction getProcessedTransaction(ChangedTransactionDetail changedTransactionDetail, LoanTransaction transaction) {
        LoanTransaction newTransaction = changedTransactionDetail.getNewTransactionMappings().get(transaction.getId());
        return newTransaction == null ? transaction : newTransaction;
    }

    private void processInterestRateChange(final List<LoanRepaymentScheduleInstallment> installments,
            final LoanTermVariationsData interestRateChange, final ProgressiveLoanInterestScheduleModel scheduleModel) {
        final LocalDate interestRateChangeSubmittedOnDate = interestRateChange.getTermVariationApplicableFrom();
        final BigDecimal newInterestRate = interestRateChange.getDecimalValue();
        if (interestRateChange.getTermVariationType().isInterestPauseVariation()) {
            final LocalDate pauseEndDate = interestRateChange.getDateValue();
            emiCalculator.applyInterestPause(scheduleModel, interestRateChangeSubmittedOnDate, pauseEndDate);
        } else {
            emiCalculator.changeInterestRate(scheduleModel, interestRateChangeSubmittedOnDate, newInterestRate);
        }
        processInterestRateChangeOnInstallments(scheduleModel, interestRateChangeSubmittedOnDate, installments);
    }

    private void processInterestRateChangeOnInstallments(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final LocalDate interestRateChangeSubmittedOnDate, final List<LoanRepaymentScheduleInstallment> installments) {
        installments.stream() //
                .filter(installment -> isNotObligationsMet(installment)
                        && !interestRateChangeSubmittedOnDate.isAfter(installment.getDueDate()))
                .forEach(installment -> updateInstallmentIfInterestPeriodPresent(scheduleModel, installment)); //
    }

    private void updateInstallmentIfInterestPeriodPresent(final ProgressiveLoanInterestScheduleModel scheduleModel,
            final LoanRepaymentScheduleInstallment installment) {
        emiCalculator.findRepaymentPeriod(scheduleModel, installment.getDueDate()).ifPresent(interestRepaymentPeriod -> {
            installment.updateInterestCharged(interestRepaymentPeriod.getDueInterest().getAmount());
            installment.updatePrincipal(interestRepaymentPeriod.getDuePrincipal().getAmount());
        });
    }

    @Override
    public void processLatestTransaction(LoanTransaction loanTransaction, TransactionCtx ctx) {
        // If we are behind, we might need to first recalculate interest
        if (ctx instanceof ProgressiveTransactionCtx progressiveTransactionCtx) {
            recalculateInterestForDate(loanTransaction.getTransactionDate(), progressiveTransactionCtx);
        }
        switch (loanTransaction.getTypeOf()) {
            case DISBURSEMENT -> handleDisbursement(loanTransaction, ctx);
            case WRITEOFF -> handleWriteOff(loanTransaction, ctx);
            case REFUND_FOR_ACTIVE_LOAN -> handleRefund(loanTransaction, ctx);
            case CHARGEBACK -> handleChargeback(loanTransaction, ctx);
            case CREDIT_BALANCE_REFUND -> handleCreditBalanceRefund(loanTransaction, ctx);
            case REPAYMENT, MERCHANT_ISSUED_REFUND, PAYOUT_REFUND, GOODWILL_CREDIT, CHARGE_REFUND, CHARGE_ADJUSTMENT, DOWN_PAYMENT,
                    WAIVE_INTEREST, RECOVERY_REPAYMENT, INTEREST_PAYMENT_WAIVER ->
                handleRepayment(loanTransaction, ctx);
            case INTEREST_REFUND -> handleInterestRefund(loanTransaction, ctx);
            case CHARGE_OFF -> handleChargeOff(loanTransaction, ctx);
            case CHARGE_PAYMENT -> handleChargePayment(loanTransaction, ctx);
            case WAIVE_CHARGES -> log.debug("WAIVE_CHARGES transaction will not be processed.");
            case REAMORTIZE -> handleReAmortization(loanTransaction, ctx);
            case REAGE -> handleReAge(loanTransaction, ctx);
            case ACCRUAL_ACTIVITY -> calculateAccrualActivity(loanTransaction, ctx);
            // TODO: Cover rest of the transaction types
            default -> {
                log.warn("Unhandled transaction processing for transaction type: {}", loanTransaction.getTypeOf());
            }
        }
    }

    private void handleInterestRefund(LoanTransaction loanTransaction, TransactionCtx ctx) {

        if (ctx instanceof ProgressiveTransactionCtx progCtx) {
            Money interestBeforeRefund = emiCalculator.getSumOfDueInterestsOnDate(progCtx.getModel(), loanTransaction.getDateOf());
            List<Long> unmodifiedTransactionIds = progCtx.getAlreadyProcessedTransactions().stream().filter(LoanTransaction::isNotReversed)
                    .map(AbstractPersistableCustom::getId).toList();
            List<LoanTransaction> modifiedTransactions = new ArrayList<>(progCtx.getAlreadyProcessedTransactions().stream()
                    .filter(LoanTransaction::isNotReversed).filter(tr -> tr.getId() == null).toList());
            if (!modifiedTransactions.isEmpty()) {
                Money interestAfterRefund = interestRefundService.totalInterestByTransactions(this, loanTransaction.getLoan().getId(),
                        loanTransaction.getDateOf(), modifiedTransactions, unmodifiedTransactionIds);
                Money newAmount = interestBeforeRefund.minus(progCtx.getSumOfInterestRefundAmount()).minus(interestAfterRefund);
                loanTransaction.updateAmount(newAmount.getAmount());
            }
            progCtx.setSumOfInterestRefundAmount(progCtx.getSumOfInterestRefundAmount().add(loanTransaction.getAmount()));
        }
        handleRepayment(loanTransaction, ctx);
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

    protected void handleCreditBalanceRefund(LoanTransaction transaction, TransactionCtx ctx) {
        super.handleCreditBalanceRefund(transaction, ctx.getCurrency(), ctx.getInstallments(), ctx.getOverpaymentHolder());
    }

    private boolean hasNoCustomCreditAllocationRule(LoanTransaction loanTransaction) {
        List<LoanCreditAllocationRule> creditAllocationRules = loanTransaction.getLoan().getCreditAllocationRules();
        return (creditAllocationRules == null || creditAllocationRules.stream()
                .noneMatch(e -> e.getTransactionType().getLoanTransactionType().equals(loanTransaction.getTypeOf())));
    }

    protected LoanTransaction findChargebackOriginalTransaction(LoanTransaction chargebackTransaction, TransactionCtx ctx) {
        ChangedTransactionDetail changedTransactionDetail = ctx.getChangedTransactionDetail();
        Long chargebackId = chargebackTransaction.getId(); // this the normal case without reverse-replay
        if (changedTransactionDetail != null) {
            if (chargebackId == null) {
                // the chargeback transaction was changed, so we need to look it up from the ctx.
                chargebackId = changedTransactionDetail.getCurrentTransactionToOldId().get(chargebackTransaction);
            }

            Long toId = chargebackId;
            Collection<LoanTransaction> updatedTransactions = changedTransactionDetail.getNewTransactionMappings().values();
            Optional<LoanTransaction> fromTransaction = updatedTransactions.stream()
                    .filter(tr -> tr.getLoanTransactionRelations().stream().anyMatch(hasMatchingToLoanTransaction(toId, CHARGEBACK)))
                    .findFirst();
            if (fromTransaction.isPresent()) {
                return fromTransaction.get();
            }
        }
        Long toId = chargebackId;
        // if the original transaction is not in the ctx, then it means that it has not changed during reverse replay
        Optional<LoanTransaction> fromTransaction = chargebackTransaction.getLoan().getLoanTransactions().stream()
                .filter(tr -> tr.getLoanTransactionRelations().stream().anyMatch(this.hasMatchingToLoanTransaction(toId, CHARGEBACK)))
                .findFirst();
        if (fromTransaction.isEmpty()) {
            throw new RuntimeException("Chargeback transaction must have an original transaction");
        }
        return fromTransaction.get();
    }

    protected void processCreditTransaction(LoanTransaction loanTransaction, TransactionCtx ctx) {
        if (hasNoCustomCreditAllocationRule(loanTransaction)) {
            super.processCreditTransaction(loanTransaction, ctx.getOverpaymentHolder(), ctx.getCurrency(), ctx.getInstallments());
        } else {
            loanTransaction.resetDerivedComponents();
            MonetaryCurrency currency = ctx.getCurrency();
            final Comparator<LoanRepaymentScheduleInstallment> byDate = Comparator.comparing(LoanRepaymentScheduleInstallment::getDueDate);
            ctx.getInstallments().sort(byDate);
            final Money zeroMoney = Money.zero(currency);
            Money transactionAmount = loanTransaction.getAmount(currency);
            Money totalOverpaid = ctx.getOverpaymentHolder().getMoneyObject();
            Money amountToDistribute = MathUtil.negativeToZero(transactionAmount).minus(totalOverpaid);
            Money overpaymentAmount = MathUtil.negativeToZero(transactionAmount.minus(amountToDistribute));
            loanTransaction.setOverPayments(overpaymentAmount);
            if (!transactionAmount.isGreaterThanZero()) {
                return;
            }
            if (!loanTransaction.isChargeback()) {
                throw new RuntimeException("Unsupported transaction " + loanTransaction.getTypeOf().name());
            }

            LoanTransaction originalTransaction = findChargebackOriginalTransaction(loanTransaction, ctx);
            // get the original allocation from the opriginal transaction
            Map<AllocationType, Money> originalAllocationNotAdjusted = getOriginalAllocation(originalTransaction, currency);
            LoanCreditAllocationRule chargeBackAllocationRule = getChargebackAllocationRules(loanTransaction);

            // if there were earlier chargebacks then let's calculate the remaining amounts for each portion
            Map<AllocationType, Money> originalAllocation = adjustOriginalAllocationWithFormerChargebacks(originalTransaction,
                    originalAllocationNotAdjusted, loanTransaction, ctx, chargeBackAllocationRule);

            // calculate the current chargeback allocation
            Map<AllocationType, Money> chargebackAllocation = calculateChargebackAllocationMap(originalAllocation,
                    transactionAmount.getAmount(), chargeBackAllocationRule.getAllocationTypes(), currency);

            loanTransaction.updateComponents(chargebackAllocation.get(PRINCIPAL), chargebackAllocation.get(INTEREST),
                    chargebackAllocation.get(FEE), chargebackAllocation.get(PENALTY));

            final LocalDate transactionDate = loanTransaction.getTransactionDate();
            boolean loanTransactionMapped = false;
            LocalDate pastDueDate = null;
            for (final LoanRepaymentScheduleInstallment currentInstallment : ctx.getInstallments()) {
                pastDueDate = currentInstallment.getDueDate();
                if (!currentInstallment.isAdditional() && DateUtils.isAfter(currentInstallment.getDueDate(), transactionDate)) {
                    recognizeAmountsAfterChargeback(ctx, transactionDate, currentInstallment, chargebackAllocation);
                    loanTransactionMapped = true;
                    break;

                    // If already exists an additional installment just update the due date and
                    // principal from the Loan chargeback / CBR transaction
                } else if (currentInstallment.isAdditional()) {
                    if (DateUtils.isAfter(transactionDate, currentInstallment.getDueDate())) {
                        currentInstallment.updateDueDate(transactionDate);
                    }
                    recognizeAmountsAfterChargeback(ctx, transactionDate, currentInstallment, chargebackAllocation);
                    loanTransactionMapped = true;
                    break;
                }
            }

            // New installment will be added (N+1 scenario)
            if (!loanTransactionMapped) {
                if (loanTransaction.getTransactionDate().equals(pastDueDate)) {
                    LoanRepaymentScheduleInstallment currentInstallment = ctx.getInstallments().get(ctx.getInstallments().size() - 1);
                    recognizeAmountsAfterChargeback(ctx, transactionDate, currentInstallment, chargebackAllocation);
                } else {
                    Loan loan = loanTransaction.getLoan();
                    LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(loan,
                            (ctx.getInstallments().size() + 1), pastDueDate, transactionDate, zeroMoney.getAmount(), zeroMoney.getAmount(),
                            zeroMoney.getAmount(), zeroMoney.getAmount(), false, null);
                    recognizeAmountsAfterChargeback(ctx, transactionDate, installment, chargebackAllocation);
                    installment.markAsAdditional();
                    loan.addLoanRepaymentScheduleInstallment(installment);
                }
            }
            allocateOverpayment(loanTransaction, ctx);
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
        Comparator<LoanTransaction> comparator = LoanTransactionComparator.INSTANCE;
        List<LoanTransaction> chargebacksForTheSameOriginal = chargebacks.stream()
                .filter(tr -> findChargebackOriginalTransaction(tr, ctx) == originalTransaction
                        && comparator.compare(tr, chargeBackTransaction) < 0)
                .sorted(comparator).toList();

        Map<AllocationType, Money> allocation = new HashMap<>(originalAllocation);
        for (LoanTransaction loanTransaction : chargebacksForTheSameOriginal) {
            Map<AllocationType, Money> temp = calculateChargebackAllocationMap(allocation, loanTransaction.getAmount(),
                    chargeBackAllocationRule.getAllocationTypes(), ctx.getCurrency());
            allocation.keySet().forEach(k -> allocation.put(k, allocation.get(k).minus(temp.get(k))));
        }
        return allocation;
    }

    private void recognizeAmountsAfterChargeback(TransactionCtx ctx, LocalDate transactionDate,
            LoanRepaymentScheduleInstallment installment, Map<AllocationType, Money> chargebackAllocation) {
        Money principal = chargebackAllocation.get(PRINCIPAL);
        if (principal.isGreaterThanZero()) {
            installment.addToCreditedPrincipal(principal.getAmount());
            installment.addToPrincipal(transactionDate, principal);
        }

        MonetaryCurrency currency = ctx.getCurrency();
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

    protected void handleRefund(LoanTransaction loanTransaction, TransactionCtx ctx) {
        MonetaryCurrency currency = ctx.getCurrency();
        Money zero = Money.zero(currency);
        List<LoanTransactionToRepaymentScheduleMapping> transactionMappings = new ArrayList<>();
        Money transactionAmountUnprocessed = loanTransaction.getAmount(currency);

        LoanPaymentAllocationRule paymentAllocationRule = getAllocationRule(loanTransaction);
        Balances balances = new Balances(zero, zero, zero, zero);
        List<PaymentAllocationType> paymentAllocationTypes;
        FutureInstallmentAllocationRule futureInstallmentAllocationRule;
        if (paymentAllocationRule.getTransactionType().isDefault()) {
            // if the allocation rule is not defined then the reverse order of the default allocation rule will be used
            paymentAllocationTypes = new ArrayList<>(paymentAllocationRule.getAllocationTypes());
            Collections.reverse(paymentAllocationTypes);
            futureInstallmentAllocationRule = FutureInstallmentAllocationRule.LAST_INSTALLMENT;
        } else {
            paymentAllocationTypes = paymentAllocationRule.getAllocationTypes();
            futureInstallmentAllocationRule = paymentAllocationRule.getFutureInstallmentAllocationRule();
        }
        Loan loan = loanTransaction.getLoan();
        LoanScheduleProcessingType scheduleProcessingType = loan.getLoanProductRelatedDetail().getLoanScheduleProcessingType();
        if (scheduleProcessingType.isHorizontal()) {
            LinkedHashMap<DueType, List<PaymentAllocationType>> paymentAllocationsMap = paymentAllocationTypes.stream().collect(
                    Collectors.groupingBy(PaymentAllocationType::getDueType, LinkedHashMap::new, mapping(Function.identity(), toList())));

            for (Map.Entry<DueType, List<PaymentAllocationType>> paymentAllocationsEntry : paymentAllocationsMap.entrySet()) {
                transactionAmountUnprocessed = refundTransactionHorizontally(loanTransaction, ctx, transactionAmountUnprocessed,
                        paymentAllocationsEntry.getValue(), futureInstallmentAllocationRule, transactionMappings, balances);
                if (!transactionAmountUnprocessed.isGreaterThanZero()) {
                    break;
                }
            }
        } else if (scheduleProcessingType.isVertical()) {
            for (PaymentAllocationType paymentAllocationType : paymentAllocationTypes) {
                transactionAmountUnprocessed = refundTransactionVertically(loanTransaction, ctx, transactionMappings,
                        transactionAmountUnprocessed, futureInstallmentAllocationRule, balances, paymentAllocationType);
                if (!transactionAmountUnprocessed.isGreaterThanZero()) {
                    break;
                }
            }
        }

        loanTransaction.updateComponents(balances.getAggregatedPrincipalPortion(), balances.getAggregatedInterestPortion(),
                balances.getAggregatedFeeChargesPortion(), balances.getAggregatedPenaltyChargesPortion());
        loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(transactionMappings);
    }

    private void processSingleTransaction(LoanTransaction loanTransaction, final ProgressiveTransactionCtx ctx) {
        boolean isNew = loanTransaction.getId() == null;
        LoanTransaction processTransaction = loanTransaction;
        if (!isNew) {
            // For existing transactions, check if the re-payment breakup (principal, interest, fees, penalties) has
            // changed.
            processTransaction = LoanTransaction.copyTransactionProperties(loanTransaction);
            ctx.getChangedTransactionDetail().getCurrentTransactionToOldId().put(processTransaction, loanTransaction.getId());
        }
        // Reset derived component of new loan transaction and re-process transaction
        processLatestTransaction(processTransaction, ctx);
        if (loanTransaction.isInterestWaiver()) {
            processTransaction.adjustInterestComponent();
        }
        if (isNew) {
            checkRegisteredNewTransaction(loanTransaction, ctx);
        } else {
            updateOrRegisterNewTransaction(loanTransaction, processTransaction, ctx);
        }
    }

    private List<LoanTransaction> processOverpaidTransactions(List<LoanTransaction> overpaidTransactions, ProgressiveTransactionCtx ctx) {
        List<LoanTransaction> remainingTransactions = new ArrayList<>(overpaidTransactions);
        MonetaryCurrency currency = ctx.getCurrency();
        MoneyHolder overpaymentHolder = ctx.getOverpaymentHolder();
        Set<LoanCharge> charges = ctx.getCharges();
        Money zero = Money.zero(currency);
        for (LoanTransaction transaction : overpaidTransactions) {
            Money overpayment = transaction.getOverPaymentPortion(currency);
            Money ctxOverpayment = overpaymentHolder.getMoneyObject();
            Money processAmount = MathUtil.min(ctxOverpayment, overpayment, false);
            if (MathUtil.isEmpty(processAmount)) {
                continue;
            }

            LoanTransaction processTransaction = transaction;
            boolean isNew = transaction.getId() == null;
            if (!isNew) {
                processTransaction = transaction.copyTransactionPropertiesAndMappings();
                ctx.getChangedTransactionDetail().getCurrentTransactionToOldId().put(processTransaction, transaction.getId());
            }
            processTransaction.setOverPayments(overpayment = MathUtil.minus(overpayment, processAmount));
            overpaymentHolder.setMoneyObject(ctxOverpayment = MathUtil.minus(ctxOverpayment, processAmount));

            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings = new ArrayList<>();
            Balances balances = new Balances(zero, zero, zero, zero);

            Money unprocessed = processPeriods(processTransaction, processAmount, charges, transactionMappings, balances, ctx);

            processTransaction.setOverPayments(MathUtil.plus(overpayment, unprocessed));
            overpaymentHolder.setMoneyObject(MathUtil.plus(ctxOverpayment, unprocessed));

            processTransaction.updateComponents(balances.getAggregatedPrincipalPortion(), balances.getAggregatedInterestPortion(),
                    balances.getAggregatedFeeChargesPortion(), balances.getAggregatedPenaltyChargesPortion());
            processTransaction.addLoanTransactionToRepaymentScheduleMappings(transactionMappings);

            if (processTransaction.isInterestWaiver()) {
                processTransaction.adjustInterestComponent();
            }
            if (isNew) {
                processTransaction = checkRegisteredNewTransaction(transaction, ctx);
            } else {
                processTransaction = updateOrRegisterNewTransaction(transaction, processTransaction, ctx);
            }
            remainingTransactions.remove(transaction);
            if (processTransaction.isOverPaid()) {
                remainingTransactions.add(processTransaction);
                break;
            }
        }
        return remainingTransactions;
    }

    private LoanTransaction checkRegisteredNewTransaction(LoanTransaction newTransaction, TransactionCtx ctx) {
        ChangedTransactionDetail changedTransactionDetail = ctx.getChangedTransactionDetail();
        Long oldTransactionId = changedTransactionDetail.getCurrentTransactionToOldId().get(newTransaction);
        if (oldTransactionId != null) {
            LoanTransaction oldTransaction = newTransaction.getLoan().getLoanTransaction(e -> oldTransactionId.equals(e.getId()));
            LoanTransaction applicableTransaction = useOldTransactionIfApplicable(oldTransaction, newTransaction, ctx);
            if (applicableTransaction != null) {
                return applicableTransaction;
            }
        }
        return newTransaction;
    }

    private LoanTransaction updateOrRegisterNewTransaction(LoanTransaction oldTransaction, LoanTransaction newTransaction,
            TransactionCtx ctx) {
        LoanTransaction applicableTransaction = useOldTransactionIfApplicable(oldTransaction, newTransaction, ctx);
        if (applicableTransaction != null) {
            return applicableTransaction;
        }

        newTransaction.copyLoanTransactionRelations(oldTransaction.getLoanTransactionRelations());

        ChangedTransactionDetail changedTransactionDetail = ctx.getChangedTransactionDetail();
        changedTransactionDetail.getNewTransactionMappings().put(oldTransaction.getId(), newTransaction);
        changedTransactionDetail.getCurrentTransactionToOldId().put(newTransaction, oldTransaction.getId());
        return newTransaction;
    }

    @Nullable
    private static LoanTransaction useOldTransactionIfApplicable(LoanTransaction oldTransaction, LoanTransaction newTransaction,
            TransactionCtx ctx) {
        MonetaryCurrency currency = ctx.getCurrency();
        ChangedTransactionDetail changedTransactionDetail = ctx.getChangedTransactionDetail();
        Map<Long, LoanTransaction> newTransactionMappings = changedTransactionDetail.getNewTransactionMappings();
        /*
         * Check if the transaction amounts have changed or was there any transaction for the same date which was
         * reverse-replayed. If so, reverse the original transaction and update changedTransactionDetail accordingly to
         * keep the original order of the transactions.
         */
        boolean alreadyProcessed = newTransactionMappings.values().stream()
                .anyMatch(lt -> !lt.equals(newTransaction) && lt.getTransactionDate().equals(oldTransaction.getTransactionDate()));
        boolean amountMatch = LoanTransaction.transactionAmountsMatch(currency, oldTransaction, newTransaction);
        if (!alreadyProcessed && amountMatch) {
            if (!oldTransaction.getTypeOf().isWaiveCharges()) { // WAIVE_CHARGES is not reprocessed
                oldTransaction
                        .updateLoanTransactionToRepaymentScheduleMappings(newTransaction.getLoanTransactionToRepaymentScheduleMappings());
                oldTransaction.updateLoanChargePaidMappings(newTransaction.getLoanChargesPaid());
            }
            changedTransactionDetail.getCurrentTransactionToOldId().remove(newTransaction);
            newTransactionMappings.remove(oldTransaction.getId());
            return oldTransaction;
        }
        return null;
    }

    protected void createNewTransaction(final LoanTransaction oldTransaction, final LoanTransaction newTransaction,
            final TransactionCtx ctx) {
        oldTransaction.updateExternalId(null);
        oldTransaction.getLoanChargesPaid().clear();

        if (newTransaction.getTypeOf().isInterestRefund()) {
            newTransaction.getLoanTransactionRelations().stream().filter(
                    r -> r.getToTransaction().getTypeOf().isMerchantIssuedRefund() || r.getToTransaction().getTypeOf().isPayoutRefund())
                    .filter(r -> r.getToTransaction().isReversed())
                    .forEach(newRelation -> oldTransaction.getLoanTransactionRelations().stream()
                            .filter(oldRelation -> LoanTransactionRelationTypeEnum.RELATED.equals(oldRelation.getRelationType()))
                            .findFirst().map(oldRelation -> oldRelation.getToTransaction().getId())
                            .ifPresent(oldToTransactionId -> newRelation.setToTransaction(
                                    ctx.getChangedTransactionDetail().getNewTransactionMappings().get(oldToTransactionId))));
        }

        // Adding Replayed relation from newly created transaction to reversed transaction
        newTransaction.getLoanTransactionRelations()
                .add(LoanTransactionRelation.linkToTransaction(newTransaction, oldTransaction, LoanTransactionRelationTypeEnum.REPLAYED));

        // if chargeback is getting reverse-replayed, find the original transaction with CHARGEBACK relation and point
        // the relation to the new chargeback transaction
        if (oldTransaction.getTypeOf().isChargeback()) {
            LoanTransaction originalTransaction = findChargebackOriginalTransaction(newTransaction, ctx);
            Set<LoanTransactionRelation> relations = originalTransaction.getLoanTransactionRelations();
            List<LoanTransactionRelation> oldChargebackRelations = originalTransaction.getLoanTransactionRelations(
                    e -> CHARGEBACK.equals(e.getRelationType()) && e.getToTransaction().equals(oldTransaction));
            oldChargebackRelations.forEach(relations::remove);
            relations.add(LoanTransactionRelation.linkToTransaction(originalTransaction, newTransaction, CHARGEBACK));
        }
        loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(oldTransaction.getLoan(), oldTransaction, "reversed");
        oldTransaction.reverse();
    }

    private void processSingleCharge(LoanCharge loanCharge, MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments,
            LocalDate disbursementDate) {
        loanChargeProcessor.reprocess(currency, disbursementDate, installments, loanCharge);
    }

    @NotNull
    private List<ChangeOperation> createSortedChangeList(final List<LoanTermVariationsData> loanTermVariations,
            final List<LoanTransaction> loanTransactions, final Set<LoanCharge> charges) {
        List<ChangeOperation> changeOperations = new ArrayList<>();
        Map<LoanTermVariationType, List<LoanTermVariationsData>> loanTermVariationsMap = loanTermVariations.stream()
                .collect(Collectors.groupingBy(ltvd -> LoanTermVariationType.fromInt(ltvd.getTermType().getId().intValue())));
        if (loanTermVariationsMap.get(LoanTermVariationType.INTEREST_RATE_FROM_INSTALLMENT) != null) {
            changeOperations.addAll(loanTermVariationsMap.get(LoanTermVariationType.INTEREST_RATE_FROM_INSTALLMENT).stream()
                    .map(ChangeOperation::new).toList());
        }
        if (loanTermVariationsMap.get(LoanTermVariationType.INTEREST_PAUSE) != null) {
            changeOperations
                    .addAll(loanTermVariationsMap.get(LoanTermVariationType.INTEREST_PAUSE).stream().map(ChangeOperation::new).toList());
        }
        if (charges != null) {
            changeOperations.addAll(charges.stream().map(ChangeOperation::new).toList());
        }
        if (loanTransactions != null) {
            changeOperations.addAll(loanTransactions.stream().map(ChangeOperation::new).toList());
        }
        Collections.sort(changeOperations);
        return changeOperations;
    }

    private void handleDisbursement(LoanTransaction disbursementTransaction, TransactionCtx transactionCtx) {
        // TODO: Fix this and enhance EMICalculator to support reamortization and reaging
        if (disbursementTransaction.getLoan().isInterestBearing()) {
            handleDisbursementWithEMICalculator(disbursementTransaction, transactionCtx);
        } else {
            handleDisbursementWithoutEMICalculator(disbursementTransaction, transactionCtx);
        }
    }

    private void handleDisbursementWithEMICalculator(LoanTransaction disbursementTransaction, TransactionCtx transactionCtx) {
        ProgressiveLoanInterestScheduleModel model;
        if (!(transactionCtx instanceof ProgressiveTransactionCtx)
                || (model = ((ProgressiveTransactionCtx) transactionCtx).getModel()) == null) {
            throw new IllegalStateException("TransactionCtx has no model");
        }
        final MathContext mc = MoneyHelper.getMathContext();
        Loan loan = disbursementTransaction.getLoan();
        LoanProductRelatedDetail loanProductRelatedDetail = loan.getLoanRepaymentScheduleDetail();
        Integer installmentAmountInMultiplesOf = loan.getLoanProduct().getInstallmentAmountInMultiplesOf();
        List<LoanRepaymentScheduleInstallment> installments = transactionCtx.getInstallments();
        LocalDate transactionDate = disbursementTransaction.getTransactionDate();
        MonetaryCurrency currency = transactionCtx.getCurrency();
        Money downPaymentAmount = Money.zero(currency);
        if (loanProductRelatedDetail.isEnableDownPayment()) {
            BigDecimal downPaymentAmt = MathUtil.percentageOf(disbursementTransaction.getAmount(),
                    loanProductRelatedDetail.getDisbursedAmountPercentageForDownPayment(), mc);
            if (installmentAmountInMultiplesOf != null) {
                downPaymentAmt = Money.roundToMultiplesOf(downPaymentAmt, installmentAmountInMultiplesOf);
            }
            downPaymentAmount = Money.of(currency, downPaymentAmt);
            LoanRepaymentScheduleInstallment downPaymentInstallment = installments.stream()
                    .filter(i -> i.isDownPayment() && i.getPrincipal(currency).isZero()).findFirst().orElseThrow();
            downPaymentInstallment.addToPrincipal(transactionDate, downPaymentAmount);
        }

        Money amortizableAmount = disbursementTransaction.getAmount(currency).minus(downPaymentAmount);
        emiCalculator.addDisbursement(model, transactionDate, amortizableAmount);

        disbursementTransaction.resetDerivedComponents();
        if (amortizableAmount.isGreaterThanZero()) {
            model.repaymentPeriods().forEach(rm -> {
                LoanRepaymentScheduleInstallment installment = installments.stream().filter(
                        ri -> ri.getDueDate().equals(rm.getDueDate()) && !ri.isDownPayment() && !ri.getDueDate().isBefore(transactionDate))
                        .findFirst().orElse(null);
                if (installment != null) {
                    installment.updatePrincipal(rm.getDuePrincipal().getAmount());
                    installment.updateInterestCharged(rm.getDueInterest().getAmount());
                    installment.updateObligationsMet(currency, transactionDate);
                }
            });
        }
        allocateOverpayment(disbursementTransaction, transactionCtx);
    }

    private void handleDisbursementWithoutEMICalculator(LoanTransaction disbursementTransaction, TransactionCtx transactionCtx) {
        disbursementTransaction.resetDerivedComponents();
        final MathContext mc = MoneyHelper.getMathContext();
        MonetaryCurrency currency = transactionCtx.getCurrency();
        List<LoanRepaymentScheduleInstallment> installments = transactionCtx.getInstallments();
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

        if (amortizableAmount.isGreaterThanZero()) {
            Money increasePrincipalBy = amortizableAmount.dividedBy(noCandidateRepaymentInstallments, MoneyHelper.getMathContext());
            MoneyHolder moneyHolder = new MoneyHolder(amortizableAmount);

            candidateRepaymentInstallments.forEach(i -> {
                Money previousPrincipal = i.getPrincipal(currency);
                Money newPrincipal = previousPrincipal.add(increasePrincipalBy);
                if (installmentAmountInMultiplesOf != null) {
                    newPrincipal = Money.roundToMultiplesOf(newPrincipal, installmentAmountInMultiplesOf);
                }
                i.updatePrincipal(newPrincipal.getAmount());
                moneyHolder.setMoneyObject(moneyHolder.getMoneyObject().minus(newPrincipal).plus(previousPrincipal));
                i.updateObligationsMet(currency, disbursementTransaction.getTransactionDate());
            });
            // Hence the rounding, we might need to amend the last installment amount
            candidateRepaymentInstallments.get(noCandidateRepaymentInstallments - 1)
                    .addToPrincipal(disbursementTransaction.getTransactionDate(), moneyHolder.getMoneyObject());
        }

        allocateOverpayment(disbursementTransaction, transactionCtx);
    }

    private void allocateOverpayment(LoanTransaction loanTransaction, TransactionCtx transactionCtx) {
        MoneyHolder overpaymentHolder = transactionCtx.getOverpaymentHolder();
        Money overpayment = overpaymentHolder.getMoneyObject();
        if (overpayment.isGreaterThanZero()) {
            MonetaryCurrency currency = transactionCtx.getCurrency();
            Money transactionAmount = loanTransaction.getAmount(currency);
            loanTransaction.setOverPayments(MathUtil.min(transactionAmount, overpayment, false));

            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings = new ArrayList<>();
            Money zero = Money.zero(currency);
            Balances balances = new Balances(zero, zero, zero, zero);
            LoanPaymentAllocationRule defaultAllocationRule = getDefaultAllocationRule(loanTransaction.getLoan());
            Money transactionAmountUnprocessed = processPeriods(loanTransaction, overpayment, defaultAllocationRule, Set.of(),
                    transactionMappings, balances, transactionCtx);

            overpaymentHolder.setMoneyObject(transactionAmountUnprocessed);
            loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(transactionMappings);
        }
    }

    protected void handleWriteOff(final LoanTransaction transaction, TransactionCtx ctx) {
        super.handleWriteOff(transaction, ctx.getCurrency(), ctx.getInstallments());
    }

    private List<LoanRepaymentScheduleInstallment> findOverdueInstallmentsBeforeDateSortedByInstallmentNumber(LocalDate targetDate,
            ProgressiveTransactionCtx transactionCtx) {
        return transactionCtx.getInstallments().stream() //
                .filter(installment -> !installment.isDownPayment() && !installment.isAdditional())
                .filter(installment -> installment.isOverdueOn(targetDate))
                .sorted(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).toList();
    }

    private void recalculateInterestForDate(LocalDate targetDate, ProgressiveTransactionCtx ctx) {
        if (ctx.getInstallments() != null && !ctx.getInstallments().isEmpty()) {
            Loan loan = ctx.getInstallments().get(0).getLoan();
            if (loan.isInterestRecalculationEnabled() && !loan.isNpa() && !ctx.isChargedOff()) {

                List<LoanRepaymentScheduleInstallment> overdueInstallmentsSortedByInstallmentNumber = findOverdueInstallmentsBeforeDateSortedByInstallmentNumber(
                        targetDate, ctx);
                if (!overdueInstallmentsSortedByInstallmentNumber.isEmpty()) {
                    List<LoanRepaymentScheduleInstallment> normalInstallments = ctx.getInstallments().stream() //
                            .filter(installment -> !installment.isAdditional() && !installment.isDownPayment()).toList();

                    Optional<LoanRepaymentScheduleInstallment> currentInstallmentOptional = normalInstallments.stream().filter(
                            installment -> installment.getFromDate().isBefore(targetDate) && !installment.getDueDate().isBefore(targetDate))
                            .findAny();

                    // get DUE installment or last installment
                    LoanRepaymentScheduleInstallment lastInstallment = normalInstallments.stream()
                            .max(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).get();
                    LoanRepaymentScheduleInstallment currentInstallment = currentInstallmentOptional.orElse(lastInstallment);

                    Money overDuePrincipal = Money.zero(ctx.getCurrency());
                    Money aggregatedOverDuePrincipal = Money.zero(ctx.getCurrency());
                    for (LoanRepaymentScheduleInstallment processingInstallment : overdueInstallmentsSortedByInstallmentNumber) {
                        // add and subtract outstanding principal
                        if (!overDuePrincipal.isZero()) {
                            adjustOverduePrincipalForInstallment(targetDate, processingInstallment, overDuePrincipal,
                                    aggregatedOverDuePrincipal, ctx);
                        }

                        overDuePrincipal = processingInstallment.getPrincipalOutstanding(ctx.getCurrency());
                        aggregatedOverDuePrincipal = aggregatedOverDuePrincipal.add(overDuePrincipal);
                    }

                    boolean adjustNeeded = !currentInstallment.equals(lastInstallment) || !lastInstallment.isOverdueOn(targetDate);
                    if (adjustNeeded) {
                        adjustOverduePrincipalForInstallment(targetDate, currentInstallment, overDuePrincipal, aggregatedOverDuePrincipal,
                                ctx);
                    }
                }
            }
        }
    }

    private void adjustOverduePrincipalForInstallment(LocalDate currentDate, LoanRepaymentScheduleInstallment currentInstallment,
            Money overduePrincipal, Money aggregatedOverDuePrincipal, ProgressiveTransactionCtx ctx) {
        if (currentInstallment.getLoan().getLoanInterestRecalculationDetails().disallowInterestCalculationOnPastDue()) {
            return;
        }

        LocalDate fromDate = currentInstallment.getFromDate();
        LocalDate toDate = currentInstallment.getDueDate();
        boolean hasUpdate = false;

        if (currentInstallment.getLoan().getLoanInterestRecalculationDetails().getRestFrequencyType().isSameAsRepayment()) {
            // if we have same date for fromDate & last overdue balance change then it means we have the up-to-date
            // model.
            if (ctx.getLastOverdueBalanceChange() == null || fromDate.isAfter(ctx.getLastOverdueBalanceChange())) {
                emiCalculator.addBalanceCorrection(ctx.getModel(), fromDate, overduePrincipal);
                ctx.setLastOverdueBalanceChange(fromDate);
                hasUpdate = true;

                if (currentDate.isAfter(fromDate) && !currentDate.isAfter(toDate)) {
                    emiCalculator.addBalanceCorrection(ctx.getModel(), currentInstallment.getDueDate(),
                            aggregatedOverDuePrincipal.negated());
                    ctx.setLastOverdueBalanceChange(currentInstallment.getDueDate());
                }
            }
        }

        if (currentInstallment.getLoan().getLoanInterestRecalculationDetails().getRestFrequencyType().isDaily()
                // if we have same date for currentDate & last overdue balance change then it meas we have the
                // up-to-date model.
                && !currentDate.equals(ctx.getLastOverdueBalanceChange())) {
            if (ctx.getLastOverdueBalanceChange() == null || currentInstallment.getFromDate().isAfter(ctx.getLastOverdueBalanceChange())) {
                // first overdue hit for installment. setting overdue balance correction from instalment from date.
                emiCalculator.addBalanceCorrection(ctx.getModel(), fromDate, overduePrincipal);
            } else {
                // not the first balance correction on installment period, then setting overdue balance correction from
                // last balance change's current date. previous interest period already has the correct balanec
                // correction
                emiCalculator.addBalanceCorrection(ctx.getModel(), ctx.getLastOverdueBalanceChange(), overduePrincipal);
            }

            // setting negative correction for the period from current date, expecting the overdue balance's full
            // repayment on that day.
            // TODO: we might need to do it outside of this method only for the current date at the end
            if (currentDate.isAfter(currentInstallment.getFromDate()) && !currentDate.isAfter(currentInstallment.getDueDate())) {
                emiCalculator.addBalanceCorrection(ctx.getModel(), currentDate, aggregatedOverDuePrincipal.negated());
                ctx.setLastOverdueBalanceChange(currentDate);
            }
            hasUpdate = true;
        }

        if (hasUpdate) {
            updateInstallmentsPrincipalAndInterestByModel(ctx);
        }
    }

    private void updateInstallmentsPrincipalAndInterestByModel(ProgressiveTransactionCtx ctx) {
        ctx.getModel().repaymentPeriods().forEach(repayment -> {
            LoanRepaymentScheduleInstallment installment = ctx.getInstallments().stream()
                    .filter(ri -> !ri.isDownPayment() && Objects.equals(ri.getFromDate(), repayment.getFromDate())) //
                    .findFirst().orElse(null);
            if (installment != null) {
                installment.updatePrincipal(repayment.getDuePrincipal().getAmount());
                installment.updateInterestCharged(repayment.getDueInterest().getAmount());
                installment.setRecalculatedInterestComponent(true);
            }
        });
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
        AllocationType allocationType = paymentAllocationType.getAllocationType();
        MonetaryCurrency currency = loanTransaction.getLoan().getCurrency();
        Money zero = Money.zero(currency);
        LocalDate transactionDate = loanTransaction.getTransactionDate();
        LoanRepaymentScheduleInstallment.PaymentFunction paymentFunction = currentInstallment.getPaymentFunction(allocationType, action);
        ChargesPaidByFunction chargesPaidByFunction = getChargesPaymentFunction(action);
        Money portion = paymentFunction.accept(transactionDate, transactionAmountUnprocessed);

        switch (allocationType) {
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

        currentInstallment.checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);
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

    private void handleOverpayment(Money overpaymentPortion, LoanTransaction loanTransaction, TransactionCtx transactionCtx) {
        MoneyHolder overpaymentHolder = transactionCtx.getOverpaymentHolder();
        if (MathUtil.isGreaterThanZero(overpaymentPortion)) {
            onLoanOverpayment(loanTransaction, overpaymentPortion);
            overpaymentHolder.setMoneyObject(overpaymentHolder.getMoneyObject().add(overpaymentPortion));
            loanTransaction.setOverPayments(overpaymentPortion);
        } else {
            overpaymentHolder.setMoneyObject(Money.zero(transactionCtx.getCurrency()));
        }
    }

    private void handleChargeOff(final LoanTransaction loanTransaction, final TransactionCtx transactionCtx) {
        if (loanTransaction.getLoan().isProgressiveSchedule()) {
            if (LoanChargeOffBehaviour.ZERO_INTEREST
                    .equals(loanTransaction.getLoan().getLoanProductRelatedDetail().getChargeOffBehaviour())) {
                handleZeroInterestChargeOff(loanTransaction, transactionCtx);
            } else if (LoanChargeOffBehaviour.ACCELERATE_MATURITY
                    .equals(loanTransaction.getLoan().getLoanProductRelatedDetail().getChargeOffBehaviour())) {
                handleAccelerateMaturityChargeOff(loanTransaction, transactionCtx);
            }
        }

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
        if (transactionCtx instanceof ProgressiveTransactionCtx progressiveTransactionCtx) {
            progressiveTransactionCtx.setChargedOff(true);
        }
    }

    private void handleAccelerateMaturityChargeOff(final LoanTransaction loanTransaction, final TransactionCtx transactionCtx) {
        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        final List<LoanRepaymentScheduleInstallment> installments = transactionCtx.getInstallments();
        final Loan loan = loanTransaction.getLoan();
        final LoanRepaymentScheduleInstallment currentInstallment = loan.getRelatedRepaymentScheduleInstallment(transactionDate);

        if (!installments.isEmpty() && transactionDate.isBefore(loan.getMaturityDate())) {
            if (transactionCtx instanceof ProgressiveTransactionCtx progressiveTransactionCtx
                    && loanTransaction.getLoan().isInterestRecalculationEnabled()) {
                final BigDecimal newInterest = emiCalculator
                        .getPeriodInterestTillDate(progressiveTransactionCtx.getModel(), currentInstallment.getDueDate(), transactionDate)
                        .getAmount();
                currentInstallment.updateInterestCharged(newInterest);
            } else {
                final BigDecimal totalInterest = currentInstallment.getInterestOutstanding(transactionCtx.getCurrency()).getAmount();
                final long totalDaysInPeriod = ChronoUnit.DAYS.between(currentInstallment.getFromDate(), currentInstallment.getDueDate());
                final long daysTillChargeOff = ChronoUnit.DAYS.between(currentInstallment.getFromDate(), transactionDate);

                final MathContext mc = MoneyHelper.getMathContext();
                final Money interestTillChargeOff = Money.of(transactionCtx.getCurrency(),
                        totalInterest.divide(BigDecimal.valueOf(totalDaysInPeriod), mc).multiply(BigDecimal.valueOf(daysTillChargeOff), mc),
                        mc);

                currentInstallment.updateInterestCharged(interestTillChargeOff.getAmount());
            }

            currentInstallment.updateDueDate(transactionDate);

            final List<LoanRepaymentScheduleInstallment> futureInstallments = installments.stream()
                    .filter(installment -> transactionDate.isBefore(installment.getDueDate())).toList();

            final BigDecimal futurePrincipal = futureInstallments.stream().map(LoanRepaymentScheduleInstallment::getPrincipal)
                    .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

            final BigDecimal futureFee = futureInstallments.stream().map(LoanRepaymentScheduleInstallment::getFeeChargesCharged)
                    .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

            final BigDecimal futurePenalty = futureInstallments.stream().map(LoanRepaymentScheduleInstallment::getPenaltyCharges)
                    .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

            currentInstallment.updatePrincipal(MathUtil.nullToZero(currentInstallment.getPrincipal()).add(futurePrincipal));

            final List<LoanRepaymentScheduleInstallment> installmentsUpToTransactionDate = installments.stream()
                    .filter(installment -> transactionDate.isAfter(installment.getFromDate()))
                    .collect(Collectors.toCollection(ArrayList::new));

            if (futureFee.compareTo(BigDecimal.ZERO) > 0 || futurePenalty.compareTo(BigDecimal.ZERO) > 0) {
                final Optional<LocalDate> latestDueDate = loan.getCharges().stream()
                        .filter(loanCharge -> loanCharge.isActive() && loanCharge.isNotFullyPaid()).map(LoanCharge::getDueDate)
                        .max(LocalDate::compareTo);

                if (latestDueDate.isPresent()) {
                    final LoanRepaymentScheduleInstallment lastInstallment = installmentsUpToTransactionDate
                            .get(installmentsUpToTransactionDate.size() - 1);

                    final LoanRepaymentScheduleInstallment installmentForCharges = new LoanRepaymentScheduleInstallment(loan,
                            lastInstallment.getInstallmentNumber() + 1, currentInstallment.getDueDate(), latestDueDate.get(),
                            BigDecimal.ZERO, BigDecimal.ZERO, futureFee, futurePenalty, null, null, null, true, false, false);
                    installmentsUpToTransactionDate.add(installmentForCharges);
                }
            }

            loan.updateLoanSchedule(installmentsUpToTransactionDate);
            loan.updateLoanScheduleDependentDerivedFields();
        }
    }

    private void handleZeroInterestChargeOff(final LoanTransaction loanTransaction, final TransactionCtx transactionCtx) {
        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        final List<LoanRepaymentScheduleInstallment> installments = transactionCtx.getInstallments();

        if (!installments.isEmpty()) {
            if (transactionCtx instanceof ProgressiveTransactionCtx progressiveTransactionCtx
                    && loanTransaction.getLoan().isInterestRecalculationEnabled()) {
                installments.stream().filter(installment -> !installment.getFromDate().isAfter(transactionDate)
                        && installment.getDueDate().isAfter(transactionDate)).forEach(installment -> {
                            final BigDecimal newInterest = emiCalculator.getPeriodInterestTillDate(progressiveTransactionCtx.getModel(),
                                    installment.getDueDate(), transactionDate).getAmount();
                            final BigDecimal interestRemoved = installment.getInterestCharged().subtract(newInterest);
                            installment.updatePrincipal(MathUtil.nullToZero(installment.getPrincipal()).add(interestRemoved));
                            installment.updateInterestCharged(newInterest);
                        });
                progressiveTransactionCtx.setChargedOff(true);
            } else {
                calculatePartialPeriodInterest(transactionCtx, transactionDate);
            }

            final MonetaryCurrency currency = loanTransaction.getLoan().getCurrency();

            installments.stream()
                    .filter(installment -> installment.getFromDate().isAfter(transactionDate) && !installment.isObligationsMet())
                    .forEach(installment -> {
                        final BigDecimal interestOutstanding = installment.getInterestOutstanding(currency).getAmount();
                        final BigDecimal updatedInterestCharged = installment.getInterestCharged(currency).getAmount()
                                .subtract(interestOutstanding);

                        if (interestOutstanding.compareTo(BigDecimal.ZERO) > 0) {
                            final BigDecimal newPrincipal = installment.getPrincipal(currency).getAmount().add(interestOutstanding);

                            installment.updatePrincipal(newPrincipal);
                            installment.updateInterestCharged(updatedInterestCharged);
                        }
                    });

            final Money amountToEditLastInstallment = loanTransaction.getLoan().getPrincipal().minus(installments.stream()
                    .filter(i -> !i.isAdditional()).map(LoanRepaymentScheduleInstallment::getPrincipal).reduce(ZERO, BigDecimal::add));

            if (amountToEditLastInstallment.getAmount().signum() != 0) {
                installments.stream().filter(i -> !i.isAdditional() && !i.isObligationsMet()).reduce((first, second) -> second)
                        .ifPresent(last -> last.updatePrincipal(last.getPrincipal().add(amountToEditLastInstallment.getAmount())));
            }
        }
    }

    private void calculatePartialPeriodInterest(final TransactionCtx transactionCtx, final LocalDate chargeOffDate) {
        transactionCtx.getInstallments().stream()
                .filter(installment -> !installment.getFromDate().isAfter(chargeOffDate) && installment.getDueDate().isAfter(chargeOffDate))
                .forEach(installment -> {
                    final BigDecimal totalInterest = installment.getInterestOutstanding(transactionCtx.getCurrency()).getAmount();
                    final long totalDaysInPeriod = ChronoUnit.DAYS.between(installment.getFromDate(), installment.getDueDate());
                    final long daysTillChargeOff = ChronoUnit.DAYS.between(installment.getFromDate(), chargeOffDate);

                    final MathContext mc = MoneyHelper.getMathContext();
                    final Money interestTillChargeOff = Money.of(transactionCtx.getCurrency(), totalInterest
                            .divide(BigDecimal.valueOf(totalDaysInPeriod), mc).multiply(BigDecimal.valueOf(daysTillChargeOff), mc), mc);

                    final BigDecimal interestRemoved = totalInterest.subtract(interestTillChargeOff.getAmount());
                    installment.updatePrincipal(MathUtil.nullToZero(installment.getPrincipal()).add(interestRemoved));
                    installment.updateInterestCharged(interestTillChargeOff.getAmount());
                });
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
            boolean isDue = loanCharge.isDueInPeriod(startDate, installment.getDueDate(),
                    installment.getInstallmentNumber().equals(firstNormalInstallmentNumber));
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

    private Money refundTransactionHorizontally(LoanTransaction loanTransaction, TransactionCtx ctx, Money transactionAmountUnprocessed,
            List<PaymentAllocationType> paymentAllocationTypes, FutureInstallmentAllocationRule futureInstallmentAllocationRule,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Balances balances) {
        MonetaryCurrency currency = ctx.getCurrency();
        Money zero = Money.zero(currency);
        List<LoanRepaymentScheduleInstallment> installments = ctx.getInstallments();
        Set<LoanCharge> charges = ctx.getCharges();
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
                            Money evenPortion = transactionAmountUnprocessed.dividedBy(numberOfInstallments, MoneyHelper.getMathContext());
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

    private Money refundTransactionVertically(LoanTransaction loanTransaction, TransactionCtx ctx,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Money transactionAmountUnprocessed,
            FutureInstallmentAllocationRule futureInstallmentAllocationRule, Balances balances,
            PaymentAllocationType paymentAllocationType) {
        MonetaryCurrency currency = ctx.getCurrency();
        Money zero = Money.zero(currency);
        Money refundedPortion = zero;
        List<LoanRepaymentScheduleInstallment> installments = ctx.getInstallments();
        Set<LoanCharge> charges = ctx.getCharges();
        LoanRepaymentScheduleInstallment currentInstallment = null;
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
                        Money evenPortion = transactionAmountUnprocessed.dividedBy(numberOfInstallments, MoneyHelper.getMathContext());
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
        List<LoanTransactionToRepaymentScheduleMapping> transactionMappings = new ArrayList<>();
        Money zero = Money.zero(transactionCtx.getCurrency());
        Balances balances = new Balances(zero, zero, zero, zero);
        transactionAmountUnprocessed = processPeriods(loanTransaction, transactionAmountUnprocessed, transactionCtx.getCharges(),
                transactionMappings, balances, transactionCtx);

        loanTransaction.updateComponents(balances.getAggregatedPrincipalPortion(), balances.getAggregatedInterestPortion(),
                balances.getAggregatedFeeChargesPortion(), balances.getAggregatedPenaltyChargesPortion());
        loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(transactionMappings);

        handleOverpayment(transactionAmountUnprocessed, loanTransaction, transactionCtx);
    }

    private Money processPeriods(LoanTransaction transaction, Money processAmount, Set<LoanCharge> charges,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Balances balances, TransactionCtx transactionCtx) {
        LoanPaymentAllocationRule allocationRule = getAllocationRule(transaction);
        return processPeriods(transaction, processAmount, allocationRule, charges, transactionMappings, balances, transactionCtx);
    }

    private Money processPeriods(LoanTransaction transaction, Money processAmount, LoanPaymentAllocationRule allocationRule,
            Set<LoanCharge> charges, List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Balances balances,
            TransactionCtx transactionCtx) {
        LoanScheduleProcessingType scheduleProcessingType = transaction.getLoan().getLoanProductRelatedDetail()
                .getLoanScheduleProcessingType();
        if (scheduleProcessingType.isHorizontal()) {
            return processPeriodsHorizontally(transaction, transactionCtx, processAmount, allocationRule, transactionMappings, charges,
                    balances);
        }
        if (scheduleProcessingType.isVertical()) {
            return processPeriodsVertically(transaction, transactionCtx, processAmount, allocationRule, transactionMappings, charges,
                    balances);
        }
        return processAmount;
    }

    private Money processPeriodsHorizontally(LoanTransaction loanTransaction, TransactionCtx transactionCtx,
            Money transactionAmountUnprocessed, LoanPaymentAllocationRule paymentAllocationRule,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Set<LoanCharge> charges, Balances balances) {
        LinkedHashMap<DueType, List<PaymentAllocationType>> paymentAllocationsMap = paymentAllocationRule.getAllocationTypes().stream()
                .collect(Collectors.groupingBy(PaymentAllocationType::getDueType, LinkedHashMap::new,
                        mapping(Function.identity(), toList())));

        for (Map.Entry<DueType, List<PaymentAllocationType>> paymentAllocationsEntry : paymentAllocationsMap.entrySet()) {
            transactionAmountUnprocessed = processAllocationsHorizontally(loanTransaction, transactionCtx, transactionAmountUnprocessed,
                    paymentAllocationsEntry.getValue(), paymentAllocationRule.getFutureInstallmentAllocationRule(), transactionMappings,
                    charges, balances);
        }
        return transactionAmountUnprocessed;
    }

    private Money processAllocationsHorizontally(LoanTransaction loanTransaction, TransactionCtx transactionCtx,
            Money transactionAmountUnprocessed, List<PaymentAllocationType> paymentAllocationTypes,
            FutureInstallmentAllocationRule futureInstallmentAllocationRule,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Set<LoanCharge> charges, Balances balances) {
        if (MathUtil.isEmpty(transactionAmountUnprocessed)) {
            return transactionAmountUnprocessed;
        }

        MonetaryCurrency currency = transactionCtx.getCurrency();
        List<LoanRepaymentScheduleInstallment> installments = transactionCtx.getInstallments();
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
                            Loan loan = loanTransaction.getLoan();
                            if (transactionCtx instanceof ProgressiveTransactionCtx ctx && loan.isInterestBearing()
                                    && loan.getLoanProductRelatedDetail().isInterestRecalculationEnabled() && !ctx.isChargedOff()) {
                                paidPortion = handlingPaymentAllocationForInterestBearingProgressiveLoan(loanTransaction,
                                        transactionAmountUnprocessed, balances, paymentAllocationType, oldestPastDueInstallment, ctx,
                                        loanTransactionToRepaymentScheduleMapping, oldestPastDueInstallmentCharges);
                            } else {
                                paidPortion = processPaymentAllocation(paymentAllocationType, oldestPastDueInstallment, loanTransaction,
                                        transactionAmountUnprocessed, loanTransactionToRepaymentScheduleMapping,
                                        oldestPastDueInstallmentCharges, balances, LoanRepaymentScheduleInstallment.PaymentAction.PAY);
                            }
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
                            Loan loan = loanTransaction.getLoan();
                            if (transactionCtx instanceof ProgressiveTransactionCtx ctx && loan.isInterestBearing()
                                    && loan.getLoanProductRelatedDetail().isInterestRecalculationEnabled() && !ctx.isChargedOff()) {
                                paidPortion = handlingPaymentAllocationForInterestBearingProgressiveLoan(loanTransaction,
                                        transactionAmountUnprocessed, balances, paymentAllocationType, dueInstallment, ctx,
                                        loanTransactionToRepaymentScheduleMapping, dueInstallmentCharges);
                            } else {
                                paidPortion = processPaymentAllocation(paymentAllocationType, dueInstallment, loanTransaction,
                                        transactionAmountUnprocessed, loanTransactionToRepaymentScheduleMapping, dueInstallmentCharges,
                                        balances, LoanRepaymentScheduleInstallment.PaymentAction.PAY);
                            }
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
                            Money evenPortion = transactionAmountUnprocessed.dividedBy(numberOfInstallments, MoneyHelper.getMathContext());
                            // Adjustment might be needed due to the divide operation and the rounding mode
                            Money balanceAdjustment = transactionAmountUnprocessed.minus(evenPortion.multipliedBy(numberOfInstallments));
                            for (LoanRepaymentScheduleInstallment inAdvanceInstallment : inAdvanceInstallments) {
                                Set<LoanCharge> inAdvanceInstallmentCharges = getLoanChargesOfInstallment(charges, inAdvanceInstallment,
                                        firstNormalInstallmentNumber);

                                LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                        transactionMappings, loanTransaction, inAdvanceInstallment, currency);

                                Loan loan = loanTransaction.getLoan();
                                // Adjust the portion for the last installment
                                if (inAdvanceInstallment.equals(inAdvanceInstallments.get(numberOfInstallments - 1))) {
                                    evenPortion = evenPortion.add(balanceAdjustment);
                                }
                                if (transactionCtx instanceof ProgressiveTransactionCtx ctx && loan.isInterestBearing()
                                        && loan.getLoanProductRelatedDetail().isInterestRecalculationEnabled() && !ctx.isChargedOff()) {
                                    paidPortion = handlingPaymentAllocationForInterestBearingProgressiveLoan(loanTransaction, evenPortion,
                                            balances, paymentAllocationType, inAdvanceInstallment, ctx,
                                            loanTransactionToRepaymentScheduleMapping, inAdvanceInstallmentCharges);
                                } else {
                                    paidPortion = processPaymentAllocation(paymentAllocationType, inAdvanceInstallment, loanTransaction,
                                            evenPortion, loanTransactionToRepaymentScheduleMapping, inAdvanceInstallmentCharges, balances,
                                            LoanRepaymentScheduleInstallment.PaymentAction.PAY);
                                }
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

    private Money handlingPaymentAllocationForInterestBearingProgressiveLoan(LoanTransaction loanTransaction,
            Money transactionAmountUnprocessed, Balances balances, PaymentAllocationType paymentAllocationType,
            LoanRepaymentScheduleInstallment installment, ProgressiveTransactionCtx ctx,
            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping, Set<LoanCharge> charges) {
        Money paidPortion;
        ProgressiveLoanInterestScheduleModel model = ctx.getModel();
        LocalDate payDate = loanTransaction.getTransactionDate();

        if (installment.isDownPayment() || installment.getDueDate().isAfter(ctx.getModel().getMaturityDate())) {
            // Skip interest and principal payment processing for down payment period or periods after loan maturity
            // date
            return processPaymentAllocation(paymentAllocationType, installment, loanTransaction, transactionAmountUnprocessed,
                    loanTransactionToRepaymentScheduleMapping, charges, balances, LoanRepaymentScheduleInstallment.PaymentAction.PAY);
        }

        if (DueType.IN_ADVANCE.equals(paymentAllocationType.getDueType())) {
            payDate = calculateNewPayDateInCaseOfInAdvancePayment(loanTransaction, installment);
            updateRepaymentPeriodBalances(paymentAllocationType, installment, ctx, payDate);
        }

        paidPortion = processPaymentAllocation(paymentAllocationType, installment, loanTransaction, transactionAmountUnprocessed,
                loanTransactionToRepaymentScheduleMapping, charges, balances, LoanRepaymentScheduleInstallment.PaymentAction.PAY);

        if (PRINCIPAL.equals(paymentAllocationType.getAllocationType())) {
            emiCalculator.payPrincipal(model, installment.getDueDate(), payDate, paidPortion);
            updateRepaymentPeriods(loanTransaction, ctx);
        } else if (INTEREST.equals(paymentAllocationType.getAllocationType())) {
            emiCalculator.payInterest(model, installment.getDueDate(), payDate, paidPortion);
            updateRepaymentPeriods(loanTransaction, ctx);
        }
        return paidPortion;
    }

    private void updateRepaymentPeriods(LoanTransaction loanTransaction, ProgressiveTransactionCtx ctx) {
        ctx.getModel().repaymentPeriods().forEach(rm -> {
            LoanRepaymentScheduleInstallment installment = ctx.getInstallments().stream()
                    .filter(ri -> ri.getDueDate().equals(rm.getDueDate()) && !ri.isDownPayment()).findFirst().orElse(null);
            if (installment != null) {
                installment.updatePrincipal(rm.getDuePrincipal().getAmount());
                installment.updateInterestCharged(rm.getDueInterest().getAmount());
                installment.updateObligationsMet(ctx.getCurrency(), loanTransaction.getTransactionDate());
            }
        });
    }

    private void updateRepaymentPeriodBalances(PaymentAllocationType paymentAllocationType,
            LoanRepaymentScheduleInstallment inAdvanceInstallment, ProgressiveTransactionCtx ctx, LocalDate payDate) {
        PeriodDueDetails payableDetails = emiCalculator.getDueAmounts(ctx.getModel(), inAdvanceInstallment.getDueDate(), payDate);

        switch (paymentAllocationType) {
            case IN_ADVANCE_INTEREST -> inAdvanceInstallment.updateInterestCharged(payableDetails.getDueInterest().getAmount());
            case IN_ADVANCE_PRINCIPAL -> inAdvanceInstallment.updatePrincipal(payableDetails.getDuePrincipal().getAmount());
            default -> {
            }
        }
    }

    private LocalDate calculateNewPayDateInCaseOfInAdvancePayment(LoanTransaction loanTransaction,
            LoanRepaymentScheduleInstallment inAdvanceInstallment) {
        LoanPreCloseInterestCalculationStrategy strategy = loanTransaction.getLoan().getLoanInterestRecalculationDetails()
                .getPreCloseInterestCalculationStrategy();

        return switch (strategy) {
            case TILL_PRE_CLOSURE_DATE -> loanTransaction.getTransactionDate();
            // TODO use isInPeriod
            case TILL_REST_FREQUENCY_DATE -> loanTransaction.getTransactionDate().isAfter(inAdvanceInstallment.getFromDate()) //
                    && !loanTransaction.getTransactionDate().isAfter(inAdvanceInstallment.getDueDate()) //
                            ? inAdvanceInstallment.getDueDate() //
                            : loanTransaction.getTransactionDate(); //
            case NONE -> throw new IllegalStateException("Unexpected PreClosureInterestCalculationStrategy: NONE");
        };
    }

    @NotNull
    private Set<LoanCharge> getLoanChargesOfInstallment(Set<LoanCharge> charges, LoanRepaymentScheduleInstallment installment,
            int firstInstallmentNumber) {
        boolean isFirstInstallment = installment.getInstallmentNumber().equals(firstInstallmentNumber);
        return charges.stream()
                .filter(loanCharge -> loanCharge.isDueInPeriod(installment.getFromDate(), installment.getDueDate(), isFirstInstallment))
                .collect(Collectors.toSet());
    }

    private Money processPeriodsVertically(LoanTransaction loanTransaction, TransactionCtx ctx, Money transactionAmountUnprocessed,
            LoanPaymentAllocationRule paymentAllocationRule, List<LoanTransactionToRepaymentScheduleMapping> transactionMappings,
            Set<LoanCharge> charges, Balances balances) {
        MonetaryCurrency currency = ctx.getCurrency();
        List<LoanRepaymentScheduleInstallment> installments = ctx.getInstallments();
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
                            Money evenPortion = transactionAmountUnprocessed.dividedBy(numberOfInstallments, MoneyHelper.getMathContext());
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
                    .dividedBy(loanTransaction.getLoanReAgeParameter().getNumberOfInstallments(), MoneyHelper.getMathContext());
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

    protected void calculateAccrualActivity(LoanTransaction transaction, TransactionCtx ctx) {
        super.calculateAccrualActivity(transaction, ctx.getCurrency(), ctx.getInstallments());
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

    @NotNull
    public static LoanPaymentAllocationRule getAllocationRule(LoanTransaction loanTransaction) {
        Loan loan = loanTransaction.getLoan();
        return loan.getPaymentAllocationRules().stream()
                .filter(e -> loanTransaction.getTypeOf() == e.getTransactionType().getLoanTransactionType()).findFirst()
                .orElse(getDefaultAllocationRule(loan));
    }

    @NotNull
    public static LoanPaymentAllocationRule getDefaultAllocationRule(Loan loan) {
        return loan.getPaymentAllocationRules().stream().filter(e -> e.getTransactionType().isDefault()).findFirst().orElseThrow();
    }
}
