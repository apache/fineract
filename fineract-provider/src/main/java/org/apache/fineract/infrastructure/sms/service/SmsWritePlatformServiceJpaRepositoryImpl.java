/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.sms.service;

import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.sms.data.SmsDataValidator;
import org.mifosplatform.infrastructure.sms.domain.SmsMessage;
import org.mifosplatform.infrastructure.sms.domain.SmsMessageAssembler;
import org.mifosplatform.infrastructure.sms.domain.SmsMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SmsWritePlatformServiceJpaRepositoryImpl implements SmsWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(SmsWritePlatformServiceJpaRepositoryImpl.class);

    private final SmsMessageAssembler assembler;
    private final SmsMessageRepository repository;
    private final SmsDataValidator validator;

    @Autowired
    public SmsWritePlatformServiceJpaRepositoryImpl(final SmsMessageAssembler assembler, final SmsMessageRepository repository,
            final SmsDataValidator validator) {
        this.assembler = assembler;
        this.repository = repository;
        this.validator = validator;
    }

    @Transactional
    @Override
    public CommandProcessingResult create(final JsonCommand command) {

        try {
            this.validator.validateForCreate(command.json());

            final SmsMessage message = this.assembler.assembleFromJson(command);

            // TODO: at this point we also want to fire off request using third
            // party service to send SMS.
            // TODO: decision to be made on wheter we 'wait' for response or use
            // 'future/promise' to capture response and update the SmsMessage
            // table
            this.repository.save(message);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(message.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult update(final Long resourceId, final JsonCommand command) {

        try {
            this.validator.validateForUpdate(command.json());

            final SmsMessage message = this.assembler.assembleFromResourceId(resourceId);
            final Map<String, Object> changes = message.update(command);
            if (!changes.isEmpty()) {
                this.repository.save(message);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(resourceId) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult delete(final Long resourceId) {

        try {
            final SmsMessage message = this.assembler.assembleFromResourceId(resourceId);
            this.repository.delete(message);
            this.repository.flush();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(null, dve);
            return CommandProcessingResult.empty();
        }
        return new CommandProcessingResultBuilder().withEntityId(resourceId).build();
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(@SuppressWarnings("unused") final JsonCommand command, final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();

        if (realCause.getMessage().contains("mobile_no")) { throw new PlatformDataIntegrityException("error.msg.sms.no.mobile.no.exists",
                "The group, client or staff provided has no mobile no.", "id"); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.sms.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}