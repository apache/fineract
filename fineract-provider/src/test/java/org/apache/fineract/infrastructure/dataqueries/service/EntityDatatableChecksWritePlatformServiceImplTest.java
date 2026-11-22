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
package org.apache.fineract.infrastructure.dataqueries.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import java.util.List;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.domain.EntityDatatableChecks;
import org.apache.fineract.infrastructure.dataqueries.domain.EntityDatatableChecksRepository;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EntityDatatableChecksWritePlatformServiceImplTest {

    private static final String DATATABLE = "dt_client_extra";

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private EntityDatatableChecksDataValidator fromApiJsonDeserializer;
    @Mock
    private EntityDatatableChecksRepository entityDatatableChecksRepository;
    @Mock
    private DatatableReadService datatableReadService;
    @Mock
    private DatatableWriteService datatableWriteService;
    @Mock
    private LoanProductReadPlatformService loanProductReadPlatformService;
    @Mock
    private SavingsProductReadPlatformService savingsProductReadPlatformService;
    @Mock
    private ConfigurationDomainService configurationDomainService;

    private EntityDatatableChecksWritePlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EntityDatatableChecksWritePlatformServiceImpl(context, fromApiJsonDeserializer, entityDatatableChecksRepository,
                datatableReadService, datatableWriteService, loanProductReadPlatformService, savingsProductReadPlatformService,
                new FromJsonHelper(), configurationDomainService);
    }

    @Test
    void createCheckTakesItsValuesFromTheCommand() {
        datatableWithColumn("client_id");
        when(entityDatatableChecksRepository.findByEntityStatusAndDatatableIdAndNoProduct("m_client", 100, DATATABLE))
                .thenReturn(List.of());

        service.createCheck(command("""
                {"entity": "m_client", "status": 100, "datatableName": "dt_client_extra"}
                """));

        final EntityDatatableChecks saved = savedCheck();
        assertEquals("m_client", saved.getEntity());
        assertEquals(DATATABLE, saved.getDatatableName());
        assertEquals(100, saved.getStatus());
        assertFalse(saved.isSystemDefined());
        assertNull(saved.getProductId());
    }

    @Test
    void createCheckKeepsTheSystemDefinedFlag() {
        datatableWithColumn("client_id");
        when(entityDatatableChecksRepository.findByEntityStatusAndDatatableIdAndNoProduct("m_client", 100, DATATABLE))
                .thenReturn(List.of());

        service.createCheck(command("""
                {"entity": "m_client", "status": 100, "datatableName": "dt_client_extra", "systemDefined": true}
                """));

        assertTrue(savedCheck().isSystemDefined());
    }

    @Test
    void createCheckForALoanProductStoresTheProduct() {
        datatableWithColumn("loan_id");
        when(entityDatatableChecksRepository.findByEntityStatusAndDatatableIdAndProductId("m_loan", 100, DATATABLE, 5L))
                .thenReturn(List.of());

        service.createCheck(command("""
                {"entity": "m_loan", "status": 100, "datatableName": "dt_client_extra", "productId": 5}
                """));

        verify(loanProductReadPlatformService).retrieveLoanProduct(5L);
        assertEquals(5L, savedCheck().getProductId());
    }

    private void datatableWithColumn(final String column) {
        final DatatableData datatable = mock(DatatableData.class);
        when(datatable.hasColumn(column)).thenReturn(true);
        when(datatableReadService.retrieveDatatable(DATATABLE)).thenReturn(datatable);
    }

    private EntityDatatableChecks savedCheck() {
        final ArgumentCaptor<EntityDatatableChecks> saved = ArgumentCaptor.forClass(EntityDatatableChecks.class);
        verify(entityDatatableChecksRepository).saveAndFlush(saved.capture());
        return saved.getValue();
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JsonParser.parseString(json), new FromJsonHelper(), "entityDatatableChecks", null, null, null, null,
                null, null, null, null, null, null, null, null, null);
    }
}
