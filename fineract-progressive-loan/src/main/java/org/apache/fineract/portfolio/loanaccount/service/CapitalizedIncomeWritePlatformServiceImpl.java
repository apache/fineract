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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
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

    @Transactional
    @Override
    public CommandProcessingResult addCapitalizedIncome(final Long loanId, final JsonCommand command) {
        loanTransactionValidator.validateCapitalizedIncome(command, loanId);
        final Loan loan = loanAssembler.assembleFrom(loanId);
        final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());
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
        // TODO: Transaction reverse-replay + loan repayment schedule update (follow-up story)
        // Update loan with capitalized income
        loan.addLoanTransaction(capitalizedIncomeTransaction);
        // Create capitalized income balances
        createCapitalizedIncomeBalance(capitalizedIncomeTransaction);
        // Save and flush (PK is set)
        loanTransactionRepository.saveAndFlush(capitalizedIncomeTransaction);
        // Update loan counters and save
        loan.updateLoanScheduleDependentDerivedFields();
        // Update outstanding loan balances
        loan.updateLoanOutstandingBalances();
        // Create a note if provided
        final String noteText = command.stringValueOfParameterNamed("note");
        if (noteText != null && !noteText.isEmpty()) {
            noteWritePlatformService.createLoanNote(loanId, noteText);
        }
        // Post journal entries
        journalEntryPoster.postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);

        return new CommandProcessingResultBuilder().withEntityId(loan.getId()).withEntityExternalId(loan.getExternalId()).build();
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
        capitalizedIncomeBalanceRepository.save(capitalizedIncomeBalance);
    }
}
