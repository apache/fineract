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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureau;
import org.apache.fineract.infrastructure.creditbureau.domain.CreditBureauRepository;
import org.apache.fineract.infrastructure.creditbureau.domain.OrganisationCreditBureau;
import org.apache.fineract.infrastructure.creditbureau.domain.OrganisationCreditBureauRepository;
import org.apache.fineract.infrastructure.creditbureau.serialization.CreditBureauCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganisationCreditBureauWritePlatflormServiceImplTest {

    private static final FromJsonHelper JSON_HELPER = new FromJsonHelper();
    private static final Long CREDIT_BUREAU_ID = 4L;

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private OrganisationCreditBureauRepository organisationRepository;
    @Mock
    private CreditBureauRepository creditBureauRepository;
    @Mock
    private CreditBureauCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    private OrganisationCreditBureauWritePlatflormServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrganisationCreditBureauWritePlatflormServiceImpl(context, organisationRepository, creditBureauRepository,
                fromApiJsonDeserializer);
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JSON_HELPER.parse(json), JSON_HELPER, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }

    @Test
    void addTakesAliasAndActiveFlagFromTheCommandAndLinksTheCreditBureau() {
        final CreditBureau creditBureau = new CreditBureau();
        when(creditBureauRepository.getReferenceById(CREDIT_BUREAU_ID)).thenReturn(creditBureau);

        service.addOrganisationCreditBureau(CREDIT_BUREAU_ID, command("""
                {"alias": "my bureau", "isActive": true}
                """));

        final ArgumentCaptor<OrganisationCreditBureau> saved = ArgumentCaptor.forClass(OrganisationCreditBureau.class);
        verify(organisationRepository).saveAndFlush(saved.capture());
        assertEquals("my bureau", saved.getValue().getAlias());
        assertTrue(saved.getValue().isActive());
        assertSame(creditBureau, saved.getValue().getCreditBureau());
        assertNull(saved.getValue().getCreditBureauLoanProductMapping());
    }

    @Test
    void addDefaultsToAnEmptyAliasAndInactiveWhenTheParametersAreMissing() {
        service.addOrganisationCreditBureau(CREDIT_BUREAU_ID, command("""
                {}
                """));

        final ArgumentCaptor<OrganisationCreditBureau> saved = ArgumentCaptor.forClass(OrganisationCreditBureau.class);
        verify(organisationRepository).saveAndFlush(saved.capture());
        assertEquals("", saved.getValue().getAlias());
        assertFalse(saved.getValue().isActive());
    }
}
