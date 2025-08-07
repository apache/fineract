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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.account.domain.AccountAssociationType;
import org.apache.fineract.portfolio.account.domain.AccountAssociations;
import org.apache.fineract.portfolio.account.domain.AccountAssociationsRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.guarantor.GuarantorConstants;
import org.apache.fineract.portfolio.loanaccount.guarantor.GuarantorConstants.GuarantorJSONinputParams;
import org.apache.fineract.portfolio.loanaccount.guarantor.command.GuarantorCommand;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.CreateGuarantorsRequest;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.CreateGuarantorsResponse;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.DeleteGuarantorsRequest;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.DeleteGuarantorsResponse;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.UpdateGuarantorsRequest;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.UpdateGuarantorsResponse;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.Guarantor;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorFundStatusType;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorFundingDetails;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorRepository;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorType;
import org.apache.fineract.portfolio.loanaccount.guarantor.exception.DuplicateGuarantorException;
import org.apache.fineract.portfolio.loanaccount.guarantor.exception.GuarantorNotFoundException;
import org.apache.fineract.portfolio.loanaccount.guarantor.exception.InvalidGuarantorException;
import org.apache.fineract.portfolio.loanaccount.guarantor.mapper.GuarantorMapper;
import org.apache.fineract.portfolio.loanaccount.guarantor.serialization.GuarantorCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuarantorWritePlatformServiceJpaRepositoryIImpl implements GuarantorWritePlatformService {

    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final StaffRepositoryWrapper staffRepositoryWrapper;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final GuarantorRepository guarantorRepository;
    private final GuarantorCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;
    private final SavingsAccountAssembler savingsAccountAssembler;
    private final AccountAssociationsRepository accountAssociationsRepository;
    private final GuarantorDomainService guarantorDomainService;
    private final GuarantorMapper mapper;

    @Deprecated
    @Override
    @Transactional
    public CommandProcessingResult createGuarantor(final Long loanId, final JsonCommand command) {
        final GuarantorCommand guarantorCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final List<Guarantor> existGuarantorList = this.guarantorRepository.findByLoan(loan);
        return createGuarantor(loan, command, guarantorCommand, existGuarantorList);
    }

    @Deprecated
    private CommandProcessingResult createGuarantor(final Loan loan, final JsonCommand command, final GuarantorCommand guarantorCommand,
            final Collection<Guarantor> existGuarantorList) {
        try {
            guarantorCommand.validateForCreate();
            validateLoanStatus(loan);
            final List<GuarantorFundingDetails> guarantorFundingDetails = new ArrayList<>();
            final boolean backdatedTxnsAllowedTill = false;
            AccountAssociations accountAssociations = null;
            if (guarantorCommand.getSavingsId() != null) {
                final SavingsAccount savingsAccount = this.savingsAccountAssembler.assembleFrom(guarantorCommand.getSavingsId(),
                        backdatedTxnsAllowedTill);
                validateGuarantorSavingsAccountActivationDateWithLoanSubmittedOnDate(loan, savingsAccount);
                accountAssociations = AccountAssociations.associateSavingsAccount(loan, savingsAccount,
                        AccountAssociationType.GUARANTOR_ACCOUNT_ASSOCIATION.getValue(), backdatedTxnsAllowedTill);

                GuarantorFundingDetails fundingDetails = new GuarantorFundingDetails(accountAssociations,
                        GuarantorFundStatusType.ACTIVE.getValue(), guarantorCommand.getAmount());
                guarantorFundingDetails.add(fundingDetails);
                if (loan.isDisbursed()
                        || (loan.isApproved() && (loan.getGuaranteeAmount() != null || loan.loanProduct().isHoldGuaranteeFunds()))) {
                    this.guarantorDomainService.assignGuarantor(fundingDetails, DateUtils.getBusinessLocalDate());
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
                this.accountAssociationsRepository.saveAndFlush(accountAssociations);
            }
            this.guarantorRepository.saveAndFlush(guarantor);
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(guarantor.getOfficeId())
                    .withEntityId(guarantor.getId()).withLoanId(loan.getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleGuarantorDataIntegrityIssues(throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    private void validateGuarantorSavingsAccountActivationDateWithLoanSubmittedOnDate(final Loan loan,
            final SavingsAccount savingsAccount) {
        if (DateUtils.isBefore(loan.getSubmittedOnDate(), savingsAccount.getActivationDate())) {
            throw new GeneralPlatformDomainRuleException(
                    "error.msg.guarantor.saving.account.activation.date.is.on.or.before.loan.submitted.on.date",
                    "Guarantor saving account activation date [" + savingsAccount.getActivationDate()
                            + "] is on or before the loan submitted on date [" + loan.getSubmittedOnDate() + "]",
                    savingsAccount.getActivationDate(), loan.getSubmittedOnDate());
        }
    }

    @Deprecated
    @Override
    @Transactional
    public CommandProcessingResult updateGuarantor(final Long loanId, final Long guarantorId, final JsonCommand command) {
        try {
            final GuarantorCommand guarantorCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            guarantorCommand.validateForUpdate();

            final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
            validateLoanStatus(loan);
            final Guarantor guarantorForUpdate = this.guarantorRepository.findByLoanAndId(loan, guarantorId);
            if (guarantorForUpdate == null) {
                throw new GuarantorNotFoundException(loanId, guarantorId);
            }

            final Map<String, Object> changesOnly = guarantorForUpdate.update(command);

            if (changesOnly.containsKey(GuarantorJSONinputParams.CLIENT_RELATIONSHIP_TYPE_ID.getValue())) {
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

            if (changesOnly.containsKey(GuarantorJSONinputParams.ENTITY_ID.getValue())
                    || changesOnly.containsKey(GuarantorJSONinputParams.GUARANTOR_TYPE_ID.getValue())) {
                validateGuarantorBusinessRules(guarantorForUpdate);
            }

            if (!changesOnly.isEmpty()) {
                this.guarantorRepository.saveAndFlush(guarantorForUpdate);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(guarantorForUpdate.getOfficeId())
                    .withEntityId(guarantorForUpdate.getId()).withOfficeId(guarantorForUpdate.getLoanId()).with(changesOnly).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleGuarantorDataIntegrityIssues(throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Deprecated
    @Override
    @Transactional
    public CommandProcessingResult removeGuarantor(final Long loanId, final Long guarantorId, final Long guarantorFundingId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        validateLoanStatus(loan);
        final Guarantor guarantorForDelete = this.guarantorRepository.findByLoanAndId(loan, guarantorId);
        if (guarantorForDelete == null || (guarantorFundingId == null && !guarantorForDelete.getGuarantorFundDetails().isEmpty())) {
            throw new GuarantorNotFoundException(loanId, guarantorId, guarantorFundingId);
        }
        CommandProcessingResult commandProcessingResult = removeGuarantor(guarantorForDelete, loanId, guarantorFundingId);
        if (loan.isApproved() || loan.isDisbursed()) {
            this.guarantorDomainService.validateGuarantorBusinessRules(loan);
        }
        return commandProcessingResult;
    }

    @Transactional
    @Override
    public CreateGuarantorsResponse createGuarantor(Command<CreateGuarantorsRequest> command) {
        final Long loanId = command.getPayload().getLoanId();
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final List<Guarantor> existGuarantorList = this.guarantorRepository.findByLoan(loan);
        return createGuarantor(loan, command, existGuarantorList);
    }

    private CreateGuarantorsResponse createGuarantor(final Loan loan, final Command<CreateGuarantorsRequest> command,
            final List<Guarantor> existGuarantorList) {
        try {
            validateLoanStatus(loan);
            final List<GuarantorFundingDetails> guarantorFundingDetails = new ArrayList<>();
            final boolean backdatedTxnsAllowedTill = false;
            AccountAssociations accountAssociations = null;

            if (command.getPayload().getSavingsId() != null) {
                final SavingsAccount savingsAccount = this.savingsAccountAssembler.assembleFrom(command.getPayload().getSavingsId(),
                        backdatedTxnsAllowedTill);
                validateGuarantorSavingsAccountActivationDateWithLoanSubmittedOnDate(loan, savingsAccount);
                accountAssociations = AccountAssociations.associateSavingsAccount(loan, savingsAccount,
                        AccountAssociationType.GUARANTOR_ACCOUNT_ASSOCIATION.getValue(), backdatedTxnsAllowedTill);

                GuarantorFundingDetails fundingDetails = new GuarantorFundingDetails(accountAssociations,
                        GuarantorFundStatusType.ACTIVE.getValue(), command.getPayload().getAmount());
                guarantorFundingDetails.add(fundingDetails);
                if (loan.isDisbursed()
                        || (loan.isApproved() && (loan.getGuaranteeAmount() != null || loan.loanProduct().isHoldGuaranteeFunds()))) {
                    this.guarantorDomainService.assignGuarantor(fundingDetails, DateUtils.getBusinessLocalDate());
                    loan.updateGuaranteeAmount(fundingDetails.getAmount());
                }
            }

            final Long clientRelationshipId = command.getPayload().getClientRelationshipTypeId();
            CodeValue clientRelationshipType = null;

            if (clientRelationshipId != null) {
                clientRelationshipType = this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(
                        GuarantorConstants.GUARANTOR_RELATIONSHIP_CODE_NAME, clientRelationshipId);
            }

            final Long entityId = command.getPayload().getEntityId();
            final Integer guarantorTypeId = command.getPayload().getGuarantorTypeId();
            Guarantor guarantor = null;
            for (final Guarantor availableGuarantor : existGuarantorList) {
                if (entityId != null && availableGuarantor.getEntityId() != null && availableGuarantor.getEntityId().equals(entityId)
                        && availableGuarantor.getGurantorType().equals(guarantorTypeId) && availableGuarantor.isActive()) {
                    if (command.getPayload().getSavingsId() == null
                            || availableGuarantor.hasGuarantor(command.getPayload().getSavingsId())) {
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
                    guarantor = availableGuarantor;
                    break;
                }
            }

            if (guarantor == null) {
                // Map The Command payload from DTO to Entity
                guarantor = mapper.toEntity(command.getPayload());
                guarantor.setDateOfBirth(this.toLocalDate(command.getPayload().getDob(), command.getPayload().getDateFormat(),
                        command.getPayload().getLocale()));
                guarantor.setLoan(loan);
                guarantor.setClientRelationshipType(clientRelationshipType);
                guarantor.setGuarantorFundDetails(guarantorFundingDetails);
                guarantor.setActive(true);
            } else {
                guarantor.addFundingDetails(guarantorFundingDetails);
            }
            validateGuarantorBusinessRules(guarantor);
            for (GuarantorFundingDetails fundingDetails : guarantorFundingDetails) {
                fundingDetails.updateGuarantor(guarantor);
            }

            if (accountAssociations != null) {
                this.accountAssociationsRepository.saveAndFlush(accountAssociations);
            }
            this.guarantorRepository.saveAndFlush(guarantor);
            return CreateGuarantorsResponse.builder().commandId(command.getId()).officeId(guarantor.getOfficeId())
                    .resourceId(guarantor.getId()).loanId(loan.getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleGuarantorDataIntegrityIssues(throwable, dve);
            return new CreateGuarantorsResponse();
        }
    }

    @Transactional
    @Override
    public UpdateGuarantorsResponse updateGuarantor(Command<UpdateGuarantorsRequest> command) {
        try {
            final Long loanId = command.getPayload().getLoanId();
            final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
            validateLoanStatus(loan);
            final Long guarantorId = command.getPayload().getGuarantorId();
            final Guarantor guarantorForUpdate = this.guarantorRepository.findByLoanAndId(loan, guarantorId);
            if (guarantorForUpdate == null) {
                throw new GuarantorNotFoundException(loanId, guarantorId);
            }

            final Map<String, Object> changesOnly = getUpdateChanges(guarantorForUpdate, command.getPayload());

            if (changesOnly.containsKey("clientRelationshipTypeId")) {
                final Long clientRelationshipId = command.getPayload().getClientRelationshipTypeId();
                CodeValue clientRelationshipType = null;
                if (clientRelationshipId != null) {
                    clientRelationshipType = this.codeValueRepositoryWrapper
                            .findOneByCodeNameAndIdWithNotFoundDetection("GuarantorRelationship", clientRelationshipId);
                }
                guarantorForUpdate.updateClientRelationshipType(clientRelationshipType);
            }

            final List<Guarantor> existGuarantorList = this.guarantorRepository.findByLoan(loan);
            final Integer guarantorTypeId = command.getPayload().getGuarantorTypeId();
            final GuarantorType guarantorType = GuarantorType.fromInt(guarantorTypeId);
            if (guarantorType.isCustomer() || guarantorType.isStaff()) {
                final Long entityId = command.getPayload().getEntityId();
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
            if (changesOnly.containsKey("entityId") || changesOnly.containsKey("guarantorTypeId")) {
                validateGuarantorBusinessRules(guarantorForUpdate);
            }
            if (!changesOnly.isEmpty()) {
                this.guarantorRepository.saveAndFlush(guarantorForUpdate);
            }

            return UpdateGuarantorsResponse.builder().commandId(command.getId()).officeId(guarantorForUpdate.getOfficeId())
                    .resourceId(guarantorForUpdate.getId()).loanId(guarantorForUpdate.getLoanId()).changes(new LinkedHashMap<>(changesOnly))
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleGuarantorDataIntegrityIssues(throwable, dve);
            return new UpdateGuarantorsResponse();
        }
    }

    @Transactional
    @Override
    public DeleteGuarantorsResponse removeGuarantor(Command<DeleteGuarantorsRequest> command) {
        final Long loanId = command.getPayload().getLoanId();
        final Long guarantorId = command.getPayload().getGuarantorId();
        final Long guarantorFundingId = command.getPayload().getGuarantorFundingId();

        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        validateLoanStatus(loan);
        final Guarantor guarantorForDelete = this.guarantorRepository.findByLoanAndId(loan, guarantorId);
        if (guarantorForDelete == null || (guarantorFundingId == null && !guarantorForDelete.getGuarantorFundDetails().isEmpty())) {
            throw new GuarantorNotFoundException(loanId, guarantorId, guarantorFundingId);
        }
        DeleteGuarantorsResponse response = removeGuarantorFromDatabase(guarantorForDelete, loanId, guarantorFundingId);
        if (loan.isApproved() || loan.isDisbursed()) {
            this.guarantorDomainService.validateGuarantorBusinessRules(loan);
        }
        return response;
    }

    private DeleteGuarantorsResponse removeGuarantorFromDatabase(final Guarantor guarantorForDelete, final Long loanId,
            final Long guarantorFundingId) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("Guarantor");

        if (guarantorFundingId == null) {
            if (!guarantorForDelete.isActive()) {
                baseDataValidator.failWithCodeNoParameterAddedToErrorCode(GuarantorConstants.GUARANTOR_NOT_ACTIVE_ERROR);
            }
            guarantorForDelete.updateStatus(false);
        } else {
            GuarantorFundingDetails guarantorFundingDetails = guarantorForDelete.getGuarantorFundingDetail(guarantorFundingId);
            if (guarantorFundingDetails == null) {
                throw new GuarantorNotFoundException(loanId, guarantorForDelete.getId(), guarantorFundingId);
            }
            removeguarantorFundDetails(guarantorForDelete, baseDataValidator, guarantorFundingDetails);
        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
        this.guarantorRepository.saveAndFlush(guarantorForDelete);
        return DeleteGuarantorsResponse.builder().resourceId(guarantorForDelete.getId()).loanId(guarantorForDelete.getLoanId())
                .officeId(guarantorForDelete.getOfficeId()).build();
    }

    @Deprecated
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
            if (guarantorFundingDetails == null) {
                throw new GuarantorNotFoundException(loanId, guarantorForDelete.getId(), guarantorFundingId);
            }
            removeguarantorFundDetails(guarantorForDelete, baseDataValidator, guarantorFundingDetails);

        }
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
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
            this.guarantorDomainService.releaseGuarantor(guarantorFundingDetails, DateUtils.getBusinessLocalDate());
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
                if (errorCode != null) {
                    throw new InvalidGuarantorException(guarantor.getEntityId(), guarantor.getLoanId(), errorCode);
                }
            }

        } else if (guarantor.isExistingEmployee()) {
            this.staffRepositoryWrapper.findOneWithNotFoundDetection(guarantor.getEntityId());
        }
    }

    private void validateLoanStatus(Loan loan) {
        if (!loan.getStatus().isActiveOrAwaitingApprovalOrDisbursal()) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.guarantor");
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode("loan.is.closed");
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    private void handleGuarantorDataIntegrityIssues(final Throwable realCause, final NonTransientDataAccessException dve) {
        log.error("Error occured.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.guarantor.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource Guarantor: " + realCause.getMessage());
    }

    private Map<String, Object> getUpdateChanges(Guarantor originalData, UpdateGuarantorsRequest updateData) {
        Map<String, Object> changes = new LinkedHashMap<>();

        if (!Objects.equals(originalData.getFirstname(), updateData.getFirstname())) {
            changes.put("firstname", updateData.getFirstname());
            originalData.setFirstname(updateData.getFirstname());
        }
        if (!Objects.equals(originalData.getLastname(), updateData.getLastname())) {
            changes.put("lastname", updateData.getLastname());
            originalData.setLastname(updateData.getLastname());
        }
        if (!Objects.equals(originalData.getAddressLine1(), updateData.getAddressLine1())) {
            changes.put("addressLine1", updateData.getAddressLine1());
            originalData.setAddressLine1(updateData.getAddressLine1());
        }
        if (!Objects.equals(originalData.getAddressLine2(), updateData.getAddressLine2())) {
            changes.put("addressLine2", updateData.getAddressLine2());
            originalData.setAddressLine2(updateData.getAddressLine2());
        }
        if (!Objects.equals(originalData.getCity(), updateData.getCity())) {
            changes.put("city", updateData.getCity());
            originalData.setCity(updateData.getCity());
        }
        if (!Objects.equals(originalData.getState(), updateData.getState())) {
            changes.put("state", updateData.getState());
            originalData.setState(updateData.getState());
        }
        if (!Objects.equals(originalData.getZip(), updateData.getZip())) {
            changes.put("zip", updateData.getZip());
            originalData.setZip(updateData.getZip());
        }
        if (!Objects.equals(originalData.getCountry(), updateData.getCountry())) {
            changes.put("country", updateData.getCountry());
            originalData.setCountry(updateData.getCountry());
        }
        if (!Objects.equals(originalData.getMobilePhoneNumber(), updateData.getMobileNumber())) {
            changes.put("mobileNumber", updateData.getMobileNumber());
            originalData.setMobilePhoneNumber(updateData.getMobileNumber());
        }
        if (!Objects.equals(originalData.getHousePhoneNumber(), updateData.getHousePhoneNumber())) {
            changes.put("housePhoneNumber", updateData.getHousePhoneNumber());
            originalData.setHousePhoneNumber(updateData.getHousePhoneNumber());
        }
        if (!Objects.equals(originalData.getComment(), updateData.getComment())) {
            changes.put("comment", updateData.getComment());
            originalData.setComment(updateData.getComment());
        }
        final LocalDate dob = this.toLocalDate(updateData.getDob(), updateData.getDateFormat(), updateData.getLocale());
        if (!Objects.equals(originalData.getDateOfBirth(), dob)) {
            changes.put("dob", dob);
            originalData.setDateOfBirth(dob);
        }
        if (!Objects.equals(originalData.getGurantorType(), updateData.getGuarantorTypeId())) {
            changes.put("guarantorTypeId", updateData.getGuarantorTypeId());
            originalData.setGurantorType(updateData.getGuarantorTypeId());
        }
        if (!Objects.equals(originalData.getLoanId(), updateData.getLoanId())) {
            changes.put("loanId", updateData.getLoanId());
            originalData.getLoan().setId(updateData.getLoanId());
        }
        if (originalData.getClientRelationshipType() != null) {
            final Long originalTypeId = originalData.getClientRelationshipType().getId();
            final Long updatedTypeId = updateData.getClientRelationshipTypeId();
            if (!Objects.equals(originalTypeId, updatedTypeId)) {
                changes.put("clientRelationshipTypeId", updatedTypeId);
                originalData.getClientRelationshipType().setId(updatedTypeId);
            }
        }
        if (originalData.getEntityId() != null) {
            final Long originalEntityId = originalData.getEntityId();
            final Long updatedEntityId = updateData.getEntityId();
            if (!Objects.equals(originalEntityId, updatedEntityId)) {
                changes.put("entityId", updatedEntityId);
                originalData.setEntityId(updatedEntityId);
            }
        }
        return changes;
    }

    private LocalDate toLocalDate(String date, String format, String locale) {
        if (date == null || format == null || locale == null) {
            return null;
        }
        final DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient()
                .appendPattern(format.replace("y", "u")).optionalStart().appendPattern(" HH:mm:ss").optionalEnd()
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter(Locale.forLanguageTag(locale))
                .withResolverStyle(ResolverStyle.STRICT);
        return LocalDate.parse(date, formatter);
    }
}
