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
package org.apache.fineract.portfolio.loanaccount.service.reaging;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.event.business.domain.loan.reaging.LoanReAgeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.reaging.LoanUndoReAgeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.reaging.LoanReAgeTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.reaging.LoanUndoReAgeTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.api.LoanReAgingApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.reaging.LoanReAgeParameter;
import org.apache.fineract.portfolio.loanaccount.domain.reaging.LoanReAgingParameterRepository;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.MoneyHolder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.TransactionCtx;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanReAgingServiceImpl {

    private final LoanAssembler loanAssembler;
    private final LoanReAgingValidator reAgingValidator;
    private final ExternalIdFactory externalIdFactory;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanReAgingParameterRepository reAgingParameterRepository;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final NoteRepository noteRepository;

    public CommandProcessingResult reAge(Long loanId, JsonCommand command) {
        Loan loan = loanAssembler.assembleFrom(loanId);
        reAgingValidator.validateReAge(loan, command);

        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(LoanReAgingApiConstants.localeParameterName, command.locale());
        changes.put(LoanReAgingApiConstants.dateFormatParameterName, command.dateFormat());

        LoanTransaction reAgeTransaction = createReAgeTransaction(loan, command);
        LoanReAgeParameter reAgeParameter = createReAgeParameter(reAgeTransaction, command);
        reAgeTransaction.setLoanReAgeParameter(reAgeParameter);

        loanTransactionRepository.saveAndFlush(reAgeTransaction);

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = loanRepaymentScheduleTransactionProcessorFactory
                .determineProcessor(loan.transactionProcessingStrategy());
        loanRepaymentScheduleTransactionProcessor.processLatestTransaction(reAgeTransaction, new TransactionCtx(loan.getCurrency(),
                loan.getRepaymentScheduleInstallments(), loan.getActiveCharges(), new MoneyHolder(loan.getTotalOverpaidAsMoney()), null));

        loan.updateLoanScheduleDependentDerivedFields();
        persistNote(loan, command, changes);

        // delinquency recalculation will be triggered by the event in a decoupled way via a listener
        businessEventNotifierService.notifyPostBusinessEvent(new LoanReAgeBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanReAgeTransactionBusinessEvent(reAgeTransaction));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(reAgeTransaction.getId()) //
                .withEntityExternalId(reAgeTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(command.getLoanId()) //
                .with(changes).build();
    }

    public CommandProcessingResult undoReAge(Long loanId, JsonCommand command) {
        Loan loan = loanAssembler.assembleFrom(loanId);
        reAgingValidator.validateUndoReAge(loan, command);

        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(LoanReAgingApiConstants.localeParameterName, command.locale());
        changes.put(LoanReAgingApiConstants.dateFormatParameterName, command.dateFormat());

        LoanTransaction reAgeTransaction = findLatestNonReversedReAgeTransaction(loan);
        if (reAgeTransaction == null) {
            throw new LoanTransactionNotFoundException("Re-Age transaction for loan was not found");
        }
        reverseReAgeTransaction(reAgeTransaction, command);
        loanTransactionRepository.saveAndFlush(reAgeTransaction);

        reProcessLoanTransactions(reAgeTransaction.getLoan());
        loan.updateLoanScheduleDependentDerivedFields();
        persistNote(loan, command, changes);

        // delinquency recalculation will be triggered by the event in a decoupled way via a listener
        businessEventNotifierService.notifyPostBusinessEvent(new LoanUndoReAgeBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanUndoReAgeTransactionBusinessEvent(reAgeTransaction));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(reAgeTransaction.getId()) //
                .withEntityExternalId(reAgeTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(command.getLoanId()) //
                .with(changes).build();
    }

    private void reverseReAgeTransaction(LoanTransaction reAgeTransaction, JsonCommand command) {
        ExternalId reversalExternalId = externalIdFactory.createFromCommand(command, LoanReAgingApiConstants.externalIdParameterName);
        reAgeTransaction.reverse(reversalExternalId);
        reAgeTransaction.manuallyAdjustedOrReversed();
    }

    private LoanTransaction findLatestNonReversedReAgeTransaction(Loan loan) {
        return loan.getLoanTransactions().stream() //
                .filter(LoanTransaction::isNotReversed) //
                .filter(LoanTransaction::isReAge) //
                .max(Comparator.comparing(LoanTransaction::getTransactionDate)) //
                .orElse(null);
    }

    private LoanTransaction createReAgeTransaction(Loan loan, JsonCommand command) {
        ExternalId txExternalId = externalIdFactory.createFromCommand(command, LoanReAgingApiConstants.externalIdParameterName);

        // reaging transaction date is always the current business date
        LocalDate transactionDate = DateUtils.getBusinessLocalDate();

        // in case of a reaging transaction, only the outstanding principal amount until the business date is considered
        Money txPrincipal = loan.getTotalPrincipalOutstandingUntil(transactionDate);
        BigDecimal txPrincipalAmount = txPrincipal.getAmount();

        return new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.REAGE.getValue(), transactionDate, txPrincipalAmount,
                txPrincipalAmount, ZERO, ZERO, ZERO, null, false, null, txExternalId);
    }

    private LoanReAgeParameter createReAgeParameter(LoanTransaction reAgeTransaction, JsonCommand command) {
        // TODO: these parameters should be checked when the validations are implemented
        PeriodFrequencyType periodFrequencyType = command.enumValueOfParameterNamed(LoanReAgingApiConstants.frequencyType,
                PeriodFrequencyType.class);
        LocalDate startDate = command.dateValueOfParameterNamed(LoanReAgingApiConstants.startDate);
        Integer numberOfInstallments = command.integerValueOfParameterNamed(LoanReAgingApiConstants.numberOfInstallments);
        Integer periodFrequencyNumber = command.integerValueOfParameterNamed(LoanReAgingApiConstants.frequencyNumber);
        return new LoanReAgeParameter(reAgeTransaction, periodFrequencyType, periodFrequencyNumber, startDate, numberOfInstallments);
    }

    private void reProcessLoanTransactions(Loan loan) {
        final List<LoanTransaction> filteredTransactions = loan.retrieveListOfTransactionsForReprocessing();

        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = loanRepaymentScheduleTransactionProcessorFactory
                .determineProcessor(loan.transactionProcessingStrategy());
        loanRepaymentScheduleTransactionProcessor.reprocessLoanTransactions(loan.getDisbursementDate(), filteredTransactions,
                loan.getCurrency(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges());
    }

    private void persistNote(Loan loan, JsonCommand command, Map<String, Object> changes) {
        if (command.hasParameter("note")) {
            final String note = command.stringValueOfParameterNamed("note");
            final Note newNote = Note.loanNote(loan, note);
            changes.put("note", note);

            this.noteRepository.saveAndFlush(newNote);
        }
    }
}
