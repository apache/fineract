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
import java.math.MathContext;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
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
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanCreditBalanceRefundTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanDisbursalTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanDiscountFeeAdjustmentTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanDiscountFeeTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanRepaymentTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanUndoDisbursalTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanEvent;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanLifecycleStateMachine;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanNote;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodPaymentRateChange;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelation;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionRelationRepository;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBalanceRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNoteRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanPeriodPaymentRateChangeRepository;
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
    private final WorkingCapitalLoanAccountingProcessor accountingProcessor;
    private final WorkingCapitalLoanTransactionRelationRepository relationRepository;
    private final WorkingCapitalLoanPeriodPaymentRateChangeRepository rateChangeRepository;
    private final WorkingCapitalLoanDiscountFeeAmortizationService discountFeeAmortizationService;
    private final ProjectedAmortizationScheduleRepositoryWrapper scheduleRepositoryWrapper;

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
            loan.getLoanProductRelatedDetails().setDiscountApproved(discount);
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
        loan.getLoanProductRelatedDetails().setDiscountApproved(null);

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

        // Discount amount (optional, can only be reduced per requirement)
        BigDecimal discount = null;
        if (command.parameterExists(WorkingCapitalLoanConstants.discountAmountParamName)) {
            discount = this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanConstants.discountAmountParamName,
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

        Long discountTransactionId = null;
        ExternalId discountTxnExternalId = null;

        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            WorkingCapitalLoanTransaction discountTransaction = createAndPersistDiscountFeeTransaction(loan, disbursementTransaction, null,
                    discount, actualDisbursementDate, null, null);
            discountTransactionId = discountTransaction.getId();
            discountTxnExternalId = discountTransaction.getExternalId();
            changes.put(WorkingCapitalLoanConstants.discountAmountParamName, discount);
        }
        updateBalanceOnDisburse(loan, transactionAmount);
        amortizationScheduleWriteService.generateAndSaveAmortizationScheduleOnDisbursement(loan, transactionAmount, actualDisbursementDate);

        this.loanRepository.saveAndFlush(loan);
        changes.put("status", loan.getLoanStatus());
        handleNote(loan, command, changes);

        log.debug("Working capital loan {} disbursed by user {}", loanId, currentUser != null ? currentUser.getId() : "system");

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withLoanId(loanId).withLoanExternalId(loan.getExternalId()).withEntityId(disbursementTransaction.getId()) //
                .withEntityExternalId(disbursementTransaction.getExternalId()) //
                .withSubEntityId(discountTransactionId) //
                .withSubEntityExternalId(discountTxnExternalId) //
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
        loan.getLoanProductRelatedDetails().setDiscount(null);
        amortizationScheduleWriteService.regenerateAmortizationScheduleOnUndoDisbursal(loan);

        this.loanRepository.saveAndFlush(loan);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("status", loan.getLoanStatus());
        changes.put(WorkingCapitalLoanConstants.actualDisbursementDateParamName, null);
        changes.put("actualAmount", null);
        handleNote(loan, command, changes);

        log.debug("Working capital loan {} disbursal undone", loanId);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanId) //
                .withEntityExternalId(loan.getExternalId()) //
                .withLoanId(loanId) //
                .with(changes) //
                .build();
    }

    private void saveNewTransactionRelation(WorkingCapitalLoanTransaction fromTxn, WorkingCapitalLoanTransaction toTxn,
            LoanTransactionRelationTypeEnum relationType) {
        WorkingCapitalLoanTransactionRelation relation = new WorkingCapitalLoanTransactionRelation(fromTxn, toTxn, relationType);
        fromTxn.getLoanTransactionRelations().add(relation);
        transactionRepository.saveAndFlush(fromTxn);
    }

    private WorkingCapitalLoanTransaction createAndPersistDiscountFeeTransaction(final WorkingCapitalLoan loan,
            final WorkingCapitalLoanTransaction disbursementTransaction, ExternalId txnExternalId, BigDecimal amount,
            LocalDate transactionDate, CodeValue classification, PaymentDetail paymentDetail) {
        if (amount != null) {
            loan.getLoanProductRelatedDetails().setDiscount(amount);
        }

        WorkingCapitalLoanTransaction discountTransaction = WorkingCapitalLoanTransaction.discountFee(loan, txnExternalId, amount,
                transactionDate, classification, paymentDetail);

        saveNewTransactionRelation(discountTransaction, disbursementTransaction, LoanTransactionRelationTypeEnum.RELATED);

        final WorkingCapitalLoanTransactionAllocation allocation = WorkingCapitalLoanTransactionAllocation
                .forDisbursementDiscount(discountTransaction, amount);
        allocationRepository.saveAndFlush(allocation);

        amortizationScheduleWriteService.generateAndSaveAmortizationScheduleOnDisbursement(loan,
                disbursementTransaction.getTransactionAmount(), disbursementTransaction.getTransactionDate());

        businessEventNotifierService
                .notifyPostBusinessEvent(new WorkingCapitalLoanDiscountFeeTransactionBusinessEvent(discountTransaction));
        return discountTransaction;
    }

    @Override
    public CommandProcessingResult makeDiscountFee(Long loanId, JsonCommand command) {
        final WorkingCapitalLoan loan = loanRepository.findById(loanId).orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));

        final Long relatedDisbursementTransactionId = fromApiJsonHelper
                .extractLongNamed(WorkingCapitalLoanConstants.relatedResourceIdParamName, command.parsedJson());

        BigDecimal amount = fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanConstants.transactionAmountParamName,
                command.parsedJson(), new HashSet<>());
        if (amount == null) {
            amount = loan.getLoanProductRelatedDetails().getDiscount();
        }
        final String note = this.fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.noteParamName, command.parsedJson());

        validator.validateDiscountTransaction(loan, command.json(), amount, note);

        if (loan.getLoanStatus() != LoanStatus.ACTIVE) {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.transition.not.allowed",
                    "Add discount is allowed only for disbursed (active) loans", "loanStatus");
        }

        if (relatedDisbursementTransactionId == null) {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.related.resource.id.required",
                    "Related disbursement transaction ID is required for discount fee transaction", "relatedResourceId");
        }

        final WorkingCapitalLoanTransaction relatedDisbursementTransaction = transactionRepository
                .findById(relatedDisbursementTransactionId)
                .orElseThrow(() -> new PlatformApiDataValidationException("validation.msg.wc.loan.disbursement.transaction.not.found",
                        "Disbursement transaction not found", "disbursementTransaction"));

        boolean alreadyHasDiscount = relationRepository.findByToTransactionAndFromTransactionReversedAndFromTransactionTransactionType(
                relatedDisbursementTransaction, false, LoanTransactionType.DISCOUNT_FEE).isPresent();
        if (alreadyHasDiscount) {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.discount.already.set.before.disbursement",
                    "Discount was already set before disbursement and cannot be added again",
                    WorkingCapitalLoanConstants.discountAmountParamName);
        }

        final Long classificationId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.classificationIdParamName,
                command.parsedJson());
        final CodeValue classification = classificationId != null ? Optional
                .ofNullable(codeValueRepository.findByCodeNameAndId(WorkingCapitalLoanConstants.DISBURSEMENT_CLASSIFICATION_CODE_NAME,
                        classificationId))
                .orElseThrow(() -> new PlatformApiDataValidationException("validation.msg.wc.loan.classification.not.found",
                        "Classification with ID " + classificationId + " not found", "classificationId"))
                : null;

        final Map<String, Object> changes = new LinkedHashMap<>();

        final ExternalId txnExternalId = externalIdFactory.createFromCommand(command, WorkingCapitalLoanConstants.externalIdParameterName);
        final PaymentDetail paymentDetail = createAndPersistPaymentDetailFromCommand(command, changes);
        handleNote(loan, command, changes);

        changes.put(WorkingCapitalLoanConstants.transactionAmountParamName, amount);
        changes.put(WorkingCapitalLoanConstants.relatedResourceIdParamName, relatedDisbursementTransactionId);
        changes.put(WorkingCapitalLoanConstants.transactionDateParamName, relatedDisbursementTransaction.getTransactionDate());
        changes.put(WorkingCapitalLoanConstants.transactionTypeParamName, LoanTransactionType.DISCOUNT_FEE);
        changes.put(WorkingCapitalLoanConstants.externalIdParameterName, txnExternalId);
        changes.put(WorkingCapitalLoanConstants.classificationIdParamName, classificationId);

        WorkingCapitalLoanTransaction discountTransaction = createAndPersistDiscountFeeTransaction(loan, relatedDisbursementTransaction,
                txnExternalId, amount, relatedDisbursementTransaction.getTransactionDate(), classification, paymentDetail);

        updateBalanceForDiscountChange(loan);
        loanRepository.saveAndFlush(loan);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(discountTransaction.getId())
                .withEntityExternalId(discountTransaction.getExternalId()).withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId())
                .withLoanId(loanId).with(changes).build();
    }

    @Override
    public CommandProcessingResult makeDiscountFeeAdjustment(final Long loanId, final JsonCommand command) {
        final WorkingCapitalLoan loan = loanRepository.findById(loanId).orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));
        final Long relatedDiscountTransactionId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.relatedResourceIdParamName,
                command.parsedJson());
        if (relatedDiscountTransactionId == null) {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.related.resource.id.required",
                    "Related discount transaction ID is required for discount fee adjustment",
                    WorkingCapitalLoanConstants.relatedResourceIdParamName);
        }
        final WorkingCapitalLoanTransaction relatedDiscountTransaction = transactionRepository.findById(relatedDiscountTransactionId)
                .orElseThrow(() -> new PlatformApiDataValidationException("validation.msg.wc.loan.discount.transaction.not.found",
                        "Discount transaction not found", WorkingCapitalLoanConstants.relatedResourceIdParamName));
        if (!relatedDiscountTransaction.getTypeOf().isDiscountFee() || relatedDiscountTransaction.isReversed()) {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.discount.transaction.invalid",
                    "Related transaction must be an active discount fee transaction",
                    WorkingCapitalLoanConstants.relatedResourceIdParamName);
        }
        final BigDecimal amount = fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanConstants.transactionAmountParamName,
                command.parsedJson(), new HashSet<>());
        final BigDecimal totalAdjusted = relationRepository
                .findAllByToTransactionAndFromTransactionReversedAndFromTransactionTransactionType(relatedDiscountTransaction, false,
                        LoanTransactionType.DISCOUNT_FEE_ADJUSTMENT)
                .stream().map(relation -> relation.getFromTransaction().getTransactionAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        final BigDecimal remainingDiscountAmount = relatedDiscountTransaction.getTransactionAmount().subtract(totalAdjusted);

        final LocalDate requestedTransactionDate = command
                .localDateValueOfParameterNamed(WorkingCapitalLoanConstants.transactionDateParamName);
        final LocalDate transactionDate = requestedTransactionDate != null ? requestedTransactionDate
                : relatedDiscountTransaction.getTransactionDate();
        validator.validateDiscountAdjustmentTransaction(loan, command.json(), amount, relatedDiscountTransaction, remainingDiscountAmount,
                transactionDate);
        final Long classificationId = command.longValueOfParameterNamed(WorkingCapitalLoanConstants.classificationIdParamName);
        final CodeValue classification = classificationId != null ? Optional
                .ofNullable(codeValueRepository.findByCodeNameAndId(
                        WorkingCapitalLoanConstants.DISCOUNT_FEE_ADJUSTMENT_CLASSIFICATION_CODE_NAME, classificationId))
                .orElseThrow(() -> new PlatformApiDataValidationException("validation.msg.wc.loan.classification.not.found",
                        "Classification with ID " + classificationId + " not found", "classificationId"))
                : null;
        final ExternalId txnExternalId = externalIdFactory.createFromCommand(command, WorkingCapitalLoanConstants.externalIdParameterName);
        final Map<String, Object> changes = new LinkedHashMap<>();
        final PaymentDetail paymentDetail = createAndPersistPaymentDetailFromCommand(command, changes);
        final WorkingCapitalLoanTransaction adjustmentTransaction = WorkingCapitalLoanTransaction.discountFeeAdjustment(loan, txnExternalId,
                amount, transactionDate, classification, paymentDetail);
        transactionRepository.saveAndFlush(adjustmentTransaction);
        saveNewTransactionRelation(adjustmentTransaction, relatedDiscountTransaction, LoanTransactionRelationTypeEnum.RELATED);
        allocationRepository.saveAndFlush(WorkingCapitalLoanTransactionAllocation.forDiscountFeeAdjustment(adjustmentTransaction, amount));

        if (loan.getLoanProductRelatedDetails() == null) {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.discount.not.available",
                    "Discount adjustment is not available when loan product details are missing", "loanProductRelatedDetails");
        }
        final BigDecimal currentDiscount = loan.getLoanProductRelatedDetails().getDiscount();
        loan.getLoanProductRelatedDetails()
                .setDiscount((currentDiscount != null ? currentDiscount : BigDecimal.ZERO).subtract(amount).max(BigDecimal.ZERO));

        amortizationScheduleWriteService.applyDiscountFeeAdjustment(loan, transactionDate);
        updateBalanceForDiscountChange(loan);
        loanRepository.saveAndFlush(loan);

        final String noteText = command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.noteParamName);
        createNote(noteText, loan);
        changes.put(WorkingCapitalLoanConstants.transactionAmountParamName, amount);
        changes.put(WorkingCapitalLoanConstants.relatedResourceIdParamName, relatedDiscountTransactionId);
        changes.put(WorkingCapitalLoanConstants.transactionDateParamName, transactionDate);
        changes.put(WorkingCapitalLoanConstants.transactionTypeParamName, LoanTransactionType.DISCOUNT_FEE_ADJUSTMENT);
        changes.put(WorkingCapitalLoanConstants.classificationIdParamName, classificationId);
        if (StringUtils.isNotBlank(noteText)) {
            changes.put(WorkingCapitalLoanConstants.noteParamName, noteText);
        }
        businessEventNotifierService
                .notifyPostBusinessEvent(new WorkingCapitalLoanDiscountFeeAdjustmentTransactionBusinessEvent(adjustmentTransaction));
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(adjustmentTransaction.getId())
                .withEntityExternalId(adjustmentTransaction.getExternalId()).withSubEntityId(relatedDiscountTransaction.getId())
                .withSubEntityExternalId(relatedDiscountTransaction.getExternalId()).withOfficeId(loan.getOfficeId())
                .withClientId(loan.getClientId()).withLoanId(loanId).with(changes).build();
    }

    @Override
    public CommandProcessingResult makeRepayment(final Long loanId, final JsonCommand command) {
        return makeRepaymentLikeTransaction(loanId, command, LoanTransactionType.REPAYMENT);
    }

    private CommandProcessingResult makeRepaymentLikeTransaction(final Long loanId, final JsonCommand command,
            final LoanTransactionType transactionType) {
        final WorkingCapitalLoan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));
        this.validator.validateRepayment(command.json(), loan, transactionType);

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
        final WorkingCapitalLoanTransaction transaction = resolveNewTransaction(transactionType, loan, transactionAmount, paymentDetail,
                transactionDate, classification, txnExternalId);
        this.transactionRepository.saveAndFlush(transaction);

        final WorkingCapitalLoanBalance currentBalance = this.balanceRepository.findByWcLoan_Id(loan.getId())
                .orElseGet(() -> WorkingCapitalLoanBalance.createFor(loan));
        final BigDecimal outstandingBeforeRepayment = MathUtil.nullToZero(currentBalance.getPrincipalOutstanding());
        final BigDecimal amountAppliedToOutstanding = transactionAmount.min(outstandingBeforeRepayment);

        final WorkingCapitalLoanTransactionAllocation allocation = WorkingCapitalLoanTransactionAllocation
                .forPrincipalAllocation(transaction, amountAppliedToOutstanding);
        this.allocationRepository.saveAndFlush(allocation);

        amortizationScheduleWriteService.applyRepayment(loan, transactionDate, amountAppliedToOutstanding);
        updateBalanceOnRepayment(loan, transactionAmount);
        internalWorkingCapitalLoanPaymentService.makePayment(loanId, amountAppliedToOutstanding, transactionDate);

        handleStateChanges(loan, transactionDate);
        triggerInlineAmortizationIfLoanClosed(loan, transactionDate);
        changes.put("status", loan.getLoanStatus());

        handleNote(loan, command, changes);

        if (loan.getLoanProduct().getAccountingRule().isCashBased()) {
            accountingProcessor.postJournalEntries(loan, transaction, allocation, false);
        }

        notifyPostBusinessEvent(transactionType, transaction, loan);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanId)
                .withEntityExternalId(loan.getExternalId()).withSubEntityId(transaction.getId())
                .withSubEntityExternalId(transaction.getExternalId()).withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId())
                .withLoanId(loanId).with(changes).build();

    }

    private void notifyPostBusinessEvent(LoanTransactionType transactionType, WorkingCapitalLoanTransaction transaction,
            WorkingCapitalLoan loan) {
        BusinessEvent<?> businessEvent = LoanTransactionType.REPAYMENT.equals(transactionType)
                ? new WorkingCapitalLoanRepaymentTransactionBusinessEvent(transaction, loan.getId())
                : null;
        if (businessEvent != null) {
            businessEventNotifierService.notifyPostBusinessEvent(businessEvent);
        }
    }

    private WorkingCapitalLoanTransaction resolveNewTransaction(final LoanTransactionType transactionType, WorkingCapitalLoan loan,
            BigDecimal transactionAmount, PaymentDetail paymentDetail, LocalDate transactionDate, CodeValue classification,
            ExternalId txnExternalId) {
        return switch (transactionType) {
            case REPAYMENT -> WorkingCapitalLoanTransaction.repayment(loan, transactionAmount, paymentDetail, transactionDate,
                    classification, txnExternalId);
            case GOODWILL_CREDIT -> WorkingCapitalLoanTransaction.goodwillCredit(loan, transactionAmount, paymentDetail, transactionDate,
                    classification, txnExternalId);
            default -> throw new NotImplementedException("Missing implementation for : " + transactionType.getCode());
        };
    }

    private void handleNote(WorkingCapitalLoan loan, JsonCommand command, Map<String, Object> changes) {
        final String noteText = command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.noteParamName);
        if (StringUtils.isNotBlank(noteText)) {
            changes.put(WorkingCapitalLoanConstants.noteParamName, noteText);
        }
        createNote(noteText, loan);
    }

    private void triggerInlineAmortizationIfLoanClosed(final WorkingCapitalLoan loan, final LocalDate transactionDate) {
        if ((loan.getLoanStatus().isClosed() || loan.getLoanStatus().isOverpaid())
                && loan.getLoanProduct().getAccountingRule().isCashBased()) {
            final BigDecimal discount = loan.getLoanProductRelatedDetails() != null ? loan.getLoanProductRelatedDetails().getDiscount()
                    : null;
            if (MathUtil.isGreaterThanZero(discount)) {
                discountFeeAmortizationService.processDiscountFeeAmortization(loan, transactionDate);
            }
        }
    }

    private void handleStateChanges(WorkingCapitalLoan loan, LocalDate transactionDate) {
        if (loan.getBalance() != null) {
            final BigDecimal overpaymentAmount = loan.getBalance().getOverpaymentAmount() != null ? loan.getBalance().getOverpaymentAmount()
                    : BigDecimal.ZERO;
            final BigDecimal principalOutstanding = loan.getBalance().getPrincipalOutstanding() != null
                    ? loan.getBalance().getPrincipalOutstanding()
                    : BigDecimal.ZERO;
            if (overpaymentAmount.compareTo(BigDecimal.ZERO) > 0) {
                this.stateMachine.transition(WorkingCapitalLoanEvent.LOAN_OVERPAID, loan);
                loan.setMaturedOnDate(transactionDate);
            } else if (principalOutstanding.compareTo(BigDecimal.ZERO) == 0) {
                this.stateMachine.transition(WorkingCapitalLoanEvent.LOAN_REPAID_IN_FULL, loan);
                loan.setMaturedOnDate(transactionDate);
            }
        }
    }

    @Override
    public CommandProcessingResult creditBalanceRefund(final Long loanId, final JsonCommand command) {
        final WorkingCapitalLoan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));
        this.validator.validateCreditBalanceRefund(command.json(), loan);

        if (loan.getLoanStatus() != LoanStatus.OVERPAID) {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.transition.not.allowed",
                    "Credit balance refund is allowed only for overpaid loans", "loanStatus");
        }
        final WorkingCapitalLoanBalance currentBalance = this.balanceRepository.findByWcLoan_Id(loan.getId())
                .orElseGet(() -> WorkingCapitalLoanBalance.createFor(loan));
        final BigDecimal availableOverpayment = currentBalance.getOverpaymentAmount() != null ? currentBalance.getOverpaymentAmount()
                : BigDecimal.ZERO;
        if (availableOverpayment.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.credit.balance.refund.not.allowed",
                    "Credit balance refund is allowed only when loan is overpaid", "transactionAmount");
        }

        final LocalDate transactionDate = command.localDateValueOfParameterNamed(WorkingCapitalLoanConstants.transactionDateParamName);
        final BigDecimal transactionAmount = this.fromApiJsonHelper
                .extractBigDecimalNamed(WorkingCapitalLoanConstants.transactionAmountParamName, command.parsedJson(), new HashSet<>());
        if (transactionAmount.compareTo(availableOverpayment) > 0) {
            throw new PlatformApiDataValidationException("validation.msg.wc.loan.credit.balance.refund.amount.invalid",
                    "Credit balance refund amount cannot exceed overpayment amount",
                    WorkingCapitalLoanConstants.transactionAmountParamName);
        }

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(WorkingCapitalLoanConstants.transactionDateParamName, transactionDate);
        changes.put(WorkingCapitalLoanConstants.transactionAmountParamName, transactionAmount);
        final PaymentDetail paymentDetail = createAndPersistPaymentDetailFromCommand(command, changes);

        final Long classificationId = command.longValueOfParameterNamed(WorkingCapitalLoanConstants.classificationIdParamName);
        final CodeValue classification = classificationId != null
                ? codeValueRepository.findByCodeNameAndId(WorkingCapitalLoanConstants.CREDIT_BALANCE_REFUND_CLASSIFICATION_CODE_NAME,
                        classificationId)
                : null;
        changes.put(WorkingCapitalLoanConstants.classificationIdParamName, classificationId);

        final ExternalId txnExternalId = this.externalIdFactory.createFromCommand(command,
                WorkingCapitalLoanConstants.externalIdParameterName);
        final WorkingCapitalLoanTransaction creditBalanceRefundTransaction = WorkingCapitalLoanTransaction.creditBalanceRefund(loan,
                transactionAmount, paymentDetail, transactionDate, classification, txnExternalId);
        this.transactionRepository.saveAndFlush(creditBalanceRefundTransaction);

        final WorkingCapitalLoanTransactionAllocation allocation = WorkingCapitalLoanTransactionAllocation
                .forPrincipalAllocation(creditBalanceRefundTransaction, transactionAmount);
        this.allocationRepository.saveAndFlush(allocation);

        updateBalanceOnCreditBalanceRefund(loan, transactionAmount);
        if (loan.getBalance() != null) {
            final BigDecimal principalOutstanding = loan.getBalance().getPrincipalOutstanding() != null
                    ? loan.getBalance().getPrincipalOutstanding()
                    : BigDecimal.ZERO;
            final BigDecimal overpaymentAmount = loan.getBalance().getOverpaymentAmount() != null ? loan.getBalance().getOverpaymentAmount()
                    : BigDecimal.ZERO;
            if (principalOutstanding.compareTo(BigDecimal.ZERO) == 0 && overpaymentAmount.compareTo(BigDecimal.ZERO) == 0) {
                this.stateMachine.transition(WorkingCapitalLoanEvent.LOAN_CREDIT_BALANCE_REFUND_IN_FULL, loan);
                loan.setMaturedOnDate(transactionDate);
            }
        }

        changes.put("status", loan.getLoanStatus());
        handleNote(loan, command, changes);

        this.loanRepository.saveAndFlush(loan);
        businessEventNotifierService.notifyPostBusinessEvent(
                new WorkingCapitalLoanCreditBalanceRefundTransactionBusinessEvent(creditBalanceRefundTransaction, loan.getId()));

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanId)
                .withEntityExternalId(loan.getExternalId()).withSubEntityId(creditBalanceRefundTransaction.getId())
                .withSubEntityExternalId(creditBalanceRefundTransaction.getExternalId()).withOfficeId(loan.getOfficeId())
                .withClientId(loan.getClientId()).withLoanId(loanId).with(changes).build();
    }

    @Override
    @Transactional
    public CommandProcessingResult updatePeriodPaymentRate(final Long loanId, final JsonCommand command) {
        final WorkingCapitalLoan loan = this.loanRepository.findById(loanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));
        this.validator.validateUpdatePeriodPaymentRate(command.json(), loan);

        final BigDecimal newRate = this.fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanConstants.periodPaymentRateParamName,
                command.parsedJson(), new HashSet<>());
        final BigDecimal previousRate = loan.getLoanProductRelatedDetails().getPeriodPaymentRate();

        final LocalDate businessDate = DateUtils.getBusinessLocalDate();

        final List<WorkingCapitalLoanPeriodPaymentRateChange> activeChanges = this.rateChangeRepository
                .findByWorkingCapitalLoanIdAndReversedFalse(loanId);
        for (final WorkingCapitalLoanPeriodPaymentRateChange active : activeChanges) {
            active.reverse(businessDate);
        }
        if (!activeChanges.isEmpty()) {
            this.rateChangeRepository.saveAll(activeChanges);
        }

        loan.getLoanProductRelatedDetails().setPeriodPaymentRate(newRate);

        final WorkingCapitalLoanPeriodPaymentRateChange rateChange = WorkingCapitalLoanPeriodPaymentRateChange.create(loan, businessDate,
                previousRate, newRate);
        this.rateChangeRepository.save(rateChange);

        this.amortizationScheduleWriteService.regenerateAmortizationScheduleOnRateChange(loan, newRate);

        final String noteText = command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.noteParamName);
        createNote(noteText, loan);
        this.loanRepository.saveAndFlush(loan);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(WorkingCapitalLoanConstants.periodPaymentRateParamName, newRate);
        changes.put(WorkingCapitalLoanConstants.previousPeriodPaymentRateParamName, previousRate);
        if (StringUtils.isNotBlank(noteText)) {
            changes.put(WorkingCapitalLoanConstants.noteParamName, noteText);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanId)
                .withEntityExternalId(loan.getExternalId()).withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId())
                .withLoanId(loanId).with(changes).build();
    }

    @Override
    public CommandProcessingResult makeGoodwillCredit(Long loanId, JsonCommand command) {
        return makeRepaymentLikeTransaction(loanId, command, LoanTransactionType.GOODWILL_CREDIT);
    }

    private PaymentDetail createAndPersistPaymentDetailFromCommand(final JsonCommand command, final Map<String, Object> changes) {
        final JsonElement paymentDetailsElement = command.jsonElement(WorkingCapitalLoanConstants.paymentDetailsParamName);
        if (paymentDetailsElement != null && paymentDetailsElement.isJsonNull()) {
            return null;
        }
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
        balance.setOverpaymentAmount(BigDecimal.ZERO);
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
        balance.setOverpaymentAmount(BigDecimal.ZERO);
        applyIncomeBalancesFromSchedule(loan, balance, discount);
        this.balanceRepository.saveAndFlush(balance);
    }

    private void updateBalanceOnRepayment(final WorkingCapitalLoan loan, final BigDecimal repaymentAmount) {
        final WorkingCapitalLoanBalance balance = this.balanceRepository.findByWcLoan_Id(loan.getId())
                .orElseGet(() -> WorkingCapitalLoanBalance.createFor(loan));
        final BigDecimal principalOutstanding = balance.getPrincipalOutstanding() != null ? balance.getPrincipalOutstanding()
                : BigDecimal.ZERO;
        final BigDecimal currentTotalPaidPrincipal = balance.getTotalPaidPrincipal() != null ? balance.getTotalPaidPrincipal()
                : BigDecimal.ZERO;
        final BigDecimal currentOverpayment = balance.getOverpaymentAmount() != null ? balance.getOverpaymentAmount() : BigDecimal.ZERO;
        final BigDecimal amountAppliedToOutstanding = repaymentAmount.min(principalOutstanding);
        final BigDecimal overpaymentIncrement = repaymentAmount.subtract(amountAppliedToOutstanding).max(BigDecimal.ZERO);
        final BigDecimal discount = loan.getLoanProductRelatedDetails() != null && loan.getLoanProductRelatedDetails().getDiscount() != null
                ? loan.getLoanProductRelatedDetails().getDiscount()
                : BigDecimal.ZERO;

        final BigDecimal newPrincipalOutstanding = principalOutstanding.subtract(amountAppliedToOutstanding).max(BigDecimal.ZERO);
        balance.setPrincipalOutstanding(newPrincipalOutstanding);
        balance.setOverpaymentAmount(currentOverpayment.add(overpaymentIncrement));
        balance.setTotalPaidPrincipal(currentTotalPaidPrincipal.add(repaymentAmount));
        applyIncomeBalancesFromSchedule(loan, balance, discount);

        this.balanceRepository.saveAndFlush(balance);
    }

    private void applyIncomeBalancesFromSchedule(final WorkingCapitalLoan loan, final WorkingCapitalLoanBalance balance,
            final BigDecimal discount) {
        final BigDecimal scheduleAmortization = readScheduleActualAmortization(loan);
        final BigDecimal realizedIncome = scheduleAmortization.max(BigDecimal.ZERO).min(discount);
        balance.setRealizedIncome(realizedIncome);
        balance.setUnrealizedIncome(discount.subtract(realizedIncome).max(BigDecimal.ZERO));
    }

    private BigDecimal readScheduleActualAmortization(final WorkingCapitalLoan loan) {
        final MathContext mc = MoneyHelper.getMathContext();
        return scheduleRepositoryWrapper.readModel(loan.getId(), mc, WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan))
                .map(ProjectedAmortizationScheduleModel::totalActualAmortization).orElse(BigDecimal.ZERO);
    }

    private void updateBalanceOnCreditBalanceRefund(final WorkingCapitalLoan loan, final BigDecimal refundAmount) {
        final WorkingCapitalLoanBalance balance = this.balanceRepository.findByWcLoan_Id(loan.getId())
                .orElseGet(() -> WorkingCapitalLoanBalance.createFor(loan));
        final BigDecimal currentOverpayment = balance.getOverpaymentAmount() != null ? balance.getOverpaymentAmount() : BigDecimal.ZERO;
        balance.setOverpaymentAmount(currentOverpayment.subtract(refundAmount).max(BigDecimal.ZERO));
        this.balanceRepository.saveAndFlush(balance);
    }

    private void reverseTransaction(final WorkingCapitalLoanTransaction txn) {
        txn.setReversed(true);
        txn.setReversedOnDate(DateUtils.getBusinessLocalDate());
        txn.setReversalExternalId(ExternalId.generate());
        this.transactionRepository.save(txn);
        this.transactionRepository.flush();
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
        reverseTransaction(txn);
        reverseDiscountFeesLinkedToDisbursement(loan, txn);

        final Optional<WorkingCapitalLoanBalance> balanceOpt = this.balanceRepository.findByWcLoan_Id(loan.getId());
        balanceOpt.ifPresent(b -> {
            // Restore balance to pre-disbursement state.
            b.setPrincipalOutstanding(loan.getApprovedPrincipal() != null ? loan.getApprovedPrincipal() : loan.getProposedPrincipal());
            b.setTotalPaidPrincipal(BigDecimal.ZERO);
            b.setRealizedIncome(BigDecimal.ZERO);
            b.setUnrealizedIncome(BigDecimal.ZERO);
            b.setOverpaymentAmount(BigDecimal.ZERO);
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
            if (txn.getTypeOf() != LoanTransactionType.DISBURSEMENT && txn.getTypeOf() != LoanTransactionType.DISCOUNT_FEE
                    && txn.getTypeOf() != LoanTransactionType.DISCOUNT_FEE_ADJUSTMENT
                    && txn.getTypeOf() != LoanTransactionType.DISCOUNT_FEE_AMORTIZATION
                    && txn.getTypeOf() != LoanTransactionType.DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT) {
                throw new PlatformApiDataValidationException("validation.msg.wc.loan.undo.disbursal.not.allowed",
                        "Undo disbursal is not allowed when there are other monetary transactions on the loan", "loanId");
            }
        }
    }

    private void reverseDiscountFeesLinkedToDisbursement(final WorkingCapitalLoan loan,
            final WorkingCapitalLoanTransaction disbursementTransaction) {
        loan.getTransactions().stream().filter(t -> t.getTypeOf() == LoanTransactionType.DISCOUNT_FEE && !t.isReversed()
                && isRelatedToTransaction(t, disbursementTransaction.getId())).forEach(discountTxn -> {
                    reverseDiscountFeeAdjustmentsLinkedToDiscount(loan, discountTxn);
                    reverseTransaction(discountTxn);
                });
    }

    private void reverseDiscountFeeAdjustmentsLinkedToDiscount(final WorkingCapitalLoan loan,
            final WorkingCapitalLoanTransaction discountTransaction) {
        loan.getTransactions().stream().filter(t -> t.getTypeOf() == LoanTransactionType.DISCOUNT_FEE_ADJUSTMENT && !t.isReversed()
                && isRelatedToTransaction(t, discountTransaction.getId())).forEach(this::reverseTransaction);
    }

    private boolean isRelatedToTransaction(final WorkingCapitalLoanTransaction transaction, final Long relatedTransactionId) {
        return transaction.getLoanTransactionRelations().stream().anyMatch(
                relation -> relation.getToTransaction() != null && relatedTransactionId.equals(relation.getToTransaction().getId()));
    }

    private void createNote(final String noteText, final WorkingCapitalLoan loan) {
        if (StringUtils.isNotBlank(noteText)) {
            final WorkingCapitalLoanNote note = WorkingCapitalLoanNote.create(loan, noteText);
            this.noteRepository.save(note);
        }
    }

}
