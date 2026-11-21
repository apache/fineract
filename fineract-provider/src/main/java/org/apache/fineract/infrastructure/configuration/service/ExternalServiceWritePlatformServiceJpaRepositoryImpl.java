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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.data.ExternalServicesData;
import org.apache.fineract.infrastructure.configuration.domain.ExternalServicesProperties;
import org.apache.fineract.infrastructure.configuration.domain.ExternalServicesPropertiesRepository;
import org.apache.fineract.infrastructure.configuration.domain.ExternalServicesPropertiesRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.serialization.ExternalServicesPropertiesCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesConstants.ExternalservicePropertiesJSONinputParams;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesConstants.SMTPJSONinputParams;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalServiceWritePlatformServiceJpaRepositoryImpl implements ExternalServiceWritePlatformService {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(ExternalServiceWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final ExternalServicesPropertiesRepositoryWrapper repositoryWrapper;
    private final ExternalServicesPropertiesRepository repository;
    private final ExternalServicesPropertiesCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final ExternalServicesReadPlatformService readPlatformService;

    @Transactional
    @Override
    public CommandProcessingResult updateExternalServicesProperties(String externalServiceName, JsonCommand command) {
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
            final Map<String, Object> changes = updateProperty(externalServicesProperties, command, name);
            changesList.putAll(changes);
            if (!changes.isEmpty()) {
                this.repository.saveAndFlush(externalServicesProperties);
            }
        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(externalServiceId) //
                .with(changesList) //
                .build();
        //

    }

    private Map<String, Object> updateProperty(final ExternalServicesProperties property, final JsonCommand command,
            final String paramName) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(2);

        final String valueParamName = ExternalservicePropertiesJSONinputParams.VALUE.getValue();
        if (command.isChangeInStringParameterNamed(paramName, property.getValue())) {
            final String newValue = command.stringValueOfParameterNamed(paramName);
            if (paramName.equals(SMTPJSONinputParams.PASSWORD.getValue()) && newValue.equals("XXXX")) {
                // If Param Name is Password and ParamValue is XXXX that means
                // the password has not been changed.
            } else {
                actualChanges.put(valueParamName, newValue);
            }
            property.updateValue(newValue);
        }

        return actualChanges;
    }

}
