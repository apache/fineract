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
package org.apache.fineract.infrastructure.codes.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.codes.CodeConstants.CodevalueJSONinputParams;
import org.apache.fineract.infrastructure.codes.domain.Code;
import org.apache.fineract.infrastructure.codes.domain.CodeRepository;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.codes.exception.CodeNotFoundException;
import org.apache.fineract.infrastructure.codes.serialization.CodeValueCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeValueWritePlatformServiceJpaRepositoryImpl implements CodeValueWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(CodeValueWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;
    private final CodeValueRepository codeValueRepository;
    private final CodeRepository codeRepository;
    private final CodeValueCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public CodeValueWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final CodeRepository codeRepository,
            final CodeValueRepositoryWrapper codeValueRepositoryWrapper, final CodeValueRepository codeValueRepository,
            final CodeValueCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.context = context;
        this.codeRepository = codeRepository;
        this.codeValueRepositoryWrapper = codeValueRepositoryWrapper;
        this.codeValueRepository = codeValueRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Transactional
    @Override
    @CacheEvict(value = "code_values", allEntries = true)
    public CommandProcessingResult createCodeValue(final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Long codeId = command.entityId();
            final Code code = this.codeRepository.findById(codeId).orElseThrow(() -> new CodeNotFoundException(codeId));
            final CodeValue codeValue = newCodeValue(code, command);
            this.codeValueRepository.saveAndFlush(codeValue);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(code.getId()) //
                    .withSubEntityId(codeValue.getId()) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleCodeValueDataIntegrityIssues(command, throwable, dve);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .build();
        }
    }

    private static CodeValue newCodeValue(final Code code, final JsonCommand command) {
        final String label = command.stringValueOfParameterNamed(CodevalueJSONinputParams.NAME.getValue());
        final Integer position = command.integerValueSansLocaleOfParameterNamed(CodevalueJSONinputParams.POSITION.getValue());
        final String description = command.stringValueOfParameterNamed(CodevalueJSONinputParams.DESCRIPTION.getValue());
        final Boolean isActive = command.booleanObjectValueOfParameterNamed(CodevalueJSONinputParams.IS_ACTIVE.getValue());
        final boolean mandatory = command.booleanPrimitiveValueOfParameterNamed(CodevalueJSONinputParams.IS_MANDATORY.getValue());

        return CodeValue.createNew(code, label, position == null ? 0 : position, description, isActive == null || isActive, mandatory);
    }

    private static Map<String, Object> applyChanges(final CodeValue codeValue, final JsonCommand command) {
        final Map<String, Object> changes = new LinkedHashMap<>(2);

        final String labelParamName = CodevalueJSONinputParams.NAME.getValue();
        if (command.isChangeInStringParameterNamed(labelParamName, codeValue.getLabel())) {
            final String newValue = command.stringValueOfParameterNamed(labelParamName);
            changes.put(labelParamName, newValue);
            codeValue.updateLabel(newValue);
        }

        final String descriptionParamName = CodevalueJSONinputParams.DESCRIPTION.getValue();
        if (command.isChangeInStringParameterNamed(descriptionParamName, codeValue.getDescription())) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            changes.put(descriptionParamName, newValue);
            codeValue.updateDescription(newValue);
        }

        final String positionParamName = CodevalueJSONinputParams.POSITION.getValue();
        if (command.isChangeInIntegerSansLocaleParameterNamed(positionParamName, codeValue.getPosition())) {
            final Integer newValue = command.integerValueSansLocaleOfParameterNamed(positionParamName);
            changes.put(positionParamName, newValue);
            codeValue.updatePosition(newValue);
        }

        final String isActiveParamName = CodevalueJSONinputParams.IS_ACTIVE.getValue();
        if (command.isChangeInBooleanParameterNamed(isActiveParamName, codeValue.isActive())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(isActiveParamName);
            changes.put(isActiveParamName, newValue);
            codeValue.updateActive(newValue);
        }

        return changes;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleCodeValueDataIntegrityIssues(final JsonCommand command, final Throwable realCause,
            final NonTransientDataAccessException dve) {
        if (realCause.getMessage().contains("code_value")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.code.value.duplicate.label",
                    "A code value with lable '" + name + "' already exists", "name", name);
        }

        LOG.error("Error occurred.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.code.value.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }

    @Transactional
    @Override
    @CacheEvict(value = "code_values", allEntries = true)
    public CommandProcessingResult updateCodeValue(final Long codeValueId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final CodeValue codeValue = this.codeValueRepositoryWrapper.findOneWithNotFoundDetection(codeValueId);
            final Map<String, Object> changes = applyChanges(codeValue, command);

            if (!changes.isEmpty()) {
                this.codeValueRepository.saveAndFlush(codeValue);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(codeValueId) //
                    .with(changes) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleCodeValueDataIntegrityIssues(command, throwable, dve);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .build();
        }

    }

    @Transactional
    @Override
    @CacheEvict(value = "code_values", allEntries = true)
    public CommandProcessingResult deleteCodeValue(final Long codeId, final Long codeValueId) {

        try {
            this.context.authenticatedUser();

            final Code code = this.codeRepository.findById(codeId).orElseThrow(() -> new CodeNotFoundException(codeId));

            final CodeValue codeValueToDelete = this.codeValueRepositoryWrapper.findOneWithNotFoundDetection(codeValueId);

            final boolean removed = code.remove(codeValueToDelete);
            if (removed) {
                this.codeRepository.saveAndFlush(code);
            }

            return new CommandProcessingResultBuilder() //
                    .withEntityId(codeId) //
                    .withSubEntityId(codeValueId) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            LOG.error("Error occurred.", dve);
            final Throwable realCause = dve.getMostSpecificCause();
            if (realCause.getMessage().contains("code_value")) {
                throw new PlatformDataIntegrityException("error.msg.codeValue.in.use", "This code value is in use", codeValueId, dve);
            }
            throw ErrorHandler.getMappable(dve, "error.msg.code.value.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: " + realCause.getMessage());
        }
    }
}
