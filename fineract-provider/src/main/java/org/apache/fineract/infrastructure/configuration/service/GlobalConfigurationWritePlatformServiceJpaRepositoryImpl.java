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
package org.apache.fineract.infrastructure.configuration.service;

import java.util.Map;

import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationDataValidator;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
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
public class GlobalConfigurationWritePlatformServiceJpaRepositoryImpl implements GlobalConfigurationWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(GlobalConfigurationWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final GlobalConfigurationRepositoryWrapper repository;
    private final GlobalConfigurationDataValidator globalConfigurationDataValidator;
    private final ConfigurationDomainService configurationDomainService;

    @Autowired
    public GlobalConfigurationWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final GlobalConfigurationRepositoryWrapper codeRepository, final GlobalConfigurationDataValidator dataValidator,
            final ConfigurationDomainService configurationDomainService) {
        this.context = context;
        this.repository = codeRepository;
        this.globalConfigurationDataValidator = dataValidator;
        this.configurationDomainService = configurationDomainService;

    }

    @Transactional
    @Override
    public CommandProcessingResult update(final Long configId, final JsonCommand command) {

        this.context.authenticatedUser();

        try {
            this.globalConfigurationDataValidator.validateForUpdate(command);

            final GlobalConfigurationProperty configItemForUpdate = this.repository.findOneWithNotFoundDetection(configId);
            
            final Map<String, Object> changes = configItemForUpdate.update(command);

            if (!changes.isEmpty()) {
                this.configurationDomainService.removeGlobalConfigurationPropertyDataFromCache(configItemForUpdate.getName());
                this.repository.save(configItemForUpdate);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(configId).with(changes).build();

        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(dve);
            return CommandProcessingResult.empty();
        }

    }

    @Transactional
    @Override
    public void addSurveyConfig(final String name)
    {
        try{
            final GlobalConfigurationProperty ppi = GlobalConfigurationProperty.newSurveyConfiguration(name);
            this.repository.save(ppi);
        }
        catch (final DataIntegrityViolationException dve)
        {
            handleDataIntegrityIssues(dve);
        }

    }



    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.globalConfiguration.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}