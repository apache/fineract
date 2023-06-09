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

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventConfigurationData;
import org.apache.fineract.infrastructure.event.external.repository.ExternalEventConfigurationRepository;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEventConfiguration;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalEventConfigurationReadPlatformServiceImpl implements ExternalEventConfigurationReadPlatformService {

    private final ExternalEventConfigurationRepository repository;
    private final ExternalEventsConfigurationMapper mapper;

    @Override
    public ExternalEventConfigurationData findAllExternalEventConfigurations() {
        ExternalEventConfigurationData configurationData = new ExternalEventConfigurationData();
        List<ExternalEventConfiguration> eventConfigurations = repository.findAll();
        configurationData.setExternalEventConfiguration(mapper.map(eventConfigurations));
        return configurationData;
    }

}
