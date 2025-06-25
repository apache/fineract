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
package org.apache.fineract.portfolio.loanaccount.service.contracttermination;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionContractTerminationPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanUndoContractTerminationBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSubStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanScheduleService;
import org.apache.fineract.portfolio.loanaccount.service.LoanTransactionService;
import org.apache.fineract.portfolio.loanaccount.service.LoanUtilService;
import org.apache.fineract.portfolio.loanaccount.service.ProgressiveLoanTransactionValidator;
import org.apache.fineract.portfolio.loanaccount.service.ReprocessLoanTransactionsService;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LoanContractTerminationServiceImpl {

    private final LoanAssembler loanAssembler;
    private final LoanRepository loanRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final NoteRepository noteRepository;
    private final ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    private final LoanUtilService loanUtilService;
    private final ExternalIdFactory externalIdFactory;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanScheduleService loanScheduleService;
    private final LoanChargeValidator loanChargeValidator;
    private final ProgressiveLoanTransactionValidator loanTransactionValidator;
    private final LoanTransactionService loanTransactionService;

    public CommandProcessingResult applyContractTermination(final JsonCommand command) {
        final Loan loan = loanAssembler.assembleFrom(command.getLoanId());
        // validate client or group is active
        loanUtilService.checkClientOrGroupActive(loan);

        // validate Contract Termination
        validateContractTermination(loan);

        final ExternalId externalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);
        final Map<String, Object> changes = new LinkedHashMap<>();

        final LoanTransaction contractTermination = LoanTransaction.contractTermination(loan, DateUtils.getBusinessLocalDate(), externalId);
        loanTransactionRepository.save(contractTermination);

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanTransactionNote(loan, contractTermination, noteText);
            noteRepository.save(note);
        }

        // Mark Contract Termination, Update Loan SubStatus
        loan.setLoanSubStatus(LoanSubStatus.CONTRACT_TERMINATION);
        loanRepository.save(loan);
        changes.put(LoanApiConstants.subStatusAttributeName, loan.getLoanSubStatus().getCode());

        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            final List<LoanTransaction> loanTransactions = loanTransactionService.retrieveListOfTransactionsForReprocessing(loan);
            loanTransactions.add(contractTermination);
            reprocessLoanTransactionsService.reprocessParticularTransactions(loan, loanTransactions);
            loan.addLoanTransaction(contractTermination);
        } else {
            reprocessLoanTransactionsService.processLatestTransaction(contractTermination, loan);
            loan.addLoanTransaction(contractTermination);
        }

        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanTransactionContractTerminationPostBusinessEvent(contractTermination));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(contractTermination.getId()) //
                .withEntityExternalId(contractTermination.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(command.getLoanId()) //
                .with(changes).build();
    }

    public CommandProcessingResult undoContractTermination(final JsonCommand command) {
        final Long loanId = command.getLoanId();

        loanTransactionValidator.validateContractTerminationUndo(command, loanId);

        final Loan loan = loanAssembler.assembleFrom(loanId);
        final LoanTransaction contractTerminationTransaction = loan.findContractTerminationTransaction();

        businessEventNotifierService.notifyPreBusinessEvent(new LoanUndoContractTerminationBusinessEvent(contractTerminationTransaction));
        businessEventNotifierService.notifyPreBusinessEvent(
                new LoanAdjustTransactionBusinessEvent(new LoanAdjustTransactionBusinessEvent.Data(contractTerminationTransaction)));

        // check if reversalExternalId is provided
        final String reversalExternalId = command.stringValueOfParameterNamedAllowingNull(LoanApiConstants.REVERSAL_EXTERNAL_ID_PARAMNAME);
        final ExternalId reversalTxnExternalId = ExternalIdFactory.produce(reversalExternalId);
        final Map<String, Object> changes = new LinkedHashMap<>();

        // Add note if provided
        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanTransactionNote(loan, contractTerminationTransaction, noteText);
            noteRepository.save(note);
        }

        loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(contractTerminationTransaction.getLoan(),
                contractTerminationTransaction, "reversed");
        contractTerminationTransaction.reverse(reversalTxnExternalId);
        contractTerminationTransaction.manuallyAdjustedOrReversed();

        loan.liftContractTerminationSubStatus();
        changes.put(LoanApiConstants.subStatusAttributeName, loan.getLoanSubStatus());
        loanTransactionRepository.saveAndFlush(contractTerminationTransaction);

        final ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, null, null);
        if (loan.isCumulativeSchedule() && loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanScheduleService.regenerateRepaymentScheduleWithInterestRecalculation(loan, scheduleGeneratorDTO);
        } else if (loan.isProgressiveSchedule()) {
            loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
        }

        reprocessLoanTransactionsService.reprocessTransactions(loan);

        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanUndoContractTerminationBusinessEvent(contractTerminationTransaction));

        final LoanAdjustTransactionBusinessEvent.Data eventData = new LoanAdjustTransactionBusinessEvent.Data(
                contractTerminationTransaction);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanAdjustTransactionBusinessEvent(eventData));

        return new CommandProcessingResultBuilder() //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .withEntityId(contractTerminationTransaction.getId()) //
                .withEntityExternalId(contractTerminationTransaction.getExternalId()) //
                .with(changes) //
                .build();
    }

    public void validateContractTermination(final Loan loan) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        if (!loan.isOpen()) {
            final String defaultUserMessage = "Contract termination can not be applied, Loan Account is not Active.";
            final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.account.is.not.active.state",
                    defaultUserMessage);
            dataValidationErrors.add(error);
        }

        if (!loan.getLoanProduct().getLoanProductRelatedDetail().getLoanScheduleType().equals(LoanScheduleType.PROGRESSIVE)) {
            final String defaultUserMessage = "Contract termination can not be applied, Loan product schedule type is not Progressive.";
            final ApiParameterError error = ApiParameterError.generalError(
                    "error.msg.loan.contract.termination.is.only.supported.for.progressive.loan.schedule.type", defaultUserMessage);
            dataValidationErrors.add(error);
        }

        if (loan.isChargedOff()) {
            final String defaultUserMessage = "Contract termination can not be applied, Loan Account is Charge-Off.";
            final ApiParameterError error = ApiParameterError.generalError("error.msg.loan.account.is.charge-off", defaultUserMessage);
            dataValidationErrors.add(error);
        }

        if (loan.isContractTermination()) {
            final String defaultUserMessage = "Contract termination can not be applied, Loan Account is already terminated.";
            final ApiParameterError error = ApiParameterError
                    .generalError("error.msg.loan.account.is.already.contract.termination.substate", defaultUserMessage);
            dataValidationErrors.add(error);
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

}
