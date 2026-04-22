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
import lombok.AllArgsConstructor;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventConfigurationUpdateRequest;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventConfigurationUpdateResponse;
import org.apache.fineract.infrastructure.event.external.repository.ExternalEventConfigurationRepository;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEventConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ExternalEventConfigurationWritePlatformServiceImpl implements ExternalEventConfigurationWritePlatformService {

    private final ExternalEventConfigurationRepository repository;

    @Transactional
    @Override
    public ExternalEventConfigurationUpdateResponse updateConfigurations(final ExternalEventConfigurationUpdateRequest request) {
        final var commandConfigurations = request.getExternalEventConfigurations();
        final var changes = new HashMap<String, Object>();
        final var changedConfigurations = new HashMap<String, Boolean>();
        final var modifiedConfigurations = new ArrayList<ExternalEventConfiguration>();

        for (var entry : commandConfigurations.entrySet()) {
            final var configuration = repository.findExternalEventConfigurationByTypeWithNotFoundDetection(entry.getKey());
            configuration.setEnabled(entry.getValue());
            changedConfigurations.put(entry.getKey(), entry.getValue());
            modifiedConfigurations.add(configuration);
        }

        if (!modifiedConfigurations.isEmpty()) {
            repository.saveAll(modifiedConfigurations);
        }

        if (!changedConfigurations.isEmpty()) {
            changes.put("externalEventConfigurations", changedConfigurations);
        }

        return ExternalEventConfigurationUpdateResponse.builder().changes(changes).build();
    }
}
