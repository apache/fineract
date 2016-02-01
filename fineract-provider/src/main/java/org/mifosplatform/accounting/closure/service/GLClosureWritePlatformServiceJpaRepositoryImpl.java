/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.closure.service;

import java.util.Date;
import java.util.Map;

import org.joda.time.LocalDate;
import org.mifosplatform.accounting.closure.api.GLClosureJsonInputParams;
import org.mifosplatform.accounting.closure.command.GLClosureCommand;
import org.mifosplatform.accounting.closure.domain.GLClosure;
import org.mifosplatform.accounting.closure.domain.GLClosureRepository;
import org.mifosplatform.accounting.closure.exception.GLClosureDuplicateException;
import org.mifosplatform.accounting.closure.exception.GLClosureInvalidDeleteException;
import org.mifosplatform.accounting.closure.exception.GLClosureInvalidException;
import org.mifosplatform.accounting.closure.exception.GLClosureInvalidException.GL_CLOSURE_INVALID_REASON;
import org.mifosplatform.accounting.closure.exception.GLClosureNotFoundException;
import org.mifosplatform.accounting.closure.serialization.GLClosureCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.organisation.office.exception.OfficeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GLClosureWritePlatformServiceJpaRepositoryImpl implements GLClosureWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(GLClosureWritePlatformServiceJpaRepositoryImpl.class);

    private final GLClosureRepository glClosureRepository;
    private final OfficeRepository officeRepository;
    private final GLClosureCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public GLClosureWritePlatformServiceJpaRepositoryImpl(final GLClosureRepository glClosureRepository,
            final OfficeRepository officeRepository, final GLClosureCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.glClosureRepository = glClosureRepository;
        this.officeRepository = officeRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Transactional
    @Override
    public CommandProcessingResult createGLClosure(final JsonCommand command) {
        try {
            final GLClosureCommand closureCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            closureCommand.validateForCreate();

            // check office is valid
            final Long officeId = command.longValueOfParameterNamed(GLClosureJsonInputParams.OFFICE_ID.getValue());
            final Office office = this.officeRepository.findOne(officeId);
            if (office == null) { throw new OfficeNotFoundException(officeId); }

            // TODO: Get Tenant specific date
            // ensure closure date is not in the future
            final Date todaysDate = new Date();
            final Date closureDate = command.DateValueOfParameterNamed(GLClosureJsonInputParams.CLOSING_DATE.getValue());
            if (closureDate.after(todaysDate)) { throw new GLClosureInvalidException(GL_CLOSURE_INVALID_REASON.FUTURE_DATE, closureDate); }
            // shouldn't be before an existing accounting closure
            final GLClosure latestGLClosure = this.glClosureRepository.getLatestGLClosureByBranch(officeId);
            if (latestGLClosure != null) {
                if (latestGLClosure.getClosingDate().after(closureDate)) { throw new GLClosureInvalidException(
                        GL_CLOSURE_INVALID_REASON.ACCOUNTING_CLOSED, latestGLClosure.getClosingDate()); }
            }
            final GLClosure glClosure = GLClosure.fromJson(office, command);

            this.glClosureRepository.saveAndFlush(glClosure);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(officeId)
                    .withEntityId(glClosure.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleGLClosureIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateGLClosure(final Long glClosureId, final JsonCommand command) {
        final GLClosureCommand closureCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
        closureCommand.validateForUpdate();

        // is the glClosure valid
        final GLClosure glClosure = this.glClosureRepository.findOne(glClosureId);
        if (glClosure == null) { throw new GLClosureNotFoundException(glClosureId); }

        final Map<String, Object> changesOnly = glClosure.update(command);

        if (!changesOnly.isEmpty()) {
            this.glClosureRepository.saveAndFlush(glClosure);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(glClosure.getOffice().getId())
                .withEntityId(glClosure.getId()).with(changesOnly).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteGLClosure(final Long glClosureId) {
        final GLClosure glClosure = this.glClosureRepository.findOne(glClosureId);

        if (glClosure == null) { throw new GLClosureNotFoundException(glClosureId); }

        /**
         * check if any closures are present for this branch at a later date
         * than this closure date
         **/
        final Date closureDate = glClosure.getClosingDate();
        final GLClosure latestGLClosure = this.glClosureRepository.getLatestGLClosureByBranch(glClosure.getOffice().getId());
        if (latestGLClosure.getClosingDate().after(closureDate)) { throw new GLClosureInvalidDeleteException(latestGLClosure.getOffice()
                .getId(), latestGLClosure.getOffice().getName(), latestGLClosure.getClosingDate()); }

        this.glClosureRepository.delete(glClosure);

        return new CommandProcessingResultBuilder().withOfficeId(glClosure.getOffice().getId()).withEntityId(glClosure.getId()).build();
    }

    /**
     * @param command
     * @param dve
     */
    private void handleGLClosureIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("office_id_closing_date")) { throw new GLClosureDuplicateException(
                command.longValueOfParameterNamed(GLClosureJsonInputParams.OFFICE_ID.getValue()), new LocalDate(
                        command.DateValueOfParameterNamed(GLClosureJsonInputParams.CLOSING_DATE.getValue()))); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.glClosure.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource GL Closure: " + realCause.getMessage());
    }
}
