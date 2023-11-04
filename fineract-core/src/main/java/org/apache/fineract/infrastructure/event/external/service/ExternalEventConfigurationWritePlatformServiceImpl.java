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
package org.apache.fineract.infrastructure.event.external.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.event.external.command.ExternalEventConfigurationCommand;
import org.apache.fineract.infrastructure.event.external.repository.ExternalEventConfigurationRepository;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEventConfiguration;
import org.apache.fineract.infrastructure.event.external.serialization.ExternalEventConfigurationCommandFromApiJsonDeserializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ExternalEventConfigurationWritePlatformServiceImpl implements ExternalEventConfigurationWritePlatformService {

    private final ExternalEventConfigurationRepository repository;
    private final ExternalEventConfigurationCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Transactional
    @Override
    public CommandProcessingResult updateConfigurations(final JsonCommand command) {
        final ExternalEventConfigurationCommand configurationCommand = fromApiJsonDeserializer.commandFromApiJson(command.json());
        final Map<String, Boolean> commandConfigurations = configurationCommand.getExternalEventConfigurations();
        final Map<String, Object> changes = new HashMap<>();
        final Map<String, Boolean> changedConfigurations = new HashMap<>();
        final List<ExternalEventConfiguration> modifiedConfigurations = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : commandConfigurations.entrySet()) {
            final ExternalEventConfiguration configuration = repository
                    .findExternalEventConfigurationByTypeWithNotFoundDetection(entry.getKey());
            configuration.setEnabled(entry.getValue());
            changedConfigurations.put(entry.getKey(), entry.getValue());
            modifiedConfigurations.add(configuration);
        }

        if (!modifiedConfigurations.isEmpty()) {
            this.repository.saveAll(modifiedConfigurations);
        }

        if (!changedConfigurations.isEmpty()) {
            changes.put("externalEventConfigurations", changedConfigurations);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).with(changes).build();
    }
}
