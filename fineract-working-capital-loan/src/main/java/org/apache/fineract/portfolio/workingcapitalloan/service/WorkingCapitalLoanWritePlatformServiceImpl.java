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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanEvent;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanLifecycleStateMachine;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanNote;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNoteRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanDataValidator;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetail;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Service;

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
        if (loan.getApprovedPrincipal() == null) {
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

        this.loanRepository.saveAndFlush(loan);

        createNote(command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.noteParamName), loan);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(WorkingCapitalLoanConstants.approvedOnDateParamName, approvedOnDate);
        changes.put("status", loan.getLoanStatus());

        log.debug("Working capital loan {} approved by user {}", loanId, currentUser.getId());

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanId)
                .withEntityExternalId(loan.getExternalId()).withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId())
                .withLoanId(loanId).with(changes).build();
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

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanId)
                .withEntityExternalId(loan.getExternalId()).withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId())
                .withLoanId(loanId).with(changes).build();
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

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(loanId)
                .withEntityExternalId(loan.getExternalId()).withOfficeId(loan.getOfficeId()).withClientId(loan.getClientId())
                .withLoanId(loanId).with(changes).build();
    }

    private void createNote(final String noteText, final WorkingCapitalLoan loan) {
        if (StringUtils.isNotBlank(noteText)) {
            final WorkingCapitalLoanNote note = WorkingCapitalLoanNote.create(loan, noteText);
            this.noteRepository.save(note);
        }
    }
}
