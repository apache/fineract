/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.codes.service;

import java.util.Map;

import org.mifosplatform.infrastructure.codes.domain.Code;
import org.mifosplatform.infrastructure.codes.domain.CodeRepository;
import org.mifosplatform.infrastructure.codes.exception.CodeNotFoundException;
import org.mifosplatform.infrastructure.codes.exception.SystemDefinedCodeCannotBeChangedException;
import org.mifosplatform.infrastructure.codes.serialization.CodeCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeWritePlatformServiceJpaRepositoryImpl implements CodeWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(CodeWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final CodeRepository codeRepository;
    private final CodeCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public CodeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final CodeRepository codeRepository,
            final CodeCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.context = context;
        this.codeRepository = codeRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Transactional
    @Override
    @CacheEvict(value = "codes", key = "T(org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('cv')")
    public CommandProcessingResult createCode(final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Code code = Code.fromJson(command);
            this.codeRepository.save(code);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(code.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleCodeDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "codes", key = "T(org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('cv')")
    public CommandProcessingResult updateCode(final Long codeId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Code code = retrieveCodeBy(codeId);
            final Map<String, Object> changes = code.update(command);

            if (!changes.isEmpty()) {
                this.codeRepository.save(code);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(codeId) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleCodeDataIntegrityIssues(command, dve);
            return null;
        }
    }

    @Transactional
    @Override
    @CacheEvict(value = "codes", key = "T(org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('cv')")
    public CommandProcessingResult deleteCode(final Long codeId) {

        this.context.authenticatedUser();

        final Code code = retrieveCodeBy(codeId);
        if (code.isSystemDefined()) { throw new SystemDefinedCodeCannotBeChangedException(); }

        try {
            this.codeRepository.delete(code);
            this.codeRepository.flush();
        } catch (final DataIntegrityViolationException e) {
            throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: " + e.getMostSpecificCause());
        }
        return new CommandProcessingResultBuilder().withEntityId(codeId).build();
    }

    private Code retrieveCodeBy(final Long codeId) {
        final Code code = this.codeRepository.findOne(codeId);
        if (code == null) { throw new CodeNotFoundException(codeId.toString()); }
        return code;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleCodeDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("code_name")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.code.duplicate.name", "A code with name '" + name + "' already exists",
                    "name", name);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}