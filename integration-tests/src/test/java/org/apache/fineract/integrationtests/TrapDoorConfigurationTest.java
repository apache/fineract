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
package org.apache.fineract.integrationtests;

import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.configuration.exception.GlobalConfigurationPropertyCannotBeModfied;
import org.apache.fineract.infrastructure.configuration.service.GlobalConfigurationPropertyUpdateService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
public class TrapDoorConfigurationTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private GlobalConfigurationPropertyUpdateService propertyUpdateService;

    private GlobalConfigurationProperty configurationProperty;

    private JsonCommand jsonCommand;

    @BeforeEach
    public void setUp() {
        configurationProperty = new GlobalConfigurationProperty();
        configurationProperty.setId(1L);
        configurationProperty.setName("config-name");
        configurationProperty.setTrapDoor(true);
        configurationProperty.setEnabled(true);
        configurationProperty.setValue(3L);

        JsonCommand jsonCommandMock = Mockito.mock(JsonCommand.class);

        Mockito.lenient().when(jsonCommandMock.isChangeInBooleanParameterNamed(Mockito.any(), Mockito.any())).thenReturn(false);

        Mockito.lenient().when(jsonCommandMock.isChangeInLongParameterNamed(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.lenient().when(jsonCommandMock.longValueOfParameterNamed(Mockito.any())).thenReturn(4L);

        Mockito.lenient().when(jsonCommandMock.isChangeInDateParameterNamed(Mockito.any(), Mockito.any())).thenReturn(false);
        Mockito.lenient().when(jsonCommandMock.isChangeInStringParameterNamed(Mockito.any(), Mockito.any())).thenReturn(false);

        jsonCommand = jsonCommandMock;
    }

    @Test
    public void testTrapDoorConfigurationWhenProductsExist() {
        Mockito.when(jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Boolean.class))).thenReturn(true);

        Assertions.assertThrows(GlobalConfigurationPropertyCannotBeModfied.class,
                () -> propertyUpdateService.update(configurationProperty, jsonCommand));
    }

    @Test
    public void testTrapDoorConfigurationWhenNoProductsExist() {
        Mockito.when(jdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Boolean.class))).thenReturn(false);

        Assertions.assertDoesNotThrow(() -> propertyUpdateService.update(configurationProperty, jsonCommand));
    }
}
