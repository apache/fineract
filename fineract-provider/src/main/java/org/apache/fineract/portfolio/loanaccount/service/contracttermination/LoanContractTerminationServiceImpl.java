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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
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
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionContractTerminationPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionLoanWithdrawalPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanUndoContractTerminationBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanUndoLoanWithdrawalBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSubStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
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

    private static final TerminationKind CONTRACT_TERMINATION = new TerminationKind("Contract termination",
            LoanTransactionType.CONTRACT_TERMINATION, LoanSubStatus.CONTRACT_TERMINATION,
            "error.msg.loan.contract.termination.is.only.supported.for.progressive.loan.schedule.type",
            "error.msg.loan.account.is.already.contract.termination.substate", "Loan Account is already terminated.",
            LoanTransactionContractTerminationPostBusinessEvent::new, LoanUndoContractTerminationBusinessEvent::new);
    private static final TerminationKind LOAN_WITHDRAWAL = new TerminationKind("Loan withdrawal", LoanTransactionType.LOAN_WITHDRAWAL,
            LoanSubStatus.LOAN_WITHDRAWAL, "error.msg.loan.withdrawal.is.only.supported.for.progressive.loan.schedule.type",
            "error.msg.loan.account.is.already.loan.withdrawal.substate", "Loan withdrawal has been applied to the loan account.",
            LoanTransactionLoanWithdrawalPostBusinessEvent::new, LoanUndoLoanWithdrawalBusinessEvent::new);

    public CommandProcessingResult applyContractTermination(final JsonCommand command) {
        return applyTermination(command, CONTRACT_TERMINATION, loanTransactionValidator::validateContractTermination);
    }

    public CommandProcessingResult undoContractTermination(final JsonCommand command) {
        loanTransactionValidator.validateContractTerminationUndo(command, command.getLoanId());
        return undoTermination(command, CONTRACT_TERMINATION);
    }

    public CommandProcessingResult applyLoanWithdrawal(final JsonCommand command) {
        return applyTermination(command, LOAN_WITHDRAWAL, loanTransactionValidator::validateLoanWithdrawal);
    }

    public CommandProcessingResult undoLoanWithdrawal(final JsonCommand command) {
        loanTransactionValidator.validateLoanWithdrawalUndo(command, command.getLoanId());
        return undoTermination(command, LOAN_WITHDRAWAL);
    }

    private CommandProcessingResult applyTermination(final JsonCommand command, final TerminationKind kind,
            final BiConsumer<JsonCommand, Long> requestValidation) {
        Loan loan = loanAssembler.assembleFrom(command.getLoanId());
        loanUtilService.checkClientOrGroupActive(loan);

        validateTerminationEligibility(loan, kind);
        requestValidation.accept(command, loan.getId());

        final ExternalId externalId = externalIdFactory.createFromCommand(command, LoanApiConstants.externalIdParameterName);
        final Map<String, Object> changes = new LinkedHashMap<>();

        final LocalDate transactionDate = Objects.requireNonNullElseGet(
                command.localDateValueOfParameterNamed(LoanApiConstants.transactionDateParamName), DateUtils::getBusinessLocalDate);
        final LoanTransaction termination = LoanTransaction.earlyTermination(loan, transactionDate, kind.transactionType(), externalId);

        loan.setLoanSubStatus(kind.subStatus());
        changes.put(LoanApiConstants.subStatusAttributeName, loan.getLoanSubStatus().getCode());

        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanScheduleService.regenerateRepaymentSchedule(loan);
            reprocessLoanTransactionsService.reprocessTransactions(loan, List.of(termination));
            loan.addLoanTransaction(termination);
        } else {
            reprocessLoanTransactionsService.processLatestTransaction(termination, loan);
            loan.addLoanTransaction(termination);
        }

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanTransactionNote(loan, termination, noteText);
            noteRepository.save(note);
        }
        loanTransactionRepository.saveAndFlush(termination);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(kind.appliedEvent().apply(termination));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(termination.getId()) //
                .withEntityExternalId(termination.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(command.getLoanId()) //
                .with(changes) //
                .build();
    }

    private CommandProcessingResult undoTermination(final JsonCommand command, final TerminationKind kind) {
        final Long loanId = command.getLoanId();
        final Loan loan = loanAssembler.assembleFrom(loanId);
        final LoanTransaction termination = loan.getLoanTransaction(t -> t.isNotReversed() && kind.transactionType().equals(t.getTypeOf()));

        businessEventNotifierService.notifyPreBusinessEvent(kind.undoneEvent().apply(termination));
        businessEventNotifierService
                .notifyPreBusinessEvent(new LoanAdjustTransactionBusinessEvent(new LoanAdjustTransactionBusinessEvent.Data(termination)));

        final String reversalExternalId = command.stringValueOfParameterNamedAllowingNull(LoanApiConstants.REVERSAL_EXTERNAL_ID_PARAMNAME);
        final ExternalId reversalTxnExternalId = ExternalIdFactory.produce(reversalExternalId);
        final Map<String, Object> changes = new LinkedHashMap<>();

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            final Note note = Note.loanTransactionNote(loan, termination, noteText);
            noteRepository.save(note);
        }

        loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(termination.getLoan(), termination, "reversed");
        termination.reverse(reversalTxnExternalId);
        termination.manuallyAdjustedOrReversed();

        loan.liftEarlyTerminationSubStatus();
        changes.put(LoanApiConstants.subStatusAttributeName, loan.getLoanSubStatus());
        loanTransactionRepository.saveAndFlush(termination);

        final ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, null, null);
        if (loan.isCumulativeSchedule() && loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanScheduleService.regenerateRepaymentScheduleWithInterestRecalculation(loan, scheduleGeneratorDTO);
        } else if (loan.isProgressiveSchedule()) {
            loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);
        }

        reprocessLoanTransactionsService.reprocessTransactions(loan);

        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(kind.undoneEvent().apply(termination));
        businessEventNotifierService
                .notifyPostBusinessEvent(new LoanAdjustTransactionBusinessEvent(new LoanAdjustTransactionBusinessEvent.Data(termination)));

        return new CommandProcessingResultBuilder() //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loanId) //
                .withEntityId(termination.getId()) //
                .withEntityExternalId(termination.getExternalId()) //
                .with(changes) //
                .build();
    }

    private void validateTerminationEligibility(final Loan loan, final TerminationKind kind) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final String notApplicable = kind.label() + " can not be applied, ";

        if (!loan.isOpen()) {
            dataValidationErrors.add(ApiParameterError.generalError("error.msg.loan.account.is.not.active.state",
                    notApplicable + "Loan Account is not Active."));
        }

        if (!loan.getLoanProduct().getLoanProductRelatedDetail().getLoanScheduleType().equals(LoanScheduleType.PROGRESSIVE)) {
            dataValidationErrors.add(ApiParameterError.generalError(kind.progressiveOnlyCode(),
                    notApplicable + "Loan product schedule type is not Progressive."));
        }

        if (loan.isChargedOff()) {
            dataValidationErrors.add(
                    ApiParameterError.generalError("error.msg.loan.account.is.charge-off", notApplicable + "Loan Account is Charge-Off."));
        }

        if (loan.isTerminatedEarly()) {
            final TerminationKind applied = loan.isLoanWithdrawal() ? LOAN_WITHDRAWAL : CONTRACT_TERMINATION;
            dataValidationErrors
                    .add(ApiParameterError.generalError(applied.alreadyAppliedCode(), notApplicable + applied.alreadyAppliedMessage()));
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private record TerminationKind(String label, LoanTransactionType transactionType, LoanSubStatus subStatus, String progressiveOnlyCode,
            String alreadyAppliedCode, String alreadyAppliedMessage, Function<LoanTransaction, LoanTransactionBusinessEvent> appliedEvent,
            Function<LoanTransaction, LoanTransactionBusinessEvent> undoneEvent) {
    }
}
