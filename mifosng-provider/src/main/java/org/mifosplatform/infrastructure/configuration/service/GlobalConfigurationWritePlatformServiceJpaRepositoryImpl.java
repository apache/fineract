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
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GlobalConfigurationWritePlatformServiceJpaRepositoryImpl implements GlobalConfigurationWritePlatformService {

    private final PlatformSecurityContext context;
    private final GlobalConfigurationRepository repository;
    private final GlobalConfigurationCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public GlobalConfigurationWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final GlobalConfigurationRepository codeRepository, final GlobalConfigurationCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.context = context;
        this.repository = codeRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Transactional
    @Override
    public CommandProcessingResult update(final JsonCommand command) {

        context.authenticatedUser();
        
        final UpdateGlobalConfigurationCommand configurationCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
        
        final Map<String, Boolean> params = configurationCommand.getGlobalConfiguration();
        
        final Map<String, Object> changes = new LinkedHashMap<String, Object>(params.size());
        final Map<String, Boolean> propertiesMap = new LinkedHashMap<String, Boolean>(1);
        for (String propertyName : params.keySet()) {
            final GlobalConfigurationProperty property = retrieveBy(propertyName);
            
            Boolean value = params.get(propertyName);
            boolean updated = property.updateTo(value);
            
            if (updated) {
                propertiesMap.put(propertyName, value); 
                this.repository.save(property);
            }
        }
        
        if (!propertiesMap.isEmpty()) {
            changes.put("globalConfiguration", propertiesMap);
        }
        
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).with(changes).build();
    }

    private GlobalConfigurationProperty retrieveBy(final String propertyName) {
        final GlobalConfigurationProperty property = this.repository.findOneByName(propertyName);
        if (property == null) { throw new GlobalConfigurationPropertyNotFoundException(propertyName); }
        return property;
    }
}