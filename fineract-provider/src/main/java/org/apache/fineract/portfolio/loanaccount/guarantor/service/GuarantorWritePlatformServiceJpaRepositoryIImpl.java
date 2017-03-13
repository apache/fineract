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
package org.apache.fineract.portfolio.loanaccount.guarantor.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.account.domain.AccountAssociationType;
import org.apache.fineract.portfolio.account.domain.AccountAssociations;
import org.apache.fineract.portfolio.account.domain.AccountAssociationsRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.guarantor.GuarantorConstants;
import org.apache.fineract.portfolio.loanaccount.guarantor.GuarantorConstants.GUARANTOR_JSON_INPUT_PARAMS;
import org.apache.fineract.portfolio.loanaccount.guarantor.command.GuarantorCommand;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.Guarantor;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorFundStatusType;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorFundingDetails;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorRepository;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorType;
import org.apache.fineract.portfolio.loanaccount.guarantor.exception.DuplicateGuarantorException;
import org.apache.fineract.portfolio.loanaccount.guarantor.exception.GuarantorNotFoundException;
import org.apache.fineract.portfolio.loanaccount.guarantor.exception.InvalidGuarantorException;
import org.apache.fineract.portfolio.loanaccount.guarantor.serialization.GuarantorCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuarantorWritePlatformServiceJpaRepositoryIImpl implements GuarantorWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(GuarantorWritePlatformServiceJpaRepositoryIImpl.class);

    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final StaffRepositoryWrapper staffRepositoryWrapper;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final GuarantorRepository guarantorRepository;
    private final GuarantorCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;
    private final SavingsAccountAssembler savingsAccountAssembler;
    private final AccountAssociationsRepository accountAssociationsRepository;
    private final GuarantorDomainService guarantorDomainService;

    @Autowired
    public GuarantorWritePlatformServiceJpaRepositoryIImpl(final LoanRepositoryWrapper loanRepositoryWrapper,
            final GuarantorRepository guarantorRepository, final ClientRepositoryWrapper clientRepositoryWrapper,
            final StaffRepositoryWrapper staffRepositoryWrapper, final GuarantorCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final CodeValueRepositoryWrapper codeValueRepositoryWrapper, final SavingsAccountAssembler savingsAccountAssembler,
            final AccountAssociationsRepository accountAssociationsRepository, final GuarantorDomainService guarantorDomainService) {
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.guarantorRepository = guarantorRepository;
        this.staffRepositoryWrapper = staffRepositoryWrapper;
        this.codeValueRepositoryWrapper = codeValueRepositoryWrapper;
        this.savingsAccountAssembler = savingsAccountAssembler;
        this.accountAssociationsRepository = accountAssociationsRepository;
        this.guarantorDomainService = guarantorDomainService;
    }

    @Override
    @Transactional
    public CommandProcessingResult createGuarantor(final Long loanId, final JsonCommand command) {
        final GuarantorCommand guarantorCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final List<Guarantor> existGuarantorList = this.guarantorRepository.findByLoan(loan);
        return createGuarantor(loan, command, guarantorCommand, existGuarantorList);
    }

    private CommandProcessingResult createGuarantor(final Loan loan, final JsonCommand command, final GuarantorCommand guarantorCommand,
            final Collection<Guarantor> existGuarantorList) {
        try {
            guarantorCommand.validateForCreate();
            validateLoanStatus(loan);
            final List<GuarantorFundingDetails> guarantorFundingDetails = new ArrayList<>();
            AccountAssociations accountAssociations = null;
            if (guarantorCommand.getSavingsId() != null) {
                final SavingsAccount savingsAccount = this.savingsAccountAssembler.assembleFrom(guarantorCommand.getSavingsId());
                validateGuarantorSavingsAccountActivationDateWithLoanSubmittedOnDate(loan,savingsAccount);
                accountAssociations = AccountAssociations.associateSavingsAccount(loan, savingsAccount,
                        AccountAssociationType.GUARANTOR_ACCOUNT_ASSOCIATION.getValue(), true);

                GuarantorFundingDetails fundingDetails = new GuarantorFundingDetails(accountAssociations,
                        GuarantorFundStatusType.ACTIVE.getValue(), guarantorCommand.getAmount());
                guarantorFundingDetails.add(fundingDetails);
                if (loan.isDisbursed() || loan.isApproved()
                        && (loan.getGuaranteeAmount() != null || loan.loanProduct().isHoldGuaranteeFundsEnabled())) {
                    this.guarantorDomainService.assignGuarantor(fundingDetails, LocalDate.now());
                    loan.updateGuaranteeAmount(fundingDetails.getAmount());
                }
            }

            final Long clientRelationshipId = guarantorCommand.getClientRelationshipTypeId();
            CodeValue clientRelationshipType = null;

            if (clientRelationshipId != null) {
                clientRelationshipType = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(
                        GuarantorConstants.GUARANTOR_RELATIONSHIP_CODE_NAME, clientRelationshipId);
            }

            final Long entityId = guarantorCommand.getEntityId();
            final Integer guarantorTypeId = guarantorCommand.getGuarantorTypeId();
            Guarantor guarantor = null;
            for (final Guarantor avilableGuarantor : existGuarantorList) {
                if (entityId != null && avilableGuarantor.getEntityId() != null && avilableGuarantor.getEntityId().equals(entityId)
                        && avilableGuarantor.getGurantorType().equals(guarantorTypeId) && avilableGuarantor.isActive()) {
                    if (guarantorCommand.getSavingsId() == null || avilableGuarantor.hasGuarantor(guarantorCommand.getSavingsId())) {
                        /** Get the right guarantor based on guarantorType **/
                        String defaultUserMessage = null;
                        if (guarantorTypeId.equals(GuarantorType.STAFF.getValue())) {
                            defaultUserMessage = this.staffRepositoryWrapper.findOneWithNotFoundDetection(entityId).displayName();
                        } else {
                            defaultUserMessage = this.clientRepositoryWrapper.findOneWithNotFoundDetection(entityId).getDisplayName();
                        }

                        defaultUserMessage = defaultUserMessage + " is already exist as a guarantor for this loan";
                        final String action = loan.client() != null ? "client.guarantor" : "group.guarantor";
                        throw new DuplicateGuarantorException(action, "is.already.exist.same.loan", defaultUserMessage, entityId,
                                loan.getId());
                    }
                    guarantor = avilableGuarantor;
                    break;
                }
            }

            if (guarantor == null) {
                guarantor = Guarantor.fromJson(loan, clientRelationshipType, command, guarantorFundingDetails);
            } else {
                guarantor.addFundingDetails(guarantorFundingDetails);
            }
            validateGuarantorBusinessRules(guarantor);
            for (GuarantorFundingDetails fundingDetails : guarantorFundingDetails) {
                fundingDetails.updateGuarantor(guarantor);
            }

            if (accountAssociations != null) {
                this.accountAssociationsRepository.save(accountAssociations);
            }
            this.guarantorRepository.save(guarantor);
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(guarantor.getOfficeId())
                    .withEntityId(guarantor.getId()).withLoanId(loan.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleGuarantorDataIntegrityIssues(dve);
            return CommandProcessingResult.empty();
        }
    }

    private void validateGuarantorSavingsAccountActivationDateWithLoanSubmittedOnDate(final Loan loan, final SavingsAccount savingsAccount) {
        if (loan.getSubmittedOnDate().isBefore(savingsAccount.getActivationLocalDate())) { throw new GeneralPlatformDomainRuleException(
                "error.msg.guarantor.saving.account.activation.date.is.on.or.before.loan.submitted.on.date",
                "Guarantor saving account activation date [" + savingsAccount.getActivationLocalDate()
                        + "] is on or before the loan submitted on date [" + loan.getSubmittedOnDate() + "]",
                savingsAccount.getActivationLocalDate(), loan.getSubmittedOnDate()); }
    }

    @Override
    @Transactional
    public CommandProcessingResult updateGuarantor(final Long loanId, final Long guarantorId, final JsonCommand command) {
        try {
            final GuarantorCommand guarantorCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            guarantorCommand.validateForUpdate();

            final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
            validateLoanStatus(loan);
            final Guarantor guarantorForUpdate = this.guarantorRepository.findByLoanAndId(loan, guarantorId);
            if (guarantorForUpdate == null) { throw new GuarantorNotFoundException(loanId, guarantorId); }

            final Map<String, Object> changesOnly = guarantorForUpdate.update(command);

            if (changesOnly.containsKey(GUARANTOR_JSON_INPUT_PARAMS.CLIENT_RELATIONSHIP_TYPE_ID.getValue())) {
                final Long clientRelationshipId = guarantorCommand.getClientRelationshipTypeId();
                CodeValue clientRelationshipType = null;
                if (clientRelationshipId != null) {
                    clientRelationshipType = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(
                            GuarantorConstants.GUARANTOR_RELATIONSHIP_CODE_NAME, clientRelationshipId);
                }
                guarantorForUpdate.updateClientRelationshipType(clientRelationshipType);
            }

            final List<Guarantor> existGuarantorList = this.guarantorRepository.findByLoan(loan);
            final Integer guarantorTypeId = guarantorCommand.getGuarantorTypeId();
            final GuarantorType guarantorType = GuarantorType.fromInt(guarantorTypeId);
            if (guarantorType.isCustomer() || guarantorType.isStaff()) {
                final Long entityId = guarantorCommand.getEntityId();
                for (final Guarantor guarantor : existGuarantorList) {
                    if (guarantor.getEntityId().equals(entityId) && guarantor.getGurantorType().equals(guarantorTypeId)
                            && !guarantorForUpdate.getId().equals(guarantor.getId())) {
                        String defaultUserMessage = this.clientRepositoryWrapper.findOneWithNotFoundDetection(entityId).getDisplayName();
                        defaultUserMessage = defaultUserMessage + " is already exist as a guarantor for this loan";
                        final String action = loan.client() != null ? "client.guarantor" : "group.guarantor";
                        throw new DuplicateGuarantorException(action, "is.already.exist.same.loan", defaultUserMessage, entityId, loanId);
                    }
                }
            }

            if (changesOnly.containsKey(GUARANTOR_JSON_INPUT_PARAMS.ENTITY_ID)
                    || changesOnly.containsKey(GUARANTOR_JSON_INPUT_PARAMS.GUARANTOR_TYPE_ID)) {
                validateGuarantorBusinessRules(guarantorForUpdate);
            }

            if (!changesOnly.isEmpty()) {
                this.guarantorRepository.save(guarantorForUpdate);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(guarantorForUpdate.getOfficeId())
                    .withEntityId(guarantorForUpdate.getId()).withOfficeId(guarantorForUpdate.getLoanId()).with(changesOnly).build();
        } catch (final DataIntegrityViolationException dve) {
            handleGuarantorDataIntegrityIssues(dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult removeGuarantor(final Long loanId, final Long guarantorId, final Long guarantorFundingId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        validateLoanStatus(loan);
        final Guarantor guarantorForDelete = this.guarantorRepository.findByLoanAndId(loan, guarantorId);
        if (guarantorForDelete == null || (guarantorFundingId == null && !guarantorForDelete.getGuarantorFundDetails().isEmpty())) { throw new GuarantorNotFoundException(
                loanId, guarantorId, guarantorFundingId); }
        CommandProcessingResult commandProcessingResult = removeGuarantor(guarantorForDelete, loanId, guarantorFundingId);
        if (loan.isApproved() || loan.isDisbursed()) {
            this.guarantorDomainService.validateGuarantorBusinessRules(loan);
        }
        return commandProcessingResult;
    }

    private CommandProcessingResult removeGuarantor(final Guarantor guarantorForDelete, final Long loanId, final Long guarantorFundingId) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("Guarantor");

        if (guarantorFundingId == null) {
            if (!guarantorForDelete.isActive()) {
                baseDataValidator.failWithCodeNoParameterAddedToErrorCode(GuarantorConstants.GUARANTOR_NOT_ACTIVE_ERROR);
            }
            guarantorForDelete.updateStatus(false);
        } else {
            GuarantorFundingDetails guarantorFundingDetails = guarantorForDelete.getGuarantorFundingDetail(guarantorFundingId);
            if (guarantorFundingDetails == null) { throw new GuarantorNotFoundException(loanId, guarantorForDelete.getId(),
                    guarantorFundingId); }
            removeguarantorFundDetails(guarantorForDelete, baseDataValidator, guarantorFundingDetails);

        }
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
        this.guarantorRepository.saveAndFlush(guarantorForDelete);
        CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder()
                .withEntityId(guarantorForDelete.getId()).withLoanId(guarantorForDelete.getLoanId())
                .withOfficeId(guarantorForDelete.getOfficeId());
        if (guarantorFundingId != null) {
            commandProcessingResultBuilder.withSubEntityId(guarantorFundingId);
        }
        return commandProcessingResultBuilder.build();
    }

    private void removeguarantorFundDetails(final Guarantor guarantorForDelete, final DataValidatorBuilder baseDataValidator,
            GuarantorFundingDetails guarantorFundingDetails) {
        if (!guarantorFundingDetails.getStatus().isActive()) {
            baseDataValidator.failWithCodeNoParameterAddedToErrorCode(GuarantorConstants.GUARANTOR_NOT_ACTIVE_ERROR);
        }
        GuarantorFundStatusType fundStatusType = GuarantorFundStatusType.DELETED;
        if (guarantorForDelete.getLoan().isDisbursed() || guarantorForDelete.getLoan().isApproved()) {
            fundStatusType = GuarantorFundStatusType.WITHDRAWN;
            this.guarantorDomainService.releaseGuarantor(guarantorFundingDetails, LocalDate.now());
        }
        guarantorForDelete.updateStatus(guarantorFundingDetails, fundStatusType);
    }

    private void validateGuarantorBusinessRules(final Guarantor guarantor) {
        // validate guarantor conditions
        if (guarantor.isExistingCustomer()) {
            // check client exists
            this.clientRepositoryWrapper.findOneWithNotFoundDetection(guarantor.getEntityId());
            // validate that the client is not set as a self guarantor
            if (guarantor.getClientId() != null && guarantor.getClientId().equals(guarantor.getEntityId())) {
                String errorCode = null;
                if (guarantor.getGuarantorFundDetails().isEmpty()) {
                    errorCode = "guarantor.can.not.be.own";
                } else if (guarantor.getClientRelationshipType() != null) {
                    errorCode = "guarantor.relation.should.be.empty.for.own";
                }
                if (errorCode != null) { throw new InvalidGuarantorException(guarantor.getEntityId(), guarantor.getLoanId(), errorCode); }
            }

        } else if (guarantor.isExistingEmployee()) {
            this.staffRepositoryWrapper.findOneWithNotFoundDetection(guarantor.getEntityId());
        }
    }

    private void validateLoanStatus(Loan loan) {
        if (!loan.status().isActiveOrAwaitingApprovalOrDisbursal()) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.guarantor");
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("loan.is.closed");
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    private void handleGuarantorDataIntegrityIssues(final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();
        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.guarantor.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource Guarantor: " + realCause.getMessage());
    }
}