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

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanRecoveryPaymentTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanNote;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNoteRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanDataValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A recovery payment is money collected after the loan was written off. It is deliberately NOT run through the
 * repayment pipeline: the balance was zeroed by the write-off and must stay that way, so the transaction carries no
 * allocation, moves no principal, fee or penalty, and leaves the amortization, delinquency and breach schedules alone.
 * The only balance-side effect is the running {@code totalRecovered}, which caps how much more can be recovered.
 * <p>
 * The loan status is likewise untouched: it stays {@code CLOSED_WRITTEN_OFF}. Calling the state machine here would be
 * wrong twice over - there is no transition to make, and {@code determineAndTransition} would see a zero outstanding
 * and try {@code LOAN_REPAID_IN_FULL}, which throws from {@code CLOSED_WRITTEN_OFF}.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanRecoveryPaymentWriteServiceImpl implements WorkingCapitalLoanRecoveryPaymentWriteService {

    private final WorkingCapitalLoanRepository loanRepository;
    private final WorkingCapitalLoanDataValidator validator;
    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanBalanceRepository balanceRepository;
    private final WorkingCapitalLoanNoteRepository noteRepository;
    private final PaymentDetailWritePlatformService paymentDetailService;
    private final ExternalIdFactory externalIdFactory;
    private final FromJsonHelper fromApiJsonHelper;
    private final WorkingCapitalLoanAccountingProcessor accountingProcessor;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final WorkingCapitalLoanAdjustTransactionEventPublisher adjustTransactionEventPublisher;
    private final WorkingCapitalLoanTransactionDataFactory transactionDataFactory;

    @Transactional
    @Override
    public CommandProcessingResult recoveryPayment(final Long loanId, final JsonCommand command) {
        final WorkingCapitalLoan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));
        this.validator.validateRecoveryPayment(command, loan);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed(WorkingCapitalLoanConstants.transactionDateParamName);
        final BigDecimal transactionAmount = this.fromApiJsonHelper
                .extractBigDecimalNamed(WorkingCapitalLoanConstants.transactionAmountParamName, command.parsedJson(), new HashSet<>());

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(WorkingCapitalLoanConstants.transactionDateParamName, transactionDate);
        changes.put(WorkingCapitalLoanConstants.transactionAmountParamName, transactionAmount);
        final PaymentDetail paymentDetail = createAndPersistPaymentDetailFromCommand(command, changes);

        final ExternalId externalId = this.externalIdFactory.createFromCommand(command,
                WorkingCapitalLoanConstants.externalIdParameterName);
        final WorkingCapitalLoanTransaction transaction = WorkingCapitalLoanTransaction.recoveryPayment(loan, transactionAmount,
                paymentDetail, transactionDate, externalId);
        this.transactionRepository.saveAndFlush(transaction);

        final WorkingCapitalLoanBalance balance = requireBalance(loan);
        balance.setTotalRecovered(MathUtil.add(balance.getTotalRecovered(), transactionAmount));
        this.balanceRepository.saveAndFlush(balance);

        createNote(command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.noteParamName), loan, changes);

        postJournalEntries(loan, transaction);
        this.businessEventNotifierService
                .notifyPostBusinessEvent(new WorkingCapitalLoanRecoveryPaymentTransactionBusinessEvent(transaction, loan.getId()));

        return buildResult(command, loan, transaction, changes);
    }

    @Transactional
    @Override
    public CommandProcessingResult undoRecoveryPayment(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction transaction,
            final JsonCommand command) {
        this.validator.validateUndoRecoveryPayment(command, loan, transaction);

        final ExternalId reversalExternalId = this.externalIdFactory.createFromCommand(command,
                WorkingCapitalLoanConstants.reversalExternalIdParamName);
        transaction.setReversed(true);
        transaction.setReversalExternalId(reversalExternalId);
        transaction.setReversedOnDate(DateUtils.getBusinessLocalDate());
        this.transactionRepository.saveAndFlush(transaction);

        // Giving back the recovered amount restores what is still recoverable, so the reversed money can be collected
        // again. A running total smaller than the amount being reversed means the balance row was repaired or migrated
        // inconsistently; failing keeps the money trail honest instead of silently clamping the figure at zero.
        final WorkingCapitalLoanBalance balance = requireBalance(loan);
        final BigDecimal totalRecovered = MathUtil.nullToZero(balance.getTotalRecovered());
        if (totalRecovered.compareTo(transaction.getTransactionAmount()) < 0) {
            throw new GeneralPlatformDomainRuleException("error.msg.wc.loan.total.recovered.less.than.reversed.amount",
                    "Total recovered " + totalRecovered + " is less than the recovery payment amount " + transaction.getTransactionAmount()
                            + " being reversed for Working Capital Loan " + loan.getId(),
                    loan.getId());
        }
        balance.setTotalRecovered(MathUtil.subtract(totalRecovered, transaction.getTransactionAmount()));
        this.balanceRepository.saveAndFlush(balance);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("reversed", true);
        changes.put(WorkingCapitalLoanConstants.reversalExternalIdParamName, reversalExternalId);
        changes.put("reversedOnDate", transaction.getReversedOnDate());
        // The note parameter is only readable when the request carried a body; the command permits none at all.
        final String noteText = command.parsedJson() != null
                ? command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.noteParamName)
                : null;
        createNote(noteText, loan, changes);

        postReversalJournalEntries(loan, transaction);

        this.adjustTransactionEventPublisher.publishReversal(loan.getId(), transaction);

        return buildResult(command, loan, transaction, changes);
    }

    private void postJournalEntries(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction transaction) {
        if (loan.getLoanProduct().getAccountingRule().isAccrualWithDeferredRevenueAmortization()) {
            // No allocation: the whole amount is recovery income, not a repayment split across principal, fees and
            // penalties. The processor books Dr Fund Source / Cr Income from Recovery from the transaction amount.
            this.accountingProcessor.postJournalEntries(loan, transaction, null, loan.isChargedOff());
        }
    }

    private void postReversalJournalEntries(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction transaction) {
        if (loan.getLoanProduct().getAccountingRule().isAccrualWithDeferredRevenueAmortization()) {
            this.accountingProcessor.postReversalJournalEntries(loan, transaction);
        }
    }

    /**
     * A written-off loan always has a balance row - the write-off could not have zeroed it otherwise. A missing one
     * means the account became inconsistent, and the recovery would silently record income against a balance nobody
     * looked at.
     */
    private WorkingCapitalLoanBalance requireBalance(final WorkingCapitalLoan loan) {
        final WorkingCapitalLoanBalance balance = loan.getBalance();
        if (balance == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.wc.loan.balance.not.found",
                    "No balance found for Working Capital Loan " + loan.getId(), loan.getId());
        }
        return balance;
    }

    private PaymentDetail createAndPersistPaymentDetailFromCommand(final JsonCommand command, final Map<String, Object> changes) {
        final JsonElement paymentDetailsElement = command.jsonElement(WorkingCapitalLoanConstants.paymentDetailsParamName);
        if (paymentDetailsElement != null && paymentDetailsElement.isJsonNull()) {
            return null;
        }
        if (paymentDetailsElement != null && paymentDetailsElement.isJsonObject()) {
            final JsonCommand paymentDetailsCommand = JsonCommand.fromExistingCommand(command, paymentDetailsElement);
            return this.paymentDetailService.createPaymentDetail(paymentDetailsCommand, changes);
        }
        return this.paymentDetailService.createPaymentDetail(command, changes);
    }

    private void createNote(final String noteText, final WorkingCapitalLoan loan, final Map<String, Object> changes) {
        if (StringUtils.isNotBlank(noteText)) {
            this.noteRepository.save(WorkingCapitalLoanNote.create(loan, noteText));
            changes.put(WorkingCapitalLoanConstants.noteParamName, noteText);
        }
    }

    private CommandProcessingResult buildResult(final JsonCommand command, final WorkingCapitalLoan loan,
            final WorkingCapitalLoanTransaction transaction, final Map<String, Object> changes) {
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withLoanId(loan.getId()) //
                .withLoanExternalId(loan.getExternalId()) //
                .withEntityId(transaction.getId()) //
                .withEntityExternalId(transaction.getExternalId()) //
                .with(changes) //
                .build();
    }
}
