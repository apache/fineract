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
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanBuyDownFeeAmortizationAdjustmentTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.AmortizationType;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAmortizationAllocationMapping;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.repository.LoanBuyDownFeeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.util.BuyDownFeeAmortizationUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LoanBuyDownFeeAmortizationProcessingServiceImpl implements LoanBuyDownFeeAmortizationProcessingService {

    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanBuyDownFeeBalanceRepository loanBuyDownFeeBalanceRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanJournalEntryPoster journalEntryPoster;
    private final ExternalIdFactory externalIdFactory;
    private final LoanAmortizationAllocationService loanAmortizationAllocationService;

    @Override
    @Transactional
    public void processBuyDownFeeAmortizationTillDate(@NonNull Loan loan, @NonNull LocalDate tillDate, boolean addJournal) {
        final List<LoanBuyDownFeeBalance> balances = loanBuyDownFeeBalanceRepository.findAllByLoanIdAndClosedFalse(loan.getId());

        final LocalDate maturityDate = loan.getMaturityDate() != null ? loan.getMaturityDate()
                : getFinalBuyDownFeeAmortizationTransactionDate(loan);
        LocalDate tillDatePlusOne = tillDate.plusDays(1);
        if (tillDatePlusOne.isAfter(maturityDate)) {
            tillDatePlusOne = maturityDate;
        }

        final List<LoanAmortizationAllocationMapping> loanAmortizationAllocationMappings = new ArrayList<>();

        Money totalAmortization = Money.zero(loan.getCurrency());
        final BigDecimal totalAmortized = loanTransactionRepository.getAmortizedAmountBuyDownFee(loan);
        for (LoanBuyDownFeeBalance balance : balances) {
            BigDecimal amortizationAmount;
            AmortizationType amortizationType;
            if (!balance.isDeleted()) {
                final List<LoanTransaction> adjustments = loanTransactionRepository.findAdjustments(balance.getLoanTransaction());
                final BigDecimal alreadyAmortizedAmount = loanAmortizationAllocationService
                        .calculateAlreadyAmortizedAmount(balance.getLoanTransaction().getId(), loan.getId());
                if (MathUtil.isZero(balance.getUnrecognizedAmount()) && adjustments.isEmpty()) {
                    totalAmortization = totalAmortization.add(Money.of(loan.getCurrency(), alreadyAmortizedAmount));
                    continue;
                }
                final BigDecimal grossAmortizedAmount = loanAmortizationAllocationService
                        .calculateGrossAmortizedAmount(balance.getLoanTransaction().getId(), loan.getId());
                final BigDecimal netBuyDownFeeAmount = MathUtil.subtract(balance.getAmount(),
                        MathUtil.nullToZero(balance.getAmountAdjustment()));
                final boolean fullyAmortizedOnSaleOrClosure = MathUtil.isZero(balance.getUnrecognizedAmount())
                        && grossAmortizedAmount.compareTo(netBuyDownFeeAmount) >= 0;
                final LocalDate effectiveTillDate = fullyAmortizedOnSaleOrClosure ? maturityDate : tillDatePlusOne;
                final Money amortizationTillDate = BuyDownFeeAmortizationUtil.calculateTotalAmortizationTillDate(balance, adjustments,
                        maturityDate, loan.getLoanProductRelatedDetail().getBuyDownFeeStrategy(), effectiveTillDate, loan.getCurrency());
                totalAmortization = totalAmortization.add(amortizationTillDate);
                if (alreadyAmortizedAmount.compareTo(amortizationTillDate.getAmount()) > 0) {
                    amortizationAmount = alreadyAmortizedAmount.subtract(amortizationTillDate.getAmount());
                    amortizationType = AmortizationType.AM_ADJ;
                } else {
                    amortizationAmount = amortizationTillDate.getAmount().subtract(alreadyAmortizedAmount);
                    amortizationType = AmortizationType.AM;
                }
                balance.setUnrecognizedAmount(
                        MathUtil.subtract(balance.getAmount(), balance.getAmountAdjustment(), amortizationTillDate.getAmount()));
            } else {
                amortizationAmount = balance.getAmount().subtract(balance.getUnrecognizedAmount());
                amortizationType = AmortizationType.AM_ADJ;
                balance.setClosed(true);
            }
            if (amortizationAmount.compareTo(BigDecimal.ZERO) > 0) {
                final LoanAmortizationAllocationMapping loanAmortizationAllocationMapping = loanAmortizationAllocationService
                        .createAmortizationAllocationMappingWithBaseLoanTransaction(balance.getLoanTransaction(), amortizationAmount,
                                amortizationType);
                loanAmortizationAllocationMappings.add(loanAmortizationAllocationMapping);
            }
        }

        loanBuyDownFeeBalanceRepository.saveAll(balances);
        final BigDecimal totalAmortizationAmount = totalAmortization.getAmount().subtract(totalAmortized);

        if (!MathUtil.isZero(totalAmortizationAmount)) {
            LoanTransaction transaction = MathUtil.isGreaterThanZero(totalAmortizationAmount)
                    ? LoanTransaction.buyDownFeeAmortization(loan, loan.getOffice(), tillDate, totalAmortizationAmount,
                            externalIdFactory.create())
                    : LoanTransaction.buyDownFeeAmortizationAdjustment(loan,
                            Money.of(loan.getCurrency(), MathUtil.negate(totalAmortizationAmount)), tillDate, externalIdFactory.create());
            loan.addLoanTransaction(transaction);

            transaction = loanTransactionRepository.saveAndFlush(transaction);
            final LoanTransaction finalTransaction = transaction;
            loanAmortizationAllocationMappings.forEach(loanAmortizationAllocationMapping -> loanAmortizationAllocationService
                    .setAmortizationTransactionDataAndSaveAmortizationAllocationMapping(loanAmortizationAllocationMapping,
                            finalTransaction));

            if (addJournal) {
                journalEntryPoster.postJournalEntriesForLoanTransaction(transaction, false, false);
            }

            final BusinessEvent<?> event = MathUtil.isGreaterThanZero(totalAmortizationAmount)
                    ? new LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent(transaction)
                    : new LoanBuyDownFeeAmortizationAdjustmentTransactionCreatedBusinessEvent(transaction);
            businessEventNotifierService.notifyPostBusinessEvent(event);
        }
    }

    @Override
    @Transactional
    public void processBuyDownFeeAmortizationOnLoanClosure(@NonNull final Loan loan, final boolean addJournal) {
        processRemainingBuyDownFeeAmortization(loan, getFinalBuyDownFeeAmortizationTransactionDate(loan), addJournal);
    }

    @Override
    @Transactional
    public void processBuyDownFeeAmortizationOnLoanSale(@NonNull final Loan loan, @NonNull final LocalDate transactionDate,
            final boolean addJournal) {
        processRemainingBuyDownFeeAmortization(loan, transactionDate, addJournal);
    }

    @Override
    @Transactional
    public void processBuyDownFeeAmortizationImmediately(@NonNull final Loan loan,
            @NonNull final LoanTransaction buyDownFeeRelatedTransaction, @NonNull final LocalDate transactionDate,
            final boolean addJournal) {
        final LoanBuyDownFeeBalance relatedBalance = resolveRelatedBuyDownFeeBalance(loan, buyDownFeeRelatedTransaction);
        if (relatedBalance == null) {
            return;
        }
        // Recognize only the posted fee/adjustment balance so other deferred balances keep daily COB amortization.
        createBuyDownFeeAmortizationTransaction(loan, transactionDate, false, null, List.of(relatedBalance), false)
                .ifPresent(loanTransaction -> notifyAndPostJournal(loanTransaction, addJournal));
    }

    private void processRemainingBuyDownFeeAmortization(@NonNull final Loan loan, @NonNull final LocalDate transactionDate,
            final boolean addJournal) {
        createBuyDownFeeAmortizationTransaction(loan, transactionDate, false, null,
                loanBuyDownFeeBalanceRepository.findAllByLoanIdAndClosedFalse(loan.getId()), true)
                .ifPresent(loanTransaction -> notifyAndPostJournal(loanTransaction, addJournal));
    }

    private LoanBuyDownFeeBalance resolveRelatedBuyDownFeeBalance(final Loan loan, final LoanTransaction buyDownFeeRelatedTransaction) {
        if (buyDownFeeRelatedTransaction.isBuyDownFee()) {
            // Includes deleted balances (e.g. after reverse) so recognized income can still be adjusted.
            return loanBuyDownFeeBalanceRepository.findAllByLoanIdAndClosedFalse(loan.getId()).stream()
                    .filter(balance -> balance.getLoanTransaction() != null
                            && Objects.equals(balance.getLoanTransaction().getId(), buyDownFeeRelatedTransaction.getId()))
                    .findFirst().orElse(null);
        }
        if (buyDownFeeRelatedTransaction.isBuyDownFeeAdjustment()) {
            return loanBuyDownFeeBalanceRepository.findBalanceForAdjustment(buyDownFeeRelatedTransaction.getId());
        }
        return null;
    }

    @Override
    @Transactional
    public void processBuyDownFeeAmortizationOnLoanChargeOff(@NonNull final Loan loan,
            @NonNull final LoanTransaction chargeOffTransaction) {
        LocalDate transactionDate = loan.getChargedOffOnDate();
        if (transactionDate == null) {
            transactionDate = DateUtils.getBusinessLocalDate();
        }

        final Optional<LoanTransaction> amortizationTransaction = createBuyDownFeeAmortizationTransaction(loan, transactionDate, true,
                chargeOffTransaction, loanBuyDownFeeBalanceRepository.findAllByLoanIdAndClosedFalse(loan.getId()), true);
        amortizationTransaction.ifPresent(loanTransaction -> notifyAndPostJournal(loanTransaction, true));
    }

    @Override
    @Transactional
    public void processBuyDownFeeAmortizationOnLoanUndoChargeOff(@NonNull final LoanTransaction loanTransaction) {
        final Loan loan = loanTransaction.getLoan();

        loan.getLoanTransactions().stream().filter(LoanTransaction::isBuyDownFeeAmortization)
                .filter(transaction -> transaction.getTransactionDate().equals(loanTransaction.getTransactionDate())
                        && transaction.getLoanTransactionRelations().stream()
                                .anyMatch(rel -> LoanTransactionRelationTypeEnum.RELATED.equals(rel.getRelationType())
                                        && rel.getToTransaction().equals(loanTransaction)))
                .forEach(transaction -> {
                    transaction.reverse();
                    journalEntryPoster.postJournalEntriesForLoanTransaction(transaction, false, false);
                    final LoanAdjustTransactionBusinessEvent.Data data = new LoanAdjustTransactionBusinessEvent.Data(transaction);
                    businessEventNotifierService.notifyPostBusinessEvent(new LoanAdjustTransactionBusinessEvent(data));
                });

        for (LoanBuyDownFeeBalance balance : loanBuyDownFeeBalanceRepository.findAllByLoanIdAndDeletedFalseAndClosedFalse(loan.getId())) {
            balance.setUnrecognizedAmount(balance.getChargedOffAmount());
            balance.setChargedOffAmount(BigDecimal.ZERO);
        }
    }

    /**
     * @param balances
     *            balances to recognize (all open balances for sale/closure/charge-off, or a single related balance for
     *            IMMEDIATE)
     * @param useLoanWideAmortizedTotal
     *            when true (sale/closure/charge-off), transaction amount is loan-wide remaining unrecognized; when
     *            false (IMMEDIATE), amount is derived only from the given balances so other fees are not touched
     */
    private Optional<LoanTransaction> createBuyDownFeeAmortizationTransaction(final Loan loan, final LocalDate transactionDate,
            final boolean isChargeOff, final LoanTransaction chargeOffTransaction, final List<LoanBuyDownFeeBalance> balances,
            final boolean useLoanWideAmortizedTotal) {
        final ExternalId externalId = externalIdFactory.create();
        final List<LoanAmortizationAllocationMapping> loanAmortizationAllocationMappings = new ArrayList<>();

        BigDecimal totalAmortization = BigDecimal.ZERO;
        BigDecimal selectedBalancesNetAmount = BigDecimal.ZERO;
        final BigDecimal totalAmortized = loanTransactionRepository.getAmortizedAmountBuyDownFee(loan);
        for (LoanBuyDownFeeBalance balance : balances) {
            BigDecimal amortizationAmount;
            AmortizationType amortizationType;
            if (!balance.isDeleted()) {
                final List<LoanTransaction> adjustments = loanTransactionRepository.findAdjustments(balance.getLoanTransaction());
                final LocalDate maturityDate = loan.getMaturityDate() != null ? loan.getMaturityDate() : transactionDate;
                final Money amortizationTillDate = BuyDownFeeAmortizationUtil.calculateTotalAmortizationTillDate(balance, adjustments,
                        maturityDate, loan.getLoanProductRelatedDetail().getBuyDownFeeStrategy(), maturityDate, loan.getCurrency());
                totalAmortization = totalAmortization.add(amortizationTillDate.getAmount());
                final BigDecimal alreadyAmortizedAmount = loanAmortizationAllocationService
                        .calculateAlreadyAmortizedAmount(balance.getLoanTransaction().getId(), loan.getId());
                if (alreadyAmortizedAmount.compareTo(amortizationTillDate.getAmount()) > 0) {
                    amortizationAmount = alreadyAmortizedAmount.subtract(amortizationTillDate.getAmount());
                    amortizationType = AmortizationType.AM_ADJ;
                    selectedBalancesNetAmount = selectedBalancesNetAmount.subtract(amortizationAmount);
                } else {
                    amortizationAmount = amortizationTillDate.getAmount().subtract(alreadyAmortizedAmount);
                    amortizationType = AmortizationType.AM;
                    selectedBalancesNetAmount = selectedBalancesNetAmount.add(amortizationAmount);
                }
                if (isChargeOff) {
                    balance.setChargedOffAmount(balance.getUnrecognizedAmount());
                }
                balance.setUnrecognizedAmount(BigDecimal.ZERO);
            } else {
                amortizationAmount = balance.getAmount().subtract(balance.getUnrecognizedAmount());
                amortizationType = AmortizationType.AM_ADJ;
                selectedBalancesNetAmount = selectedBalancesNetAmount.subtract(amortizationAmount);
                balance.setClosed(true);
            }
            if (amortizationAmount.compareTo(BigDecimal.ZERO) > 0) {
                final LoanAmortizationAllocationMapping loanAmortizationAllocationMapping = loanAmortizationAllocationService
                        .createAmortizationAllocationMappingWithBaseLoanTransaction(balance.getLoanTransaction(), amortizationAmount,
                                amortizationType);
                loanAmortizationAllocationMappings.add(loanAmortizationAllocationMapping);
            }
        }

        loanBuyDownFeeBalanceRepository.saveAll(balances);

        final BigDecimal amortizationTransactionAmount;
        if (useLoanWideAmortizedTotal) {
            final BigDecimal totalUnrecognizedAmount = totalAmortization.subtract(totalAmortized);
            if (MathUtil.isZero(totalUnrecognizedAmount)) {
                return Optional.empty();
            }
            amortizationTransactionAmount = totalUnrecognizedAmount;
        } else {
            if (MathUtil.isZero(selectedBalancesNetAmount)) {
                return Optional.empty();
            }
            amortizationTransactionAmount = selectedBalancesNetAmount;
        }

        final LoanTransaction amortizationTransaction = MathUtil.isGreaterThanZero(amortizationTransactionAmount)
                ? LoanTransaction.buyDownFeeAmortization(loan, loan.getOffice(), transactionDate, amortizationTransactionAmount, externalId)
                : LoanTransaction.buyDownFeeAmortizationAdjustment(loan,
                        Money.of(loan.getCurrency(), MathUtil.negate(amortizationTransactionAmount)), transactionDate, externalId);
        if (isChargeOff) {
            amortizationTransaction.getLoanTransactionRelations().add(LoanTransactionRelation.linkToTransaction(amortizationTransaction,
                    chargeOffTransaction, LoanTransactionRelationTypeEnum.RELATED));
        }

        loan.addLoanTransaction(amortizationTransaction);
        loanTransactionRepository.saveAndFlush(amortizationTransaction);
        loanAmortizationAllocationMappings.forEach(loanAmortizationAllocationMapping -> loanAmortizationAllocationService
                .setAmortizationTransactionDataAndSaveAmortizationAllocationMapping(loanAmortizationAllocationMapping,
                        amortizationTransaction));

        return Optional.of(amortizationTransaction);
    }

    private void notifyAndPostJournal(final LoanTransaction amortizationTransaction, final boolean addJournal) {
        if (amortizationTransaction.isBuyDownFeeAmortization()) {
            businessEventNotifierService
                    .notifyPostBusinessEvent(new LoanBuyDownFeeAmortizationTransactionCreatedBusinessEvent(amortizationTransaction));
        } else {
            businessEventNotifierService.notifyPostBusinessEvent(
                    new LoanBuyDownFeeAmortizationAdjustmentTransactionCreatedBusinessEvent(amortizationTransaction));
        }
        if (addJournal) {
            journalEntryPoster.postJournalEntriesForLoanTransaction(amortizationTransaction, false, false);
        }
    }

    private LocalDate getFinalBuyDownFeeAmortizationTransactionDate(final Loan loan) {
        return switch (loan.getStatus()) {
            case CLOSED_OBLIGATIONS_MET -> loan.getClosedOnDate();
            case OVERPAID -> loan.getOverpaidOnDate();
            case CLOSED_WRITTEN_OFF -> loan.getWrittenOffOnDate();
            default -> throw new IllegalStateException("Unexpected value: " + loan.getStatus());
        };
    }
}
