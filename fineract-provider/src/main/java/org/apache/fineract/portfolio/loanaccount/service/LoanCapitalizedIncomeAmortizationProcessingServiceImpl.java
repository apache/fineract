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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import org.jetbrains.annotations.NotNull;
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

    @Override
    @Transactional
    public void processCapitalizedIncomeAmortizationOnLoanClosure(@NotNull final Loan loan) {
        final LocalDate transactionDate = getFinalCapitalizedIncomeAmortizationTransactionDate(loan);
        final LoanTransaction amortizationTransaction = createCapitalizedIncomeAmortizationTransaction(loan, transactionDate, false);
        businessEventNotifierService
                .notifyPostBusinessEvent(new LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent(amortizationTransaction));
    }

    @Override
    @Transactional
    public void processCapitalizedIncomeAmortizationOnLoanChargeOff(@NotNull final Loan loan) {
        final List<Long> existingTransactionIds = loanTransactionRepository.findTransactionIdsByLoan(loan);
        final List<Long> existingReversedTransactionIds = loan.findExistingReversedTransactionIds();

        LocalDate transactionDate = loan.getChargedOffOnDate();
        if (transactionDate == null) {
            transactionDate = DateUtils.getBusinessLocalDate();
        }

        final LoanTransaction amortizationTransaction = createCapitalizedIncomeAmortizationTransaction(loan, transactionDate, true);
        journalEntryPoster.postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        businessEventNotifierService
                .notifyPostBusinessEvent(new LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent(amortizationTransaction));
    }

    private LoanTransaction createCapitalizedIncomeAmortizationTransaction(final Loan loan, final LocalDate transactionDate,
            final boolean isChargeOff) {
        ExternalId externalId = ExternalId.empty();
        BigDecimal totalUnrecognizedAmount = BigDecimal.ZERO;

        if (configurationDomainService.isExternalIdAutoGenerationEnabled()) {
            externalId = ExternalId.generate();
        }

        final List<LoanCapitalizedIncomeBalance> balances = loanCapitalizedIncomeBalanceRepository.findAllByLoanId(loan.getId());
        for (LoanCapitalizedIncomeBalance balance : balances) {
            final BigDecimal unrecognizedAmount = balance.getUnrecognizedAmount();
            totalUnrecognizedAmount = totalUnrecognizedAmount.add(unrecognizedAmount);
            if (isChargeOff) {
                balance.setChargedOffAmount(unrecognizedAmount);
            }
            balance.setUnrecognizedAmount(BigDecimal.ZERO);
        }

        final LoanTransaction amortizationTransaction = LoanTransaction.capitalizedIncomeAmortization(loan, loan.getOffice(),
                transactionDate, totalUnrecognizedAmount, externalId);

        loan.addLoanTransaction(amortizationTransaction);
        loanTransactionRepository.saveAndFlush(amortizationTransaction);

        return amortizationTransaction;
    }

    @Override
    @Transactional
    public void processCapitalizedIncomeAmortizationOnLoanUndoChargeOff(@NotNull final LoanTransaction loanTransaction) {
        final Loan loan = loanTransaction.getLoan();
        final List<Long> existingTransactionIds = loanTransactionRepository.findTransactionIdsByLoan(loan);
        final List<Long> existingReversedTransactionIds = loan.findExistingReversedTransactionIds();

        loan.getLoanTransactions().stream().filter(LoanTransaction::isCapitalizedIncomeAmortization)
                .filter(transaction -> transaction.getTransactionDate().equals(loanTransaction.getTransactionDate()))
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

    private LocalDate getFinalCapitalizedIncomeAmortizationTransactionDate(final Loan loan) {
        return switch (loan.getStatus()) {
            case CLOSED_OBLIGATIONS_MET -> loan.getClosedOnDate();
            case OVERPAID -> loan.getOverpaidOnDate();
            case CLOSED_WRITTEN_OFF -> loan.getWrittenOffOnDate();
            default -> throw new IllegalStateException("Unexpected value: " + loan.getStatus());
        };
    }
}
