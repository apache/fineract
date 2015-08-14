/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.service;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.configuration.data.ExternalServicesData;
import org.mifosplatform.infrastructure.configuration.domain.ExternalServicesProperties;
import org.mifosplatform.infrastructure.configuration.domain.ExternalServicesPropertiesRepository;
import org.mifosplatform.infrastructure.configuration.domain.ExternalServicesPropertiesRepositoryWrapper;
import org.mifosplatform.infrastructure.configuration.serialization.ExternalServicesPropertiesCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExternalServiceWritePlatformServiceJpaRepositoryImpl implements ExternalServiceWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ExternalServiceWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final ExternalServicesPropertiesRepositoryWrapper repositoryWrapper;
    private final ExternalServicesPropertiesRepository repository;
    private final ExternalServicesPropertiesCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final ExternalServicesReadPlatformService readPlatformService;

    @Autowired
    public ExternalServiceWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final ExternalServicesPropertiesRepositoryWrapper repositoryWrapper, final ExternalServicesPropertiesRepository repository,
            final ExternalServicesPropertiesCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final ExternalServicesReadPlatformService readPlatformService) {

        this.context = context;
        this.repositoryWrapper = repositoryWrapper;
        this.repository = repository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.readPlatformService = readPlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult updateExternalServicesProperties(String externalServiceName, JsonCommand command) {
        // TODO Auto-generated method stub
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForUpdate(command.json(), externalServiceName);
        Set<String> keyName = this.fromApiJsonDeserializer.getNameKeys(command.json());
        ExternalServicesData external = this.readPlatformService.getExternalServiceDetailsByServiceName(externalServiceName);
        Long externalServiceId = external.getId();
        Iterator<String> it = keyName.iterator();
        Map<String, Object> changesList = new LinkedHashMap<>();
        while (it.hasNext()) {
            String name = it.next();
            final ExternalServicesProperties externalServicesProperties = this.repositoryWrapper.findOneByIdAndName(externalServiceId, name,
                    externalServiceName);
            final Map<String, Object> changes = externalServicesProperties.update(command, name);
            changesList.putAll(changes);
            if (!changes.isEmpty()) {
                this.repository.saveAndFlush(externalServicesProperties);
            }
        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()).withEntityId(externalServiceId).with(changesList).build();
        //

    }

}
