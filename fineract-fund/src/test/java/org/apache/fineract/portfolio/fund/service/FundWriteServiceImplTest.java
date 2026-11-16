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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.fineract.portfolio.fund.data.FundCreateRequest;
import org.apache.fineract.portfolio.fund.data.FundUpdateRequest;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.fund.domain.FundRepository;
import org.apache.fineract.portfolio.fund.exception.FundNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FundWriteServiceImplTest {

    @Mock
    private FundRepository fundRepository;

    private FundWriteServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FundWriteServiceImpl(fundRepository);
    }

    @Test
    void createFundPersistsNewFund() {
        var request = FundCreateRequest.builder().name("EU Agri Fund").externalId("eu-agri").build();

        service.createFund(request);

        ArgumentCaptor<Fund> captor = ArgumentCaptor.forClass(Fund.class);
        verify(fundRepository).saveAndFlush(captor.capture());
        assertEquals("EU Agri Fund", captor.getValue().getName());
        assertEquals("eu-agri", captor.getValue().getExternalId());
    }

    @Test
    void createFundStoresAnEmptyExternalIdAsNull() {
        service.createFund(FundCreateRequest.builder().name("EU Agri Fund").externalId("").build());

        ArgumentCaptor<Fund> captor = ArgumentCaptor.forClass(Fund.class);
        verify(fundRepository).saveAndFlush(captor.capture());
        assertNull(captor.getValue().getExternalId());
    }

    @Test
    void updateFundWithNameOnlyLeavesExternalIdUntouched() {
        Fund fund = Fund.createNew("Old Name", "ext-1");
        when(fundRepository.findById(1L)).thenReturn(Optional.of(fund));

        var response = service.updateFund(FundUpdateRequest.builder().id(1L).name("New Name").build());

        assertEquals("New Name", fund.getName());
        assertEquals("New Name", response.getChanges().get(FundUpdateRequest.Fields.name));
        assertFalse(response.getChanges().containsKey(FundUpdateRequest.Fields.externalId));
        verify(fundRepository).saveAndFlush(fund);
    }

    @Test
    void updateFundWithUnchangedValuesDoesNotSave() {
        Fund fund = Fund.createNew("Same Name", "ext-1");
        when(fundRepository.findById(1L)).thenReturn(Optional.of(fund));

        var response = service.updateFund(FundUpdateRequest.builder().id(1L).name("Same Name").externalId("ext-1").build());

        // like the legacy CommandProcessingResult, an empty change set is reported as null
        assertNull(response.getChanges());
        verify(fundRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateFundWithEmptyExternalIdClearsIt() {
        Fund fund = Fund.createNew("Name", "ext-1");
        when(fundRepository.findById(1L)).thenReturn(Optional.of(fund));

        var response = service.updateFund(FundUpdateRequest.builder().id(1L).externalId("").build());

        // the change set reports the value as sent, while the entity stores an empty external ID as null
        assertEquals("", response.getChanges().get(FundUpdateRequest.Fields.externalId));
        assertNull(fund.getExternalId());
        assertEquals("Name", fund.getName());
        verify(fundRepository).saveAndFlush(fund);
    }

    @Test
    void updateUnknownFundThrowsNotFound() {
        when(fundRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(FundNotFoundException.class, () -> service.updateFund(FundUpdateRequest.builder().id(99L).name("x").build()));
    }
}
