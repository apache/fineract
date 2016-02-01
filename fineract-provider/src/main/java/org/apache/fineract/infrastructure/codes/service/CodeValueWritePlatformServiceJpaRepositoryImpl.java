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

import java.util.Map;

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
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeValueWritePlatformServiceJpaRepositoryImpl implements CodeValueWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(CodeValueWritePlatformServiceJpaRepositoryImpl.class);

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
            final Code code = this.codeRepository.findOne(codeId);
            if (code == null) {
                throw new CodeNotFoundException(codeId);
            }
            final CodeValue codeValue = CodeValue.fromJson(code, command);
            this.codeValueRepository.save(codeValue);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(code.getId()) //
                    .withSubEntityId(codeValue.getId())//
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleCodeValueDataIntegrityIssues(command, dve);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .build();
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleCodeValueDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("code_value")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.code.value.duplicate.label", "A code value with lable '" + name
                    + "' already exists", "name", name);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.code.value.unknown.data.integrity.issue",
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
            final Map<String, Object> changes = codeValue.update(command);

            if (!changes.isEmpty()) {
                this.codeValueRepository.saveAndFlush(codeValue);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(codeValueId) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleCodeValueDataIntegrityIssues(command, dve);
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

            final Code code = this.codeRepository.findOne(codeId);
            if (code == null) { throw new CodeNotFoundException(codeId); }

            final CodeValue codeValueToDelete = this.codeValueRepositoryWrapper.findOneWithNotFoundDetection(codeValueId);

            final boolean removed = code.remove(codeValueToDelete);
            if (removed) {
                this.codeRepository.saveAndFlush(code);
            }

            return new CommandProcessingResultBuilder() //
                    .withEntityId(codeId) //
                    .withSubEntityId(codeValueId)//
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            logger.error(dve.getMessage(), dve);
            final Throwable realCause = dve.getMostSpecificCause();
            if (realCause.getMessage().contains("code_value")) { throw new PlatformDataIntegrityException("error.msg.codeValue.in.use",
                    "This code value is in use", codeValueId); }
            throw new PlatformDataIntegrityException("error.msg.code.value.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: " + dve.getMostSpecificCause().getMessage());
        }
    }
}