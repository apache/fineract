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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.event.external.command.ExternalEventConfigurationCommand;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventConfigurationRequest;
import org.apache.fineract.infrastructure.event.external.repository.ExternalEventConfigurationRepository;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEventConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExternalEventConfigurationWritePlatformServiceTest {

    @Mock
    private ExternalEventConfigurationRepository repository;

    private ExternalEventConfigurationWritePlatformServiceImpl underTest;

    @BeforeEach
    public void setUp() {
        underTest = new ExternalEventConfigurationWritePlatformServiceImpl(repository);
    }

    @Test
    public void givenExternalEventConfigurationsWithChangeWhenUpdateConfigurationThenConfigurationIsUpdated() {
        // given
        final ExternalEventConfigurationCommand mockCommand = Mockito.mock(ExternalEventConfigurationCommand.class);
        Map<String, Boolean> mapOfConfigurationsForUpdate = new HashMap<>();
        mapOfConfigurationsForUpdate.put("aType", Boolean.TRUE);

        ExternalEventConfigurationRequest payload = ExternalEventConfigurationRequest.builder()
                .externalEventConfigurations(mapOfConfigurationsForUpdate).build();

        when(mockCommand.getPayload()).thenReturn(payload);
        when(repository.findExternalEventConfigurationByTypeWithNotFoundDetection(Mockito.anyString()))
                .thenReturn(new ExternalEventConfiguration("aType", false));
        // when
        underTest.updateConfigurations(mockCommand);
        // then
        verify(repository, times(1)).saveAll(Mockito.anyCollection());
    }

}
