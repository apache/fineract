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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventConfigKey;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventConfigurationRequest;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventConfigurationResponse;
import org.apache.fineract.infrastructure.event.external.repository.ExternalEventConfigurationRepository;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEventConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalEventConfigurationWritePlatformServiceImpl implements ExternalEventConfigurationWritePlatformService {

    private final ExternalEventConfigurationRepository repository;

    @Transactional
    @Override
    public ExternalEventConfigurationResponse updateConfigurations(Command<ExternalEventConfigurationRequest> command) {
        final Map<String, Boolean> commandConfigurations = command.getPayload().getExternalEventConfigurations();
        final Map<String, Boolean> changedConfigurations = new HashMap<>();
        final Map<ExternalEventConfigKey, Map<String, Boolean>> changes = new HashMap<>();
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
            changes.put(ExternalEventConfigKey.externalEventConfigurations, changedConfigurations);
        }
        final ExternalEventConfigurationResponse response = new ExternalEventConfigurationResponse();
        response.setChanges(changes);
        return response;
    }
}
