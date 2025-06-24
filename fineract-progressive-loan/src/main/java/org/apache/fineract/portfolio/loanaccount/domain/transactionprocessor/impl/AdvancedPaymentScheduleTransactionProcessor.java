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
import static org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper.isInPeriod;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction.accrualAdjustment;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction.accrueTransaction;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum.CHARGEBACK;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.FEE;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.INTEREST;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.PENALTY;
import static org.apache.fineract.portfolio.loanproduct.domain.AllocationType.PRINCIPAL;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.data.OutstandingAmountsDTO;
import org.apache.fineract.portfolio.loanaccount.data.TransactionChangeData;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeOffBehaviour;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCreditAllocationRule;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalculationDetails;
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
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.apache.fineract.portfolio.loanaccount.domain.reaging.LoanReAgeParameter;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.AbstractLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanaccount.service.InterestRefundService;
import org.apache.fineract.portfolio.loanaccount.service.LoanBalanceService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeService;
import org.apache.fineract.portfolio.loanaccount.service.schedule.LoanScheduleComponent;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.apache.fineract.portfolio.loanproduct.calc.data.OutstandingDetails;
import org.apache.fineract.portfolio.loanproduct.calc.data.PeriodDueDetails;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanproduct.calc.data.RepaymentPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.AllocationType;
import org.apache.fineract.portfolio.loanproduct.domain.CreditAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.DueType;
import org.apache.fineract.portfolio.loanproduct.domain.FutureInstallmentAllocationRule;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.apache.fineract.util.LoopContext;
import org.apache.fineract.util.LoopGuard;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class AdvancedPaymentScheduleTransactionProcessor extends AbstractLoanRepaymentScheduleTransactionProcessor {

    public static final String ADVANCED_PAYMENT_ALLOCATION_STRATEGY = "advanced-payment-allocation-strategy";
    public static final String ADVANCED_PAYMENT_ALLOCATION_STRATEGY_NAME = "Advanced payment allocation strategy";

    private final EMICalculator emiCalculator;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final InterestRefundService interestRefundService;
    private final LoanScheduleComponent loanSchedule;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanChargeService loanChargeService;

    public AdvancedPaymentScheduleTransactionProcessor(final EMICalculator emiCalculator, final LoanRepositoryWrapper loanRepositoryWrapper,
            final InterestRefundService interestRefundService, final ExternalIdFactory externalIdFactory,
            final LoanScheduleComponent loanSchedule, final LoanTransactionRepository loanTransactionRepository,
            final LoanChargeValidator loanChargeValidator, final LoanBalanceService loanBalanceService,
            final LoanChargeService loanChargeService) {
        super(externalIdFactory, loanChargeValidator, loanBalanceService);
        this.emiCalculator = emiCalculator;
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.interestRefundService = interestRefundService;
        this.loanTransactionRepository = loanTransactionRepository;
        this.loanSchedule = loanSchedule;
        this.loanChargeService = loanChargeService;
    }

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

    @Transactional
    public Pair<ChangedTransactionDetail, ProgressiveLoanInterestScheduleModel> reprocessProgressiveLoanTransactionsTransactional(
            final LocalDate disbursementDate, final LocalDate targetDate, final List<LoanTransaction> loanTransactions,
            final MonetaryCurrency currency, final List<LoanRepaymentScheduleInstallment> installments, final Set<LoanCharge> charges) {
        return reprocessProgressiveLoanTransactions(disbursementDate, targetDate, loanTransactions, currency, installments, charges);
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
        final Loan loan = loanTransactions.getFirst().getLoan();
        List<LoanTermVariationsData> loanTermVariations = loan.getActiveLoanTermVariations().stream().map(LoanTermVariations::toData)
                .collect(Collectors.toCollection(ArrayList::new));
        final Integer installmentAmountInMultiplesOf = loan.getLoanProductRelatedDetail().getInstallmentAmountInMultiplesOf();
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
        final List<TransactionChangeData> transactionChanges = changedTransactionDetail.getTransactionChanges();

        for (TransactionChangeData change : transactionChanges) {
            LoanTransaction oldTransaction = change.getOldTransaction();
            LoanTransaction newTransaction = change.getNewTransaction();

            if (oldTransaction != null) {
                createNewTransaction(oldTransaction, newTransaction, ctx);
            }
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
        final List<LoanTransaction> transactions = loanTransactionRepository.findNonReversedTransactionsForReprocessingByLoan(loan);
        MonetaryCurrency currency = loan.getLoanRepaymentScheduleDetail().getCurrency();
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        Set<LoanCharge> charges = loan.getActiveCharges();
        return reprocessProgressiveLoanTransactions(loan.getDisbursementDate(), targetDate, transactions, currency, installments, charges)
                .getRight();
    }

    @NotNull
    private static LoanTransaction getProcessedTransaction(final ChangedTransactionDetail changedTransactionDetail,
            final LoanTransaction transaction) {
        return changedTransactionDetail.getTransactionChanges().stream()
                .filter(change -> change.getOldTransaction() != null && change.getOldTransaction().getId() != null
                        && change.getOldTransaction().getId().equals(transaction.getId()))
                .map(TransactionChangeData::getNewTransaction).findFirst().orElse(transaction);
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
    public ChangedTransactionDetail processLatestTransaction(LoanTransaction loanTransaction, TransactionCtx ctx) {
        // If we are behind, we might need to first recalculate interest
        if (ctx instanceof ProgressiveTransactionCtx progressiveTransactionCtx) {
            if (loanTransaction.isRepaymentLikeType() && loanTransaction.isNotReversed()) {
                progressiveTransactionCtx.setPrepayAttempt(calculateIsPrepayAttempt(loanTransaction, progressiveTransactionCtx));
            }
            recalculateInterestForDate(loanTransaction.getTransactionDate(), progressiveTransactionCtx);
        }
        switch (loanTransaction.getTypeOf()) {
            case DISBURSEMENT -> handleDisbursement(loanTransaction, ctx);
            case WRITEOFF -> handleWriteOff(loanTransaction, ctx);
            case REFUND_FOR_ACTIVE_LOAN -> handleRefund(loanTransaction, ctx);
            case CHARGEBACK -> handleChargeback(loanTransaction, ctx);
            case CREDIT_BALANCE_REFUND -> handleCreditBalanceRefund(loanTransaction, ctx);
            case REPAYMENT, MERCHANT_ISSUED_REFUND, PAYOUT_REFUND, GOODWILL_CREDIT, CHARGE_REFUND, CHARGE_ADJUSTMENT, DOWN_PAYMENT,
                    WAIVE_INTEREST, RECOVERY_REPAYMENT, INTEREST_PAYMENT_WAIVER, CAPITALIZED_INCOME_ADJUSTMENT ->
                handleRepayment(loanTransaction, ctx);
            case INTEREST_REFUND -> handleInterestRefund(loanTransaction, ctx);
            case CHARGE_OFF -> handleChargeOff(loanTransaction, ctx);
            case CHARGE_PAYMENT -> handleChargePayment(loanTransaction, ctx);
            case WAIVE_CHARGES -> log.debug("WAIVE_CHARGES transaction will not be processed.");
            case REAMORTIZE -> handleReAmortization(loanTransaction, ctx);
            case REAGE -> handleReAge(loanTransaction, ctx);
            case ACCRUAL_ACTIVITY -> calculateAccrualActivity(loanTransaction, ctx);
            case CAPITALIZED_INCOME -> handleCapitalizedIncome(loanTransaction, ctx);
            case CONTRACT_TERMINATION -> handleContractTermination(loanTransaction, ctx);
            // TODO: Cover rest of the transaction types
            default -> log.warn("Unhandled transaction processing for transaction type: {}", loanTransaction.getTypeOf());
        }
        return ctx.getChangedTransactionDetail();
    }

    private void handleContractTermination(final LoanTransaction loanTransaction, final TransactionCtx transactionCtx) {
        Money principalPortion = Money.zero(transactionCtx.getCurrency());
        Money interestPortion = Money.zero(transactionCtx.getCurrency());
        Money feeChargesPortion = Money.zero(transactionCtx.getCurrency());
        Money penaltyChargesPortion = Money.zero(transactionCtx.getCurrency());

        if (transactionCtx.getInstallments().stream().anyMatch(this::isNotObligationsMet)) {
            handleAccelerateMaturityDate(loanTransaction, transactionCtx);

            final BigDecimal newInterest = getInterestTillChargeOffForPeriod(loanTransaction.getLoan(),
                    loanTransaction.getTransactionDate(), transactionCtx);
            createMissingAccrualTransactionDuringChargeOffIfNeeded(newInterest, loanTransaction, loanTransaction.getTransactionDate(),
                    transactionCtx);

            if (!loanTransaction.getLoan().isInterestBearingAndInterestRecalculationEnabled()) {
                recalculateInstallmentFeeCharges(loanTransaction);
            }

            loanTransaction.resetDerivedComponents();
            // determine how much is outstanding total and breakdown for principal, interest and charges
            for (final LoanRepaymentScheduleInstallment currentInstallment : transactionCtx.getInstallments()) {
                principalPortion = principalPortion.plus(currentInstallment.getPrincipalOutstanding(transactionCtx.getCurrency()));
                interestPortion = interestPortion.plus(currentInstallment.getInterestOutstanding(transactionCtx.getCurrency()));
                feeChargesPortion = feeChargesPortion.plus(currentInstallment.getFeeChargesOutstanding(transactionCtx.getCurrency()));
                penaltyChargesPortion = penaltyChargesPortion
                        .plus(currentInstallment.getPenaltyChargesOutstanding(transactionCtx.getCurrency()));
            }

            loanTransaction.updateComponentsAndTotal(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
        } else {
            loanTransaction.resetDerivedComponents();
            loanTransaction.updateComponentsAndTotal(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
        }

        if (transactionCtx instanceof ProgressiveTransactionCtx progressiveTransactionCtx) {
            progressiveTransactionCtx.setContractTerminated(true);
        }

        if (isAllComponentsZero(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion)
                && loanTransaction.isNotReversed()) {
            loanTransaction.reverse();
            loanTransaction.getLoan().liftContractTerminationSubStatus();

            if (transactionCtx instanceof ProgressiveTransactionCtx progressiveCtx) {
                progressiveCtx.setContractTerminated(false);
            }
        }
    }

    private void handleInterestRefund(final LoanTransaction loanTransaction, final TransactionCtx ctx) {
        final Loan loan = loanTransaction.getLoan();
        final LoanTransaction chargeOffTransaction = loan.getLoanTransactions().stream().filter(t -> t.isChargeOff() && t.isNotReversed())
                .findFirst().orElse(null);
        if (loan.isChargedOff() && chargeOffTransaction != null) {
            final LoanChargeOffBehaviour chargeOffBehaviour = loanTransaction.getLoan().getLoanProductRelatedDetail()
                    .getChargeOffBehaviour();
            if (loan.isProgressiveSchedule() && !LoanChargeOffBehaviour.REGULAR.equals(chargeOffBehaviour)) {
                loanTransaction.updateAmount(getInterestTillChargeOffForPeriod(loan, chargeOffTransaction.getTransactionDate(), ctx));
            } else {
                Money interestPortion = Money.zero(ctx.getCurrency());
                for (final LoanRepaymentScheduleInstallment currentInstallment : ctx.getInstallments()) {
                    interestPortion = interestPortion.plus(currentInstallment.getInterestCharged(ctx.getCurrency()));
                }
                loanTransaction.updateAmount(interestPortion.getAmount());
            }
            if (ctx instanceof ProgressiveTransactionCtx progCtx) {
                progCtx.setSumOfInterestRefundAmount(progCtx.getSumOfInterestRefundAmount().add(loanTransaction.getAmount()));
            }
        } else {
            if (ctx instanceof ProgressiveTransactionCtx progCtx) {
                final Money interestBeforeRefund = emiCalculator.getSumOfDueInterestsOnDate(progCtx.getModel(),
                        loanTransaction.getDateOf());
                final List<Long> unmodifiedTransactionIds = progCtx.getAlreadyProcessedTransactions().stream()
                        .filter(LoanTransaction::isNotReversed).map(AbstractPersistableCustom::getId).toList();
                final List<LoanTransaction> modifiedTransactions = new ArrayList<>(progCtx.getAlreadyProcessedTransactions().stream()
                        .filter(LoanTransaction::isNotReversed).filter(tr -> tr.getId() == null).toList());
                if (!modifiedTransactions.isEmpty()) {
                    final Money interestAfterRefund = interestRefundService.totalInterestByTransactions(this, loan.getId(),
                            loanTransaction.getDateOf(), modifiedTransactions, unmodifiedTransactionIds);
                    final Money newAmount = interestBeforeRefund.minus(progCtx.getSumOfInterestRefundAmount()).minus(interestAfterRefund);
                    loanTransaction.updateAmount(newAmount.getAmount());
                }
                progCtx.setSumOfInterestRefundAmount(progCtx.getSumOfInterestRefundAmount().add(loanTransaction.getAmount()));
            }
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
        Integer installmentAmountInMultiplesOf = loanTransaction.getLoan().getLoanProductRelatedDetail()
                .getInstallmentAmountInMultiplesOf();

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

    protected void handleCreditBalanceRefund(LoanTransaction loanTransaction, TransactionCtx ctx) {
        MonetaryCurrency currency = ctx.getCurrency();
        List<LoanRepaymentScheduleInstallment> installments = ctx.getInstallments();
        MoneyHolder overpaymentHolder = ctx.getOverpaymentHolder();

        if (ctx instanceof ProgressiveTransactionCtx progressiveTransactionCtx
                && loanTransaction.getLoan().isInterestBearingAndInterestRecalculationEnabled()) {
            var model = progressiveTransactionCtx.getModel();

            // Copy and paste Logic from super.handleCreditBalanceRefund
            loanTransaction.resetDerivedComponents();
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings = new ArrayList<>();
            final Comparator<LoanRepaymentScheduleInstallment> byDate = Comparator.comparing(LoanRepaymentScheduleInstallment::getDueDate);
            List<LoanRepaymentScheduleInstallment> installmentToBeProcessed = installments.stream().filter(i -> !i.isDownPayment())
                    .sorted(byDate).toList();
            final Money zeroMoney = Money.zero(currency);
            Money transactionAmount = loanTransaction.getAmount(currency);
            Money principalPortion = MathUtil.negativeToZero(loanTransaction.getAmount(currency).minus(overpaymentHolder.getMoneyObject()));
            Money repaidAmount = MathUtil.negativeToZero(transactionAmount.minus(principalPortion));
            loanTransaction.setOverPayments(repaidAmount);
            overpaymentHolder.setMoneyObject(overpaymentHolder.getMoneyObject().minus(repaidAmount));
            loanTransaction.updateComponents(principalPortion, zeroMoney, zeroMoney, zeroMoney);

            if (principalPortion.isGreaterThanZero()) {
                final LocalDate transactionDate = loanTransaction.getTransactionDate();
                boolean loanTransactionMapped = false;
                LocalDate pastDueDate = null;
                for (final LoanRepaymentScheduleInstallment currentInstallment : installmentToBeProcessed) {
                    pastDueDate = currentInstallment.getDueDate();
                    if (!currentInstallment.isAdditional() && DateUtils.isAfter(currentInstallment.getDueDate(), transactionDate)) {

                        emiCalculator.creditPrincipal(model, transactionDate, transactionAmount);
                        updateRepaymentPeriods(loanTransaction, progressiveTransactionCtx);

                        if (repaidAmount.isGreaterThanZero()) {
                            emiCalculator.payPrincipal(model, currentInstallment.getDueDate(), transactionDate, repaidAmount);
                            updateRepaymentPeriods(loanTransaction, progressiveTransactionCtx);
                            currentInstallment.payPrincipalComponent(transactionDate, repaidAmount);
                            transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction,
                                    currentInstallment, repaidAmount, zeroMoney, zeroMoney, zeroMoney));
                        }
                        loanTransactionMapped = true;
                        break;

                        // If already exists an additional installment just update the due date and
                        // principal from the Loan chargeback / CBR transaction
                    } else if (currentInstallment.isAdditional()) {
                        if (DateUtils.isAfter(transactionDate, currentInstallment.getDueDate())) {
                            currentInstallment.updateDueDate(transactionDate);
                        }

                        currentInstallment.updateCredits(transactionDate, transactionAmount);
                        if (repaidAmount.isGreaterThanZero()) {
                            currentInstallment.payPrincipalComponent(loanTransaction.getTransactionDate(), repaidAmount);
                            transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction,
                                    currentInstallment, repaidAmount, zeroMoney, zeroMoney, zeroMoney));
                        }
                        loanTransactionMapped = true;
                        break;
                    }
                }

                // New installment will be added (N+1 scenario)
                if (!loanTransactionMapped) {
                    if (transactionDate.equals(pastDueDate)) {
                        // Transaction is on Maturity date, no additional installment is needed
                        LoanRepaymentScheduleInstallment currentInstallment = installmentToBeProcessed.getLast();

                        emiCalculator.creditPrincipal(model, transactionDate, transactionAmount);
                        updateRepaymentPeriods(loanTransaction, progressiveTransactionCtx);

                        if (repaidAmount.isGreaterThanZero()) {
                            emiCalculator.payPrincipal(model, currentInstallment.getDueDate(), transactionDate, repaidAmount);
                            updateRepaymentPeriods(loanTransaction, progressiveTransactionCtx);
                            currentInstallment.payPrincipalComponent(transactionDate, repaidAmount);
                            transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction,
                                    currentInstallment, repaidAmount, zeroMoney, zeroMoney, zeroMoney));
                        }
                    } else {
                        // transaction is after maturity date, create an additional installment
                        Loan loan = loanTransaction.getLoan();
                        LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(loan, (installments.size() + 1),
                                pastDueDate, transactionDate, transactionAmount.getAmount(), zeroMoney.getAmount(), zeroMoney.getAmount(),
                                zeroMoney.getAmount(), false, null);
                        installment.markAsAdditional();
                        installment.addToCreditedPrincipal(transactionAmount.getAmount());
                        loan.addLoanRepaymentScheduleInstallment(installment);

                        if (repaidAmount.isGreaterThanZero()) {
                            installment.payPrincipalComponent(loanTransaction.getTransactionDate(), repaidAmount);
                            transactionMappings.add(LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, installment,
                                    repaidAmount, zeroMoney, zeroMoney, zeroMoney));
                        }
                    }
                }

                loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(transactionMappings);
            }
        } else {
            super.handleCreditBalanceRefund(loanTransaction, currency, installments, overpaymentHolder);
        }
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
            final List<TransactionChangeData> transactionChanges = changedTransactionDetail.getTransactionChanges();
            if (chargebackId == null) {
                // the chargeback transaction was changed, so we need to look it up from the ctx.
                chargebackId = transactionChanges.stream().filter(change -> change.getNewTransaction().equals(chargebackTransaction))
                        .flatMap(change -> Optional.ofNullable(change.getOldTransaction()).map(AbstractPersistableCustom::getId).stream())
                        .findFirst().orElse(null);
            }

            Long toId = chargebackId;
            Optional<LoanTransaction> fromTransaction = changedTransactionDetail.getTransactionChanges().stream()
                    .map(TransactionChangeData::getNewTransaction)
                    .filter(tr -> tr.getLoanTransactionRelations().stream().anyMatch(hasMatchingToLoanTransaction(toId, CHARGEBACK))
                            || tr.getLoanTransactionRelations().stream()
                                    .anyMatch(this.hasMatchingToLoanTransaction(chargebackTransaction, CHARGEBACK)))
                    .findFirst();
            if (fromTransaction.isPresent()) {
                return fromTransaction.get();
            }
        }
        Long toId = chargebackId;
        // if the original transaction is not in the ctx, then it means that it has not changed during reverse replay
        Optional<LoanTransaction> fromTransaction = chargebackTransaction.getLoan().getLoanTransactions().stream()
                .filter(tr -> tr.getLoanTransactionRelations().stream().anyMatch(this.hasMatchingToLoanTransaction(toId, CHARGEBACK))
                        || tr.getLoanTransactionRelations().stream()
                                .anyMatch(this.hasMatchingToLoanTransaction(chargebackTransaction, CHARGEBACK)))
                .findFirst();
        if (fromTransaction.isEmpty()) {
            throw new RuntimeException("Chargeback transaction must have an original transaction");
        }
        return fromTransaction.get();
    }

    private Map<AllocationType, Money> calculateChargebackAllocationMapPrincipalOnly(Money transactionAmount, MonetaryCurrency currency) {
        Map<AllocationType, Money> chargebackAllocation = new HashMap<>();
        chargebackAllocation.put(PRINCIPAL, transactionAmount);
        chargebackAllocation.put(INTEREST, Money.zero(currency));
        chargebackAllocation.put(PENALTY, Money.zero(currency));
        chargebackAllocation.put(FEE, Money.zero(currency));
        return chargebackAllocation;
    }

    /**
     * Finds and returns installment from the provided context which has the given due date.
     *
     * @param ctx
     *            Progressive transaction context
     * @param dueDate
     *            installment's due date
     * @return found installment or null if there is no matching installment.
     */
    private LoanRepaymentScheduleInstallment getInstallmentWithDueDate(@NotNull final ProgressiveTransactionCtx ctx,
            @NotNull final LocalDate dueDate) {
        return ctx.getInstallments().stream().filter(i -> i.getDueDate().isEqual(dueDate)).findAny().orElse(null);
    }

    /**
     * Finds and returns installment from the provided context which has from date <= given date < due date.
     *
     * @param ctx
     *            Progressive transaction context
     * @param date
     *            the given date
     * @return found installment or null if there is no matching installment.
     */
    private LoanRepaymentScheduleInstallment getRelatedInstallmentByFromDateInclusiveAndToDateExclusive(
            @NotNull final ProgressiveTransactionCtx ctx, @NotNull final LocalDate date) {
        if (ctx.getInstallments() == null) {
            return null;
        }
        return ctx.getInstallments().stream().filter(i -> !date.isBefore(i.getFromDate()) && date.isBefore(i.getDueDate())).findAny()
                .orElse(null);
    }

    protected void processCreditTransactionWithEmiCalculator(LoanTransaction loanTransaction, ProgressiveTransactionCtx ctx) {

        ProgressiveLoanInterestScheduleModel model = ctx.getModel();
        MonetaryCurrency currency = ctx.getCurrency();
        loanTransaction.resetDerivedComponents();
        Money transactionAmount = loanTransaction.getAmount(currency);
        Money totalOverpaid = ctx.getOverpaymentHolder().getMoneyObject();
        loanTransaction.setOverPayments(totalOverpaid);
        if (!transactionAmount.isGreaterThanZero()) {
            return;
        }
        if (!loanTransaction.isChargeback()) {
            throw new RuntimeException("Unsupported transaction " + loanTransaction.getTypeOf().name());
        }
        Map<AllocationType, Money> chargebackAllocation;

        if (hasNoCustomCreditAllocationRule(loanTransaction)) {
            // whole amount should allocate as principal no need to check previous chargebacks.
            chargebackAllocation = calculateChargebackAllocationMapPrincipalOnly(transactionAmount, currency);
        } else {
            chargebackAllocation = calculateChargebackAllocationMapByCreditAllocationRule(loanTransaction, ctx);
        }

        loanTransaction.updateComponents(chargebackAllocation.get(PRINCIPAL), chargebackAllocation.get(INTEREST),
                chargebackAllocation.get(FEE), chargebackAllocation.get(PENALTY));

        LocalDate lastInstallmentDueDate = model.getMaturityDate();
        if (!loanTransaction.getTransactionDate().isAfter(lastInstallmentDueDate)) {
            // handle charge back before or on last installments due date

            if (chargebackAllocation.get(PRINCIPAL).isGreaterThanZero()) {
                emiCalculator.creditPrincipal(model, loanTransaction.getTransactionDate(), chargebackAllocation.get(PRINCIPAL));
            }

            if (chargebackAllocation.get(INTEREST).isGreaterThanZero()) {
                emiCalculator.creditInterest(model, loanTransaction.getTransactionDate(), chargebackAllocation.get(INTEREST));
            }

            // update repayment periods until maturity date, for principal and interest portions
            updateRepaymentPeriods(loanTransaction, ctx);

            // search last instalment if transaction is posted on last installment's due date
            // otherwise search installment where transaction date is in from date (inclusive) to due date (exclusive)
            // interval
            LoanRepaymentScheduleInstallment instalment = lastInstallmentDueDate.isEqual(loanTransaction.getTransactionDate())
                    ? getInstallmentWithDueDate(ctx, loanTransaction.getTransactionDate())
                    : getRelatedInstallmentByFromDateInclusiveAndToDateExclusive(ctx, loanTransaction.getTransactionDate());
            // recognize fees and penalties only because principal and interest portions are already updated.
            recognizeFeePenaltiesAmountsAfterChargeback(ctx, instalment, chargebackAllocation);
        } else {
            // (N+1)th installment case for post maturity date charge back
            LoanRepaymentScheduleInstallment instalment = ctx.getInstallments().stream()
                    .filter(LoanRepaymentScheduleInstallment::isAdditional).findAny()
                    .orElseGet(() -> createAdditionalInstalment(loanTransaction, ctx));
            // recognize principal, interest, fees and penalties portions
            recognizeAmountsAfterChargeback(ctx, loanTransaction.getTransactionDate(), instalment, chargebackAllocation);
            if (instalment.getDueDate().isBefore(loanTransaction.getTransactionDate())) {
                instalment.updateDueDate(loanTransaction.getTransactionDate());
            }
        }

        allocateOverpayment(loanTransaction, ctx);
    }

    private LoanRepaymentScheduleInstallment createAdditionalInstalment(LoanTransaction loanTransaction, ProgressiveTransactionCtx ctx) {
        LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(loanTransaction.getLoan(),
                (ctx.getInstallments().size() + 1), ctx.getModel().getMaturityDate(), loanTransaction.getTransactionDate(), ZERO, ZERO,
                ZERO, ZERO, false, null);
        installment.markAsAdditional();
        loanTransaction.getLoan().addLoanRepaymentScheduleInstallment(installment);
        return installment;
    }

    private Map<AllocationType, Money> calculateChargebackAllocationMapByCreditAllocationRule(LoanTransaction loanTransaction,
            TransactionCtx ctx) {
        MonetaryCurrency currency = ctx.getCurrency();
        LoanTransaction originalTransaction = findChargebackOriginalTransaction(loanTransaction, ctx);
        // get the original allocation from the original transaction
        Map<AllocationType, Money> originalAllocationNotAdjusted = getOriginalAllocation(originalTransaction, currency);
        LoanCreditAllocationRule chargebackAllocationRule = getChargebackAllocationRules(loanTransaction);

        // if there were earlier chargebacks then let's calculate the remaining amounts for each portion
        Map<AllocationType, Money> originalAllocation = adjustOriginalAllocationWithFormerChargebacks(originalTransaction,
                originalAllocationNotAdjusted, loanTransaction, ctx, chargebackAllocationRule);

        // calculate the current chargeback allocation
        return calculateChargebackAllocationMap(originalAllocation, loanTransaction.getAmount(currency).getAmount(),
                chargebackAllocationRule.getAllocationTypes(), currency);
    }

    protected void processCreditTransaction(LoanTransaction loanTransaction, TransactionCtx ctx) {
        if (loanTransaction.getLoan().isInterestBearingAndInterestRecalculationEnabled()) {
            processCreditTransactionWithEmiCalculator(loanTransaction, (ProgressiveTransactionCtx) ctx);
        } else if (hasNoCustomCreditAllocationRule(loanTransaction)) {
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
            // get the original allocation from the original transaction
            Map<AllocationType, Money> originalAllocationNotAdjusted = getOriginalAllocation(originalTransaction, currency);
            LoanCreditAllocationRule chargebackAllocationRule = getChargebackAllocationRules(loanTransaction);

            // if there were earlier chargebacks then let's calculate the remaining amounts for each portion
            Map<AllocationType, Money> originalAllocation = adjustOriginalAllocationWithFormerChargebacks(originalTransaction,
                    originalAllocationNotAdjusted, loanTransaction, ctx, chargebackAllocationRule);

            // calculate the current chargeback allocation
            Map<AllocationType, Money> chargebackAllocation = calculateChargebackAllocationMap(originalAllocation,
                    transactionAmount.getAmount(), chargebackAllocationRule.getAllocationTypes(), currency);

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
                    LoanRepaymentScheduleInstallment currentInstallment = ctx.getInstallments().getLast();
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
            Map<AllocationType, Money> originalAllocation, LoanTransaction chargebackTransaction, TransactionCtx ctx,
            LoanCreditAllocationRule chargebackAllocationRule) {
        // these are the list of existing transactions
        List<LoanTransaction> allTransactions = new ArrayList<>(chargebackTransaction.getLoan().getLoanTransactions());

        // Remove the current chargeback from the list
        allTransactions.remove(chargebackTransaction);
        final ChangedTransactionDetail changedTransactionDetail = ctx.getChangedTransactionDetail();
        if (changedTransactionDetail != null) {
            final List<TransactionChangeData> transactionChanges = changedTransactionDetail.getTransactionChanges();

            transactionChanges.stream().filter(change -> change.getNewTransaction().equals(chargebackTransaction))
                    .map(TransactionChangeData::getOldTransaction).filter(Objects::nonNull).findFirst().ifPresent(allTransactions::remove);

            // Add the replayed transactions and remove their old version before the replay
            for (TransactionChangeData change : transactionChanges) {
                LoanTransaction oldTransaction = change.getOldTransaction();
                LoanTransaction newTransaction = change.getNewTransaction();

                if (oldTransaction != null) {
                    allTransactions.removeIf(tr -> Objects.equals(tr.getId(), oldTransaction.getId()));
                }
                allTransactions.add(newTransaction);
            }
        }

        // keep only the chargeback transactions
        List<LoanTransaction> chargebacks = allTransactions.stream().filter(LoanTransaction::isChargeback).toList();

        // let's figure out the original transaction for these chargebacks, and order them by ascending order
        Comparator<LoanTransaction> comparator = LoanTransactionComparator.INSTANCE;
        List<LoanTransaction> chargebacksForTheSameOriginal = chargebacks.stream()
                .filter(tr -> findChargebackOriginalTransaction(tr, ctx) == originalTransaction
                        && comparator.compare(tr, chargebackTransaction) < 0)
                .sorted(comparator).toList();

        Map<AllocationType, Money> allocation = new HashMap<>(originalAllocation);
        for (LoanTransaction loanTransaction : chargebacksForTheSameOriginal) {
            Map<AllocationType, Money> temp = calculateChargebackAllocationMap(allocation, loanTransaction.getAmount(),
                    chargebackAllocationRule.getAllocationTypes(), ctx.getCurrency());
            allocation.keySet().forEach(k -> allocation.put(k, allocation.get(k).minus(temp.get(k))));
        }
        return allocation;
    }

    private void recognizeFeePenaltiesAmountsAfterChargeback(TransactionCtx ctx, LoanRepaymentScheduleInstallment installment,
            Map<AllocationType, Money> chargebackAllocation) {
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

    private void recognizeAmountsAfterChargeback(final TransactionCtx ctx, final LocalDate transactionDate,
            final LoanRepaymentScheduleInstallment installment, final Map<AllocationType, Money> chargebackAllocation) {
        final Money principal = chargebackAllocation.get(PRINCIPAL);
        if (principal != null && principal.isGreaterThanZero()) {
            installment.addToCreditedPrincipal(principal.getAmount());
            installment.addToPrincipal(transactionDate, principal);
        }

        final Money interest = chargebackAllocation.get(INTEREST);
        if (interest != null && interest.isGreaterThanZero()) {
            installment.addToCreditedInterest(interest.getAmount());
            installment.addToInterest(transactionDate, interest);
        }
        recognizeFeePenaltiesAmountsAfterChargeback(ctx, installment, chargebackAllocation);
    }

    @NotNull
    private LoanCreditAllocationRule getChargebackAllocationRules(LoanTransaction loanTransaction) {
        return loanTransaction.getLoan().getCreditAllocationRules().stream()
                .filter(tr -> tr.getTransactionType().equals(CreditAllocationTransactionType.CHARGEBACK)).findFirst().orElseThrow();
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

    private Predicate<LoanTransactionRelation> hasMatchingToLoanTransaction(LoanTransaction loanTransaction,
            LoanTransactionRelationTypeEnum typeEnum) {
        return relation -> relation.getRelationType().equals(typeEnum) && relation.getToTransaction() == loanTransaction;
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
            ctx.getChangedTransactionDetail().addTransactionChange(new TransactionChangeData(loanTransaction, processTransaction));
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
                ctx.getChangedTransactionDetail().addTransactionChange(new TransactionChangeData(transaction, processTransaction));
            }
            processTransaction.setOverPayments(overpayment = MathUtil.minus(overpayment, processAmount));
            overpaymentHolder.setMoneyObject(ctxOverpayment = MathUtil.minus(ctxOverpayment, processAmount));

            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings = new ArrayList<>();
            Balances balances = new Balances(zero, zero, zero, zero);

            Money unprocessed = processPeriods(processTransaction, processAmount, transactionMappings, balances, ctx);

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
        Optional<TransactionChangeData> transactionChange = changedTransactionDetail.getTransactionChanges().stream()
                .filter(change -> change.getNewTransaction().equals(newTransaction)).findFirst();

        if (transactionChange.isPresent()) {
            LoanTransaction oldTransaction = transactionChange.get().getOldTransaction();
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

        ctx.getChangedTransactionDetail().addTransactionChange(new TransactionChangeData(oldTransaction, newTransaction));
        return newTransaction;
    }

    @Nullable
    private static LoanTransaction useOldTransactionIfApplicable(LoanTransaction oldTransaction, LoanTransaction newTransaction,
            TransactionCtx ctx) {
        MonetaryCurrency currency = ctx.getCurrency();
        ChangedTransactionDetail changedTransactionDetail = ctx.getChangedTransactionDetail();
        /*
         * Check if the transaction amounts have changed or was there any transaction for the same date which was
         * reverse-replayed. If so, reverse the original transaction and update changedTransactionDetail accordingly to
         * keep the original order of the transactions.
         */
        boolean alreadyProcessed = changedTransactionDetail.getTransactionChanges().stream().map(TransactionChangeData::getNewTransaction)
                .anyMatch(lt -> !lt.equals(newTransaction) && lt.getTransactionDate().equals(oldTransaction.getTransactionDate()));
        boolean amountMatch = LoanTransaction.transactionAmountsMatch(currency, oldTransaction, newTransaction);
        if (!alreadyProcessed && amountMatch) {
            if (!oldTransaction.getTypeOf().isWaiveCharges()) { // WAIVE_CHARGES is not reprocessed
                oldTransaction
                        .updateLoanTransactionToRepaymentScheduleMappings(newTransaction.getLoanTransactionToRepaymentScheduleMappings());
                oldTransaction.updateLoanChargePaidMappings(newTransaction.getLoanChargesPaid());
            }
            changedTransactionDetail.removeTransactionChange(newTransaction);
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
                            .flatMap(oldToTransactionId -> ctx.getChangedTransactionDetail().getTransactionChanges().stream()
                                    .filter(change -> change.getOldTransaction() != null && change.getOldTransaction().getId() != null
                                            && change.getOldTransaction().getId().equals(oldToTransactionId))
                                    .map(TransactionChangeData::getNewTransaction).findFirst())
                            .ifPresent(newRelation::setToTransaction));
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
        final Loan loan = disbursementTransaction.getLoan();
        if (loan.isInterestBearing()) {
            handleDisbursementWithEMICalculator(disbursementTransaction, transactionCtx);
        } else {
            handleDisbursementWithoutEMICalculator(disbursementTransaction, transactionCtx);
        }
        if (!disbursementTransaction.getLoan().isInterestBearingAndInterestRecalculationEnabled()) {
            recalculateInstallmentFeeCharges(disbursementTransaction);
        }
    }

    private void recalculateInstallmentFeeCharges(final LoanTransaction loanTransaction) {
        final Loan loan = loanTransaction.getLoan();
        final List<LoanCharge> loanInstallmentFeeCharges = loan.getActiveCharges().stream()
                .filter(c -> c.isInstalmentFee() && c.getSubmittedOnDate().isBefore(loanTransaction.getTransactionDate())).toList();
        loanChargeService.recalculateParticularChargesAfterTransactionOccurs(loan, loanInstallmentFeeCharges,
                loanTransaction.getTransactionDate());
        if (!loanInstallmentFeeCharges.isEmpty()) {
            final List<LoanRepaymentScheduleInstallment> installmentsToUpdate = loan.getRepaymentScheduleInstallments().stream()
                    .filter(i -> i.isNotFullyPaidOff() && !i.isAdditional()).toList();
            for (LoanRepaymentScheduleInstallment installment : installmentsToUpdate) {
                if (installment.isDownPayment()) {
                    continue;
                }
                final BigDecimal newFee = installment.getInstallmentCharges().stream().map(LoanInstallmentCharge::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (newFee.compareTo(BigDecimal.ZERO) != 0) {
                    installment.setFeeChargesCharged(newFee);
                }
            }
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
        Integer installmentAmountInMultiplesOf = loanProductRelatedDetail.getInstallmentAmountInMultiplesOf();
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

        boolean needsNPlusOneInstallment = installments.stream()
                .filter(i -> i.getDueDate().isAfter(transactionDate) || i.getDueDate().isEqual(transactionDate))
                .filter(i -> !i.isDownPayment() && !i.isAdditional()).findAny().isEmpty();

        if (needsNPlusOneInstallment) {
            // CREATE N+1 installment like the non-EMI version does
            LoanRepaymentScheduleInstallment newInstallment = new LoanRepaymentScheduleInstallment(disbursementTransaction.getLoan(),
                    installments.size() + 1, disbursementTransaction.getTransactionDate(), disbursementTransaction.getTransactionDate(),
                    Money.zero(currency).getAmount(), Money.zero(currency).getAmount(), Money.zero(currency).getAmount(),
                    Money.zero(currency).getAmount(), true, null);
            newInstallment.updatePrincipal(amortizableAmount.getAmount());
            newInstallment.markAsAdditional();
            disbursementTransaction.getLoan().addLoanRepaymentScheduleInstallment(newInstallment);
            installments.add(newInstallment);
        }

        disbursementTransaction.resetDerivedComponents();
        recalculateRepaymentPeriodsWithEMICalculation(amortizableAmount, model, installments, transactionDate, currency);
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
        if (candidateRepaymentInstallments.isEmpty()) {
            LoanRepaymentScheduleInstallment newInstallment;
            if (installments.stream().filter(LoanRepaymentScheduleInstallment::isAdditional).findAny().isEmpty()) {
                newInstallment = new LoanRepaymentScheduleInstallment(disbursementTransaction.getLoan(), installments.size() + 1,
                        disbursementTransaction.getTransactionDate(), disbursementTransaction.getTransactionDate(),
                        Money.zero(currency).getAmount(), Money.zero(currency).getAmount(), Money.zero(currency).getAmount(),
                        Money.zero(currency).getAmount(), false, null);
                newInstallment.markAsAdditional();
                disbursementTransaction.getLoan().addLoanRepaymentScheduleInstallment(newInstallment);
                installments.add(newInstallment);
            } else {
                newInstallment = installments.stream().filter(LoanRepaymentScheduleInstallment::isAdditional).findFirst().orElseThrow();
                newInstallment.updateDueDate(disbursementTransaction.getTransactionDate());
            }
            candidateRepaymentInstallments = Collections.singletonList(newInstallment);
        }

        LoanProductRelatedDetail loanProductRelatedDetail = disbursementTransaction.getLoan().getLoanRepaymentScheduleDetail();
        Integer installmentAmountInMultiplesOf = disbursementTransaction.getLoan().getLoanProductRelatedDetail()
                .getInstallmentAmountInMultiplesOf();
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

        recalculateRepaymentInstallmentsWithoutEMICalculation(disbursementTransaction, amortizableAmount, candidateRepaymentInstallments,
                currency, installmentAmountInMultiplesOf);
        allocateOverpayment(disbursementTransaction, transactionCtx);
    }

    private void handleCapitalizedIncome(LoanTransaction capitalizedIncomeTransaction, TransactionCtx transactionCtx) {
        if (capitalizedIncomeTransaction.getLoan().isInterestBearing()) {
            handleCapitalizedIncomeWithEMICalculator(capitalizedIncomeTransaction, transactionCtx);
        } else {
            handleCapitalizedIncomeWithoutEMICalculator(capitalizedIncomeTransaction, transactionCtx);
        }
    }

    private void handleCapitalizedIncomeWithEMICalculator(LoanTransaction capitalizedIncomeTransaction, TransactionCtx transactionCtx) {
        ProgressiveLoanInterestScheduleModel model;
        if (!(transactionCtx instanceof ProgressiveTransactionCtx)
                || (model = ((ProgressiveTransactionCtx) transactionCtx).getModel()) == null) {
            throw new IllegalStateException("TransactionCtx has no model");
        }
        List<LoanRepaymentScheduleInstallment> installments = transactionCtx.getInstallments();
        LocalDate transactionDate = capitalizedIncomeTransaction.getTransactionDate();
        MonetaryCurrency currency = transactionCtx.getCurrency();

        Money amortizableAmount = capitalizedIncomeTransaction.getAmount(currency);
        emiCalculator.addCapitalizedIncome(model, transactionDate, amortizableAmount);

        recalculateRepaymentPeriodsWithEMICalculation(amortizableAmount, model, installments, transactionDate, currency);
        allocateOverpayment(capitalizedIncomeTransaction, transactionCtx);
    }

    private void handleCapitalizedIncomeWithoutEMICalculator(LoanTransaction capitalizedIncomeTransaction, TransactionCtx transactionCtx) {
        MonetaryCurrency currency = transactionCtx.getCurrency();
        List<LoanRepaymentScheduleInstallment> installments = transactionCtx.getInstallments();
        List<LoanRepaymentScheduleInstallment> candidateRepaymentInstallments = installments.stream().filter(
                i -> i.getDueDate().isAfter(capitalizedIncomeTransaction.getTransactionDate()) && !i.isDownPayment() && !i.isAdditional())
                .toList();
        Integer installmentAmountInMultiplesOf = capitalizedIncomeTransaction.getLoan().getLoanProduct().getLoanProductRelatedDetail()
                .getInstallmentAmountInMultiplesOf();

        Money amortizableAmount = capitalizedIncomeTransaction.getAmount(currency);

        recalculateRepaymentInstallmentsWithoutEMICalculation(capitalizedIncomeTransaction, amortizableAmount,
                candidateRepaymentInstallments, currency, installmentAmountInMultiplesOf);
        allocateOverpayment(capitalizedIncomeTransaction, transactionCtx);
    }

    private void recalculateRepaymentPeriodsWithEMICalculation(Money amortizableAmount, ProgressiveLoanInterestScheduleModel model,
            List<LoanRepaymentScheduleInstallment> installments, LocalDate transactionDate, MonetaryCurrency currency) {
        boolean isPostMaturityDisbursement = installments.stream().filter(i -> !i.isDownPayment() && !i.isAdditional())
                .allMatch(i -> i.getDueDate().isBefore(transactionDate));

        if (amortizableAmount.isGreaterThanZero()) {
            if (isPostMaturityDisbursement) {
                LoanRepaymentScheduleInstallment additionalInstallment = installments.stream()
                        .filter(LoanRepaymentScheduleInstallment::isAdditional).findFirst().orElse(null);
                if (additionalInstallment != null && additionalInstallment.getPrincipal(currency).isZero()) {
                    additionalInstallment.updatePrincipal(amortizableAmount.getAmount());
                }
            }

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
    }

    private void recalculateRepaymentInstallmentsWithoutEMICalculation(LoanTransaction loanTransaction, Money amortizableAmount,
            List<LoanRepaymentScheduleInstallment> candidateRepaymentInstallments, MonetaryCurrency currency,
            Integer installmentAmountInMultiplesOf) {
        if (amortizableAmount.isGreaterThanZero()) {
            int noCandidateRepaymentInstallments = candidateRepaymentInstallments.size();

            // Handle the case where no future installments exist (e.g., second disbursement after loan closure)
            if (noCandidateRepaymentInstallments == 0) {
                log.debug("No candidate repayment installments found for disbursement on {}. Creating new installments.",
                        loanTransaction.getTransactionDate());
                return;
            }

            // Original logic for when candidate installments exist
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
                i.updateObligationsMet(currency, loanTransaction.getTransactionDate());
            });
            // Hence the rounding, we might need to amend the last installment amount
            candidateRepaymentInstallments.get(noCandidateRepaymentInstallments - 1).addToPrincipal(loanTransaction.getTransactionDate(),
                    moneyHolder.getMoneyObject());
        }
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
            Money transactionAmountUnprocessed = processPeriods(loanTransaction, overpayment, defaultAllocationRule, transactionMappings,
                    balances, transactionCtx);

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

    public void recalculateInterestForDate(LocalDate targetDate, ProgressiveTransactionCtx ctx) {
        if (ctx.getInstallments() != null && !ctx.getInstallments().isEmpty()) {
            Loan loan = ctx.getInstallments().getFirst().getLoan();
            if (loan.isInterestBearingAndInterestRecalculationEnabled() && !loan.isNpa() && !ctx.isChargedOff()
                    && !ctx.isContractTerminated()) {

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
                    if (aggregatedOverDuePrincipal.isGreaterThanZero() && (ctx.getModel().lastOverdueBalanceChange() == null
                            || ctx.getModel().lastOverdueBalanceChange().isBefore(targetDate))) {
                        ctx.getModel().lastOverdueBalanceChange(targetDate);
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

        if (!currentDate.equals(ctx.getModel().lastOverdueBalanceChange())) {
            // if we have same date for fromDate & last overdue balance change then it means we have the up-to-date
            // model.
            if (ctx.getModel().lastOverdueBalanceChange() == null
                    || currentInstallment.getFromDate().isAfter(ctx.getModel().lastOverdueBalanceChange())) {
                // first overdue hit for installment. setting overdue balance correction from instalment from date.
                emiCalculator.addBalanceCorrection(ctx.getModel(), fromDate, overduePrincipal);
            } else {
                // not the first balance correction on installment period, then setting overdue balance correction from
                // last balance change's current date. previous interest period already has the correct balance
                // correction.
                emiCalculator.addBalanceCorrection(ctx.getModel(), ctx.getModel().lastOverdueBalanceChange(), overduePrincipal);
            }

            hasUpdate = true;

            if (currentDate.isAfter(fromDate) && !currentDate.isAfter(toDate)) {
                LocalDate lastOverdueBalanceChange;
                if (shouldRecalculateTillInstallmentDueDate(currentInstallment.getLoan().getLoanInterestRecalculationDetails(),
                        ctx.isPrepayAttempt())) {
                    lastOverdueBalanceChange = toDate;
                } else {
                    lastOverdueBalanceChange = currentDate;
                }
                if (DateUtils.isBefore(ctx.getModel().lastOverdueBalanceChange(), toDate)) {
                    emiCalculator.addBalanceCorrection(ctx.getModel(), lastOverdueBalanceChange, aggregatedOverDuePrincipal.negated());
                    ctx.getModel().lastOverdueBalanceChange(lastOverdueBalanceChange);
                }
            }
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
        Money principalPortion = Money.zero(transactionCtx.getCurrency());
        Money interestPortion = Money.zero(transactionCtx.getCurrency());
        Money feeChargesPortion = Money.zero(transactionCtx.getCurrency());
        Money penaltyChargesPortion = Money.zero(transactionCtx.getCurrency());

        if (transactionCtx.getInstallments().stream().anyMatch(this::isNotObligationsMet)) {
            if (LoanChargeOffBehaviour.ZERO_INTEREST
                    .equals(loanTransaction.getLoan().getLoanProductRelatedDetail().getChargeOffBehaviour())) {
                handleZeroInterestChargeOff(loanTransaction, transactionCtx);
            } else if (LoanChargeOffBehaviour.ACCELERATE_MATURITY
                    .equals(loanTransaction.getLoan().getLoanProductRelatedDetail().getChargeOffBehaviour())) {
                handleAccelerateMaturityDate(loanTransaction, transactionCtx);
            }

            final BigDecimal newInterest = getInterestTillChargeOffForPeriod(loanTransaction.getLoan(),
                    loanTransaction.getTransactionDate(), transactionCtx);
            createMissingAccrualTransactionDuringChargeOffIfNeeded(newInterest, loanTransaction, loanTransaction.getTransactionDate(),
                    transactionCtx);

            if (!loanTransaction.getLoan().isInterestBearingAndInterestRecalculationEnabled()) {
                recalculateInstallmentFeeCharges(loanTransaction);
            }

            loanTransaction.resetDerivedComponents();
            // determine how much is outstanding total and breakdown for principal, interest and charges
            for (final LoanRepaymentScheduleInstallment currentInstallment : transactionCtx.getInstallments()) {
                principalPortion = principalPortion.plus(currentInstallment.getPrincipalOutstanding(transactionCtx.getCurrency()));
                interestPortion = interestPortion.plus(currentInstallment.getInterestOutstanding(transactionCtx.getCurrency()));
                feeChargesPortion = feeChargesPortion.plus(currentInstallment.getFeeChargesOutstanding(transactionCtx.getCurrency()));
                penaltyChargesPortion = penaltyChargesPortion
                        .plus(currentInstallment.getPenaltyChargesOutstanding(transactionCtx.getCurrency()));
            }

            loanTransaction.updateComponentsAndTotal(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
        } else {
            loanTransaction.resetDerivedComponents();
            loanTransaction.updateComponentsAndTotal(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion);
        }

        if (transactionCtx instanceof ProgressiveTransactionCtx progressiveTransactionCtx) {
            progressiveTransactionCtx.setChargedOff(true);
        }

        if (isAllComponentsZero(principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion)
                && loanTransaction.isNotReversed()) {
            loanTransaction.reverse();
            loanTransaction.getLoan().liftChargeOff();

            if (transactionCtx instanceof ProgressiveTransactionCtx progressiveCtx) {
                progressiveCtx.setChargedOff(false);
            }
        }
    }

    private boolean isAllComponentsZero(final Money... components) {
        return Arrays.stream(components).allMatch(Money::isZero);
    }

    private void handleAccelerateMaturityDate(final LoanTransaction loanTransaction, final TransactionCtx transactionCtx) {
        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        final List<LoanRepaymentScheduleInstallment> installments = transactionCtx.getInstallments();
        final Loan loan = loanTransaction.getLoan();
        final LoanRepaymentScheduleInstallment currentInstallment = loan.getRelatedRepaymentScheduleInstallment(transactionDate);

        if (!installments.isEmpty() && transactionDate.isBefore(loan.getMaturityDate())) {
            if (transactionCtx instanceof ProgressiveTransactionCtx progressiveTransactionCtx
                    && loanTransaction.getLoan().isInterestBearingAndInterestRecalculationEnabled()) {
                final BigDecimal interestOutstanding = currentInstallment.getInterestOutstanding(loan.getCurrency()).getAmount();
                final BigDecimal newInterest = emiCalculator.getPeriodInterestTillDate(progressiveTransactionCtx.getModel(),
                        currentInstallment.getDueDate(), transactionDate, true).getAmount();
                if (interestOutstanding.compareTo(BigDecimal.ZERO) > 0 || newInterest.compareTo(BigDecimal.ZERO) > 0) {
                    currentInstallment.updateInterestCharged(newInterest);
                }
            } else {
                final BigDecimal totalInterest = currentInstallment.getInterestOutstanding(transactionCtx.getCurrency()).getAmount();
                if (totalInterest.compareTo(BigDecimal.ZERO) > 0) {
                    final long totalDaysInPeriod = ChronoUnit.DAYS.between(currentInstallment.getFromDate(),
                            currentInstallment.getDueDate());
                    final long daysTillChargeOff = ChronoUnit.DAYS.between(currentInstallment.getFromDate(), transactionDate);

                    final MathContext mc = MoneyHelper.getMathContext();
                    final Money interestTillChargeOff = Money.of(transactionCtx.getCurrency(), totalInterest
                            .divide(BigDecimal.valueOf(totalDaysInPeriod), mc).multiply(BigDecimal.valueOf(daysTillChargeOff), mc), mc);

                    currentInstallment.updateInterestCharged(interestTillChargeOff.getAmount());
                }
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

            final List<LoanTransaction> transactionsToBeReprocessed = loan.getLoanTransactions().stream()
                    .filter(transaction -> transaction.getTransactionDate().isBefore(transactionDate))
                    .filter(transaction -> transaction.getLoanTransactionToRepaymentScheduleMappings().stream().anyMatch(mapping -> {
                        final LoanRepaymentScheduleInstallment installment = mapping.getInstallment();
                        return transactionDate.isBefore(installment.getFromDate())
                                && installments.stream().anyMatch(i -> i.getInstallmentNumber().equals(installment.getInstallmentNumber()));
                    })).toList();

            if (futureFee.compareTo(BigDecimal.ZERO) > 0 || futurePenalty.compareTo(BigDecimal.ZERO) > 0) {
                final Optional<LocalDate> latestDueDate = loan.getCharges().stream()
                        .filter(loanCharge -> loanCharge.isActive() && loanCharge.isNotFullyPaid() && loanCharge.getDueDate() != null)
                        .map(LoanCharge::getDueDate).max(LocalDate::compareTo);

                if (latestDueDate.isPresent()) {
                    final LoanRepaymentScheduleInstallment lastInstallment = installmentsUpToTransactionDate.getLast();

                    final LoanRepaymentScheduleInstallment installmentForCharges = new LoanRepaymentScheduleInstallment(loan,
                            lastInstallment.getInstallmentNumber() + 1, currentInstallment.getDueDate(), latestDueDate.get(),
                            BigDecimal.ZERO, BigDecimal.ZERO, futureFee, futurePenalty, null, null, null, null, true, false, false);
                    installmentsUpToTransactionDate.add(installmentForCharges);
                }
            }

            transactionCtx.getInstallments().stream().filter(installment -> installment.getFromDate().isBefore(transactionDate))
                    .filter(installment -> transactionsToBeReprocessed.stream()
                            .anyMatch(transaction -> transaction.getLoanTransactionToRepaymentScheduleMappings().stream().anyMatch(
                                    mapping -> mapping.getInstallment().getInstallmentNumber().equals(installment.getInstallmentNumber()))))
                    .forEach(LoanRepaymentScheduleInstallment::resetDerivedComponents);

            loanSchedule.updateLoanSchedule(loan, installmentsUpToTransactionDate);

            if (transactionCtx instanceof ProgressiveTransactionCtx progressiveTransactionCtx && loan.isInterestRecalculationEnabled()) {
                updateRepaymentPeriodsAfterAccelerateMaturityDate(progressiveTransactionCtx, transactionDate, transactionsToBeReprocessed);
            } else {
                for (LoanTransaction processTransaction : transactionsToBeReprocessed) {
                    final LoanTransaction newTransaction = LoanTransaction.copyTransactionProperties(processTransaction);
                    processLatestTransaction(newTransaction, transactionCtx);
                    createNewTransaction(processTransaction, newTransaction, transactionCtx);
                    newTransaction.updateLoan(loan);
                    loan.getLoanTransactions().add(newTransaction);
                }
                loanBalanceService.updateLoanSummaryDerivedFields(loan);
            }
        }
    }

    private void handleZeroInterestChargeOff(final LoanTransaction loanTransaction, final TransactionCtx transactionCtx) {
        final LocalDate transactionDate = loanTransaction.getTransactionDate();
        final List<LoanRepaymentScheduleInstallment> installments = transactionCtx.getInstallments();
        final MonetaryCurrency currency = loanTransaction.getLoan().getCurrency();

        if (!installments.isEmpty()) {
            if (transactionCtx instanceof ProgressiveTransactionCtx progressiveTransactionCtx
                    && loanTransaction.getLoan().isInterestBearingAndInterestRecalculationEnabled()) {
                installments.stream().filter(installment -> !installment.getFromDate().isAfter(transactionDate)
                        && installment.getDueDate().isAfter(transactionDate)).forEach(installment -> {
                            final BigDecimal interestOutstanding = installment.getInterestOutstanding(currency).getAmount();
                            final BigDecimal newInterest = emiCalculator.getPeriodInterestTillDate(progressiveTransactionCtx.getModel(),
                                    installment.getDueDate(), transactionDate, true).getAmount();
                            if (MathUtil.isGreaterThanZero(interestOutstanding) || MathUtil.isGreaterThanZero(newInterest)) {
                                final BigDecimal interestRemoved = MathUtil.subtract(MathUtil.nullToZero(installment.getInterestCharged()),
                                        newInterest);
                                installment.updatePrincipal(MathUtil.nullToZero(installment.getPrincipal()).add(interestRemoved));
                                installment.updateInterestCharged(newInterest);
                            }
                        });
                progressiveTransactionCtx.setChargedOff(true);
            } else {
                calculatePartialPeriodInterest(transactionCtx, transactionDate);
            }

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

            BigDecimal principalBalance = amountToEditLastInstallment.getAmount();
            for (int i = installments.size() - 1; i > 0 && BigDecimal.ZERO.compareTo(principalBalance) != 0; i--) {
                final LoanRepaymentScheduleInstallment installment = installments.get(i);
                if (!installment.isAdditional() && !installment.isObligationsMet()) {
                    final BigDecimal installmentPrincipal = MathUtil.nullToZero(installment.getPrincipal());

                    installment.updatePrincipal(MathUtil.negativeToZero(installmentPrincipal.add(principalBalance)));
                    if (!installment.isObligationsMet()) {
                        installment.checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);
                    }
                    if (MathUtil.isLessThanOrEqualTo(MathUtil.abs(principalBalance), installmentPrincipal)) {
                        principalBalance = BigDecimal.ZERO;
                    } else {
                        principalBalance = principalBalance.signum() < 0 ? principalBalance.add(installmentPrincipal)
                                : principalBalance.subtract(installmentPrincipal);
                    }
                }
            }
        }
    }

    private void calculatePartialPeriodInterest(final TransactionCtx transactionCtx, final LocalDate chargeOffDate) {
        transactionCtx.getInstallments().stream()
                .filter(installment -> !installment.getFromDate().isAfter(chargeOffDate) && installment.getDueDate().isAfter(chargeOffDate))
                .forEach(installment -> {
                    final BigDecimal totalInterest = installment.getInterestOutstanding(transactionCtx.getCurrency()).getAmount();
                    if (totalInterest.compareTo(BigDecimal.ZERO) > 0) {
                        final long totalDaysInPeriod = ChronoUnit.DAYS.between(installment.getFromDate(), installment.getDueDate());
                        final long daysTillChargeOff = ChronoUnit.DAYS.between(installment.getFromDate(), chargeOffDate);

                        final MathContext mc = MoneyHelper.getMathContext();
                        final Money interestTillChargeOff = Money.of(transactionCtx.getCurrency(), totalInterest
                                .divide(BigDecimal.valueOf(totalDaysInPeriod), mc).multiply(BigDecimal.valueOf(daysTillChargeOff), mc), mc);

                        final BigDecimal interestRemoved = totalInterest.subtract(interestTillChargeOff.getAmount());
                        installment.updatePrincipal(MathUtil.nullToZero(installment.getPrincipal()).add(interestRemoved));
                        installment.updateInterestCharged(interestTillChargeOff.getAmount());
                    }
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
        HorizontalPaymentAllocationContext paymentAllocationContext = new HorizontalPaymentAllocationContext(ctx, loanTransaction,
                paymentAllocationTypes, futureInstallmentAllocationRule, transactionMappings, balances);
        paymentAllocationContext.setTransactionAmountUnprocessed(transactionAmountUnprocessed);

        LoopGuard.runSafeDoWhileLoop(paymentAllocationContext.getCtx().getInstallments().size() * 100, //
                paymentAllocationContext, //
                (HorizontalPaymentAllocationContext context) -> !context.isExitCondition()
                        && context.getCtx().getInstallments().stream()
                                .anyMatch(installment -> installment.getTotalPaid(context.getCtx().getCurrency()).isGreaterThanZero())
                        && context.getTransactionAmountUnprocessed().isGreaterThanZero(), //
                context -> {
                    LoanRepaymentScheduleInstallment latestPastDueInstallment = getLatestPastDueInstallmentForRefund(
                            context.getLoanTransaction(), context.getCtx().getCurrency(), context.getCtx().getInstallments());
                    LoanRepaymentScheduleInstallment dueInstallment = getDueInstallmentForRefund(context.getLoanTransaction(),
                            context.getCtx().getCurrency(), context.getCtx().getInstallments());

                    List<LoanRepaymentScheduleInstallment> inAdvanceInstallments = getFutureInstallmentsForRefund(
                            context.getLoanTransaction(), context.getCtx().getCurrency(), context.getCtx().getInstallments(),
                            context.getFutureInstallmentAllocationRule());

                    int firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper
                            .fetchFirstNormalInstallmentNumber(context.getCtx().getInstallments());
                    for (PaymentAllocationType paymentAllocationType : context.getPaymentAllocationTypes()) {
                        switch (paymentAllocationType.getDueType()) {
                            case PAST_DUE -> {
                                if (latestPastDueInstallment != null) {
                                    Set<LoanCharge> oldestPastDueInstallmentCharges = getLoanChargesOfInstallment(
                                            context.getCtx().getCharges(), latestPastDueInstallment, firstNormalInstallmentNumber);
                                    LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                            context.getTransactionMappings(), context.getLoanTransaction(), latestPastDueInstallment,
                                            context.getCtx().getCurrency());
                                    context.setAllocatedAmount(processPaymentAllocation(paymentAllocationType, latestPastDueInstallment,
                                            context.getLoanTransaction(), context.getTransactionAmountUnprocessed(),
                                            loanTransactionToRepaymentScheduleMapping, oldestPastDueInstallmentCharges,
                                            context.getBalances(), LoanRepaymentScheduleInstallment.PaymentAction.UNPAY));
                                    context.setTransactionAmountUnprocessed(
                                            context.getTransactionAmountUnprocessed().minus(context.getAllocatedAmount()));
                                } else {
                                    context.setExitCondition(true);
                                }
                            }
                            case DUE -> {
                                if (dueInstallment != null) {
                                    Set<LoanCharge> dueInstallmentCharges = getLoanChargesOfInstallment(context.getCtx().getCharges(),
                                            dueInstallment, firstNormalInstallmentNumber);
                                    LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                            context.getTransactionMappings(), context.getLoanTransaction(), dueInstallment,
                                            context.getCtx().getCurrency());
                                    context.setAllocatedAmount(processPaymentAllocation(paymentAllocationType, dueInstallment,
                                            context.getLoanTransaction(), context.getTransactionAmountUnprocessed(),
                                            loanTransactionToRepaymentScheduleMapping, dueInstallmentCharges, context.getBalances(),
                                            LoanRepaymentScheduleInstallment.PaymentAction.UNPAY));
                                    context.setTransactionAmountUnprocessed(
                                            context.getTransactionAmountUnprocessed().minus(context.getAllocatedAmount()));
                                } else {
                                    context.setExitCondition(true);
                                }
                            }
                            case IN_ADVANCE -> {
                                int numberOfInstallments = inAdvanceInstallments.size();
                                if (numberOfInstallments > 0) {
                                    Money evenPortion = context.getTransactionAmountUnprocessed().dividedBy(numberOfInstallments,
                                            MoneyHelper.getMathContext());
                                    Money balanceAdjustment = context.getTransactionAmountUnprocessed()
                                            .minus(evenPortion.multipliedBy(numberOfInstallments));
                                    for (LoanRepaymentScheduleInstallment inAdvanceInstallment : inAdvanceInstallments) {
                                        Set<LoanCharge> inAdvanceInstallmentCharges = getLoanChargesOfInstallment(
                                                context.getCtx().getCharges(), inAdvanceInstallment, firstNormalInstallmentNumber);
                                        if (inAdvanceInstallment.equals(inAdvanceInstallments.get(numberOfInstallments - 1))) {
                                            evenPortion = evenPortion.add(balanceAdjustment);
                                        }
                                        LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                                context.getTransactionMappings(), context.getLoanTransaction(), inAdvanceInstallment,
                                                context.getCtx().getCurrency());
                                        context.setAllocatedAmount(processPaymentAllocation(paymentAllocationType, inAdvanceInstallment,
                                                context.getLoanTransaction(), evenPortion, loanTransactionToRepaymentScheduleMapping,
                                                inAdvanceInstallmentCharges, context.getBalances(),
                                                LoanRepaymentScheduleInstallment.PaymentAction.UNPAY));
                                        context.setTransactionAmountUnprocessed(
                                                context.getTransactionAmountUnprocessed().minus(context.getAllocatedAmount()));
                                    }
                                } else {
                                    context.setExitCondition(true);
                                }
                            }
                        }
                    }
                });
        return paymentAllocationContext.getTransactionAmountUnprocessed();
    }

    private Money refundTransactionVertically(LoanTransaction loanTransaction, TransactionCtx ctx,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Money transactionAmountUnprocessed,
            FutureInstallmentAllocationRule futureInstallmentAllocationRule, Balances balances,
            PaymentAllocationType paymentAllocationType) {
        VerticalPaymentAllocationContext paymentAllocationContext = new VerticalPaymentAllocationContext(ctx, loanTransaction,
                futureInstallmentAllocationRule, transactionMappings, balances);
        paymentAllocationContext.setTransactionAmountUnprocessed(transactionAmountUnprocessed);
        paymentAllocationContext.setPaymentAllocationType(paymentAllocationType);
        LoopGuard.runSafeDoWhileLoop(paymentAllocationContext.getCtx().getInstallments().size() * 100, //
                paymentAllocationContext, //
                (VerticalPaymentAllocationContext context) -> context.getInstallment() != null
                        && context.getTransactionAmountUnprocessed().isGreaterThanZero()
                        && context.getAllocatedAmount().isGreaterThanZero(), //
                context -> {
                    switch (context.getPaymentAllocationType().getDueType()) {
                        case PAST_DUE -> {
                            context.setInstallment(getLatestPastDueInstallmentForRefund(context.getLoanTransaction(),
                                    context.getCtx().getCurrency(), context.getCtx().getInstallments()));
                            if (context.getInstallment() != null) {
                                Set<LoanCharge> oldestPastDueInstallmentCharges = getLoanChargesOfInstallment(context.getCtx().getCharges(),
                                        context.getInstallment(), context.getFirstNormalInstallmentNumber());
                                LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                        context.getTransactionMappings(), context.getLoanTransaction(), context.getInstallment(),
                                        context.getCtx().getCurrency());
                                context.setAllocatedAmount(processPaymentAllocation(context.getPaymentAllocationType(),
                                        context.getInstallment(), context.getLoanTransaction(), context.getTransactionAmountUnprocessed(),
                                        loanTransactionToRepaymentScheduleMapping, oldestPastDueInstallmentCharges, context.getBalances(),
                                        LoanRepaymentScheduleInstallment.PaymentAction.UNPAY));
                                context.setTransactionAmountUnprocessed(
                                        context.getTransactionAmountUnprocessed().minus(context.getAllocatedAmount()));
                            }
                        }
                        case DUE -> {
                            context.setInstallment(getDueInstallmentForRefund(context.getLoanTransaction(), context.getCtx().getCurrency(),
                                    context.getCtx().getInstallments()));
                            if (context.getInstallment() != null) {
                                Set<LoanCharge> dueInstallmentCharges = getLoanChargesOfInstallment(context.getCtx().getCharges(),
                                        context.getInstallment(), context.getFirstNormalInstallmentNumber());
                                LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                        context.getTransactionMappings(), context.getLoanTransaction(), context.getInstallment(),
                                        context.getCtx().getCurrency());
                                context.setAllocatedAmount(processPaymentAllocation(context.getPaymentAllocationType(),
                                        context.getInstallment(), context.getLoanTransaction(), context.getTransactionAmountUnprocessed(),
                                        loanTransactionToRepaymentScheduleMapping, dueInstallmentCharges, context.getBalances(),
                                        LoanRepaymentScheduleInstallment.PaymentAction.UNPAY));
                                context.setTransactionAmountUnprocessed(
                                        context.getTransactionAmountUnprocessed().minus(context.getAllocatedAmount()));
                            }
                        }
                        case IN_ADVANCE -> {
                            List<LoanRepaymentScheduleInstallment> currentInstallments = getFutureInstallmentsForRefund(
                                    context.getLoanTransaction(), context.getCtx().getCurrency(), context.getCtx().getInstallments(),
                                    context.getFutureInstallmentAllocationRule());
                            int numberOfInstallments = currentInstallments.size();
                            context.setAllocatedAmount(Money.zero(context.getCtx().getCurrency()));
                            if (numberOfInstallments > 0) {
                                Money evenPortion = context.getTransactionAmountUnprocessed().dividedBy(numberOfInstallments,
                                        MoneyHelper.getMathContext());
                                Money balanceAdjustment = context.getTransactionAmountUnprocessed()
                                        .minus(evenPortion.multipliedBy(numberOfInstallments));
                                for (LoanRepaymentScheduleInstallment internalCurrentInstallment : currentInstallments) {
                                    context.setInstallment(internalCurrentInstallment);
                                    Set<LoanCharge> inAdvanceInstallmentCharges = getLoanChargesOfInstallment(context.getCtx().getCharges(),
                                            context.getInstallment(), context.getFirstNormalInstallmentNumber());
                                    if (internalCurrentInstallment.equals(currentInstallments.get(numberOfInstallments - 1))) {
                                        evenPortion = evenPortion.add(balanceAdjustment);
                                    }
                                    LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                            context.getTransactionMappings(), context.getLoanTransaction(), context.getInstallment(),
                                            context.getCtx().getCurrency());
                                    Money internalUnpaidPortion = processPaymentAllocation(context.getPaymentAllocationType(),
                                            context.getInstallment(), context.getLoanTransaction(), evenPortion,
                                            loanTransactionToRepaymentScheduleMapping, inAdvanceInstallmentCharges, context.getBalances(),
                                            LoanRepaymentScheduleInstallment.PaymentAction.UNPAY);
                                    if (internalUnpaidPortion.isGreaterThanZero()) {
                                        context.setAllocatedAmount(internalUnpaidPortion);
                                    }
                                    context.setTransactionAmountUnprocessed(
                                            context.getTransactionAmountUnprocessed().minus(internalUnpaidPortion));
                                }
                            } else {
                                context.setInstallment(null);
                            }
                        }
                    }
                });
        return paymentAllocationContext.getTransactionAmountUnprocessed();
    }

    @Nullable
    private static LoanRepaymentScheduleInstallment getDueInstallmentForRefund(LoanTransaction loanTransaction, MonetaryCurrency currency,
            List<LoanRepaymentScheduleInstallment> installments) {
        return installments.stream().filter(installment -> installment.getTotalPaid(currency).isGreaterThanZero())
                .filter(installment -> loanTransaction.isOn(installment.getDueDate()))
                .max(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).orElse(null);
    }

    @Nullable
    private static LoanRepaymentScheduleInstallment getLatestPastDueInstallmentForRefund(LoanTransaction loanTransaction,
            MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments) {
        return installments.stream().filter(installment -> installment.getTotalPaid(currency).isGreaterThanZero())
                .filter(e -> loanTransaction.isAfter(e.getDueDate()))
                .max(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).orElse(null);
    }

    @NotNull
    private static List<LoanRepaymentScheduleInstallment> getFutureInstallmentsForRefund(LoanTransaction loanTransaction,
            MonetaryCurrency currency, List<LoanRepaymentScheduleInstallment> installments,
            FutureInstallmentAllocationRule futureInstallmentAllocationRule) {
        List<LoanRepaymentScheduleInstallment> inAdvanceInstallments = new ArrayList<>();
        if (FutureInstallmentAllocationRule.REAMORTIZATION.equals(futureInstallmentAllocationRule)) {
            inAdvanceInstallments = installments.stream().filter(installment -> installment.getTotalPaid(currency).isGreaterThanZero())
                    .filter(e -> loanTransaction.isBefore(e.getDueDate())).toList();
        } else if (FutureInstallmentAllocationRule.NEXT_INSTALLMENT.equals(futureInstallmentAllocationRule)) {
            inAdvanceInstallments = installments.stream().filter(installment -> installment.getTotalPaid(currency).isGreaterThanZero())
                    .filter(e -> loanTransaction.isBefore(e.getDueDate()))
                    .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream().toList();
        } else if (FutureInstallmentAllocationRule.LAST_INSTALLMENT.equals(futureInstallmentAllocationRule)) {
            inAdvanceInstallments = installments.stream().filter(installment -> installment.getTotalPaid(currency).isGreaterThanZero())
                    .filter(e -> loanTransaction.isBefore(e.getDueDate()))
                    .max(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream().toList();
        } else if (FutureInstallmentAllocationRule.NEXT_LAST_INSTALLMENT.equals(futureInstallmentAllocationRule)) {
            // try to resolve as current installment ( not due )
            inAdvanceInstallments = installments.stream().filter(installment -> installment.getTotalPaid(currency).isGreaterThanZero())
                    .filter(e -> loanTransaction.isBefore(e.getDueDate()))
                    .filter(f -> loanTransaction.isAfter(f.getFromDate()) || loanTransaction.isOn(f.getFromDate())).toList();
            // if there is no current installment, resolve similar to LAST_INSTALLMENT
            if (inAdvanceInstallments.isEmpty()) {
                inAdvanceInstallments = installments.stream().filter(installment -> installment.getTotalPaid(currency).isGreaterThanZero())
                        .filter(e -> loanTransaction.isBefore(e.getDueDate()))
                        .max(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream().toList();
            }
        }
        return inAdvanceInstallments;
    }

    private void processTransaction(LoanTransaction loanTransaction, TransactionCtx transactionCtx, Money transactionAmountUnprocessed) {
        List<LoanTransactionToRepaymentScheduleMapping> transactionMappings = new ArrayList<>();
        Money zero = Money.zero(transactionCtx.getCurrency());
        Balances balances = new Balances(zero, zero, zero, zero);
        transactionAmountUnprocessed = processPeriods(loanTransaction, transactionAmountUnprocessed, transactionMappings, balances,
                transactionCtx);

        loanTransaction.updateComponents(balances.getAggregatedPrincipalPortion(), balances.getAggregatedInterestPortion(),
                balances.getAggregatedFeeChargesPortion(), balances.getAggregatedPenaltyChargesPortion());
        loanTransaction.updateLoanTransactionToRepaymentScheduleMappings(transactionMappings);

        handleOverpayment(transactionAmountUnprocessed, loanTransaction, transactionCtx);
    }

    private Money processPeriods(LoanTransaction transaction, Money processAmount,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Balances balances, TransactionCtx transactionCtx) {
        LoanPaymentAllocationRule allocationRule = getAllocationRule(transaction);
        return processPeriods(transaction, processAmount, allocationRule, transactionMappings, balances, transactionCtx);
    }

    private Money processPeriods(LoanTransaction transaction, Money processAmount, LoanPaymentAllocationRule allocationRule,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Balances balances, TransactionCtx transactionCtx) {
        LoanScheduleProcessingType scheduleProcessingType = transaction.getLoan().getLoanProductRelatedDetail()
                .getLoanScheduleProcessingType();
        if (scheduleProcessingType.isHorizontal()) {
            return processPeriodsHorizontally(transaction, transactionCtx, processAmount, allocationRule, transactionMappings, balances);
        }
        if (scheduleProcessingType.isVertical()) {
            return processPeriodsVertically(transaction, transactionCtx, processAmount, allocationRule, transactionMappings, balances);
        }
        return processAmount;
    }

    private Money processPeriodsHorizontally(LoanTransaction loanTransaction, TransactionCtx transactionCtx,
            Money transactionAmountUnprocessed, LoanPaymentAllocationRule paymentAllocationRule,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Balances balances) {
        LinkedHashMap<DueType, List<PaymentAllocationType>> paymentAllocationsMap = paymentAllocationRule.getAllocationTypes().stream()
                .collect(Collectors.groupingBy(PaymentAllocationType::getDueType, LinkedHashMap::new,
                        mapping(Function.identity(), toList())));

        for (Map.Entry<DueType, List<PaymentAllocationType>> paymentAllocationsEntry : paymentAllocationsMap.entrySet()) {
            transactionAmountUnprocessed = processAllocationsHorizontally(loanTransaction, transactionCtx, transactionAmountUnprocessed,
                    paymentAllocationsEntry.getValue(), paymentAllocationRule.getFutureInstallmentAllocationRule(), transactionMappings,
                    balances);
        }
        return transactionAmountUnprocessed;
    }

    private Money processAllocationsHorizontally(LoanTransaction loanTransaction, TransactionCtx ctx, Money transactionAmountUnprocessed,
            List<PaymentAllocationType> paymentAllocationTypes, FutureInstallmentAllocationRule futureInstallmentAllocationRule,
            List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Balances balances) {
        if (MathUtil.isEmpty(transactionAmountUnprocessed)) {
            return transactionAmountUnprocessed;
        }
        HorizontalPaymentAllocationContext paymentAllocationContext = new HorizontalPaymentAllocationContext(ctx, loanTransaction,
                paymentAllocationTypes, futureInstallmentAllocationRule, transactionMappings, balances);
        paymentAllocationContext.setTransactionAmountUnprocessed(transactionAmountUnprocessed);
        boolean interestBearingAndInterestRecalculationEnabled = loanTransaction.getLoan()
                .isInterestBearingAndInterestRecalculationEnabled();

        if (interestBearingAndInterestRecalculationEnabled && ctx instanceof ProgressiveTransactionCtx progressiveTransactionCtx
                && !progressiveTransactionCtx.isChargedOff() && !progressiveTransactionCtx.isContractTerminated()) {
            // Clear any previously skipped installments before re-evaluating
            progressiveTransactionCtx.getSkipRepaymentScheduleInstallments().clear();
            paymentAllocationContext
                    .setInAdvanceInstallmentsFilteringRules(installment -> loanTransaction.isBefore(installment.getDueDate())
                            && (installment.isNotFullyPaidOff() || (installment.isDueBalanceZero()
                                    && !progressiveTransactionCtx.getSkipRepaymentScheduleInstallments().contains(installment))));
        } else {
            paymentAllocationContext.setInAdvanceInstallmentsFilteringRules(
                    installment -> loanTransaction.isBefore(installment.getDueDate()) && installment.isNotFullyPaidOff());
        }
        LoopGuard.runSafeDoWhileLoop(paymentAllocationContext.getCtx().getInstallments().size() * 100, //
                paymentAllocationContext, //
                (HorizontalPaymentAllocationContext context) -> !context.isExitCondition()
                        && context.getCtx().getInstallments().stream().anyMatch(LoanRepaymentScheduleInstallment::isNotFullyPaidOff)
                        && context.getTransactionAmountUnprocessed().isGreaterThanZero(), //
                context -> {
                    LoanRepaymentScheduleInstallment oldestPastDueInstallment = context.getCtx().getInstallments().stream()
                            .filter(LoanRepaymentScheduleInstallment::isNotFullyPaidOff)
                            .filter(e -> context.getLoanTransaction().isAfter(e.getDueDate()))
                            .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).orElse(null);
                    LoanRepaymentScheduleInstallment dueInstallment = context.getCtx().getInstallments().stream()
                            .filter(LoanRepaymentScheduleInstallment::isNotFullyPaidOff)
                            .filter(e -> context.getLoanTransaction().isOn(e.getDueDate()))
                            .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).orElse(null);

                    // For having similar logic we are populating installment list even when the future installment
                    // allocation rule is NEXT_INSTALLMENT or LAST_INSTALLMENT hence the list has only one element.
                    List<LoanRepaymentScheduleInstallment> inAdvanceInstallments = new ArrayList<>();
                    if (FutureInstallmentAllocationRule.REAMORTIZATION.equals(context.getFutureInstallmentAllocationRule())) {
                        inAdvanceInstallments = context.getCtx().getInstallments().stream()
                                .filter(LoanRepaymentScheduleInstallment::isNotFullyPaidOff) //
                                .filter(e -> context.getLoanTransaction().isBefore(e.getDueDate())) //
                                .toList(); //
                    } else if (FutureInstallmentAllocationRule.NEXT_INSTALLMENT.equals(context.getFutureInstallmentAllocationRule())) {
                        inAdvanceInstallments = context.getCtx().getInstallments().stream()
                                .filter(LoanRepaymentScheduleInstallment::isNotFullyPaidOff) //
                                .filter(e -> context.getLoanTransaction().isBefore(e.getDueDate())) //
                                .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream() //
                                .toList(); //
                    } else if (FutureInstallmentAllocationRule.LAST_INSTALLMENT.equals(context.getFutureInstallmentAllocationRule())) {
                        inAdvanceInstallments = context.getCtx().getInstallments().stream()
                                .filter(context.getInAdvanceInstallmentsFilteringRules())
                                .max(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream() //
                                .toList(); //
                    } else if (FutureInstallmentAllocationRule.NEXT_LAST_INSTALLMENT.equals(context.getFutureInstallmentAllocationRule())) {
                        // try to resolve as current installment ( not due )
                        inAdvanceInstallments = context.getCtx().getInstallments().stream()
                                .filter(LoanRepaymentScheduleInstallment::isNotFullyPaidOff) //
                                .filter(e -> context.getLoanTransaction().isBefore(e.getDueDate())) //
                                .filter(f -> context.getLoanTransaction().isAfter(f.getFromDate())
                                        || context.getLoanTransaction().isOn(f.getFromDate())) //
                                .toList(); //
                        // if there is no current installment, resolve similar to LAST_INSTALLMENT
                        if (inAdvanceInstallments.isEmpty()) {
                            inAdvanceInstallments = context.getCtx().getInstallments().stream()
                                    .filter(context.getInAdvanceInstallmentsFilteringRules())
                                    .max(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream() //
                                    .toList(); //
                        }
                    }

                    int firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper
                            .fetchFirstNormalInstallmentNumber(context.getCtx().getInstallments());

                    for (PaymentAllocationType paymentAllocationType : context.getPaymentAllocationTypes()) {
                        switch (paymentAllocationType.getDueType()) {
                            case PAST_DUE -> {
                                if (oldestPastDueInstallment != null) {
                                    Set<LoanCharge> oldestPastDueInstallmentCharges = getLoanChargesOfInstallment(
                                            context.getCtx().getCharges(), oldestPastDueInstallment, firstNormalInstallmentNumber);
                                    LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                            context.getTransactionMappings(), context.getLoanTransaction(), oldestPastDueInstallment,
                                            context.getCtx().getCurrency());
                                    Loan loan = context.getLoanTransaction().getLoan();
                                    if (context.getCtx() instanceof ProgressiveTransactionCtx progressiveTransactionCtx
                                            && loan.isInterestBearingAndInterestRecalculationEnabled()
                                            && !progressiveTransactionCtx.isChargedOff()
                                            && !progressiveTransactionCtx.isContractTerminated()) {
                                        context.setAllocatedAmount(
                                                handlingPaymentAllocationForInterestBearingProgressiveLoan(context.getLoanTransaction(),
                                                        context.getTransactionAmountUnprocessed(), context.getBalances(),
                                                        paymentAllocationType, oldestPastDueInstallment, progressiveTransactionCtx,
                                                        loanTransactionToRepaymentScheduleMapping, oldestPastDueInstallmentCharges));
                                    } else {
                                        context.setAllocatedAmount(processPaymentAllocation(paymentAllocationType, oldestPastDueInstallment,
                                                context.getLoanTransaction(), context.getTransactionAmountUnprocessed(),
                                                loanTransactionToRepaymentScheduleMapping, oldestPastDueInstallmentCharges,
                                                context.getBalances(), LoanRepaymentScheduleInstallment.PaymentAction.PAY));
                                    }
                                    context.setTransactionAmountUnprocessed(
                                            context.getTransactionAmountUnprocessed().minus(context.getAllocatedAmount()));
                                } else {
                                    context.setExitCondition(true);
                                }
                            }
                            case DUE -> {
                                if (dueInstallment != null) {
                                    Set<LoanCharge> dueInstallmentCharges = getLoanChargesOfInstallment(context.getCtx().getCharges(),
                                            dueInstallment, firstNormalInstallmentNumber);
                                    LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                            context.getTransactionMappings(), context.getLoanTransaction(), dueInstallment,
                                            context.getCtx().getCurrency());
                                    Loan loan = context.getLoanTransaction().getLoan();
                                    if (context.getCtx() instanceof ProgressiveTransactionCtx progressiveTransactionCtx
                                            && loan.isInterestBearingAndInterestRecalculationEnabled()
                                            && !progressiveTransactionCtx.isChargedOff()
                                            && !progressiveTransactionCtx.isContractTerminated()) {
                                        context.setAllocatedAmount(handlingPaymentAllocationForInterestBearingProgressiveLoan(
                                                context.getLoanTransaction(), context.getTransactionAmountUnprocessed(),
                                                context.getBalances(), paymentAllocationType, dueInstallment, progressiveTransactionCtx,
                                                loanTransactionToRepaymentScheduleMapping, dueInstallmentCharges));
                                    } else {
                                        context.setAllocatedAmount(processPaymentAllocation(paymentAllocationType, dueInstallment,
                                                context.getLoanTransaction(), context.getTransactionAmountUnprocessed(),
                                                loanTransactionToRepaymentScheduleMapping, dueInstallmentCharges, context.getBalances(),
                                                LoanRepaymentScheduleInstallment.PaymentAction.PAY));
                                    }
                                    context.setTransactionAmountUnprocessed(
                                            context.getTransactionAmountUnprocessed().minus(context.getAllocatedAmount()));
                                } else {
                                    context.setExitCondition(true);
                                }
                            }
                            case IN_ADVANCE -> {
                                int numberOfInstallments = inAdvanceInstallments.size();
                                if (numberOfInstallments > 0) {
                                    // This will be the same amount as transactionAmountUnprocessed in case of the
                                    // future
                                    // installment allocation is NEXT_INSTALLMENT or LAST_INSTALLMENT
                                    Money evenPortion = context.getTransactionAmountUnprocessed().dividedBy(numberOfInstallments,
                                            MoneyHelper.getMathContext());
                                    // Adjustment might be needed due to the divide operation and the rounding mode
                                    Money balanceAdjustment = context.getTransactionAmountUnprocessed()
                                            .minus(evenPortion.multipliedBy(numberOfInstallments));
                                    if (evenPortion.add(balanceAdjustment).isLessThanZero()) {
                                        // Note: Rounding mode DOWN grants that evenPortion cant pay more than
                                        // unprocessed
                                        // transaction amount.
                                        evenPortion = context.getTransactionAmountUnprocessed().dividedBy(numberOfInstallments,
                                                new MathContext(MoneyHelper.getMathContext().getPrecision(), RoundingMode.DOWN));
                                        balanceAdjustment = context.getTransactionAmountUnprocessed()
                                                .minus(evenPortion.multipliedBy(numberOfInstallments));
                                    }

                                    for (LoanRepaymentScheduleInstallment inAdvanceInstallment : inAdvanceInstallments) {
                                        Set<LoanCharge> inAdvanceInstallmentCharges = getLoanChargesOfInstallment(
                                                context.getCtx().getCharges(), inAdvanceInstallment, firstNormalInstallmentNumber);

                                        LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                                context.getTransactionMappings(), context.getLoanTransaction(), inAdvanceInstallment,
                                                context.getCtx().getCurrency());

                                        Loan loan = context.getLoanTransaction().getLoan();
                                        // Adjust the portion for the last installment
                                        if (inAdvanceInstallment.equals(inAdvanceInstallments.get(numberOfInstallments - 1))) {
                                            evenPortion = evenPortion.add(balanceAdjustment);
                                        }
                                        if (context.getCtx() instanceof ProgressiveTransactionCtx progressiveTransactionCtx
                                                && loan.isInterestBearingAndInterestRecalculationEnabled()
                                                && !progressiveTransactionCtx.isChargedOff()
                                                && !progressiveTransactionCtx.isContractTerminated()) {
                                            context.setAllocatedAmount(handlingPaymentAllocationForInterestBearingProgressiveLoan(
                                                    context.getLoanTransaction(), evenPortion, context.getBalances(), paymentAllocationType,
                                                    inAdvanceInstallment, progressiveTransactionCtx,
                                                    loanTransactionToRepaymentScheduleMapping, inAdvanceInstallmentCharges));
                                        } else {
                                            context.setAllocatedAmount(processPaymentAllocation(paymentAllocationType, inAdvanceInstallment,
                                                    context.getLoanTransaction(), evenPortion, loanTransactionToRepaymentScheduleMapping,
                                                    inAdvanceInstallmentCharges, context.getBalances(),
                                                    LoanRepaymentScheduleInstallment.PaymentAction.PAY));
                                        }
                                        context.setTransactionAmountUnprocessed(
                                                context.getTransactionAmountUnprocessed().minus(context.getAllocatedAmount()));
                                    }
                                } else {
                                    context.setExitCondition(true);
                                }
                            }
                        }
                    }
                });
        return paymentAllocationContext.getTransactionAmountUnprocessed();
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
            ctx.getSkipRepaymentScheduleInstallments().add(installment);
            return processPaymentAllocation(paymentAllocationType, installment, loanTransaction, transactionAmountUnprocessed,
                    loanTransactionToRepaymentScheduleMapping, charges, balances, LoanRepaymentScheduleInstallment.PaymentAction.PAY);
        }

        if (DueType.IN_ADVANCE.equals(paymentAllocationType.getDueType())) {
            payDate = calculateNewPayDateInCaseOfInAdvancePayment(loanTransaction, installment, ctx.isPrepayAttempt());
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
                installment.setCreditedInterest(rm.getCreditedInterest().getAmount());
                installment.setCreditedPrincipal(rm.getCreditedPrincipal().getAmount());
                installment.updateObligationsMet(ctx.getCurrency(), loanTransaction.getTransactionDate());
            }
        });
    }

    private void updateRepaymentPeriodBalances(PaymentAllocationType paymentAllocationType,
            LoanRepaymentScheduleInstallment inAdvanceInstallment, ProgressiveTransactionCtx ctx, LocalDate payDate) {
        PeriodDueDetails payableDetails = emiCalculator.getDueAmounts(ctx.getModel(), inAdvanceInstallment.getDueDate(), payDate);

        if (payableDetails.getDueInterest().isZero() && payableDetails.getDuePrincipal().isZero()) {
            ctx.getSkipRepaymentScheduleInstallments().add(inAdvanceInstallment);
        }
        switch (paymentAllocationType) {
            case IN_ADVANCE_INTEREST -> inAdvanceInstallment.updateInterestCharged(payableDetails.getDueInterest().getAmount());
            case IN_ADVANCE_PRINCIPAL -> inAdvanceInstallment.updatePrincipal(payableDetails.getDuePrincipal().getAmount());
            default -> {
            }
        }
    }

    private boolean calculateIsPrepayAttempt(LoanTransaction loanTransaction, ProgressiveTransactionCtx progressiveTransactionCtx) {
        Loan loan = loanTransaction.getLoan();
        LoanRepaymentScheduleInstallment installment = loan.getRelatedRepaymentScheduleInstallment(loanTransaction.getTransactionDate());
        if (installment == null) {
            return false;
        }
        if (loan.getLoanInterestRecalculationDetails() == null) {
            return false;
        }
        OutstandingDetails outstandingAmounts = emiCalculator.getOutstandingAmountsTillDate(progressiveTransactionCtx.getModel(),
                installment.getDueDate());
        OutstandingAmountsDTO result = new OutstandingAmountsDTO(progressiveTransactionCtx.getCurrency()) //
                .principal(outstandingAmounts.getOutstandingPrincipal()) //
                .interest(outstandingAmounts.getOutstandingInterest());//

        return loanTransaction.getAmount(progressiveTransactionCtx.getCurrency()).isGreaterThanOrEqualTo(result.getTotalOutstanding());
    }

    private LocalDate calculateNewPayDateInCaseOfInAdvancePayment(LoanTransaction loanTransaction,
            LoanRepaymentScheduleInstallment inAdvanceInstallment, boolean prepayAttempt) {
        if (shouldRecalculateTillInstallmentDueDate(loanTransaction.getLoan().getLoanInterestRecalculationDetails(), prepayAttempt)) {
            return isInPeriod(loanTransaction.getTransactionDate(), inAdvanceInstallment, false) ? inAdvanceInstallment.getDueDate()
                    : loanTransaction.getTransactionDate();
        } else {
            return loanTransaction.getTransactionDate();
        }
    }

    private boolean shouldRecalculateTillInstallmentDueDate(LoanInterestRecalculationDetails recalculationDetails,
            boolean isPrepayAttempt) {
        // Rest frequency type and pre close interest calculation strategy can be controversial
        // if restFrequencyType == DAILY and preCloseInterestCalculationStrategy == TILL_PRE_CLOSURE_DATE
        // no problem. Calculate till transaction date
        // if restFrequencyType == SAME_AS_REPAYMENT_PERIOD and preCloseInterestCalculationStrategy ==
        // TILL_REST_FREQUENCY_DATE
        // again, no problem. Calculate till due date of current installment
        // if restFrequencyType == DAILY and preCloseInterestCalculationStrategy == TILL_REST_FREQUENCY_DATE
        // or restFrequencyType == SAME_AS_REPAYMENT_PERIOD and preCloseInterestCalculationStrategy ==
        // TILL_PRE_CLOSURE_DATE
        // we cannot harmonize the two configs. Behaviour should mimic prepay api.
        return switch (recalculationDetails.getRestFrequencyType()) {
            case DAILY ->
                isPrepayAttempt && recalculationDetails.getPreCloseInterestCalculationStrategy().calculateTillRestFrequencyEnabled();
            case SAME_AS_REPAYMENT_PERIOD ->
                recalculationDetails.getPreCloseInterestCalculationStrategy().calculateTillRestFrequencyEnabled();
            case WEEKLY -> throw new IllegalStateException("Unexpected RecalculationFrequencyType: WEEKLY");
            case MONTHLY -> throw new IllegalStateException("Unexpected RecalculationFrequencyType: MONTHLY");
            case INVALID -> throw new IllegalStateException("Unexpected RecalculationFrequencyType: INVALID");
        };
    }

    @NotNull
    private Set<LoanCharge> getLoanChargesOfInstallment(final Set<LoanCharge> charges, final LoanRepaymentScheduleInstallment installment,
            final int firstInstallmentNumber) {
        final boolean isFirstInstallment = installment.getInstallmentNumber().equals(firstInstallmentNumber);
        return charges.stream()
                .filter(loanCharge -> (loanCharge.isInstalmentFee() && loanCharge.hasInstallmentFor(installment))
                        || loanCharge.isDueInPeriod(installment.getFromDate(), installment.getDueDate(), isFirstInstallment))
                .collect(Collectors.toSet());
    }

    private Money processPeriodsVertically(LoanTransaction loanTransaction, TransactionCtx ctx, Money transactionAmountUnprocessed,
            LoanPaymentAllocationRule paymentAllocationRule, List<LoanTransactionToRepaymentScheduleMapping> transactionMappings,
            Balances balances) {
        VerticalPaymentAllocationContext paymentAllocationContext = new VerticalPaymentAllocationContext(ctx, loanTransaction,
                paymentAllocationRule.getFutureInstallmentAllocationRule(), transactionMappings, balances);
        paymentAllocationContext.setTransactionAmountUnprocessed(transactionAmountUnprocessed);
        for (PaymentAllocationType paymentAllocationType : paymentAllocationRule.getAllocationTypes()) {
            paymentAllocationContext.setAllocatedAmount(Money.zero(ctx.getCurrency()));
            paymentAllocationContext.setInstallment(null);
            paymentAllocationContext.setPaymentAllocationType(paymentAllocationType);
            LoopGuard.runSafeDoWhileLoop(paymentAllocationContext.getCtx().getInstallments().size() * 100, //
                    paymentAllocationContext, //
                    (VerticalPaymentAllocationContext context) -> context.getInstallment() != null
                            && context.getTransactionAmountUnprocessed().isGreaterThanZero()
                            && context.getAllocatedAmount().isGreaterThanZero(), //
                    context -> {
                        Predicate<LoanRepaymentScheduleInstallment> predicate = getFilterPredicate(context.getPaymentAllocationType(),
                                context.getCtx().getCurrency());
                        switch (context.getPaymentAllocationType().getDueType()) {
                            case PAST_DUE -> {
                                context.setInstallment(context.getCtx().getInstallments().stream().filter(predicate)
                                        .filter(e -> context.getLoanTransaction().isAfter(e.getDueDate()))
                                        .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).orElse(null));
                                if (context.getInstallment() != null) {
                                    Set<LoanCharge> oldestPastDueInstallmentCharges = getLoanChargesOfInstallment(
                                            context.getCtx().getCharges(), context.getInstallment(),
                                            context.getFirstNormalInstallmentNumber());
                                    LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                            context.getTransactionMappings(), context.getLoanTransaction(), context.getInstallment(),
                                            context.getCtx().getCurrency());
                                    context.setAllocatedAmount(
                                            processPaymentAllocation(context.getPaymentAllocationType(), context.getInstallment(),
                                                    context.getLoanTransaction(), context.getTransactionAmountUnprocessed(),
                                                    loanTransactionToRepaymentScheduleMapping, oldestPastDueInstallmentCharges,
                                                    context.getBalances(), LoanRepaymentScheduleInstallment.PaymentAction.PAY));
                                    context.setTransactionAmountUnprocessed(
                                            context.getTransactionAmountUnprocessed().minus(context.getAllocatedAmount()));
                                }
                            }
                            case DUE -> {
                                context.setInstallment(context.getCtx().getInstallments().stream().filter(predicate)
                                        .filter(e -> context.getLoanTransaction().isOn(e.getDueDate()))
                                        .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).orElse(null));
                                if (context.getInstallment() != null) {
                                    Set<LoanCharge> dueInstallmentCharges = getLoanChargesOfInstallment(context.getCtx().getCharges(),
                                            context.getInstallment(), context.getFirstNormalInstallmentNumber());
                                    LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                            context.getTransactionMappings(), context.getLoanTransaction(), context.getInstallment(),
                                            context.getCtx().getCurrency());
                                    context.setAllocatedAmount(
                                            processPaymentAllocation(context.getPaymentAllocationType(), context.getInstallment(),
                                                    context.getLoanTransaction(), context.getTransactionAmountUnprocessed(),
                                                    loanTransactionToRepaymentScheduleMapping, dueInstallmentCharges, context.getBalances(),
                                                    LoanRepaymentScheduleInstallment.PaymentAction.PAY));
                                    context.setTransactionAmountUnprocessed(
                                            context.getTransactionAmountUnprocessed().minus(context.getAllocatedAmount()));
                                }
                            }
                            case IN_ADVANCE -> {
                                // For having similar logic we are populating installment list even when the future
                                // installment
                                // allocation rule is NEXT_INSTALLMENT or LAST_INSTALLMENT hence the list has only one
                                // element.
                                List<LoanRepaymentScheduleInstallment> currentInstallments = new ArrayList<>();
                                if (FutureInstallmentAllocationRule.REAMORTIZATION.equals(context.getFutureInstallmentAllocationRule())) {
                                    currentInstallments = context.getCtx().getInstallments().stream().filter(predicate)
                                            .filter(e -> context.getLoanTransaction().isBefore(e.getDueDate())).toList();
                                } else if (FutureInstallmentAllocationRule.NEXT_INSTALLMENT
                                        .equals(context.getFutureInstallmentAllocationRule())) {
                                    currentInstallments = context.getCtx().getInstallments().stream().filter(predicate)
                                            .filter(e -> context.getLoanTransaction().isBefore(e.getDueDate()))
                                            .min(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream()
                                            .toList();
                                } else if (FutureInstallmentAllocationRule.LAST_INSTALLMENT
                                        .equals(context.getFutureInstallmentAllocationRule())) {
                                    currentInstallments = context.getCtx().getInstallments().stream().filter(predicate)
                                            .filter(e -> context.getLoanTransaction().isBefore(e.getDueDate()))
                                            .max(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream()
                                            .toList();
                                } else if (FutureInstallmentAllocationRule.NEXT_LAST_INSTALLMENT
                                        .equals(context.getFutureInstallmentAllocationRule())) {
                                    // get current installment where from date < transaction date < to date OR
                                    // transaction date
                                    // is on first installment's first day ( from day )
                                    currentInstallments = context.getCtx().getInstallments().stream().filter(predicate)
                                            .filter(e -> context.getLoanTransaction().isBefore(e.getDueDate()))
                                            .filter(f -> context.getLoanTransaction().isAfter(f.getFromDate())
                                                    || context.getLoanTransaction().isOn(f.getFromDate()))
                                            .toList();
                                    // if there is no current in advance installment resolve similar to LAST_INSTALLMENT
                                    if (currentInstallments.isEmpty()) {
                                        currentInstallments = context.getCtx().getInstallments().stream().filter(predicate)
                                                .filter(e -> context.getLoanTransaction().isBefore(e.getDueDate()))
                                                .max(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).stream()
                                                .toList();
                                    }
                                }
                                int numberOfInstallments = currentInstallments.size();
                                context.setAllocatedAmount(Money.zero(context.getCtx().getCurrency()));
                                if (numberOfInstallments > 0) {
                                    // This will be the same amount as transactionAmountUnprocessed in case of the
                                    // future
                                    // installment allocation is NEXT_INSTALLMENT or LAST_INSTALLMENT
                                    Money evenPortion = context.getTransactionAmountUnprocessed().dividedBy(numberOfInstallments,
                                            MoneyHelper.getMathContext());
                                    // Adjustment might be needed due to the divide operation and the rounding mode
                                    Money balanceAdjustment = context.getTransactionAmountUnprocessed()
                                            .minus(evenPortion.multipliedBy(numberOfInstallments));
                                    for (LoanRepaymentScheduleInstallment internalCurrentInstallment : currentInstallments) {
                                        context.setInstallment(internalCurrentInstallment);
                                        Set<LoanCharge> inAdvanceInstallmentCharges = getLoanChargesOfInstallment(
                                                context.getCtx().getCharges(), context.getInstallment(),
                                                context.getFirstNormalInstallmentNumber());
                                        // Adjust the portion for the last installment
                                        if (internalCurrentInstallment.equals(currentInstallments.get(numberOfInstallments - 1))) {
                                            evenPortion = evenPortion.add(balanceAdjustment);
                                        }
                                        LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = getTransactionMapping(
                                                context.getTransactionMappings(), context.getLoanTransaction(), context.getInstallment(),
                                                context.getCtx().getCurrency());
                                        Money internalPaidPortion = processPaymentAllocation(context.getPaymentAllocationType(),
                                                context.getInstallment(), context.getLoanTransaction(), evenPortion,
                                                loanTransactionToRepaymentScheduleMapping, inAdvanceInstallmentCharges,
                                                context.getBalances(), LoanRepaymentScheduleInstallment.PaymentAction.PAY);
                                        // Some extra logic to allocate as much as possible across the installments if
                                        // the
                                        // outstanding balances are different
                                        if (internalPaidPortion.isGreaterThanZero()) {
                                            context.setAllocatedAmount(internalPaidPortion);
                                        }
                                        context.setTransactionAmountUnprocessed(
                                                context.getTransactionAmountUnprocessed().minus(internalPaidPortion));
                                    }
                                } else {
                                    context.setInstallment(null);
                                }
                            }
                        }
                    });
        }
        return paymentAllocationContext.getTransactionAmountUnprocessed();
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
            Integer installmentAmountInMultiplesOf = loanTransaction.getLoan().getLoanProductRelatedDetail()
                    .getInstallmentAmountInMultiplesOf();
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

    private void updateRepaymentPeriodsAfterAccelerateMaturityDate(final ProgressiveTransactionCtx transactionCtx,
            final LocalDate transactionDate, final List<LoanTransaction> transactionsToBeReprocessed) {
        final List<LoanRepaymentScheduleInstallment> previousInstallmentsMapping = new ArrayList<>();
        transactionsToBeReprocessed.forEach(t -> {
            previousInstallmentsMapping.addAll(t.getLoanTransactionToRepaymentScheduleMappings().stream()
                    .map(LoanTransactionToRepaymentScheduleMapping::getInstallment).toList());
            t.getLoanTransactionToRepaymentScheduleMappings().clear();
        });
        final List<RepaymentPeriod> repaymentPeriods = transactionCtx.getModel().repaymentPeriods();

        if (repaymentPeriods.isEmpty()) {
            return;
        }

        final List<RepaymentPeriod> periodsBeforeAccelerateMaturity = repaymentPeriods.stream()
                .filter(rp -> rp.getFromDate().isBefore(transactionDate)).toList();

        if (periodsBeforeAccelerateMaturity.isEmpty()) {
            return;
        }

        final RepaymentPeriod lastPeriod = periodsBeforeAccelerateMaturity.getLast();

        final List<RepaymentPeriod> periodsToRemove = repaymentPeriods.stream().filter(rp -> rp.getFromDate().isAfter(transactionDate))
                .toList();

        lastPeriod.setDueDate(transactionDate);
        lastPeriod.getInterestPeriods().removeIf(interestPeriod -> !interestPeriod.getFromDate().isBefore(transactionDate));

        transactionCtx.getModel().repaymentPeriods().removeAll(periodsToRemove);

        final BigDecimal totalPrincipal = periodsToRemove.stream().map(rp -> rp.getDuePrincipal().getAmount()).reduce(BigDecimal.ZERO,
                BigDecimal::add);

        final BigDecimal newInterest = emiCalculator
                .getPeriodInterestTillDate(transactionCtx.getModel(), lastPeriod.getDueDate(), transactionDate, false).getAmount();

        lastPeriod.setEmi(lastPeriod.getDuePrincipal().add(totalPrincipal).add(newInterest));

        emiCalculator.calculateRateFactorForRepaymentPeriod(lastPeriod, transactionCtx.getModel());
        transactionCtx.getModel().disableEMIRecalculation();

        for (LoanTransaction processTransaction : transactionsToBeReprocessed) {
            transactionCtx.getModel().repaymentPeriods().stream()
                    .filter(repaymentPeriod -> repaymentPeriod.getFromDate().isBefore(transactionDate))
                    .filter(repaymentPeriod -> previousInstallmentsMapping.stream()
                            .anyMatch(installment -> installment.getFromDate().equals(repaymentPeriod.getFromDate())))
                    .forEach(RepaymentPeriod::resetDerivedComponents);
            emiCalculator.addBalanceCorrection(transactionCtx.getModel(), processTransaction.getTransactionDate(),
                    processTransaction.getPrincipalPortion(transactionCtx.getCurrency()));
            processSingleTransaction(processTransaction, transactionCtx);
        }
    }

    private BigDecimal getInterestTillChargeOffForPeriod(final Loan loan, final LocalDate chargeOffDate,
            final TransactionCtx transactionCtx) {
        BigDecimal interestTillChargeOff = BigDecimal.ZERO;
        final MonetaryCurrency currency = loan.getCurrency();

        final List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments().stream()
                .filter(i -> !i.isAdditional()).toList();

        for (LoanRepaymentScheduleInstallment installment : installments) {
            final boolean isPastPeriod = !installment.getDueDate().isAfter(chargeOffDate);
            final boolean isInPeriod = !installment.getFromDate().isAfter(chargeOffDate) && installment.getDueDate().isAfter(chargeOffDate);

            BigDecimal interest = BigDecimal.ZERO;

            if (isPastPeriod) {
                interest = installment.getInterestCharged(currency).minus(installment.getCreditedInterest()).getAmount();
            } else if (isInPeriod) {
                if (transactionCtx instanceof ProgressiveTransactionCtx progressiveTransactionCtx
                        && loan.isInterestBearingAndInterestRecalculationEnabled()) {
                    interest = emiCalculator
                            .getPeriodInterestTillDate(progressiveTransactionCtx.getModel(), installment.getDueDate(), chargeOffDate, true)
                            .getAmount();
                } else {
                    final BigDecimal totalInterest = installment.getInterestOutstanding(currency).getAmount();
                    if (LoanChargeOffBehaviour.ZERO_INTEREST.equals(loan.getLoanProductRelatedDetail().getChargeOffBehaviour())
                            || LoanChargeOffBehaviour.ACCELERATE_MATURITY
                                    .equals(loan.getLoanProductRelatedDetail().getChargeOffBehaviour())) {
                        interest = totalInterest;
                    } else {
                        final long totalDaysInPeriod = ChronoUnit.DAYS.between(installment.getFromDate(), installment.getDueDate());
                        final long daysTillChargeOff = ChronoUnit.DAYS.between(installment.getFromDate(), chargeOffDate);
                        final MathContext mc = MoneyHelper.getMathContext();

                        interest = Money.of(currency, totalInterest.divide(BigDecimal.valueOf(totalDaysInPeriod), mc)
                                .multiply(BigDecimal.valueOf(daysTillChargeOff), mc), mc).getAmount();
                    }
                }
            }
            interestTillChargeOff = interestTillChargeOff.add(interest);
        }

        return interestTillChargeOff;
    }

    private void createMissingAccrualTransactionDuringChargeOffIfNeeded(final BigDecimal newInterest,
            final LoanTransaction chargeOffTransaction, final LocalDate chargeOffDate, final TransactionCtx ctx) {
        final Loan loan = chargeOffTransaction.getLoan();
        final List<LoanRepaymentScheduleInstallment> relevantInstallments = loan.getRepaymentScheduleInstallments().stream()
                .filter(i -> !i.getFromDate().isAfter(chargeOffDate)).toList();

        if (relevantInstallments.isEmpty()) {
            return;
        }

        final BigDecimal sumOfAccrualsTillChargeOff = loan.getLoanTransactions().stream()
                .filter(lt -> lt.isAccrual() && !lt.getTransactionDate().isAfter(chargeOffDate) && lt.isNotReversed())
                .map(lt -> Optional.ofNullable(lt.getInterestPortion()).orElse(BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal sumOfAccrualAdjustmentsTillChargeOff = loan.getLoanTransactions().stream()
                .filter(lt -> lt.isAccrualAdjustment() && !lt.getTransactionDate().isAfter(chargeOffDate) && lt.isNotReversed())
                .map(lt -> Optional.ofNullable(lt.getInterestPortion()).orElse(BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal missingAccrualAmount = newInterest.subtract(sumOfAccrualsTillChargeOff).add(sumOfAccrualAdjustmentsTillChargeOff);

        if (missingAccrualAmount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        final LoanTransaction newAccrualTransaction;

        if (missingAccrualAmount.compareTo(BigDecimal.ZERO) > 0) {
            newAccrualTransaction = accrueTransaction(loan, loan.getOffice(), chargeOffDate, missingAccrualAmount, missingAccrualAmount,
                    ZERO, ZERO, externalIdFactory.create());
        } else {
            newAccrualTransaction = accrualAdjustment(loan, loan.getOffice(), chargeOffDate, missingAccrualAmount.abs(),
                    missingAccrualAmount.abs(), ZERO, ZERO, externalIdFactory.create());
        }

        ctx.getChangedTransactionDetail().addNewTransactionChangeBeforeExistingOne(new TransactionChangeData(null, newAccrualTransaction),
                chargeOffTransaction);
    }

    @Getter
    @Setter
    private static class VerticalPaymentAllocationContext implements LoopContext {

        private final TransactionCtx ctx;
        private final LoanTransaction loanTransaction;
        private final FutureInstallmentAllocationRule futureInstallmentAllocationRule;
        private final List<LoanTransactionToRepaymentScheduleMapping> transactionMappings;
        private final Balances balances;
        private final int firstNormalInstallmentNumber;
        private LoanRepaymentScheduleInstallment installment;
        private Money transactionAmountUnprocessed;
        private Money allocatedAmount;
        private PaymentAllocationType paymentAllocationType;

        VerticalPaymentAllocationContext(TransactionCtx ctx, LoanTransaction loanTransaction,
                FutureInstallmentAllocationRule futureInstallmentAllocationRule,
                List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Balances balances) {
            this.ctx = ctx;
            this.loanTransaction = loanTransaction;
            this.futureInstallmentAllocationRule = futureInstallmentAllocationRule;
            this.transactionMappings = transactionMappings;
            this.balances = balances;
            firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper
                    .fetchFirstNormalInstallmentNumber(getCtx().getInstallments());
        }
    }

    @Getter
    @Setter
    private static class HorizontalPaymentAllocationContext implements LoopContext {

        private final TransactionCtx ctx;
        private final LoanTransaction loanTransaction;
        private final List<PaymentAllocationType> paymentAllocationTypes;
        private final FutureInstallmentAllocationRule futureInstallmentAllocationRule;
        private final List<LoanTransactionToRepaymentScheduleMapping> transactionMappings;
        private final Balances balances;
        private final int firstNormalInstallmentNumber;
        private LoanRepaymentScheduleInstallment installment;
        private Money transactionAmountUnprocessed;
        private Money allocatedAmount;
        private boolean exitCondition;
        private Predicate<LoanRepaymentScheduleInstallment> inAdvanceInstallmentsFilteringRules;

        HorizontalPaymentAllocationContext(TransactionCtx ctx, LoanTransaction loanTransaction,
                List<PaymentAllocationType> paymentAllocationTypes, FutureInstallmentAllocationRule futureInstallmentAllocationRule,
                List<LoanTransactionToRepaymentScheduleMapping> transactionMappings, Balances balances) {
            this.ctx = ctx;
            this.loanTransaction = loanTransaction;
            this.paymentAllocationTypes = paymentAllocationTypes;
            this.futureInstallmentAllocationRule = futureInstallmentAllocationRule;
            this.transactionMappings = transactionMappings;
            this.balances = balances;
            firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper
                    .fetchFirstNormalInstallmentNumber(getCtx().getInstallments());
        }
    }
}
