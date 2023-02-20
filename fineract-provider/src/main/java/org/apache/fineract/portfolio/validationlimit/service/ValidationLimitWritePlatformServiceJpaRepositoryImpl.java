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
package org.apache.fineract.portfolio.validationlimit.service;

import java.util.Map;
import javax.persistence.PersistenceException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.validationlimit.api.ValidationLimitApiConstants;
import org.apache.fineract.portfolio.validationlimit.domain.ValidationLimit;
import org.apache.fineract.portfolio.validationlimit.domain.ValidationLimitRepository;
import org.apache.fineract.portfolio.validationlimit.exception.ValidationLimitAlreadyPresentException;
import org.apache.fineract.portfolio.validationlimit.exception.ValidationLimitNotFoundException;
import org.apache.fineract.portfolio.validationlimit.serialization.ValidationLimitCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValidationLimitWritePlatformServiceJpaRepositoryImpl implements ValidationLimitWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationLimitWritePlatformServiceJpaRepositoryImpl.class);
    private final PlatformSecurityContext context;
    private final ValidationLimitCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    private final ValidationLimitRepository validationLimitRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;

    @Autowired
    public ValidationLimitWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final ValidationLimitCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final ValidationLimitRepository validationLimitRepository, final CodeValueRepositoryWrapper codeValueRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;

        this.validationLimitRepository = validationLimitRepository;
        this.codeValueRepository = codeValueRepository;

    }

    @Transactional
    @Override
    @CacheEvict(value = "validationLimit", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public CommandProcessingResult createValidationLimit(final JsonCommand command) {
        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            CodeValue clientLevel = null;
            final Long clientLevelId = command.longValueOfParameterNamed(ValidationLimitApiConstants.CLIENT_LEVEL_ID);
            if (clientLevelId != null) {
                clientLevel = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_LEVELS,
                        clientLevelId);
            }

            final ValidationLimit validationLimit = ValidationLimit.fromJson(clientLevel, command);

            final ValidationLimit exitingValidationLimit = this.validationLimitRepository
                    .findByClientLevelId(validationLimit.getClientLevel().getId());
            if (exitingValidationLimit != null) {
                throw new ValidationLimitAlreadyPresentException(validationLimit.getClientLevel().getId());
            }

            this.validationLimitRepository.save(validationLimit);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(validationLimit.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "validationlimit", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public CommandProcessingResult updateValidationLimit(final Long validationLimitId, final JsonCommand command) {

        try {
            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final ValidationLimit validationLimitForUpdate = this.validationLimitRepository.findById(validationLimitId).orElse(null);
            if (validationLimitForUpdate == null) {
                throw new ValidationLimitNotFoundException(validationLimitId);
            }

            final Map<String, Object> changes = validationLimitForUpdate.update(command);

            if (changes.containsKey(ClientApiConstants.clientLevelIdParamName)) {
                final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.clientLevelIdParamName);
                CodeValue clientLevel = null;
                if (newValue != null) {
                    clientLevel = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_LEVELS,
                            newValue);
                }
                validationLimitForUpdate.updateClientLevel(clientLevel);

            }

            if (!changes.isEmpty()) {
                this.validationLimitRepository.save(validationLimitForUpdate);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(validationLimitId).with(changes)
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {

        if (realCause.getMessage().contains("clientLevel")) {
            final String clientLevel = command.stringValueOfParameterNamed("clientLevel");
            throw new PlatformDataIntegrityException("error.msg.validation.limit.duplicate.clientLevel",
                    "Client level  `" + clientLevel + "` already exists", "clientLevel", clientLevel);
        }
        final String message = dve.getMessage();
        LOG.error(message, dve);
        throw new PlatformDataIntegrityException("error.msg.validation.limit.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }

}
