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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanDisbursalTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanRepaymentTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanUndoDisbursalTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.data.RepaymentAmortizationData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanEvent;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanLifecycleStateMachine;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanNote;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNoteRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionAllocationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanDataValidator;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetail;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanWritePlatformServiceImpl implements WorkingCapitalLoanWritePlatformService {

    private final PlatformSecurityContext context;
    private final WorkingCapitalLoanRepository loanRepository;
    private final WorkingCapitalLoanDataValidator validator;
    private final WorkingCapitalLoanLifecycleStateMachine stateMachine;
    private final FromJsonHelper fromApiJsonHelper;
    private final WorkingCapitalLoanNoteRepository noteRepository;
    private final ExternalIdFactory externalIdFactory;
    private final WorkingCapitalLoanTransactionRepository transactionRepository;
    private final WorkingCapitalLoanTransactionAllocationRepository allocationRepository;
    private final PaymentDetailWritePlatformService paymentDetailService;
    private final WorkingCapitalLoanBalanceRepository balanceRepository;
    private final WorkingCapitalLoanAmortizationScheduleWriteService amortizationScheduleWriteService;
    private final InternalWorkingCapitalLoanPaymentService internalWorkingCapitalLoanPaymentService;
    private final CodeValueRepository codeValueRepository;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public CommandProcessingResult approveApplication(final Long loanId, final JsonCommand command) {
        final WorkingCapitalLoan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));

        this.validator.validateApproval(command.json(), loan);

        final AppUser currentUser = this.context.authenticatedUser();

        this.stateMachine.transition(WorkingCapitalLoanEvent.LOAN_APPROVED, loan);

        // Approved date
        final LocalDate approvedOnDate = command.localDateValueOfParameterNamed(WorkingCapitalLoanConstants.approvedOnDateParamName);
        loan.setApprovedOnDate(approvedOnDate);
        loan.setApprovedBy(currentUser);

        // Principal amount (optional, defaults to proposed)
        if (command.parameterExists(WorkingCapitalLoanConstants.approvedLoanAmountParamName)) {
            final BigDecimal approvedAmount = this.fromApiJsonHelper
                    .extractBigDecimalNamed(WorkingCapitalLoanConstants.approvedLoanAmountParamName, command.parsedJson(), new HashSet<>());
            if (approvedAmount != null) {
                loan.setApprovedPrincipal(approvedAmount);
            }
        }
        if (loan.getApprovedPrincipal() == null || loan.getApprovedPrincipal().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setApprovedPrincipal(loan.getProposedPrincipal());
        }

        // Expected disbursement date (mandatory, validated)
        final LocalDate expectedDisbursementDate = command
                .localDateValueOfParameterNamed(WorkingCapitalLoanConstants.expectedDisbursementDateParamName);
        if (expectedDisbursementDate != null && !loan.getDisbursementDetails().isEmpty()) {
            loan.getDisbursementDetails().getFirst().setExpectedDisbursementDate(expectedDisbursementDate);
        }

        // Discount amount (optional, can only be reduced per requirement)
        if (command.parameterExists(WorkingCapitalLoanConstants.discountAmountParamName)) {
            final BigDecimal discount = this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanConstants.discountAmountParamName,
                    command.parsedJson(), new HashSet<>());
            if (discount != null) {
                loan.getLoanProductRelatedDetails().setDiscount(discount);
            }
        }

        // Keep first tranche expected amount aligned with approved principal (submit stores proposed principal only).
        if (!loan.getDisbursementDetails().isEmpty()) {
            loan.getDisbursementDetails().getFirst().setExpectedAmount(loan.getApprovedPrincipal());
        }

        this.loanRepository.saveAndFlush(loan);

        this.amortizationScheduleWriteService.generateAndSaveAmortizationScheduleOnApproval(loan);

        createNote(command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.noteParamName), loan);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(WorkingCapitalLoanConstants.approvedOnDateParamName, approvedOnDate);
        changes.put("status", loan.getLoanStatus());

        log.debug("Working capital loan {} approved by user {}", loanId, currentUser.getId());

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withEntityExternalId(loan.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Override
    public CommandProcessingResult undoApplicationApproval(final Long loanId, final JsonCommand command) {
        final WorkingCapitalLoan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));

        this.validator.validateUndoApproval(command.json());

        this.stateMachine.transition(WorkingCapitalLoanEvent.LOAN_APPROVAL_UNDO, loan);

        loan.setApprovedOnDate(null);
        loan.setApprovedBy(null);
        loan.setApprovedPrincipal(BigDecimal.ZERO);

        // Reset discount to product default.
        // Note: if discount was customized at submission time, it resets to product default,
        // not the submission-time value, because we don't store a pre-approval snapshot.
        // The loan is back in SUBMITTED state and can be modified.
        final WorkingCapitalLoanProduct product = loan.getLoanProduct();
        final WorkingCapitalLoanProductRelatedDetail productDetail = product.getRelatedDetail();
        loan.getLoanProductRelatedDetails().setDiscount(productDetail.getDiscount());

        this.loanRepository.saveAndFlush(loan);

        createNote(command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.noteParamName), loan);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("status", loan.getLoanStatus());

        log.debug("Working capital loan {} approval undone", loanId);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withEntityExternalId(loan.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Override
    public CommandProcessingResult rejectApplication(final Long loanId, final JsonCommand command) {
        final WorkingCapitalLoan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));

        this.validator.validateRejection(command.json(), loan);

        final AppUser currentUser = this.context.authenticatedUser();

        this.stateMachine.transition(WorkingCapitalLoanEvent.LOAN_REJECTED, loan);

        final LocalDate rejectedOnDate = command.localDateValueOfParameterNamed(WorkingCapitalLoanConstants.rejectedOnDateParamName);
        loan.setRejectedOnDate(rejectedOnDate);
        loan.setRejectedBy(currentUser);

        this.loanRepository.saveAndFlush(loan);

        createNote(command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.noteParamName), loan);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(WorkingCapitalLoanConstants.rejectedOnDateParamName, rejectedOnDate);
        changes.put("status", loan.getLoanStatus());

        log.debug("Working capital loan {} rejected by user {}", loanId, currentUser.getId());

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withEntityExternalId(loan.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult disburseLoan(final Long loanId, final JsonCommand command) {
        final WorkingCapitalLoan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));

        if (!this.stateMachine.canTransition(WorkingCapitalLoanEvent.LOAN_DISBURSED, loan)) {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.transition.not.allowed",
                    "Disbursement is not allowed from current status " + loan.getLoanStatus(), "loanStatus");
        }

        this.validator.validateDisbursement(command.json(), loan);

        final AppUser currentUser = this.context.getAuthenticatedUserIfPresent();

        final LocalDate actualDisbursementDate = command
                .localDateValueOfParameterNamed(WorkingCapitalLoanConstants.actualDisbursementDateParamName);
        final BigDecimal transactionAmount = this.fromApiJsonHelper
                .extractBigDecimalNamed(WorkingCapitalLoanConstants.transactionAmountParamName, command.parsedJson(), new HashSet<>());
        final Long classificationId = this.fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.classificationIdParamName,
                command.parsedJson());
        final CodeValue classification = classificationId != null
                ? this.codeValueRepository.findByCodeNameAndId(WorkingCapitalLoanConstants.DISBURSEMENT_CLASSIFICATION_CODE_NAME,
                        classificationId)
                : null;

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(WorkingCapitalLoanConstants.actualDisbursementDateParamName, actualDisbursementDate);
        changes.put(WorkingCapitalLoanConstants.transactionAmountParamName, transactionAmount);
        changes.put(WorkingCapitalLoanConstants.classificationIdParamName, classificationId);
        final PaymentDetail paymentDetail = createAndPersistPaymentDetailFromCommand(command, changes);

        this.stateMachine.transition(WorkingCapitalLoanEvent.LOAN_DISBURSED, loan);

        if (!loan.getDisbursementDetails().isEmpty()) {
            loan.getDisbursementDetails().getFirst().setActualDisbursementDate(actualDisbursementDate);
            loan.getDisbursementDetails().getFirst().setActualAmount(transactionAmount);
            loan.getDisbursementDetails().getFirst().setDisbursedBy(currentUser);
        }

        if (command.parameterExists(WorkingCapitalLoanConstants.discountAmountParamName)) {
            final BigDecimal discount = this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanConstants.discountAmountParamName,
                    command.parsedJson(), new HashSet<>());
            if (discount != null) {
                loan.getLoanProductRelatedDetails().setDiscount(discount);
                changes.put(WorkingCapitalLoanConstants.discountAmountParamName, discount);
            }
        }

        final ExternalId txnExternalId = this.externalIdFactory.createFromCommand(command,
                WorkingCapitalLoanConstants.externalIdParameterName);
        final WorkingCapitalLoanTransaction disbursementTransaction = WorkingCapitalLoanTransaction.disbursement(loan, transactionAmount,
                paymentDetail, actualDisbursementDate, txnExternalId, classification);
        this.transactionRepository.saveAndFlush(disbursementTransaction);
        businessEventNotifierService
                .notifyPostBusinessEvent(new WorkingCapitalLoanDisbursalTransactionBusinessEvent(disbursementTransaction, loan.getId()));

        final WorkingCapitalLoanTransactionAllocation allocation = WorkingCapitalLoanTransactionAllocation
                .forPrincipalAllocation(disbursementTransaction, transactionAmount);
        this.allocationRepository.saveAndFlush(allocation);

        updateBalanceOnDisburse(loan, transactionAmount);
        amortizationScheduleWriteService.generateAndSaveAmortizationScheduleOnDisbursement(loan, transactionAmount, actualDisbursementDate);

        this.loanRepository.saveAndFlush(loan);

        final String noteText = command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.noteParamName);
        if (StringUtils.isNotBlank(noteText)) {
            changes.put(WorkingCapitalLoanConstants.noteParamName, noteText);
        }
        changes.put("status", loan.getLoanStatus());
        createNote(noteText, loan);

        log.debug("Working capital loan {} disbursed by user {}", loanId, currentUser != null ? currentUser.getId() : "system");

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withEntityExternalId(loan.getExternalId()) //
                .withSubEntityId(disbursementTransaction.getId()) //
                .withSubEntityExternalId(disbursementTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Override
    public CommandProcessingResult undoDisbursal(final Long loanId, final JsonCommand command) {
        final WorkingCapitalLoan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));

        this.validator.validateUndoDisbursal(command.json());

        if (loan.getClient() != null && loan.getClient().isNotActive()) {
            throw new ClientNotActiveException(loan.getClient().getId());
        }

        ensureUndoDisbursalAllowed(loan);

        this.stateMachine.transition(WorkingCapitalLoanEvent.LOAN_DISBURSAL_UNDO, loan);

        final WorkingCapitalLoanTransaction reversedTransaction = reverseDisbursementTransactionAndResetBalance(loan);
        businessEventNotifierService
                .notifyPostBusinessEvent(new WorkingCapitalLoanUndoDisbursalTransactionBusinessEvent(reversedTransaction, loan.getId()));

        if (loan.getDisbursementDetails() != null) {
            for (WorkingCapitalLoanDisbursementDetails detail : loan.getDisbursementDetails()) {
                if (detail.getActualDisbursementDate() != null) {
                    detail.setActualDisbursementDate(null);
                    detail.setActualAmount(null);
                    detail.setDisbursedBy(null);
                }
            }
        }
        amortizationScheduleWriteService.regenerateAmortizationScheduleOnUndoDisbursal(loan);

        this.loanRepository.saveAndFlush(loan);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("status", loan.getLoanStatus());
        changes.put(WorkingCapitalLoanConstants.actualDisbursementDateParamName, null);
        changes.put("actualAmount", null);
        final String noteText = command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.noteParamName);
        if (StringUtils.isNotBlank(noteText)) {
            changes.put(WorkingCapitalLoanConstants.noteParamName, noteText);
        }
        createNote(noteText, loan);

        log.debug("Working capital loan {} disbursal undone", loanId);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withEntityExternalId(loan.getExternalId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    @Override
    public CommandProcessingResult updateDiscount(final Long loanId, final JsonCommand command) {
        final WorkingCapitalLoan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));
        this.validator.validateUpdateDiscount(command.json(), loan);

        if (loan.getLoanStatus() != LoanStatus.ACTIVE) {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.transition.not.allowed",
                    "Add discount is allowed only for disbursed (active) loans", "loanStatus");
        }

        ensureDiscountNotAlreadySetBeforeDisbursement(loan);

        final BigDecimal discount = this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanConstants.discountAmountParamName,
                command.parsedJson(), new HashSet<>());
        if (discount != null) {
            loan.getLoanProductRelatedDetails().setDiscount(discount);
        }
        updateBalanceForDiscountChange(loan);

        final String noteText = command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.noteParamName);
        createNote(noteText, loan);
        this.loanRepository.saveAndFlush(loan);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(WorkingCapitalLoanConstants.discountAmountParamName, discount);
        if (StringUtils.isNotBlank(noteText)) {
            changes.put(WorkingCapitalLoanConstants.noteParamName, noteText);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanId)
                .withEntityExternalId(loan.getExternalId()).withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId())
                .withLoanId(loanId).with(changes).build();
    }

    @Override
    public CommandProcessingResult makeRepayment(final Long loanId, final JsonCommand command) {
        final WorkingCapitalLoan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));
        this.validator.validateRepayment(command.json(), loan);

        if (loan.getLoanStatus() != LoanStatus.ACTIVE && loan.getLoanStatus() != LoanStatus.OVERPAID) {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.transition.not.allowed",
                    "Repayment is allowed only for active/overpaid loans", "loanStatus");
        }

        final LocalDate transactionDate = command.localDateValueOfParameterNamed(WorkingCapitalLoanConstants.transactionDateParamName);
        final BigDecimal transactionAmount = this.fromApiJsonHelper
                .extractBigDecimalNamed(WorkingCapitalLoanConstants.transactionAmountParamName, command.parsedJson(), new HashSet<>());
        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(WorkingCapitalLoanConstants.transactionDateParamName, transactionDate);
        changes.put(WorkingCapitalLoanConstants.transactionAmountParamName, transactionAmount);
        final PaymentDetail paymentDetail = createAndPersistPaymentDetailFromCommand(command, changes);

        final Long classificationId = command.longValueOfParameterNamed(WorkingCapitalLoanConstants.classificationIdParamName);
        final CodeValue classification = classificationId != null
                ? codeValueRepository.findByCodeNameAndId(WorkingCapitalLoanConstants.REPAYMENT_CLASSIFICATION_CODE_NAME, classificationId)
                : null;
        changes.put(WorkingCapitalLoanConstants.classificationIdParamName, classificationId);

        final ExternalId txnExternalId = this.externalIdFactory.createFromCommand(command,
                WorkingCapitalLoanConstants.externalIdParameterName);
        final WorkingCapitalLoanTransaction repaymentTransaction = WorkingCapitalLoanTransaction.repayment(loan, transactionAmount,
                paymentDetail, transactionDate, classification, txnExternalId);
        this.transactionRepository.saveAndFlush(repaymentTransaction);

        final WorkingCapitalLoanTransactionAllocation allocation = WorkingCapitalLoanTransactionAllocation
                .forPrincipalAllocation(repaymentTransaction, transactionAmount);
        this.allocationRepository.saveAndFlush(allocation);

        final RepaymentAmortizationData amortizationData = amortizationScheduleWriteService.applyRepayment(loan, transactionDate,
                transactionAmount);
        updateBalanceOnRepayment(loan, transactionAmount, amortizationData);
        internalWorkingCapitalLoanPaymentService.makePayment(loanId, transactionAmount, transactionDate);

        if (loan.getBalance() != null && loan.getBalance().getPrincipalOutstanding() != null) {
            final int compareToZero = loan.getBalance().getPrincipalOutstanding().compareTo(BigDecimal.ZERO);
            if (compareToZero == 0) {
                this.stateMachine.transition(WorkingCapitalLoanEvent.LOAN_REPAID_IN_FULL, loan);
                loan.setMaturedOnDate(transactionDate);
            } else if (compareToZero < 0) {
                this.stateMachine.transition(WorkingCapitalLoanEvent.LOAN_OVERPAID, loan);
                loan.setMaturedOnDate(transactionDate);
            }
        }

        final String noteText = command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.noteParamName);
        if (StringUtils.isNotBlank(noteText)) {
            changes.put(WorkingCapitalLoanConstants.noteParamName, noteText);
        }
        changes.put("status", loan.getLoanStatus());
        createNote(noteText, loan);

        this.loanRepository.saveAndFlush(loan);
        businessEventNotifierService
                .notifyPostBusinessEvent(new WorkingCapitalLoanRepaymentTransactionBusinessEvent(repaymentTransaction, loan.getId()));

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanId)
                .withEntityExternalId(loan.getExternalId()).withSubEntityId(repaymentTransaction.getId())
                .withSubEntityExternalId(repaymentTransaction.getExternalId()).withOfficeId(loan.getOfficeId())
                .withClientId(loan.getClientId()).withLoanId(loanId).with(changes).build();
    }

    private PaymentDetail createAndPersistPaymentDetailFromCommand(final JsonCommand command, final Map<String, Object> changes) {
        final JsonElement paymentDetailsElement = command.jsonElement(WorkingCapitalLoanConstants.paymentDetailsParamName);
        if (paymentDetailsElement != null && paymentDetailsElement.isJsonObject()) {
            final JsonCommand paymentDetailsCommand = JsonCommand.fromExistingCommand(command, paymentDetailsElement);
            return paymentDetailService.createPaymentDetail(paymentDetailsCommand, changes);
        }
        return paymentDetailService.createPaymentDetail(command, changes);
    }

    private void updateBalanceOnDisburse(final WorkingCapitalLoan loan, final BigDecimal disbursedAmount) {
        WorkingCapitalLoanBalance balance = this.balanceRepository.findByWcLoan_Id(loan.getId()).orElse(null);
        if (balance == null) {
            balance = WorkingCapitalLoanBalance.createFor(loan);
        }
        final BigDecimal discount = loan.getLoanProductRelatedDetails() != null && loan.getLoanProductRelatedDetails().getDiscount() != null
                ? loan.getLoanProductRelatedDetails().getDiscount()
                : BigDecimal.ZERO;
        balance.setPrincipalOutstanding(disbursedAmount.add(discount));
        this.balanceRepository.saveAndFlush(balance);
    }

    private void updateBalanceForDiscountChange(final WorkingCapitalLoan loan) {
        final WorkingCapitalLoanBalance balance = this.balanceRepository.findByWcLoan_Id(loan.getId())
                .orElseGet(() -> WorkingCapitalLoanBalance.createFor(loan));
        final BigDecimal discount = loan.getLoanProductRelatedDetails() != null && loan.getLoanProductRelatedDetails().getDiscount() != null
                ? loan.getLoanProductRelatedDetails().getDiscount()
                : BigDecimal.ZERO;
        final BigDecimal disbursedAmount = loan.getDisbursementDetails() != null && !loan.getDisbursementDetails().isEmpty()
                && loan.getDisbursementDetails().getFirst().getActualAmount() != null
                        ? loan.getDisbursementDetails().getFirst().getActualAmount()
                        : BigDecimal.ZERO;
        balance.setPrincipalOutstanding(disbursedAmount.add(discount));
        this.balanceRepository.saveAndFlush(balance);
    }

    private void updateBalanceOnRepayment(final WorkingCapitalLoan loan, final BigDecimal repaymentAmount,
            final RepaymentAmortizationData amortizationData) {
        final WorkingCapitalLoanBalance balance = this.balanceRepository.findByWcLoan_Id(loan.getId())
                .orElseGet(() -> WorkingCapitalLoanBalance.createFor(loan));
        final BigDecimal principalOutstanding = balance.getPrincipalOutstanding() != null ? balance.getPrincipalOutstanding()
                : BigDecimal.ZERO;
        final BigDecimal totalPaidPrincipal = balance.getTotalPaidPrincipal() != null ? balance.getTotalPaidPrincipal() : BigDecimal.ZERO;

        balance.setPrincipalOutstanding(principalOutstanding.subtract(repaymentAmount));
        balance.setTotalPaidPrincipal(totalPaidPrincipal.add(repaymentAmount));
        balance.setRealizedIncome(amortizationData.totalAmortizedAmount());

        final BigDecimal discount = loan.getLoanProductRelatedDetails() != null && loan.getLoanProductRelatedDetails().getDiscount() != null
                ? loan.getLoanProductRelatedDetails().getDiscount()
                : BigDecimal.ZERO;
        final BigDecimal unrealizedIncome = discount.subtract(amortizationData.totalAmortizedAmount());
        balance.setUnrealizedIncome(unrealizedIncome.max(BigDecimal.ZERO));

        this.balanceRepository.saveAndFlush(balance);
    }

    private WorkingCapitalLoanTransaction reverseDisbursementTransactionAndResetBalance(final WorkingCapitalLoan loan) {
        final List<WorkingCapitalLoanTransaction> transactions = this.transactionRepository
                .findByWcLoan_IdOrderByTransactionDateAscIdAsc(loan.getId());
        final List<WorkingCapitalLoanTransaction> activeDisbursements = transactions.stream()
                .filter(txn -> txn.getTypeOf() == LoanTransactionType.DISBURSEMENT && !txn.isReversed()).toList();
        if (activeDisbursements.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.undo.disbursal.not.allowed",
                    "Undo disbursal is not allowed when there is no active disbursement transaction", "loanId");
        }
        if (activeDisbursements.size() > 1) {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.undo.disbursal.not.allowed",
                    "Multiple active disbursement transactions found while only single disbursement is supported", "loanId");
        }

        final WorkingCapitalLoanTransaction txn = activeDisbursements.getFirst();
        txn.setReversed(true);
        txn.setReversedOnDate(DateUtils.getBusinessLocalDate());
        txn.setReversalExternalId(ExternalId.generate());
        this.transactionRepository.save(txn);
        this.transactionRepository.flush();

        final Optional<WorkingCapitalLoanBalance> balanceOpt = this.balanceRepository.findByWcLoan_Id(loan.getId());
        balanceOpt.ifPresent(b -> {
            // Restore balance to pre-disbursement state.
            b.setPrincipalOutstanding(loan.getApprovedPrincipal() != null ? loan.getApprovedPrincipal() : loan.getProposedPrincipal());
            b.setTotalPaidPrincipal(BigDecimal.ZERO);
            b.setRealizedIncome(BigDecimal.ZERO);
            b.setUnrealizedIncome(BigDecimal.ZERO);
            this.balanceRepository.saveAndFlush(b);
        });
        return txn;
    }

    private void ensureUndoDisbursalAllowed(final WorkingCapitalLoan loan) {
        final List<WorkingCapitalLoanTransaction> transactions = this.transactionRepository
                .findByWcLoan_IdOrderByTransactionDateAscIdAsc(loan.getId());

        for (WorkingCapitalLoanTransaction txn : transactions) {
            if (txn.isReversed()) {
                continue;
            }
            if (txn.getTypeOf() != LoanTransactionType.DISBURSEMENT) {
                throw new PlatformApiDataValidationException("validation.msg.wc.loan.undo.disbursal.not.allowed",
                        "Undo disbursal is not allowed when there are other monetary transactions on the loan", "loanId");
            }
        }
    }

    private void createNote(final String noteText, final WorkingCapitalLoan loan) {
        if (StringUtils.isNotBlank(noteText)) {
            final WorkingCapitalLoanNote note = WorkingCapitalLoanNote.create(loan, noteText);
            this.noteRepository.save(note);
        }
    }

    private void ensureDiscountNotAlreadySetBeforeDisbursement(final WorkingCapitalLoan loan) {
        final BigDecimal productDefaultDiscount = loan.getLoanProduct() != null && loan.getLoanProduct().getRelatedDetail() != null
                ? loan.getLoanProduct().getRelatedDetail().getDiscount()
                : null;
        final BigDecimal loanDiscount = loan.getLoanProductRelatedDetails() != null ? loan.getLoanProductRelatedDetails().getDiscount()
                : null;
        final boolean equalByValue = productDefaultDiscount == null ? loanDiscount == null
                : loanDiscount != null && productDefaultDiscount.compareTo(loanDiscount) == 0;
        if (!equalByValue) {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.discount.already.set.before.disbursement",
                    "Discount was already set before disbursement and cannot be added again",
                    WorkingCapitalLoanConstants.discountAmountParamName);
        }
    }
}
