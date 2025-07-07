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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanCapitalizedIncomeAmortizationAdjustmentTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.util.CapitalizedIncomeAmortizationUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LoanCapitalizedIncomeAmortizationProcessingServiceImpl implements LoanCapitalizedIncomeAmortizationProcessingService {

    private final ConfigurationDomainService configurationDomainService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanJournalEntryPoster journalEntryPoster;
    private final ExternalIdFactory externalIdFactory;

    @Override
    @Transactional
    public void processCapitalizedIncomeAmortizationOnLoanClosure(@NotNull final Loan loan, final boolean addJournal) {
        List<Long> existingTransactionIds = Collections.emptyList();
        List<Long> existingReversedTransactionIds = Collections.emptyList();
        if (addJournal) {
            existingTransactionIds = loanTransactionRepository.findTransactionIdsByLoan(loan);
            existingReversedTransactionIds = loanTransactionRepository.findReversedTransactionIdsByLoan(loan);
        }

        final LocalDate transactionDate = getFinalCapitalizedIncomeAmortizationTransactionDate(loan);
        final Optional<LoanTransaction> amortizationTransaction = createCapitalizedIncomeAmortizationTransaction(loan, transactionDate,
                false, null);
        amortizationTransaction.ifPresent(loanTransaction -> {
            if (loanTransaction.isCapitalizedIncomeAmortization()) {
                businessEventNotifierService
                        .notifyPostBusinessEvent(new LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent(loanTransaction));
            } else {
                businessEventNotifierService.notifyPostBusinessEvent(
                        new LoanCapitalizedIncomeAmortizationAdjustmentTransactionCreatedBusinessEvent(loanTransaction));
            }
        });

        if (addJournal) {
            journalEntryPoster.postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        }
    }

    @Override
    @Transactional
    public void processCapitalizedIncomeAmortizationOnLoanChargeOff(@NotNull final Loan loan,
            @NonNull final LoanTransaction chargeOffTransaction) {
        final List<Long> existingTransactionIds = loanTransactionRepository.findTransactionIdsByLoan(loan);
        final List<Long> existingReversedTransactionIds = loanTransactionRepository.findReversedTransactionIdsByLoan(loan);

        LocalDate transactionDate = loan.getChargedOffOnDate();
        if (transactionDate == null) {
            transactionDate = DateUtils.getBusinessLocalDate();
        }

        final Optional<LoanTransaction> amortizationTransaction = createCapitalizedIncomeAmortizationTransaction(loan, transactionDate,
                true, chargeOffTransaction);
        if (amortizationTransaction.isPresent()) {
            journalEntryPoster.postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
            if (amortizationTransaction.get().isCapitalizedIncomeAmortization()) {
                businessEventNotifierService.notifyPostBusinessEvent(
                        new LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent(amortizationTransaction.get()));
            } else {
                businessEventNotifierService.notifyPostBusinessEvent(
                        new LoanCapitalizedIncomeAmortizationAdjustmentTransactionCreatedBusinessEvent(amortizationTransaction.get()));
            }
        }
    }

    private Optional<LoanTransaction> createCapitalizedIncomeAmortizationTransaction(final Loan loan, final LocalDate transactionDate,
            final boolean isChargeOff, final LoanTransaction chargeOffTransaction) {
        ExternalId externalId = ExternalId.empty();

        if (configurationDomainService.isExternalIdAutoGenerationEnabled()) {
            externalId = ExternalId.generate();
        }

        final List<LoanCapitalizedIncomeBalance> balances = loanCapitalizedIncomeBalanceRepository.findAllByLoanId(loan.getId());

        BigDecimal totalAmortizationAmount = BigDecimal.ZERO;
        for (LoanCapitalizedIncomeBalance balance : balances) {
            List<LoanTransaction> adjustments = loanTransactionRepository.findAdjustments(balance.getLoanTransaction());
            LocalDate maturityDate = loan.getMaturityDate() != null ? loan.getMaturityDate() : transactionDate;
            final Money amortizationTillDate = CapitalizedIncomeAmortizationUtil.calculateTotalAmortizationTillDate(balance, adjustments,
                    maturityDate, loan.getLoanProductRelatedDetail().getCapitalizedIncomeStrategy(), maturityDate, loan.getCurrency());
            totalAmortizationAmount = totalAmortizationAmount.add(amortizationTillDate.getAmount());
            if (isChargeOff) {
                balance.setChargedOffAmount(balance.getUnrecognizedAmount());
            }
            balance.setUnrecognizedAmount(BigDecimal.ZERO);
        }

        BigDecimal amortizedAmount = loanTransactionRepository.getAmortizedAmountCapitalizedIncome(loan);
        BigDecimal totalUnrecognizedAmount = totalAmortizationAmount.subtract(amortizedAmount);
        if (MathUtil.isZero(totalUnrecognizedAmount)) {
            return Optional.empty();
        }

        final LoanTransaction amortizationTransaction = MathUtil.isGreaterThanZero(totalUnrecognizedAmount)
                ? LoanTransaction.capitalizedIncomeAmortization(loan, loan.getOffice(), transactionDate, totalUnrecognizedAmount,
                        externalId)
                : LoanTransaction.capitalizedIncomeAmortizationAdjustment(loan,
                        Money.of(loan.getCurrency(), MathUtil.negate(totalUnrecognizedAmount)), transactionDate, externalId);
        if (isChargeOff) {
            amortizationTransaction.getLoanTransactionRelations().add(LoanTransactionRelation.linkToTransaction(amortizationTransaction,
                    chargeOffTransaction, LoanTransactionRelationTypeEnum.RELATED));
        }

        loan.addLoanTransaction(amortizationTransaction);
        loanTransactionRepository.saveAndFlush(amortizationTransaction);

        return Optional.of(amortizationTransaction);
    }

    @Override
    @Transactional
    public void processCapitalizedIncomeAmortizationOnLoanUndoChargeOff(@NotNull final LoanTransaction loanTransaction) {
        final Loan loan = loanTransaction.getLoan();
        final List<Long> existingTransactionIds = loanTransactionRepository.findTransactionIdsByLoan(loan);
        final List<Long> existingReversedTransactionIds = loanTransactionRepository.findReversedTransactionIdsByLoan(loan);

        loan.getLoanTransactions().stream().filter(LoanTransaction::isCapitalizedIncomeAmortization)
                .filter(transaction -> transaction.getTransactionDate().equals(loanTransaction.getTransactionDate())
                        && transaction.getLoanTransactionRelations().stream()
                                .anyMatch(rel -> LoanTransactionRelationTypeEnum.RELATED.equals(rel.getRelationType())
                                        && rel.getToTransaction().equals(loanTransaction)))
                .forEach(transaction -> {
                    transaction.reverse();
                    final LoanAdjustTransactionBusinessEvent.Data data = new LoanAdjustTransactionBusinessEvent.Data(transaction);
                    businessEventNotifierService.notifyPostBusinessEvent(new LoanAdjustTransactionBusinessEvent(data));

                });

        for (LoanCapitalizedIncomeBalance balance : loanCapitalizedIncomeBalanceRepository.findAllByLoanId(loan.getId())) {
            balance.setUnrecognizedAmount(balance.getChargedOffAmount());
            balance.setChargedOffAmount(BigDecimal.ZERO);
        }

        journalEntryPoster.postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
    }

    @Override
    @Transactional
    public void processCapitalizedIncomeAmortizationTillDate(@NonNull final Loan loan, @NonNull final LocalDate tillDate,
            final boolean addJournal) {
        final List<Long> existingTransactionIds = loanTransactionRepository.findTransactionIdsByLoan(loan);
        final List<Long> existingReversedTransactionIds = loanTransactionRepository.findReversedTransactionIdsByLoan(loan);

        List<LoanCapitalizedIncomeBalance> balances = loanCapitalizedIncomeBalanceRepository.findAllByLoanId(loan.getId());

        LocalDate maturityDate = loan.getMaturityDate() != null ? loan.getMaturityDate()
                : getFinalCapitalizedIncomeAmortizationTransactionDate(loan);
        LocalDate tillDatePlusOne = tillDate.plusDays(1);
        if (tillDatePlusOne.isAfter(maturityDate)) {
            tillDatePlusOne = maturityDate;
        }

        Money totalAmortization = Money.zero(loan.getCurrency());
        for (LoanCapitalizedIncomeBalance balance : balances) {
            List<LoanTransaction> adjustments = loanTransactionRepository.findAdjustments(balance.getLoanTransaction());
            Money amortizationTillDate = CapitalizedIncomeAmortizationUtil.calculateTotalAmortizationTillDate(balance, adjustments,
                    maturityDate, loan.getLoanProductRelatedDetail().getCapitalizedIncomeStrategy(), tillDatePlusOne, loan.getCurrency());
            totalAmortization = totalAmortization.add(amortizationTillDate);

            balance.setUnrecognizedAmount(balance.getAmount().subtract(MathUtil.nullToZero(balance.getAmountAdjustment()))
                    .subtract(amortizationTillDate.getAmount()));
        }

        loanCapitalizedIncomeBalanceRepository.saveAll(balances);

        BigDecimal totalAmortized = loanTransactionRepository.getAmortizedAmountCapitalizedIncome(loan);
        BigDecimal totalAmortizationAmount = totalAmortization.getAmount().subtract(totalAmortized);

        if (!MathUtil.isZero(totalAmortizationAmount)) {
            LoanTransaction transaction = MathUtil.isGreaterThanZero(totalAmortizationAmount)
                    ? LoanTransaction.capitalizedIncomeAmortization(loan, loan.getOffice(), tillDate, totalAmortizationAmount,
                            externalIdFactory.create())
                    : LoanTransaction.capitalizedIncomeAmortizationAdjustment(loan,
                            Money.of(loan.getCurrency(), MathUtil.negate(totalAmortizationAmount)), tillDate, externalIdFactory.create());
            loan.addLoanTransaction(transaction);

            transaction = loanTransactionRepository.save(transaction);
            loanTransactionRepository.flush();

            if (addJournal) {
                journalEntryPoster.postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
            }

            BusinessEvent<?> event = MathUtil.isGreaterThanZero(totalAmortizationAmount)
                    ? new LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent(transaction)
                    : new LoanCapitalizedIncomeAmortizationAdjustmentTransactionCreatedBusinessEvent(transaction);
            businessEventNotifierService.notifyPostBusinessEvent(event);
        }
    }

    private LocalDate getFinalCapitalizedIncomeAmortizationTransactionDate(final Loan loan) {
        return switch (loan.getStatus()) {
            case CLOSED_OBLIGATIONS_MET -> loan.getClosedOnDate();
            case OVERPAID -> loan.getOverpaidOnDate();
            case CLOSED_WRITTEN_OFF -> loan.getWrittenOffOnDate();
            default -> throw new IllegalStateException("Unexpected value: " + loan.getStatus());
        };
    }
}
