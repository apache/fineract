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
package org.apache.fineract.portfolio.loanaccount.service.reamortization;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.reamortization.LoanReAmortizeTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.reamortization.LoanUndoReAmortizeTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.api.LoanReAmortizationApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanReAmortizationServiceImpl {

    private final LoanAssembler loanAssembler;
    private final LoanReAmortizationValidator reAmortizationValidator;
    private final ExternalIdFactory externalIdFactory;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanTransactionRepository loanTransactionRepository;

    public CommandProcessingResult reAmortize(Long loanId, JsonCommand command) {
        Loan loan = loanAssembler.assembleFrom(loanId);
        reAmortizationValidator.validateReAmortize(loan, command);

        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(LoanReAmortizationApiConstants.localeParameterName, command.locale());
        changes.put(LoanReAmortizationApiConstants.dateFormatParameterName, command.dateFormat());

        LoanTransaction reAmortizeTransaction = createReAmortizeTransaction(loan, command);
        loanTransactionRepository.saveAndFlush(reAmortizeTransaction);

        // delinquency recalculation will be triggered by the event in a decoupled way via a listener
        businessEventNotifierService.notifyPostBusinessEvent(new LoanReAmortizeTransactionBusinessEvent(reAmortizeTransaction));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(reAmortizeTransaction.getId()) //
                .withEntityExternalId(reAmortizeTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(command.getLoanId()) //
                .with(changes).build();
    }

    public CommandProcessingResult undoReAmortize(Long loanId, JsonCommand command) {
        Loan loan = loanAssembler.assembleFrom(loanId);
        reAmortizationValidator.validateUndoReAmortize(loan, command);

        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(LoanReAmortizationApiConstants.localeParameterName, command.locale());
        changes.put(LoanReAmortizationApiConstants.dateFormatParameterName, command.dateFormat());

        LoanTransaction reAmortizeTransaction = findLatestNonReversedReAmortizeTransaction(loan);
        if (reAmortizeTransaction == null) {
            // TODO: when validations implemented; throw exception if there isn't a reamortize transaction available
        }
        reverseReAmortizeTransaction(reAmortizeTransaction, command);
        loanTransactionRepository.saveAndFlush(reAmortizeTransaction);

        // delinquency recalculation will be triggered by the event in a decoupled way via a listener
        businessEventNotifierService.notifyPostBusinessEvent(new LoanUndoReAmortizeTransactionBusinessEvent(reAmortizeTransaction));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(reAmortizeTransaction.getId()) //
                .withEntityExternalId(reAmortizeTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(command.getLoanId()) //
                .with(changes).build();
    }

    private void reverseReAmortizeTransaction(LoanTransaction reAmortizeTransaction, JsonCommand command) {
        ExternalId reversalExternalId = externalIdFactory.createFromCommand(command,
                LoanReAmortizationApiConstants.externalIdParameterName);
        reAmortizeTransaction.reverse(reversalExternalId);
        reAmortizeTransaction.manuallyAdjustedOrReversed();
    }

    private LoanTransaction findLatestNonReversedReAmortizeTransaction(Loan loan) {
        return loan.getLoanTransactions().stream() //
                .filter(LoanTransaction::isNotReversed) //
                .filter(LoanTransaction::isReAmortize) //
                .max(Comparator.comparing(LoanTransaction::getTransactionDate)) //
                .orElse(null);
    }

    private LoanTransaction createReAmortizeTransaction(Loan loan, JsonCommand command) {
        ExternalId txExternalId = externalIdFactory.createFromCommand(command, LoanReAmortizationApiConstants.externalIdParameterName);

        // reamortize transaction date is always the current business date
        LocalDate transactionDate = DateUtils.getBusinessLocalDate();

        // in case of a reamortize transaction, only the outstanding principal amount until the business date is
        // considered
        Money txPrincipal = loan.getTotalPrincipalOutstandingUntil(transactionDate);
        BigDecimal txPrincipalAmount = txPrincipal.getAmount();

        return new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.REAMORTIZE.getValue(), transactionDate, txPrincipalAmount,
                txPrincipalAmount, ZERO, ZERO, ZERO, null, false, null, txExternalId);
    }
}
