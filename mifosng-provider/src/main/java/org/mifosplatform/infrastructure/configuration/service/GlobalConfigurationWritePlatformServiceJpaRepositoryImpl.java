/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.mifosplatform.infrastructure.configuration.command.UpdateGlobalConfigurationCommand;
import org.mifosplatform.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.mifosplatform.infrastructure.configuration.domain.GlobalConfigurationRepository;
import org.mifosplatform.infrastructure.configuration.exception.GlobalConfigurationPropertyNotFoundException;
import org.mifosplatform.infrastructure.configuration.serialization.GlobalConfigurationCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.mifosplatform.infrastructure.configuration.data.GlobalConfigurationDataValidator;
import org.springframework.dao.DataIntegrityViolationException;

@Service
public class GlobalConfigurationWritePlatformServiceJpaRepositoryImpl implements GlobalConfigurationWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(GlobalConfigurationWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final GlobalConfigurationRepository repository;
    private final GlobalConfigurationCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final GlobalConfigurationDataValidator globalConfigurationDataValidator ;

    @Autowired
    public GlobalConfigurationWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final GlobalConfigurationRepository codeRepository,
            final GlobalConfigurationCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final GlobalConfigurationDataValidator dataValidator) {
        this.context = context;
        this.repository = codeRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.globalConfigurationDataValidator = dataValidator;

    }

//    @Transactional
//    @Override
//    public CommandProcessingResult update(final JsonCommand command) {
//
//        this.context.authenticatedUser();
//
//        final UpdateGlobalConfigurationCommand configurationCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
//
//        final Map<String, Boolean> params = configurationCommand.getGlobalConfiguration();
//
//        final Map<String, Object> changes = new LinkedHashMap<String, Object>(params.size());
//        final Map<String, Boolean> propertiesMap = new LinkedHashMap<String, Boolean>(1);
//        for (final String propertyName : params.keySet()) {
//            final GlobalConfigurationProperty property = retrieveBy(propertyName);
//
//            final Boolean value = params.get(propertyName);
//            final boolean updated = property.updateTo(value);
//
//            if (updated) {
//                propertiesMap.put(propertyName, value);
//                this.repository.save(property);
//            }
//        }
//
//        if (!propertiesMap.isEmpty()) {
//            changes.put("globalConfiguration", propertiesMap);
//        }
//
//        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).with(changes).build();
//    }

    @Transactional
    @Override
    public CommandProcessingResult update(final Long configId , final JsonCommand command) {

        this.context.authenticatedUser();

        try {
            this.globalConfigurationDataValidator.validateForUpdate(command);

            final GlobalConfigurationProperty configItemForUpdate = this.repository.findOne(configId);

            if (configItemForUpdate == null) {
                throw new GlobalConfigurationPropertyNotFoundException(configId);
            }

            final Map<String, Object> changes = configItemForUpdate.update(command);

            if (!changes.isEmpty()) {
                this.repository.save(configItemForUpdate);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(configId).with(changes).build();

        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }

      }

    private GlobalConfigurationProperty retrieveBy(final String propertyName) {
        final GlobalConfigurationProperty property = this.repository.findOneByName(propertyName);
        if (property == null) { throw new GlobalConfigurationPropertyNotFoundException(propertyName); }
        return property;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.globalConfiguration.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}