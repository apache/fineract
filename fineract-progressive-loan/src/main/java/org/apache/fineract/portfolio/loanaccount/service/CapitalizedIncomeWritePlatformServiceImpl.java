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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanCapitalizedIncomeAdjustmentTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanCapitalizedIncomeTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import org.apache.fineract.portfolio.note.service.NoteWritePlatformService;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class CapitalizedIncomeWritePlatformServiceImpl implements CapitalizedIncomePlatformService {

    private final ProgressiveLoanTransactionValidator loanTransactionValidator;
    private final LoanAssembler loanAssembler;
    private final LoanTransactionRepository loanTransactionRepository;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final LoanJournalEntryPoster journalEntryPoster;
    private final NoteWritePlatformService noteWritePlatformService;
    private final ExternalIdFactory externalIdFactory;
    private final LoanCapitalizedIncomeBalanceRepository capitalizedIncomeBalanceRepository;
    private final ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    private final LoanBalanceService loanBalanceService;
    private final LoanLifecycleStateMachine loanLifecycleStateMachine;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Transactional
    @Override
    public CommandProcessingResult addCapitalizedIncome(final Long loanId, final JsonCommand command) {
        loanTransactionValidator.validateCapitalizedIncome(command, loanId);
        final Loan loan = loanAssembler.assembleFrom(loanId);
        final List<Long> existingTransactionIds = new ArrayList<>(loanTransactionRepository.findTransactionIdsByLoan(loan));
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loanTransactionRepository.findReversedTransactionIdsByLoan(loan));
        final Map<String, Object> changes = new LinkedHashMap<>();
        // Create payment details
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        // Extract transaction details
        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final ExternalId txnExternalId = externalIdFactory.createFromCommand(command, "externalId");

        // Create capitalized income transaction
        final Money capitalizedIncomeAmount = calculateCapitalizedIncomeAmount(loan, transactionAmount);
        final LoanTransaction capitalizedIncomeTransaction = LoanTransaction.capitalizedIncome(loan, capitalizedIncomeAmount, paymentDetail,
                transactionDate, txnExternalId);
        // Update loan with capitalized income
        loan.addLoanTransaction(capitalizedIncomeTransaction);
        // Recalculate loan transactions
        recalculateLoanTransactions(loan, transactionDate, capitalizedIncomeTransaction);
        // Save and flush (PK is set)
        loanTransactionRepository.saveAndFlush(capitalizedIncomeTransaction);
        // Create capitalized income balances
        createCapitalizedIncomeBalance(capitalizedIncomeTransaction);
        // Update loan counters and save
        loan.updateLoanScheduleDependentDerivedFields();
        // Update loan summary data
        loanBalanceService.updateLoanSummaryDerivedFields(loan);
        // Create a note if provided
        final String noteText = command.stringValueOfParameterNamed("note");
        if (noteText != null && !noteText.isEmpty()) {
            noteWritePlatformService.createLoanTransactionNote(capitalizedIncomeTransaction.getId(), noteText);
        }

        // Post journal entries
        journalEntryPoster.postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        loanLifecycleStateMachine.determineAndTransition(loan, transactionDate);

        businessEventNotifierService
                .notifyPostBusinessEvent(new LoanCapitalizedIncomeTransactionCreatedBusinessEvent(capitalizedIncomeTransaction));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        return new CommandProcessingResultBuilder() //
                .withEntityId(capitalizedIncomeTransaction.getId()) //
                .withEntityExternalId(capitalizedIncomeTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withLoanId(loan.getId()) //
                .build();
    }

    @Override
    public CommandProcessingResult capitalizedIncomeAdjustment(final Long loanId, final Long capitalizedIncomeTransactionId,
            final JsonCommand command) {
        loanTransactionValidator.validateCapitalizedIncomeAdjustment(command, loanId, capitalizedIncomeTransactionId);
        final Loan loan = loanAssembler.assembleFrom(loanId);
        final List<Long> existingTransactionIds = new ArrayList<>(loanTransactionRepository.findTransactionIdsByLoan(loan));
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loanTransactionRepository.findReversedTransactionIdsByLoan(loan));
        final Map<String, Object> changes = new LinkedHashMap<>();
        // Create payment details
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        // Extract transaction details
        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final ExternalId txnExternalId = externalIdFactory.createFromCommand(command, "externalId");

        Optional<LoanTransaction> capitalizedIncome = loanTransactionRepository.findById(capitalizedIncomeTransactionId);
        LoanTransaction capitalizedIncomeAdjustment = LoanTransaction.capitalizedIncomeAdjustment(loan,
                Money.of(loan.getCurrency(), transactionAmount), paymentDetail, transactionDate, txnExternalId);
        capitalizedIncomeAdjustment.getLoanTransactionRelations().add(LoanTransactionRelation.linkToTransaction(capitalizedIncomeAdjustment,
                capitalizedIncome.get(), LoanTransactionRelationTypeEnum.ADJUSTMENT));
        loan.addLoanTransaction(capitalizedIncomeAdjustment);
        recalculateLoanTransactions(loan, transactionDate, capitalizedIncomeAdjustment);
        LoanTransaction savedCapitalizedIncomeAdjustment = loanTransactionRepository.saveAndFlush(capitalizedIncomeAdjustment);

        // Update outstanding loan balances
        loanBalanceService.updateLoanOutstandingBalances(loan);

        // Create a note if provided
        final String noteText = command.stringValueOfParameterNamed("note");
        if (noteText != null && !noteText.isEmpty()) {
            noteWritePlatformService.createLoanTransactionNote(savedCapitalizedIncomeAdjustment.getId(), noteText);
        }
        // Post journal entries
        journalEntryPoster.postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        LoanCapitalizedIncomeBalance capitalizedIncomeBalance = capitalizedIncomeBalanceRepository.findByLoanIdAndLoanTransactionId(loanId,
                capitalizedIncomeTransactionId);
        capitalizedIncomeBalance
                .setAmountAdjustment(MathUtil.nullToZero(capitalizedIncomeBalance.getAmountAdjustment()).add(transactionAmount));
        capitalizedIncomeBalance.setUnrecognizedAmount(
                MathUtil.negativeToZero(capitalizedIncomeBalance.getUnrecognizedAmount().subtract(transactionAmount)));
        capitalizedIncomeBalanceRepository.save(capitalizedIncomeBalance);

        loanLifecycleStateMachine.determineAndTransition(loan, transactionDate);

        businessEventNotifierService.notifyPostBusinessEvent(
                new LoanCapitalizedIncomeAdjustmentTransactionCreatedBusinessEvent(savedCapitalizedIncomeAdjustment));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));

        return new CommandProcessingResultBuilder() //
                .withEntityId(savedCapitalizedIncomeAdjustment.getId()) //
                .withEntityExternalId(savedCapitalizedIncomeAdjustment.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withLoanId(loan.getId()) //
                .build();
    }

    private void recalculateLoanTransactions(Loan loan, LocalDate transactionDate, LoanTransaction transaction) {
        if (loan.isInterestBearingAndInterestRecalculationEnabled() || DateUtils.isBeforeBusinessDate(transactionDate)) {
            reprocessLoanTransactionsService.reprocessTransactions(loan);
        } else {
            reprocessLoanTransactionsService.processLatestTransaction(transaction, loan);
        }
    }

    @Override
    public void resetBalance(final Long loanId) {
        capitalizedIncomeBalanceRepository
                .delete((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("loan").get("id"), loanId));
    }

    private Money calculateCapitalizedIncomeAmount(final Loan loan, final BigDecimal transactionAmount) {
        return switch (loan.getLoanProductRelatedDetail().getCapitalizedIncomeCalculationType()) {
            case FLAT -> Money.of(loan.getCurrency(), transactionAmount);
        };
    }

    private void createCapitalizedIncomeBalance(final LoanTransaction capitalizedIncomeTransaction) {
        LoanCapitalizedIncomeBalance capitalizedIncomeBalance = new LoanCapitalizedIncomeBalance();
        capitalizedIncomeBalance.setLoan(capitalizedIncomeTransaction.getLoan());
        capitalizedIncomeBalance.setLoanTransaction(capitalizedIncomeTransaction);
        capitalizedIncomeBalance.setDate(capitalizedIncomeTransaction.getTransactionDate());
        capitalizedIncomeBalance.setAmount(capitalizedIncomeTransaction.getAmount());
        capitalizedIncomeBalance.setUnrecognizedAmount(capitalizedIncomeTransaction.getAmount());
        capitalizedIncomeBalanceRepository.saveAndFlush(capitalizedIncomeBalance);
    }

}
