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
package org.apache.fineract.portfolio.loanorigination.service;

import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.CHANNEL_TYPE_CODE_NAME;
import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.CHANNEL_TYPE_ID_PARAM;
import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.EXTERNAL_ID_PARAM;
import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.NAME_PARAM;
import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.ORIGINATOR_TYPE_CODE_NAME;
import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.ORIGINATOR_TYPE_ID_PARAM;
import static org.apache.fineract.portfolio.loanorigination.api.LoanOriginatorApiConstants.STATUS_PARAM;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginator;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorMapping;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorMappingRepository;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorRepository;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginatorStatus;
import org.apache.fineract.portfolio.loanorigination.exception.LoanNotInSubmittedStatusException;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorCannotBeDeletedException;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorDuplicateExternalIdException;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorMappingAlreadyExistsException;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorMappingNotFoundException;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorNotActiveException;
import org.apache.fineract.portfolio.loanorigination.exception.LoanOriginatorNotFoundException;
import org.apache.fineract.portfolio.loanorigination.serialization.LoanOriginatorDataValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(value = "fineract.module.loan-origination.enabled", havingValue = "true")
public class LoanOriginatorWritePlatformServiceImpl implements LoanOriginatorWritePlatformService {

    private final LoanOriginatorRepository loanOriginatorRepository;
    private final LoanOriginatorMappingRepository loanOriginatorMappingRepository;
    private final LoanOriginatorDataValidator loanOriginatorDataValidator;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;
    private final LoanRepositoryWrapper loanRepositoryWrapper;

    @Override
    public CommandProcessingResult create(final JsonCommand command) {
        this.loanOriginatorDataValidator.validateForCreate(command.json());

        final String externalIdValue = command.stringValueOfParameterNamed(EXTERNAL_ID_PARAM);
        final ExternalId externalId = new ExternalId(externalIdValue);

        if (this.loanOriginatorRepository.existsByExternalId(externalId)) {
            throw new LoanOriginatorDuplicateExternalIdException(externalIdValue);
        }

        final String name = command.stringValueOfParameterNamed(NAME_PARAM);

        final String statusValue = command.stringValueOfParameterNamed(STATUS_PARAM);
        final LoanOriginatorStatus status = (statusValue != null && !statusValue.isEmpty()) ? LoanOriginatorStatus.fromString(statusValue)
                : LoanOriginatorStatus.ACTIVE;

        final CodeValue originatorType = resolveCodeValue(command, ORIGINATOR_TYPE_ID_PARAM, ORIGINATOR_TYPE_CODE_NAME);
        final CodeValue channelType = resolveCodeValue(command, CHANNEL_TYPE_ID_PARAM, CHANNEL_TYPE_CODE_NAME);

        final LoanOriginator originator = LoanOriginator.create(externalId, name, status, originatorType, channelType);
        this.loanOriginatorRepository.saveAndFlush(originator);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(originator.getId())
                .withEntityExternalId(externalId).build();
    }

    @Override
    public CommandProcessingResult update(final Long id, final JsonCommand command) {
        this.loanOriginatorDataValidator.validateForUpdate(command.json());

        final LoanOriginator originator = this.loanOriginatorRepository.findById(id)
                .orElseThrow(() -> new LoanOriginatorNotFoundException(id));

        final Map<String, Object> changes = new LinkedHashMap<>();

        if (command.isChangeInStringParameterNamed(NAME_PARAM, originator.getName())) {
            final String newName = command.stringValueOfParameterNamed(NAME_PARAM);
            originator.setName(newName);
            changes.put(NAME_PARAM, newName);
        }

        if (command.isChangeInStringParameterNamed(STATUS_PARAM, originator.getStatus().getValue())) {
            final String newStatusValue = command.stringValueOfParameterNamed(STATUS_PARAM);
            final LoanOriginatorStatus newStatus = LoanOriginatorStatus.fromString(newStatusValue);
            originator.setStatus(newStatus);
            changes.put(STATUS_PARAM, newStatusValue);
        }

        final Long currentOriginatorTypeId = originator.getOriginatorType() != null ? originator.getOriginatorType().getId() : null;
        if (command.isChangeInLongParameterNamed(ORIGINATOR_TYPE_ID_PARAM, currentOriginatorTypeId)) {
            final CodeValue newOriginatorType = resolveCodeValue(command, ORIGINATOR_TYPE_ID_PARAM, ORIGINATOR_TYPE_CODE_NAME);
            originator.setOriginatorType(newOriginatorType);
            changes.put(ORIGINATOR_TYPE_ID_PARAM, newOriginatorType != null ? newOriginatorType.getId() : null);
        }

        final Long currentChannelTypeId = originator.getChannelType() != null ? originator.getChannelType().getId() : null;
        if (command.isChangeInLongParameterNamed(CHANNEL_TYPE_ID_PARAM, currentChannelTypeId)) {
            final CodeValue newChannelType = resolveCodeValue(command, CHANNEL_TYPE_ID_PARAM, CHANNEL_TYPE_CODE_NAME);
            originator.setChannelType(newChannelType);
            changes.put(CHANNEL_TYPE_ID_PARAM, newChannelType != null ? newChannelType.getId() : null);
        }

        if (!changes.isEmpty()) {
            this.loanOriginatorRepository.saveAndFlush(originator);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(originator.getId())
                .withEntityExternalId(originator.getExternalId()).with(changes).build();
    }

    @Override
    public CommandProcessingResult delete(final Long id) {
        final LoanOriginator originator = this.loanOriginatorRepository.findById(id)
                .orElseThrow(() -> new LoanOriginatorNotFoundException(id));

        if (this.loanOriginatorMappingRepository.existsByOriginatorId(id)) {
            throw new LoanOriginatorCannotBeDeletedException(id);
        }

        final ExternalId externalId = originator.getExternalId();
        this.loanOriginatorRepository.delete(originator);

        return new CommandProcessingResultBuilder().withEntityId(id).withEntityExternalId(externalId).build();
    }

    @Override
    public CommandProcessingResult attachOriginatorToLoan(final Long loanId, final Long originatorId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);

        if (!loan.isSubmittedAndPendingApproval()) {
            throw new LoanNotInSubmittedStatusException(loanId, loan.getStatus().getCode());
        }

        final LoanOriginator originator = this.loanOriginatorRepository.findById(originatorId)
                .orElseThrow(() -> new LoanOriginatorNotFoundException(originatorId));

        if (originator.getStatus() != LoanOriginatorStatus.ACTIVE) {
            throw new LoanOriginatorNotActiveException(originatorId, originator.getStatus().getValue());
        }

        if (this.loanOriginatorMappingRepository.existsByLoanIdAndOriginatorId(loanId, originatorId)) {
            throw new LoanOriginatorMappingAlreadyExistsException(loanId, originatorId);
        }

        final LoanOriginatorMapping mapping = LoanOriginatorMapping.create(loanId, originator);
        this.loanOriginatorMappingRepository.saveAndFlush(mapping);

        return new CommandProcessingResultBuilder().withEntityId(loanId).withEntityExternalId(loan.getExternalId())
                .withSubEntityId(originatorId).withSubEntityExternalId(originator.getExternalId()).build();
    }

    @Override
    public CommandProcessingResult detachOriginatorFromLoan(final Long loanId, final Long originatorId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);

        if (!loan.isSubmittedAndPendingApproval()) {
            throw new LoanNotInSubmittedStatusException(loanId, loan.getStatus().getCode());
        }

        final LoanOriginator originator = this.loanOriginatorRepository.findById(originatorId)
                .orElseThrow(() -> new LoanOriginatorNotFoundException(originatorId));

        final LoanOriginatorMapping mapping = this.loanOriginatorMappingRepository.findByLoanIdAndOriginatorId(loanId, originatorId)
                .orElseThrow(() -> new LoanOriginatorMappingNotFoundException(loanId, originatorId));

        this.loanOriginatorMappingRepository.delete(mapping);

        return new CommandProcessingResultBuilder().withEntityId(loanId).withEntityExternalId(loan.getExternalId())
                .withSubEntityId(originatorId).withSubEntityExternalId(originator.getExternalId()).build();
    }

    private CodeValue resolveCodeValue(final JsonCommand command, final String paramName, final String codeName) {
        final Long codeValueId = command.longValueOfParameterNamed(paramName);
        if (codeValueId == null) {
            return null;
        }
        return this.codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(codeName, codeValueId);
    }
}
