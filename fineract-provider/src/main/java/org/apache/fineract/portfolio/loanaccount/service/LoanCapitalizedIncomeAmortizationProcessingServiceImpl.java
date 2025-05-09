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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
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

    @Override
    @Transactional
    public void processCapitalizedIncomeAmortizationOnLoanClosure(@NotNull final Loan loan) {
        final LocalDate finalAmortizationTransactionDate = getFinalCapitalizedIncomeAmortizationTransactionDate(loan);
        ExternalId externalId = ExternalId.empty();
        BigDecimal totalUnrecognizedAmountCapitalizedIncomeAmount = BigDecimal.ZERO;

        final boolean isExternalIdAutoGenerationEnabled = configurationDomainService.isExternalIdAutoGenerationEnabled();
        if (isExternalIdAutoGenerationEnabled) {
            externalId = ExternalId.generate();
        }

        for (LoanCapitalizedIncomeBalance balance : loanCapitalizedIncomeBalanceRepository.findAllByLoanId(loan.getId())) {
            totalUnrecognizedAmountCapitalizedIncomeAmount = totalUnrecognizedAmountCapitalizedIncomeAmount
                    .add(balance.getUnrecognizedAmount());
            balance.setUnrecognizedAmount(BigDecimal.ZERO);
        }

        final LoanTransaction finalCapitalizedIncomeAmortization = LoanTransaction.capitalizedIncomeAmortization(loan, loan.getOffice(),
                finalAmortizationTransactionDate, totalUnrecognizedAmountCapitalizedIncomeAmount, externalId);
        loan.addLoanTransaction(finalCapitalizedIncomeAmortization);
        loanTransactionRepository.saveAndFlush(finalCapitalizedIncomeAmortization);
        businessEventNotifierService.notifyPostBusinessEvent(
                new LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent(finalCapitalizedIncomeAmortization));
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
