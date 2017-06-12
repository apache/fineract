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
package org.apache.fineract.adhocquery.service;

import java.util.Map;

import org.apache.fineract.adhocquery.domain.AdHoc;
import org.apache.fineract.adhocquery.domain.AdHocRepository;
import org.apache.fineract.adhocquery.exception.AdHocNotFoundException;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdHocWritePlatformServiceJpaRepositoryImpl implements AdHocWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(AdHocWritePlatformServiceJpaRepositoryImpl.class);
    private final PlatformSecurityContext context;
    private final AdHocRepository adHocRepository;
    private final AdHocDataValidator adHocCommandFromApiJsonDeserializer;
   

    @Autowired
    public AdHocWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final AdHocRepository adHocRepository,
             final AdHocDataValidator adHocCommandFromApiJsonDeserializer) {
        this.context = context;
        this.adHocRepository = adHocRepository;
        this.adHocCommandFromApiJsonDeserializer = adHocCommandFromApiJsonDeserializer;
       
    }

    @Transactional
    @Override
    public CommandProcessingResult createAdHocQuery(final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.adHocCommandFromApiJsonDeserializer.validateForCreate(command.json());

            final AdHoc entity = AdHoc.fromJson(command);
            this.adHocRepository.save(entity);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(entity.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .build();
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("unq_name")) {

            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.adhocquery.duplicate.name", "AdHocQuery with name `" + name + "` already exists",
                    "name", name);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.adhocquery.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }

    @Transactional
    @Override
    public CommandProcessingResult updateAdHocQuery(final Long adHocId, final JsonCommand command) {
        try {
            this.context.authenticatedUser();

            this.adHocCommandFromApiJsonDeserializer.validateForUpdate(command.json());

            final AdHoc adHoc = this.adHocRepository.findOne(adHocId);
            if (adHoc == null) { throw new AdHocNotFoundException(adHocId); }

            final Map<String, Object> changes = adHoc.update(command);
            if (!changes.isEmpty()) {
                this.adHocRepository.saveAndFlush(adHoc);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(adHocId) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .build();
        }
    }
    /**
     * Method for Delete adhoc
     */
    @Transactional
    @Override
    public CommandProcessingResult deleteAdHocQuery(Long adHocId) {

        try {
            /**
             * Checking the adhocQuery present in DB or not using adHocId
             */
            final AdHoc adHoc = this.adHocRepository.findOne(adHocId);
            if (adHoc == null) { throw new AdHocNotFoundException(adHocId); }
            
            this.adHocRepository.delete(adHoc);
            return new CommandProcessingResultBuilder().withEntityId(adHocId).build();
        } catch (final DataIntegrityViolationException e) {
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: " + e.getMostSpecificCause());
        }
    }

    /**
     * Method for disabling the adhocquery
     */
    @Transactional
    @Override
    public CommandProcessingResult disableAdHocQuery(Long adHocId) {
        try {
            /**
             * Checking the adhocquery present in DB or not using adHocId
             */
            final AdHoc adHoc = this.adHocRepository.findOne(adHocId);
            if (adHoc == null) { throw new AdHocNotFoundException(adHocId); }
            adHoc.disableActive();
            this.adHocRepository.save(adHoc);
            return new CommandProcessingResultBuilder().withEntityId(adHocId).build();

        } catch (final DataIntegrityViolationException e) {
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: " + e.getMostSpecificCause());
        }
    }

    /**
     * Method for Enabling the Active
     */
    @Transactional
    @Override
    public CommandProcessingResult enableAdHocQuery(Long adHocId) {
        try {
            /**
             * Checking the adHoc present in DB or not using id
             */
            final AdHoc adHoc = this.adHocRepository.findOne(adHocId);
            if (adHoc == null) { throw new AdHocNotFoundException(adHocId); }
            adHoc.enableActive();
            this.adHocRepository.save(adHoc);
            return new CommandProcessingResultBuilder().withEntityId(adHocId).build();

        } catch (final DataIntegrityViolationException e) {
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: " + e.getMostSpecificCause());
        }
    }
}