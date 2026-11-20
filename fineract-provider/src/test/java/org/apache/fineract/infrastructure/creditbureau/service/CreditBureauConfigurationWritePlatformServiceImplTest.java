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
package org.apache.fineract.infrastructure.creditbureau.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauConfiguration;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauConfigurationRepository;
import org.apache.fineract.infrastructure.creditbureau.domain.OrganisationCreditBureau;
import org.apache.fineract.infrastructure.creditbureau.domain.OrganisationCreditBureauRepository;
import org.apache.fineract.infrastructure.creditbureau.serialization.CreditBureauConfigurationCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreditBureauConfigurationWritePlatformServiceImplTest {

    private static final FromJsonHelper JSON_HELPER = new FromJsonHelper();
    private static final Long ORGANISATION_ID = 7L;
    private static final Long CONFIGURATION_ID = 11L;

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private CreditBureauConfigurationCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    @Mock
    private CreditBureauConfigurationRepository configurationRepository;
    @Mock
    private OrganisationCreditBureauRepository organisationRepository;

    private CreditBureauConfigurationWritePlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CreditBureauConfigurationWritePlatformServiceImpl(context, fromApiJsonDeserializer, configurationRepository,
                organisationRepository);
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JSON_HELPER.parse(json), JSON_HELPER, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }

    // the result builder leaves the changes null when nothing changed
    private static Map<String, Object> changesOf(final CommandProcessingResult result) {
        return result.getChanges() == null ? Map.of() : result.getChanges();
    }

    private static CreditBureauConfiguration existing() {
        return new CreditBureauConfiguration().setConfigurationKey("key").setValue("value").setDescription("description");
    }

    @Test
    void addTakesKeyValueAndDescriptionFromTheCommandAndLinksTheOrganisation() {
        final OrganisationCreditBureau organisation = new OrganisationCreditBureau();
        when(organisationRepository.getReferenceById(ORGANISATION_ID)).thenReturn(organisation);

        service.addCreditBureauConfiguration(ORGANISATION_ID, command("""
                {"configkey": "k", "value": "v", "description": "d"}
                """));

        final ArgumentCaptor<CreditBureauConfiguration> saved = ArgumentCaptor.forClass(CreditBureauConfiguration.class);
        verify(configurationRepository).saveAndFlush(saved.capture());
        assertEquals("k", saved.getValue().getConfigurationKey());
        assertEquals("v", saved.getValue().getValue());
        assertEquals("d", saved.getValue().getDescription());
        assertSame(organisation, saved.getValue().getOrganisationCreditbureau());
    }

    @Test
    void addStoresMissingParametersAsEmptyStrings() {
        service.addCreditBureauConfiguration(ORGANISATION_ID, command("""
                {"configkey": "k"}
                """));

        final ArgumentCaptor<CreditBureauConfiguration> saved = ArgumentCaptor.forClass(CreditBureauConfiguration.class);
        verify(configurationRepository).saveAndFlush(saved.capture());
        assertEquals("k", saved.getValue().getConfigurationKey());
        assertEquals("", saved.getValue().getValue());
        assertEquals("", saved.getValue().getDescription());
    }

    @Test
    void updateAppliesAndReportsOnlyTheChangedValue() {
        final CreditBureauConfiguration configuration = existing();
        when(configurationRepository.findById(CONFIGURATION_ID)).thenReturn(Optional.of(configuration));

        final CommandProcessingResult result = service.updateCreditBureauConfiguration(CONFIGURATION_ID, command("""
                {"value": "new value"}
                """));

        assertEquals(Map.of("value", "new value"), result.getChanges());
        assertEquals("key", configuration.getConfigurationKey());
        assertEquals("new value", configuration.getValue());
        assertEquals("description", configuration.getDescription());
        verify(configurationRepository).save(configuration);
    }

    @Test
    void updateReportsChangesInTheOrderKeyValueDescription() {
        final CreditBureauConfiguration configuration = existing();
        when(configurationRepository.findById(CONFIGURATION_ID)).thenReturn(Optional.of(configuration));

        // description is listed first on purpose: the order of the changes must not depend on the order in the request
        final CommandProcessingResult result = service.updateCreditBureauConfiguration(CONFIGURATION_ID, command("""
                {"description": "new description", "value": "new value", "configkey": "new key"}
                """));

        assertEquals(List.of("configkey", "value", "description"), List.copyOf(result.getChanges().keySet()));
        assertEquals("new key", configuration.getConfigurationKey());
        assertEquals("new value", configuration.getValue());
        assertEquals("new description", configuration.getDescription());
    }

    @Test
    void updateWithUnchangedValuesReportsNothingAndDoesNotSave() {
        final CreditBureauConfiguration configuration = existing();
        when(configurationRepository.findById(CONFIGURATION_ID)).thenReturn(Optional.of(configuration));

        final CommandProcessingResult result = service.updateCreditBureauConfiguration(CONFIGURATION_ID, command("""
                {"configkey": "key", "value": "value"}
                """));

        assertTrue(changesOf(result).isEmpty());
        verify(configurationRepository, never()).save(any());
    }

    @Test
    void updateWithEmptyValueClearsTheFieldAndReportsTheEmptyString() {
        final CreditBureauConfiguration configuration = existing();
        when(configurationRepository.findById(CONFIGURATION_ID)).thenReturn(Optional.of(configuration));

        final CommandProcessingResult result = service.updateCreditBureauConfiguration(CONFIGURATION_ID, command("""
                {"value": ""}
                """));

        assertEquals(Map.of("value", ""), result.getChanges());
        assertNull(configuration.getValue());
    }

    /**
     * Documents existing behaviour, not intended behaviour: the description is compared with the current configuration
     * key instead of the current description, so whether a description change is detected depends on the key.
     */
    @Test
    void updateComparesTheDescriptionWithTheConfigurationKey() {
        final CreditBureauConfiguration configuration = existing();
        when(configurationRepository.findById(CONFIGURATION_ID)).thenReturn(Optional.of(configuration));

        // equal to the current key, so it is not seen as a change although it differs from the current description
        final CommandProcessingResult sameAsKey = service.updateCreditBureauConfiguration(CONFIGURATION_ID, command("""
                {"description": "key"}
                """));
        assertTrue(changesOf(sameAsKey).isEmpty());
        assertEquals("description", configuration.getDescription());

        // equal to the current description, but different from the key, so it is reported as a change
        final CommandProcessingResult sameAsDescription = service.updateCreditBureauConfiguration(CONFIGURATION_ID, command("""
                {"description": "description"}
                """));
        assertEquals(Map.of("description", "description"), sameAsDescription.getChanges());
    }
}
