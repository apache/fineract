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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.loan.WorkingCapitalLoanStatusChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanUndoWriteOffTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanWriteOffTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanCharge;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanNote;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanWriteOffDomainService;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanChargeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNoteRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionAllocationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanDataValidator;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkingCapitalLoanWriteOffWriteServiceImpl implements WorkingCapitalLoanWriteOffWriteService {

    private final WorkingCapitalLoanRepository loanRepository;
    private final WorkingCapitalLoanDataValidator validator;
    private final WorkingCapitalLoanWriteOffDomainService writeOffDomainService;
    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    private final WorkingCapitalLoanChargeRepository chargeRepository;
    private final WorkingCapitalLoanNoteRepository noteRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final ExternalIdFactory externalIdFactory;
    private final PlatformSecurityContext context;
    private final WorkingCapitalLoanAccountingProcessor accountingProcessor;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final WorkingCapitalLoanDelinquencyRangeScheduleService delinquencyRangeScheduleService;

    @Transactional
    @Override
    public CommandProcessingResult writeOff(final Long loanId, final JsonCommand command) {
        final WorkingCapitalLoan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));
        this.validator.validateWriteOff(command, loan);

        final LoanStatus oldStatus = loan.getLoanStatus();
        final AppUser currentUser = this.context.authenticatedUser();
        final LocalDate transactionDate = command.localDateValueOfParameterNamed(WorkingCapitalLoanConstants.transactionDateParamName);
        final Long writeOffReasonId = command.longValueOfParameterNamed(WorkingCapitalLoanConstants.writeoffReasonIdParamName);
        final CodeValue writeOffReason = writeOffReasonId != null
                ? this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(WorkingCapitalLoanConstants.WRITE_OFF_REASONS,
                        writeOffReasonId)
                : null;

        // Capture the outstanding portions before the write-off zeroes them: they become the transaction allocation and
        // drive the write-off journal entries.
        final WorkingCapitalLoanBalance balance = requireBalance(loan);
        final BigDecimal principalPortion = balance.getPrincipalOutstanding();
        final BigDecimal feePortion = balance.getFeeOutstanding();
        final BigDecimal penaltyPortion = balance.getPenaltyOutstanding();
        final BigDecimal writeOffAmount = MathUtil.add(principalPortion, feePortion, penaltyPortion);

        // Terminal write-off: transition to CLOSED_WRITTEN_OFF, zero the balance (marking each active charge's
        // remainder as written off so the charge view matches it) and record the audit trail.
        final List<WorkingCapitalLoanCharge> charges = this.chargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(loanId);
        this.writeOffDomainService.writeOff(loan, transactionDate, currentUser, writeOffReason, charges);

        final ExternalId externalId = this.externalIdFactory.createFromCommand(command,
                WorkingCapitalLoanConstants.externalIdParameterName);
        final WorkingCapitalLoanTransaction writeOffTransaction = WorkingCapitalLoanTransaction.writeOff(loan, writeOffAmount,
                transactionDate, externalId);
        this.transactionRepository.saveAndFlush(writeOffTransaction);
        final WorkingCapitalLoanTransactionAllocation allocation = WorkingCapitalLoanTransactionAllocation.forPortions(writeOffTransaction,
                principalPortion, feePortion, penaltyPortion, BigDecimal.ZERO);
        this.allocationRepository.saveAndFlush(allocation);

        this.loanRepository.saveAndFlush(loan);
        this.chargeRepository.saveAll(charges);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(WorkingCapitalLoanConstants.transactionDateParamName, transactionDate);
        if (writeOffReasonId != null) {
            changes.put(WorkingCapitalLoanConstants.writeoffReasonIdParamName, writeOffReasonId);
        }
        createNote(command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.noteParamName), loan, changes);

        // The zeroed balance has to be pushed through the delinquency schedule: its periods are capped to what the
        // customer can still owe, so a written-off loan classifies as not delinquent and its active tags are lifted.
        // Nothing else would do it - a CLOSED_WRITTEN_OFF loan is out of COB scope, so the tag would stay frozen.
        this.delinquencyRangeScheduleService.reprocessDelinquencySchedule(loan);

        if (loan.getLoanProduct().getAccountingRule().isAccrualWithDeferredRevenueAmortization()) {
            this.accountingProcessor.postJournalEntries(loan, writeOffTransaction, allocation, loan.isChargedOff());
        }
        this.businessEventNotifierService
                .notifyPostBusinessEvent(new WorkingCapitalLoanWriteOffTransactionBusinessEvent(writeOffTransaction, loan.getId()));
        notifyBalanceChanged(loan);
        notifyStatusChanged(loan, oldStatus);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withLoanId(loanId) //
                .withLoanExternalId(loan.getExternalId()) //
                .withEntityId(writeOffTransaction.getId()) //
                .withEntityExternalId(writeOffTransaction.getExternalId()) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult undoWriteOff(final Long loanId, final JsonCommand command) {
        final WorkingCapitalLoan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));
        this.validator.validateUndoWriteOff(command, loan);

        final LoanStatus oldStatus = loan.getLoanStatus();
        // The write-off could not have happened without a balance row, so a missing one means the account became
        // inconsistent in the meantime; restoring the outstanding would silently do nothing.
        requireBalance(loan);
        final WorkingCapitalLoanTransaction writeOffTransaction = this.transactionRepository
                .findActiveByTypeOrderByIdDesc(loanId, LoanTransactionType.WRITEOFF).stream().findFirst()
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.wc.loan.write.off.transaction.not.found",
                        "No active write-off transaction found for loan " + loanId, loanId));

        // createFromCommand tolerates a bodiless request, which validateUndoWriteOff explicitly permits.
        final ExternalId reversalExternalId = this.externalIdFactory.createFromCommand(command,
                WorkingCapitalLoanConstants.reversalExternalIdParamName);
        writeOffTransaction.setReversed(true);
        writeOffTransaction.setReversalExternalId(reversalExternalId);
        writeOffTransaction.setReversedOnDate(DateUtils.getBusinessLocalDate());
        this.transactionRepository.saveAndFlush(writeOffTransaction);

        // Reopen the loan to ACTIVE, restore the outstanding balance (and the charges' outstanding) and clear the
        // write-off audit trail.
        final List<WorkingCapitalLoanCharge> charges = this.chargeRepository.findByLoanIdAndActiveTrueOrderByDueDateAscIdAsc(loanId);
        this.writeOffDomainService.undoWriteOff(loan, charges);
        this.loanRepository.saveAndFlush(loan);
        this.chargeRepository.saveAll(charges);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(WorkingCapitalLoanConstants.reversalExternalIdParamName, reversalExternalId);
        // The note parameter is only readable when the request carried a body; the command permits none at all.
        final String noteText = command.parsedJson() != null
                ? command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.noteParamName)
                : null;
        createNote(noteText, loan, changes);

        // The restored balance re-derives the delinquency the loan had before the write-off: the schedule replays the
        // repayment history and reclassifies against the outstanding that is back on the books.
        this.delinquencyRangeScheduleService.reprocessDelinquencySchedule(loan);

        if (loan.getLoanProduct().getAccountingRule().isAccrualWithDeferredRevenueAmortization()) {
            this.accountingProcessor.postReversalJournalEntries(loan, writeOffTransaction);
        }
        this.businessEventNotifierService
                .notifyPostBusinessEvent(new WorkingCapitalLoanUndoWriteOffTransactionBusinessEvent(writeOffTransaction, loan.getId()));
        notifyBalanceChanged(loan);
        notifyStatusChanged(loan, oldStatus);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withLoanId(loanId) //
                .withLoanExternalId(loan.getExternalId()) //
                .withEntityId(writeOffTransaction.getId()) //
                .withEntityExternalId(writeOffTransaction.getExternalId()) //
                .with(changes) //
                .build();
    }

    /**
     * A disbursed loan always has a balance row. A missing one means the account is inconsistent, and writing it off
     * would silently record a zero amount and close the loan on a balance nobody looked at -- the same reasoning (and
     * the same error code) charge-off applies.
     */
    private WorkingCapitalLoanBalance requireBalance(final WorkingCapitalLoan loan) {
        final WorkingCapitalLoanBalance balance = loan.getBalance();
        if (balance == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.wc.loan.balance.not.found",
                    "No balance found for Working Capital Loan " + loan.getId(), loan.getId());
        }
        return balance;
    }

    private void notifyBalanceChanged(final WorkingCapitalLoan loan) {
        this.businessEventNotifierService.notifyPostBusinessEvent(new WorkingCapitalLoanBalanceChangedBusinessEvent(loan));
    }

    private void notifyStatusChanged(final WorkingCapitalLoan loan, final LoanStatus oldStatus) {
        if (oldStatus != loan.getLoanStatus()) {
            this.businessEventNotifierService.notifyPostBusinessEvent(new WorkingCapitalLoanStatusChangedBusinessEvent(loan));
        }
    }

    private void createNote(final String noteText, final WorkingCapitalLoan loan, final Map<String, Object> changes) {
        if (StringUtils.isNotBlank(noteText)) {
            this.noteRepository.save(WorkingCapitalLoanNote.create(loan, noteText));
            changes.put(WorkingCapitalLoanConstants.noteParamName, noteText);
        }
    }
}
