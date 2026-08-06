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
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanNote;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionFinder;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNoteRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionAllocationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanDataValidator;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanChargeOffWriteServiceImpl implements WorkingCapitalLoanChargeOffWriteService {

    private final PlatformSecurityContext context;
    private final WorkingCapitalLoanRepository loanRepository;
    private final WorkingCapitalLoanDataValidator validator;
    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanTransactionFinder transactionFinder;
    private final WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    private final WorkingCapitalLoanBalanceRepository balanceRepository;
    private final WorkingCapitalLoanNoteRepository noteRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final ExternalIdFactory externalIdFactory;
    private final WorkingCapitalLoanAccountingProcessor accountingProcessor;

    @Transactional
    @Override
    public CommandProcessingResult chargeOff(final Long loanId, final JsonCommand command) {
        final WorkingCapitalLoan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));
        this.validator.validateChargeOff(command, loan);

        final AppUser currentUser = this.context.authenticatedUser();
        final LocalDate transactionDate = command.localDateValueOfParameterNamed(WorkingCapitalLoanConstants.transactionDateParamName);
        final ExternalId txnExternalId = this.externalIdFactory.createFromCommand(command,
                WorkingCapitalLoanConstants.externalIdParameterName);

        // Resolve the optional charge-off reason.
        final Long chargeOffReasonId = command.longValueOfParameterNamed(WorkingCapitalLoanConstants.chargeOffReasonIdParamName);
        final CodeValue chargeOffReason = chargeOffReasonId != null
                ? this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(WorkingCapitalLoanConstants.CHARGE_OFF_REASONS,
                        chargeOffReasonId)
                : null;

        // The charge-off amount is the outstanding balance as of the charge-off date: the charge-off date cannot be
        // earlier than the last user transaction, and system postings (accrual, discount-fee amortization) do not move
        // the balance, so the current outstanding equals the as-of-date balance.
        // A disbursed loan always has a balance row; a missing one means the account is inconsistent, and charging it
        // off would silently record a zero amount.
        final WorkingCapitalLoanBalance balance = this.balanceRepository.findByWcLoan_Id(loan.getId())
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.wc.loan.balance.not.found",
                        "No balance found for Working Capital Loan " + loanId, loanId));
        final BigDecimal chargeOffAmount = balance.getTotalOutstanding();

        // Non-monetary tag transaction: records the charged-off amount but does not move the running balance.
        final WorkingCapitalLoanTransaction chargeOffTransaction = WorkingCapitalLoanTransaction.chargeOff(loan, chargeOffAmount,
                transactionDate, txnExternalId);
        this.transactionRepository.saveAndFlush(chargeOffTransaction);

        // Snapshot the outstanding portions so charge-off accounting can post per-portion journal entries later.
        final WorkingCapitalLoanTransactionAllocation allocation = WorkingCapitalLoanTransactionAllocation.forPortions(chargeOffTransaction,
                balance.getPrincipalOutstanding(), balance.getFeeOutstanding(), balance.getPenaltyOutstanding(),
                balance.getOverpaymentAmount());
        this.allocationRepository.saveAndFlush(allocation);

        loan.markAsChargedOff(transactionDate, currentUser, chargeOffReason);
        this.loanRepository.saveAndFlush(loan);

        // Post charge-off journal entries: write off the outstanding receivables against charge-off expense / income
        // reversal. No portfolio or schedule impact -- pure accounting tag.
        if (loan.getLoanProduct().getAccountingRule().isAccrualWithDeferredRevenueAmortization()) {
            this.accountingProcessor.postJournalEntries(loan, chargeOffTransaction, allocation, loan.isChargedOff());
        }

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(WorkingCapitalLoanConstants.transactionDateParamName, transactionDate);
        if (chargeOffReasonId != null) {
            changes.put(WorkingCapitalLoanConstants.chargeOffReasonIdParamName, chargeOffReasonId);
        }
        createNote(command, loan, changes);

        log.debug("Charged off WC loan {} on {}", loanId, transactionDate);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(chargeOffTransaction.getId()) //
                .withEntityExternalId(chargeOffTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withLoanId(loanId) //
                .withLoanExternalId(loan.getExternalId()) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult undoChargeOff(final Long loanId, final JsonCommand command) {
        final WorkingCapitalLoan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));
        this.validator.validateUndoChargeOff(command, loan);

        final WorkingCapitalLoanTransaction chargeOffTransaction = this.transactionFinder.findChargedOffTransaction(loan)
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.wc.loan.charge.off.transaction.not.found",
                        "No active charge-off transaction found for loan " + loanId, loanId));

        final ExternalId reversalExternalId = this.externalIdFactory
                .create(command.stringValueOfParameterNamedAllowingNull(WorkingCapitalLoanConstants.reversalExternalIdParamName));
        chargeOffTransaction.setReversed(true);
        chargeOffTransaction.setReversalExternalId(reversalExternalId);
        chargeOffTransaction.setReversedOnDate(DateUtils.getBusinessLocalDate());
        this.transactionRepository.saveAndFlush(chargeOffTransaction);

        loan.liftChargeOff();
        this.loanRepository.saveAndFlush(loan);

        // Reverse the charge-off journal entries. No schedule reprocessing -- pure tag.
        if (loan.getLoanProduct().getAccountingRule().isAccrualWithDeferredRevenueAmortization()) {
            this.accountingProcessor.postReversalJournalEntries(loan, chargeOffTransaction);
        }

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("reversalExternalId", reversalExternalId);
        createNote(command, loan, changes);

        log.debug("Reversed charge-off for WC loan {}", loanId);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(chargeOffTransaction.getId()) //
                .withEntityExternalId(chargeOffTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withLoanId(loanId) //
                .withLoanExternalId(loan.getExternalId()) //
                .with(changes) //
                .build();
    }

    private void createNote(final JsonCommand command, final WorkingCapitalLoan loan, final Map<String, Object> changes) {
        final String noteText = command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.noteParamName);
        if (StringUtils.isNotBlank(noteText)) {
            this.noteRepository.save(WorkingCapitalLoanNote.create(loan, noteText));
            changes.put(WorkingCapitalLoanConstants.noteParamName, noteText);
        }
    }
}
