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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.configuration.data.ExternalServicesData;
import org.apache.fineract.infrastructure.configuration.domain.ExternalServicePropertiesPK;
import org.apache.fineract.infrastructure.configuration.domain.ExternalServicesProperties;
import org.apache.fineract.infrastructure.configuration.domain.ExternalServicesPropertiesRepository;
import org.apache.fineract.infrastructure.configuration.domain.ExternalServicesPropertiesRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.serialization.ExternalServicesPropertiesCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExternalServiceWritePlatformServiceJpaRepositoryImplTest {

    private static final FromJsonHelper JSON_HELPER = new FromJsonHelper();
    private static final String SERVICE_NAME = "SMTP";
    private static final Long SERVICE_ID = 2L;

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private ExternalServicesPropertiesRepositoryWrapper repositoryWrapper;
    @Mock
    private ExternalServicesPropertiesRepository repository;
    @Mock
    private ExternalServicesPropertiesCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    @Mock
    private ExternalServicesReadPlatformService readPlatformService;

    private ExternalServiceWritePlatformServiceJpaRepositoryImpl service;

    @BeforeEach
    void setUp() {
        service = new ExternalServiceWritePlatformServiceJpaRepositoryImpl(context, repositoryWrapper, repository, fromApiJsonDeserializer,
                readPlatformService);
        when(readPlatformService.getExternalServiceDetailsByServiceName(SERVICE_NAME))
                .thenReturn(new ExternalServicesData().setId(SERVICE_ID).setName(SERVICE_NAME));
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JSON_HELPER.parse(json), JSON_HELPER, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }

    // the result builder leaves the changes null when nothing changed
    private static Map<String, Object> changesOf(final CommandProcessingResult result) {
        return result.getChanges() == null ? Map.of() : result.getChanges();
    }

    private ExternalServicesProperties property(final String name, final String value) {
        final ExternalServicesProperties property = new ExternalServicesProperties()
                .setExternalServicePropertiesPK(new ExternalServicePropertiesPK().setExternalServiceId(SERVICE_ID).setName(name))
                .setValue(value);
        when(repositoryWrapper.findOneByIdAndName(SERVICE_ID, name, SERVICE_NAME)).thenReturn(property);
        return property;
    }

    private CommandProcessingResult update(final String json, final String... names) {
        final JsonCommand command = command(json);
        when(fromApiJsonDeserializer.getNameKeys(command.json())).thenReturn(new LinkedHashSet<>(List.of(names)));
        return service.updateExternalServicesProperties(SERVICE_NAME, command);
    }

    @Test
    void updateAppliesTheNewValueAndReportsItUnderTheValueKey() {
        final ExternalServicesProperties host = property("host", "old.example.com");

        final CommandProcessingResult result = update("""
                {"host": "new.example.com"}
                """, "host");

        assertEquals(Map.of("value", "new.example.com"), result.getChanges());
        assertEquals(SERVICE_ID, result.getResourceId());
        assertEquals("new.example.com", host.getValue());
        verify(repository).saveAndFlush(host);
    }

    @Test
    void updateWithAnUnchangedValueReportsNothingAndDoesNotSave() {
        final ExternalServicesProperties host = property("host", "smtp.example.com");

        final CommandProcessingResult result = update("""
                {"host": "smtp.example.com"}
                """, "host");

        assertTrue(changesOf(result).isEmpty());
        assertEquals("smtp.example.com", host.getValue());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void updateWithAnEmptyValueClearsThePropertyAndReportsTheEmptyString() {
        final ExternalServicesProperties host = property("host", "smtp.example.com");

        final CommandProcessingResult result = update("""
                {"host": ""}
                """, "host");

        assertEquals(Map.of("value", ""), result.getChanges());
        assertNull(host.getValue());
        verify(repository).saveAndFlush(host);
    }

    @Test
    void updateReportsAChangedPassword() {
        final ExternalServicesProperties password = property("password", "secret");

        final CommandProcessingResult result = update("""
                {"password": "new-secret"}
                """, "password");

        assertEquals(Map.of("value", "new-secret"), result.getChanges());
        assertEquals("new-secret", password.getValue());
        verify(repository).saveAndFlush(password);
    }

    /**
     * Documents existing behaviour: the masked password "XXXX" is not reported as a change and not saved explicitly,
     * but the value of the loaded property is still set to "XXXX".
     */
    @Test
    void updateWithTheMaskedPasswordReportsNothingAndDoesNotSaveButSetsTheValue() {
        final ExternalServicesProperties password = property("password", "secret");

        final CommandProcessingResult result = update("""
                {"password": "XXXX"}
                """, "password");

        assertTrue(changesOf(result).isEmpty());
        assertEquals("XXXX", password.getValue());
        verify(repository, never()).saveAndFlush(any());
    }

    /**
     * Documents existing behaviour: every changed property is reported under the same "value" key, so only the last
     * changed property is visible in the result.
     */
    @Test
    void updateOfSeveralPropertiesReportsOnlyTheLastChangedValue() {
        final ExternalServicesProperties host = property("host", "old.example.com");
        final ExternalServicesProperties port = property("port", "25");

        final CommandProcessingResult result = update("""
                {"host": "new.example.com", "port": "587"}
                """, "host", "port");

        assertEquals(Map.of("value", "587"), result.getChanges());
        assertEquals("new.example.com", host.getValue());
        assertEquals("587", port.getValue());
        verify(repository).saveAndFlush(host);
        verify(repository).saveAndFlush(port);
    }
}
