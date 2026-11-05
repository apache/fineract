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
package org.apache.fineract.portfolio.client.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientActivateBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientCloseBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientReactivateBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientRejectBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientUndoRejectionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientUndoWithdrawalBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientWithdrawBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.data.ClientDataValidator;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientStatus;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.client.exception.InvalidClientStateTransitionException;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Moves clients between lifecycle states: activate, close, reject, withdraw, reactivate and undo a rejection or
 * withdrawal.
 */
@AllArgsConstructor
@Service
@Slf4j
public class ClientLifecycleWritePlatformService {

    private final PlatformSecurityContext context;
    private final ClientRepositoryWrapper clientRepository;
    private final ClientDataValidator fromApiJsonDeserializer;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final SavingsAccountRepositoryWrapper savingsRepositoryWrapper;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    private final ClientActivationHelper clientActivationHelper;

    @Transactional
    public CommandProcessingResult activateClient(final Long clientId, final JsonCommand command) {
        try {
            this.fromApiJsonDeserializer.validateActivation(command);

            final Client client = this.clientRepository.findOneInUserScope(clientId, true);
            this.clientActivationHelper.validateParentGroupRulesBeforeClientActivation(client);
            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
            final LocalDate activationDate = command.localDateValueOfParameterNamed("activationDate");

            this.clientActivationHelper.runEntityDatatableCheck(clientId, client.getLegalForm());

            final AppUser currentUser = this.context.authenticatedUser();
            client.activate(currentUser, fmt, activationDate);
            CommandProcessingResult result = this.clientActivationHelper.openSavingsAccount(client, fmt);
            clientRepository.saveAndFlush(client);
            businessEventNotifierService.notifyPostBusinessEvent(new ClientActivateBusinessEvent(client));
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withOfficeId(client.officeId()) //
                    .withEntityExternalId(client.getExternalId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .withSavingsId(result.getSavingsId()) //
                    .setRollbackTransaction(result.isRollbackTransaction()) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            ClientDataIntegrityIssueHandler.handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    public CommandProcessingResult closeClient(final Long clientId, final JsonCommand command) {
        try {

            final AppUser currentUser = this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateClose(command);

            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final LocalDate closureDate = command.localDateValueOfParameterNamed(ClientApiConstants.closureDateParamName);
            final Long closureReasonId = command.longValueOfParameterNamed(ClientApiConstants.closureReasonIdParamName);

            final CodeValue closureReason = this.codeValueRepository
                    .findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_CLOSURE_REASON, closureReasonId);

            if (ClientStatus.fromInt(client.getStatus()).isClosed()) {
                final String errorMessage = "Client is already closed.";
                throw new InvalidClientStateTransitionException("close", "is.already.closed", errorMessage);
            } else if (ClientStatus.fromInt(client.getStatus()).isUnderTransfer()) {
                final String errorMessage = "Cannot Close a Client under Transfer";
                throw new InvalidClientStateTransitionException("close", "is.under.transfer", errorMessage);
            }

            if (client.isNotPending() && DateUtils.isAfter(client.getActivationDate(), closureDate)) {
                final String errorMessage = "The client closureDate cannot be before the client ActivationDate.";
                throw new InvalidClientStateTransitionException("close", "date.cannot.before.client.actvation.date", errorMessage,
                        closureDate, client.getActivationDate());
            }
            final LegalForm legalForm = LegalForm.fromInt(client.getLegalForm());
            entityDatatableChecksWritePlatformService.runTheCheck(clientId, EntityTables.CLIENT.getName(), StatusEnum.CLOSE.getValue(),
                    EntityTables.CLIENT.getForeignKeyColumnNameOnDatatable(), legalForm.getLabel());

            final List<Loan> clientLoans = this.loanRepositoryWrapper.findLoanByClientId(clientId);
            for (final Loan loan : clientLoans) {
                final LoanStatusMapper loanStatus = new LoanStatusMapper(loan.getStatus().getValue());
                if (loanStatus.isOpen() || loanStatus.isPendingApproval() || loanStatus.isAwaitingDisbursal()) {
                    final String errorMessage = "Client cannot be closed because of non-closed loans.";
                    throw new InvalidClientStateTransitionException("close", "loan.non-closed", errorMessage);
                } else if (loanStatus.isClosed() && DateUtils.isAfter(loan.getClosedOnDate(), closureDate)) {
                    final String errorMessage = "The client closureDate cannot be before the loan closedOnDate.";
                    throw new InvalidClientStateTransitionException("close", "date.cannot.before.loan.closed.date", errorMessage,
                            closureDate, loan.getClosedOnDate());
                } else if (loanStatus.isOverpaid()) {
                    final String errorMessage = "Client cannot be closed because of overpaid loans.";
                    throw new InvalidClientStateTransitionException("close", "loan.overpaid", errorMessage);
                }
            }
            final List<SavingsAccount> clientSavingAccounts = this.savingsRepositoryWrapper.findSavingAccountByClientId(clientId);

            for (final SavingsAccount saving : clientSavingAccounts) {
                if (saving.isActive() || saving.isSubmittedAndPendingApproval() || saving.isApproved()) {
                    final String errorMessage = "Client cannot be closed because of non-closed savings account.";
                    throw new InvalidClientStateTransitionException("close", "non-closed.savings.account", errorMessage);
                }
            }

            client.close(currentUser, closureReason, closureDate);
            this.clientRepository.saveAndFlush(client);
            businessEventNotifierService.notifyPostBusinessEvent(new ClientCloseBusinessEvent(client));
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .withEntityExternalId(client.getExternalId()) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            ClientDataIntegrityIssueHandler.handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }
    }

    public CommandProcessingResult rejectClient(final Long entityId, final JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateRejection(command);

        final Client client = this.clientRepository.findOneWithNotFoundDetection(entityId);
        final LocalDate rejectionDate = command.localDateValueOfParameterNamed(ClientApiConstants.rejectionDateParamName);
        final Long rejectionReasonId = command.longValueOfParameterNamed(ClientApiConstants.rejectionReasonIdParamName);

        final CodeValue rejectionReason = this.codeValueRepository
                .findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_REJECT_REASON, rejectionReasonId);

        if (client.isNotPending()) {
            final String errorMessage = "Only clients pending activation may be withdrawn.";
            throw new InvalidClientStateTransitionException("rejection", "on.account.not.in.pending.activation.status", errorMessage,
                    rejectionDate, client.getSubmittedOnDate());
        } else if (DateUtils.isAfter(client.getSubmittedOnDate(), rejectionDate)) {
            final String errorMessage = "The client rejection date cannot be before the client submitted date.";
            throw new InvalidClientStateTransitionException("rejection", "date.cannot.before.client.submitted.date", errorMessage,
                    rejectionDate, client.getSubmittedOnDate());
        }
        client.reject(currentUser, rejectionReason, rejectionDate);
        clientRepository.saveAndFlush(client);
        businessEventNotifierService.notifyPostBusinessEvent(new ClientRejectBusinessEvent(client));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withClientId(entityId) //
                .withEntityId(entityId) //
                .withEntityExternalId(client.getExternalId()) //
                .build();
    }

    public CommandProcessingResult withdrawClient(Long entityId, JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateWithdrawn(command);

        final Client client = this.clientRepository.findOneWithNotFoundDetection(entityId);
        final LocalDate withdrawalDate = command.localDateValueOfParameterNamed(ClientApiConstants.withdrawalDateParamName);
        final Long withdrawalReasonId = command.longValueOfParameterNamed(ClientApiConstants.withdrawalReasonIdParamName);

        final CodeValue withdrawalReason = this.codeValueRepository
                .findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_WITHDRAW_REASON, withdrawalReasonId);

        if (client.isNotPending()) {
            final String errorMessage = "Only clients pending activation may be withdrawn.";
            throw new InvalidClientStateTransitionException("withdrawal", "on.account.not.in.pending.activation.status", errorMessage,
                    withdrawalDate, client.getSubmittedOnDate());
        } else if (DateUtils.isAfter(client.getSubmittedOnDate(), withdrawalDate)) {
            final String errorMessage = "The client withdrawal date cannot be before the client submitted date.";
            throw new InvalidClientStateTransitionException("withdrawal", "date.cannot.before.client.submitted.date", errorMessage,
                    withdrawalDate, client.getSubmittedOnDate());
        }
        client.withdraw(currentUser, withdrawalReason, withdrawalDate);
        this.clientRepository.saveAndFlush(client);
        businessEventNotifierService.notifyPostBusinessEvent(new ClientWithdrawBusinessEvent(client));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withClientId(entityId) //
                .withEntityId(entityId) //
                .withEntityExternalId(client.getExternalId()) //
                .build();
    }

    public CommandProcessingResult reActivateClient(Long entityId, JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateReactivate(command);

        final Client client = this.clientRepository.findOneWithNotFoundDetection(entityId);
        final LocalDate reactivateDate = command.localDateValueOfParameterNamed(ClientApiConstants.reactivationDateParamName);

        if (!client.isClosed()) {
            final String errorMessage = "only closed clients may be reactivated.";
            throw new InvalidClientStateTransitionException("reactivation", "on.nonclosed.account", errorMessage);
        } else if (DateUtils.isAfter(client.getClosureDate(), reactivateDate)) {
            final String errorMessage = "The client reactivation date cannot be before the client closed date.";
            throw new InvalidClientStateTransitionException("reactivation", "date.cannot.before.client.closed.date", errorMessage,
                    reactivateDate, client.getClosureDate());
        }
        client.reActivate(currentUser, reactivateDate);
        this.clientRepository.saveAndFlush(client);
        businessEventNotifierService.notifyPostBusinessEvent(new ClientReactivateBusinessEvent(client));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withClientId(entityId) //
                .withEntityId(entityId) //
                .withEntityExternalId(client.getExternalId()) //
                .build();
    }

    public CommandProcessingResult undoRejection(Long entityId, JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateUndoRejection(command);

        final Client client = this.clientRepository.findOneWithNotFoundDetection(entityId);
        final LocalDate undoRejectDate = command.localDateValueOfParameterNamed(ClientApiConstants.reopenedDateParamName);

        if (!client.isRejected()) {
            final String errorMessage = "only rejected clients may be reactivated.";
            throw new InvalidClientStateTransitionException("undorejection", "on.nonrejected.account", errorMessage);
        } else if (DateUtils.isAfter(client.getRejectedDate(), undoRejectDate)) {
            final String errorMessage = "The client reactivation date cannot be before the client rejected date.";
            throw new InvalidClientStateTransitionException("reopened", "date.cannot.before.client.rejected.date", errorMessage,
                    undoRejectDate, client.getRejectedDate());
        }

        client.reOpened(currentUser, undoRejectDate);
        this.clientRepository.saveAndFlush(client);
        businessEventNotifierService.notifyPostBusinessEvent(new ClientUndoRejectionBusinessEvent(client));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withClientId(entityId) //
                .withEntityId(entityId) //
                .withEntityExternalId(client.getExternalId()) //
                .build();
    }

    public CommandProcessingResult undoWithdrawal(Long entityId, JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateUndoWithDrawn(command);

        final Client client = this.clientRepository.findOneWithNotFoundDetection(entityId);
        final LocalDate undoWithdrawalDate = command.localDateValueOfParameterNamed(ClientApiConstants.reopenedDateParamName);

        if (!client.isWithdrawn()) {
            final String errorMessage = "only withdrawal clients may be reactivated.";
            throw new InvalidClientStateTransitionException("undoWithdrawal", "on.nonwithdrawal.account", errorMessage);
        } else if (DateUtils.isAfter(client.getWithdrawalDate(), undoWithdrawalDate)) {
            final String errorMessage = "The client reactivation date cannot be before the client withdrawal date.";
            throw new InvalidClientStateTransitionException("reopened", "date.cannot.before.client.withdrawal.date", errorMessage,
                    undoWithdrawalDate, client.getWithdrawalDate());
        }
        client.reOpened(currentUser, undoWithdrawalDate);
        this.clientRepository.saveAndFlush(client);
        businessEventNotifierService.notifyPostBusinessEvent(new ClientUndoWithdrawalBusinessEvent(client));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withClientId(entityId) //
                .withEntityId(entityId) //
                .withEntityExternalId(client.getExternalId()) //
                .build();
    }
}
