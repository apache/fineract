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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.dataqueries.data.EntityDataTableChecksTemplateData;
import org.apache.fineract.infrastructure.dataqueries.domain.EntityDatatableChecksRepository;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EntityDatatableChecksReadPlatformServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;
    @Mock
    private EntityDatatableChecksRepository entityDatatableChecksRepository;
    @Mock
    private DatatableReadService datatableReadService;
    @Mock
    private LoanProductReadPlatformService loanProductReadPlatformService;
    @Mock
    private SavingsProductReadPlatformService savingsProductReadPlatformService;
    @Mock
    private PaginationHelper paginationHelper;

    @Test
    void templateOffersOnlyEntitiesThatSupportChecks() {
        when(sqlGenerator.escape(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sqlGenerator.formatValue(any(), anyString())).thenAnswer(invocation -> "'" + invocation.getArgument(1) + "'");
        EntityDatatableChecksReadPlatformServiceImpl underTest = new EntityDatatableChecksReadPlatformServiceImpl(jdbcTemplate,
                sqlGenerator, entityDatatableChecksRepository, datatableReadService, loanProductReadPlatformService,
                savingsProductReadPlatformService, paginationHelper);

        EntityDataTableChecksTemplateData template = underTest.retrieveTemplate();

        @SuppressWarnings("unchecked")
        List<String> entities = (List<String>) ReflectionTestUtils.getField(template, "entities");
        assertEquals(List.of("m_client", "m_group", "m_loan", "m_savings_account", "m_wc_loan"), entities);
    }
}
