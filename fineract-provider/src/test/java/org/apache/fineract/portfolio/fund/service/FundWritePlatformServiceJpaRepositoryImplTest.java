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
package org.apache.fineract.portfolio.fund.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.fund.domain.FundRepository;
import org.apache.fineract.portfolio.fund.serialization.FundCommandFromApiJsonDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FundWritePlatformServiceJpaRepositoryImplTest {

    private static final Long FUND_ID = 5L;

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private FundCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    @Mock
    private FundRepository fundRepository;

    private FundWritePlatformServiceJpaRepositoryImpl service;

    @BeforeEach
    void setUp() {
        service = new FundWritePlatformServiceJpaRepositoryImpl(context, fromApiJsonDeserializer, fundRepository);
    }

    @Test
    void createFundTakesNameAndExternalIdFromTheCommand() {
        service.createFund(command("""
                {"name": "Donor fund", "externalId": "donor-1"}
                """));

        final ArgumentCaptor<Fund> saved = ArgumentCaptor.forClass(Fund.class);
        verify(fundRepository).saveAndFlush(saved.capture());
        assertEquals("Donor fund", saved.getValue().getName());
        assertEquals("donor-1", saved.getValue().getExternalId());
    }

    @Test
    void createFundStoresAnEmptyExternalIdAsNull() {
        service.createFund(command("""
                {"name": "Donor fund", "externalId": ""}
                """));

        final ArgumentCaptor<Fund> saved = ArgumentCaptor.forClass(Fund.class);
        verify(fundRepository).saveAndFlush(saved.capture());
        assertNull(saved.getValue().getExternalId());
    }

    @Test
    void updateFundAppliesAndReportsOnlyTheChangedValues() {
        final Fund fund = Fund.createNew("Donor fund", "donor-1");
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fund));

        final CommandProcessingResult result = service.updateFund(FUND_ID, command("""
                {"name": "Renamed fund", "externalId": "donor-1"}
                """));

        assertEquals(Map.of("name", "Renamed fund"), result.getChanges());
        assertEquals("Renamed fund", fund.getName());
        assertEquals("donor-1", fund.getExternalId());
        verify(fundRepository).saveAndFlush(fund);
    }

    @Test
    void updateFundClearsTheExternalIdWhenItIsEmpty() {
        final Fund fund = Fund.createNew("Donor fund", "donor-1");
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fund));

        final CommandProcessingResult result = service.updateFund(FUND_ID, command("""
                {"externalId": ""}
                """));

        // the change set reports the value as sent, while the entity stores an empty external ID as null
        assertEquals(Map.of("externalId", ""), result.getChanges());
        assertNull(fund.getExternalId());
        verify(fundRepository).saveAndFlush(fund);
    }

    @Test
    void updateFundWithoutChangesDoesNotSave() {
        final Fund fund = Fund.createNew("Donor fund", "donor-1");
        when(fundRepository.findById(FUND_ID)).thenReturn(Optional.of(fund));

        final CommandProcessingResult result = service.updateFund(FUND_ID, command("""
                {"name": "Donor fund"}
                """));

        // CommandProcessingResult reports an empty change set as null
        assertNull(result.getChanges());
        verify(fundRepository, never()).saveAndFlush(any(Fund.class));
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JsonParser.parseString(json), new FromJsonHelper(), "funds", FUND_ID, null, null, null, null, null,
                null, null, null, null, null, null, null);
    }
}
