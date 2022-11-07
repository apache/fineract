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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventConfigurationData;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventConfigurationItemData;
import org.apache.fineract.infrastructure.event.external.repository.ExternalEventConfigurationRepository;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEventConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExternalEventConfigurationReadPlatformServiceTest {

    @Mock
    private ExternalEventConfigurationRepository repository;

    @Mock
    private ExternalEventsConfigurationMapper mapper;

    private ExternalEventConfigurationReadPlatformServiceImpl underTest;

    @BeforeEach
    public void setUp() {
        underTest = new ExternalEventConfigurationReadPlatformServiceImpl(repository, mapper);
    }

    @Test
    public void givenConfigurationsThenReturnConfigurationData() {
        // given
        List<ExternalEventConfiguration> configurations = Arrays.asList(new ExternalEventConfiguration("aType", true),
                new ExternalEventConfiguration("bType", false));
        List<ExternalEventConfigurationItemData> configurationDataItems = Arrays
                .asList(new ExternalEventConfigurationItemData("aType", true), new ExternalEventConfigurationItemData("bType", false));
        when(repository.findAll()).thenReturn(configurations);
        when(mapper.map(Mockito.anyList())).thenReturn(configurationDataItems);

        // when
        ExternalEventConfigurationData actualConfiguration = underTest.findAllExternalEventConfigurations();
        // then
        assertThat(actualConfiguration.getExternalEventConfiguration(), hasSize(2));
        assertThat(actualConfiguration.getExternalEventConfiguration().get(0), any(ExternalEventConfigurationItemData.class));
        assertThat(actualConfiguration.getExternalEventConfiguration().get(0).getType(), equalTo("aType"));
        assertThat(actualConfiguration.getExternalEventConfiguration().get(0).isEnabled(), equalTo(true));
    }

    @Test
    public void givenNoConfigurationsThenReturnEmptyConfigurationData() {
        // given
        List<ExternalEventConfiguration> emptyConfiguration = new ArrayList<>();
        when(repository.findAll()).thenReturn(emptyConfiguration);
        // when
        ExternalEventConfigurationData actualConfiguration = underTest.findAllExternalEventConfigurations();
        // then
        assertThat(actualConfiguration.getExternalEventConfiguration(), hasSize(0));

    }
}
