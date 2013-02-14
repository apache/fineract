/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.codes.service;

import java.util.Map;

import org.mifosplatform.infrastructure.codes.domain.Code;
import org.mifosplatform.infrastructure.codes.domain.CodeRepository;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepository;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.mifosplatform.infrastructure.codes.exception.CodeNotFoundException;
import org.mifosplatform.infrastructure.codes.serialization.CodeValueCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    public CommandProcessingResult createCodeValue(final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Long codeId = command.getCodeId();
            final Code code = this.codeRepository.findOne(codeId);
            final CodeValue codeValue = CodeValue.fromJson(code, command);
            this.codeValueRepository.save(codeValue);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(codeValue.getId()) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
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
        } catch (DataIntegrityViolationException dve) {
            handleCodeValueDataIntegrityIssues(command, dve);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .build();
        }

    }

    @Transactional
    @Override
    public CommandProcessingResult deleteCodeValue(final Long codeId, final Long codeValueId) {

        try {
            this.context.authenticatedUser();

            final Code code = this.codeRepository.findOne(codeId);
            if (code == null) { throw new CodeNotFoundException(codeId); }

            final CodeValue codeValueToDelete = this.codeValueRepositoryWrapper.findOneWithNotFoundDetection(codeValueId);

            boolean removed = code.remove(codeValueToDelete);
            if (removed) {
                this.codeRepository.save(code);
            }

            return new CommandProcessingResultBuilder() //
                    .withEntityId(codeValueId) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            logger.error(dve.getMessage(), dve);
            throw new PlatformDataIntegrityException("error.msg.code.value.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: " + dve.getMostSpecificCause().getMessage());
        }
    }
}